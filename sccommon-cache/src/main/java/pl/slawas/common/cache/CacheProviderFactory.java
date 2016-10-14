package pl.slawas.common.cache;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import net.sf.ehcache.config.Configuration;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jdom.Element;

import pl.slawas.common.cache.ehcache.EhCacheConstants;
import pl.slawas.common.cache.ehcache.EhCacheProvider;
import pl.slawas.helpers.Strings;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * 
 * EhCacheProviderFactory obsługa pamięci podręczen opartej o EhCache
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class CacheProviderFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(CacheProviderFactory.class);

	public static final String DEFAULT_MANAGER_NAME = "default";
	public static final String EMPTY_REGION_NAME = "EMPTY";

	/**
	 * Mapa instancji managerów pamięci podręcznej. Kluczem jest nazwa instancji
	 * managera pamięci.
	 */
	private static final Map<String, _IObjectCacheProvider<?>> instances = new HashMap<String, _IObjectCacheProvider<?>>();
	private static final Object lockRegistry = new Object();
	private static final Map<String, CacheProviderConfiguration<?>> configurations = new HashMap<String, CacheProviderConfiguration<?>>();

	/**
	 * Pobieranie nowej instancji dostawcy obsługi pamięci podręcznej EhCache
	 * 
	 * @param props
	 * @return
	 */
	public static _IObjectCacheProvider<?> getInstance(Properties props) {
		String customProviderImpl = (props
				.getProperty(EhCacheConstants.PROP_PROVIDER_IMPL) != null ? (String) props
				.getProperty(EhCacheConstants.PROP_PROVIDER_IMPL) : null);
		_IObjectCacheProvider<?> instance = null;
		if (StringUtils.isNotBlank(customProviderImpl)) {
			try {
				Class<?> impl = Class.forName(customProviderImpl);
				_IObjectCacheProvider<?> newInstance = (_IObjectCacheProvider<?>) impl
						.newInstance();
				instance = loadInstance(newInstance, props);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(e);
			} catch (IllegalAccessException e) {
				throw new IllegalArgumentException(e);
			} catch (InstantiationException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			/** domyślnie używamy EhCache */
			_IObjectCacheProvider<Configuration> newInstance = new EhCacheProvider();
			instance = loadInstance(newInstance, props);
		}
		logger.trace(
				"\n***************\n* Pobrałem instancje provider'a: {} o nazwie: {}\n***************",
				new Object[] { instance.getClass().getName(),
						instance.getName() });
		return instance;
	}

	/**
	 * Właściwa implementacja ładowania instancji provider'a
	 * 
	 * @param newInstance
	 *            nowa instancja obiektu provider'a, zupełnie pusta!!!! Musi
	 *            mieć tylko ustawione nie pusty URL do konfiguracji.
	 * @param props
	 *            dodatkowe parametry
	 * @return instancja provider'a
	 */
	@SuppressWarnings("unchecked")
	public static <T> _IObjectCacheProvider<T> loadInstance(
			_IObjectCacheProvider<T> newInstance, Properties props) {
		_IObjectCacheProvider<T> existsInstance = null;

		/* wpierw sprawdzam czy mam przygotowaną konfigurację - start */
		URL configurationURL = newInstance.getConfigurationFileURL();
		CacheProviderEnum associatedProvider = newInstance
				.getAssociatedProvider();
		String confKey = associatedProvider.name() + ":URL:"
				+ (configurationURL == null ? "null" : configurationURL);
		CacheProviderConfiguration<T> conf = (CacheProviderConfiguration<T>) configurations
				.get(confKey);
		if (conf == null) {
			conf = newInstance.initConfiguration(configurationURL);
			configurations.put(confKey, conf);
		}
		/* wpierw sprawdzam czy mam przygotowaną konfigurację - koniec */

		String instanceName = buildInstanceNameBasedOnManagerName(
				associatedProvider, conf.getManagerName());
		synchronized (lockRegistry) {
			/* sprawdzam czy mam instancję - start */
			existsInstance = (_IObjectCacheProvider<T>) instances
					.get(instanceName);
			if (existsInstance == null) {
				/* mam nową instancję provider'a */
				logger.info(
						"\n***************\n* Incjalizuje nowego provider'a: {}\n***************",
						newInstance.getClass().getName());
				instances.put(instanceName, newInstance);
				newInstance.init(conf, props);
				return newInstance;
			}
			return existsInstance;
			/* sprawdzam czy mam instancję - koniec */
		}
	}

	/**
	 * Zamykanie/usuwanie instancji pamięci podręcznej.
	 */
	static void close() {
		if (instances.size() != 0) {
			Set<Entry<String, _IObjectCacheProvider<?>>> allInstances = instances
					.entrySet();
			for (Entry<String, _IObjectCacheProvider<?>> instance : allInstances) {
				instance.getValue().close();
			}
			instances.clear();
		}
	}

	/**
	 * Pobieranie statystyk dla wszystkich regionów pamięci podręcznej.
	 * 
	 * @param offset
	 *            pozycja, od której ma zaczynać się lista statystyk - domyślnie
	 *            0
	 * @param pageSize
	 *            maksymalna liczba rekordów jaka ma być zwrócona, rozmiar
	 *            strony - domyślnie
	 *            {@link EhCacheConstants#DEFAULT_MAX_RESULT_SIZE}
	 * @return lista statystyk regionów w postaci {@link Element}
	 */
	public static ObjectCacheStatisticsList getAllStatistics(Integer offset,
			Integer pageSize) {
		List<_IObjectCacheStatistics> result = new ArrayList<_IObjectCacheStatistics>();
		int lOffset = (offset == null ? 0 : offset.intValue());
		int lPageSize = (pageSize == null ? EhCacheConstants.DEFAULT_MAX_RESULT_SIZE
				: pageSize.intValue());
		if (instances.size() == 0) {
			return new ObjectCacheStatisticsList(result, 0, lOffset, lPageSize);
		}
		Set<Entry<String, _IObjectCacheProvider<?>>> allInstances = instances
				.entrySet();
		List<String> allRegions = new ArrayList<String>();
		for (Entry<String, _IObjectCacheProvider<?>> instance : allInstances) {
			String managerName = instance.getKey();
			_IObjectCacheProvider<?> provider = instance.getValue();
			if (provider.getCacheNames().length != 0) {
				for (String regionName : provider.getCacheNames()) {
					allRegions.add(buildRegionName(managerName, regionName));
				}
			}
		}
		int allRegionsSize = allRegions.size();
		if (allRegions.size() == 0 || lOffset >= allRegionsSize) {
			return new ObjectCacheStatisticsList(result, 0, lOffset, lPageSize);
		}
		try {
			Strings.sort(allRegions);
		} catch (Exception e) {
			logger.warn(
					"[getAllStatistics] Nieudana próba posortowania nazw regionów.",
					e);
		}
		int rowCounter = -1;
		int rowCounterOnPage = 0;
		for (String commonRegionName : allRegions) {
			rowCounter++;
			if (rowCounter < offset) {
				/* pomijam czytanie statystyk */
				continue;
			}
			if (rowCounterOnPage > pageSize) {
				/* przerywam czytanie statystyk */
				break;
			}
			String managerName = decodeManagerName(commonRegionName);
			String regionName = decodeRegionName(commonRegionName);
			_IObjectCacheProvider<?> provider = instances.get(managerName);
			_IObjectCacheStatistics stats = provider.getStatistics(regionName);
			if (stats == null) {
				stats = new EmptyObjectCacheStatistics(managerName, regionName,
						provider.getAssociatedProvider());
			}
			result.add(stats);
			rowCounterOnPage++;

		}
		return new ObjectCacheStatisticsList(result, allRegionsSize, lOffset,
				lPageSize);
	}

	private static String buildRegionName(String managerName, String regionName) {
		return managerName
				+ "."
				+ (StringUtils.isNotBlank(regionName) ? regionName
						: EMPTY_REGION_NAME);
	}

	private static String decodeManagerName(String commonRegionName) {
		return commonRegionName.split("\\.")[0];
	}

	private static String decodeRegionName(String commonRegionName) {
		return commonRegionName.split("\\.")[1];
	}

	@SuppressWarnings("unchecked")
	public static <Conf> _IObjectCacheProvider<Conf> registerInstanceByManagerName(
			CacheProviderConfiguration<Conf> configuration,
			_IObjectCacheProvider<Conf> newInstance, String manager) {

		/* wpierw sprawdzam czy mam przygotowaną konfigurację - start */
		URL configurationURL = newInstance.getConfigurationFileURL();
		CacheProviderEnum associatedProvider = newInstance
				.getAssociatedProvider();
		String confKey = associatedProvider.name() + ":URL:"
				+ (configurationURL == null ? "null" : configurationURL);
		CacheProviderConfiguration<Conf> conf = (CacheProviderConfiguration<Conf>) configurations
				.get(confKey);
		if (conf == null) {
			conf = configuration;
			configurations.put(confKey, conf);
		}
		/* wpierw sprawdzam czy mam przygotowaną konfigurację - koniec */

		String instanceName = buildInstanceNameBasedOnManagerName(
				associatedProvider, manager);
		synchronized (lockRegistry) {
			/* teraz rejestruję instancję - start */
			if (!isRegisteredInstance(
					associatedProvider.getCacheProviderInterfaceClass(),
					instanceName)) {
				instances.put(instanceName, newInstance);
				return newInstance;
			} else {
				return (_IObjectCacheProvider<Conf>) instances
						.get(instanceName);
			}
			/* teraz rejestruję instancję - koniec */
		}
	}

	public static String buildInstanceNameBasedOnManagerName(
			CacheProviderEnum cacheProvider, String manager) {
		String instanceName = null;
		if (StringUtils.isBlank(manager)) {
			instanceName = cacheProvider.name();
		} else {
			instanceName = cacheProvider.name() + "." + manager;
		}
		return instanceName;
	}

	public static boolean isRegisteredInstance(Class<?> instanceInterfaceClazz,
			String instanceName) {
		Object instance = instances.get(instanceName);
		if (instance == null) {
			return false;
		}
		Class<?>[] interfaces = instance.getClass().getInterfaces();
		if (ArrayUtils.contains(interfaces, instanceInterfaceClazz)) {
			return true;
		}
		throw new IllegalAccessError(
				"I found instance of cache provider named '"
						+ instanceName
						+ "', but it is different implementation other than I expect. Expected that the instance should implement interface "
						+ instanceInterfaceClazz.getName() + " but implement "
						+ ArrayUtils.toString(interfaces) + ".");
	}

	public static Set<String> getInstanceNames() {
		return instances.keySet();
	}

	public static void unbindInstanceByManagerName(
			CacheProviderEnum associatedProvider, String manager) {
		String instanceName = buildInstanceNameBasedOnManagerName(
				associatedProvider, manager);
		synchronized (lockRegistry) {
			if (instances.containsKey(instanceName)) {
				instances.remove(instanceName);
			}
		}
	}

}
