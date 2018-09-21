package pl.slawas.common.cache;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.ehcache.xml.XmlConfiguration;
import org.jdom.Element;

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

	private static final Logger logger = LoggerFactory.getLogger(CacheProviderFactory.class);

	public static final String DEFAULT_MANAGER_NAME = "default";
	public static final String EMPTY_REGION_NAME = "EMPTY";

	/**
	 * Mapa instancji managerów pamięci podręcznej. Kluczem jest nazwa instancji
	 * managera pamięci.
	 */
	private static final Map<String, IObjectCacheProvider<?>> instances = new HashMap<>();
	private static final Object lockRegistry = new Object();

	private CacheProviderFactory() {
	}

	/**
	 * Pobieranie nowej instancji dostawcy obsługi pamięci podręcznej EhCache
	 * 
	 * @param props
	 * @return
	 */
	public static IObjectCacheProvider<?> getInstance(Properties props) {
		String customProviderImpl = (props.getProperty(CacheConstants.PROP_PROVIDER_IMPL) != null
				? (String) props.getProperty(CacheConstants.PROP_PROVIDER_IMPL)
				: null);
		IObjectCacheProvider<?> instance = null;
		if (StringUtils.isNotBlank(customProviderImpl)) {
			try {
				Class<?> impl = Class.forName(customProviderImpl);
				IObjectCacheProvider<?> newInstance = (IObjectCacheProvider<?>) impl.newInstance();
				instance = loadInstance(newInstance, props);
			} catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
				throw new IllegalArgumentException(e);
			}
		} else {
			/** domyślnie używamy EhCache */
			IObjectCacheProvider<XmlConfiguration> newInstance = new EhCacheProvider();
			instance = loadInstance(newInstance, props);
		}
		logger.trace("\n***************\n* Pobrałem instancje provider'a: {} o nazwie: {}\n***************",
				new Object[] { instance.getClass().getName(), instance.getName() });
		return instance;
	}

	/**
	 * Właściwa implementacja ładowania instancji provider'a
	 * 
	 * @param newInstance
	 *            nowa instancja obiektu provider'a, zupełnie pusta!!!! Musi mieć
	 *            tylko ustawione nie pusty URL do konfiguracji.
	 * @param props
	 *            dodatkowe parametry
	 * @return instancja provider'a
	 */
	@SuppressWarnings("unchecked")
	public static <T> IObjectCacheProvider<T> loadInstance(IObjectCacheProvider<T> newInstance, Properties props) {
		IObjectCacheProvider<T> existsInstance = null;

		CacheProviderEnum associatedProvider = newInstance.getAssociatedProvider();
		String providerName = props.getProperty(CacheConstants.PROP_PROVIDER_NAME,
				CacheConstants.DEFAULT_PROVIDER_NAME);
		String instanceName = buildInstanceNameBasedOnManagerName(associatedProvider, providerName);
		synchronized (lockRegistry) {
			/* sprawdzam czy mam instancję - start */
			existsInstance = (IObjectCacheProvider<T>) checkRegisteredInstance(
					associatedProvider.getCacheProviderInterfaceClass(), instanceName);
			if (existsInstance == null) {
				/* mam nową instancję provider'a */
				logger.info("\n***************\n* New provider is initializing: {}\n***************",
						newInstance.getClass().getName());
				instances.put(instanceName, newInstance);
				/* wpierw inicjalizacja konfiguracji - start */
				newInstance.initConfiguration(props);
				/* wpierw inicjalizacja konfiguracji - koniec */
				newInstance.init();
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
			Set<Entry<String, IObjectCacheProvider<?>>> allInstances = instances.entrySet();
			for (Entry<String, IObjectCacheProvider<?>> instance : allInstances) {
				instance.getValue().close();
			}
			instances.clear();
		}
	}

	/**
	 * Pobieranie statystyk dla wszystkich regionów pamięci podręcznej.
	 * 
	 * @param offset
	 *            pozycja, od której ma zaczynać się lista statystyk - domyślnie 0
	 * @param pageSize
	 *            maksymalna liczba rekordów jaka ma być zwrócona, rozmiar strony -
	 *            domyślnie {@link CacheConstants#DEFAULT_MAX_RESULT_SIZE}
	 * @return lista statystyk regionów w postaci {@link Element}
	 */
	public static ObjectCacheStatisticsList getAllStatistics(Integer offset, Integer pageSize) {
		List<IObjectCacheStatistics> result = new ArrayList<>();
		int lOffset = (offset == null ? 0 : offset.intValue());
		int lPageSize = (pageSize == null ? CacheConstants.DEFAULT_MAX_RESULT_SIZE : pageSize.intValue());
		if (instances.size() == 0) {
			return new ObjectCacheStatisticsList(result, 0, lOffset, lPageSize);
		}
		Set<Entry<String, IObjectCacheProvider<?>>> allInstances = instances.entrySet();
		List<String> allRegions = new ArrayList<String>();
		for (Entry<String, IObjectCacheProvider<?>> instance : allInstances) {
			String managerName = instance.getKey();
			IObjectCacheProvider<?> provider = instance.getValue();
			if (provider.getCacheNames().length != 0) {
				for (String regionName : provider.getCacheNames()) {
					allRegions.add(buildRegionName(managerName, regionName));
				}
			}
		}
		int allRegionsSize = allRegions.size();
		if (allRegionsSize == 0 || lOffset >= allRegionsSize) {
			return new ObjectCacheStatisticsList(result, 0, lOffset, lPageSize);
		}
		try {
			Strings.sort(allRegions);
		} catch (Exception e) {
			logger.warn("[getAllStatistics] Nieudana próba posortowania nazw regionów.", e);
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
			IObjectCacheProvider<?> provider = instances.get(managerName);
			IObjectCacheStatistics stats = provider.getStatistics(regionName);
			if (stats == null) {
				stats = new EmptyObjectCacheStatistics(managerName, regionName, provider.getAssociatedProvider());
			}
			result.add(stats);
			rowCounterOnPage++;

		}
		return new ObjectCacheStatisticsList(result, allRegionsSize, lOffset, lPageSize);
	}

	private static String buildRegionName(String managerName, String regionName) {
		return managerName + "." + (StringUtils.isNotBlank(regionName) ? regionName : EMPTY_REGION_NAME);
	}

	private static String decodeManagerName(String commonRegionName) {
		return commonRegionName.split("\\.")[0];
	}

	private static String decodeRegionName(String commonRegionName) {
		return commonRegionName.split("\\.")[1];
	}

	public static String buildInstanceNameBasedOnManagerName(CacheProviderEnum cacheProvider, String manager) {
		String instanceName = null;
		if (StringUtils.isBlank(manager)) {
			instanceName = cacheProvider.name();
		} else {
			instanceName = cacheProvider.name() + "." + manager;
		}
		return instanceName;
	}

	public static Object checkRegisteredInstance(Class<?> instanceInterfaceClazz, String instanceName) {
		Object instance = instances.get(instanceName);
		if (instance == null) {
			return null;
		}
		Class<?>[] interfaces = instance.getClass().getInterfaces();
		if (ArrayUtils.contains(interfaces, instanceInterfaceClazz)) {
			return instance;
		}
		throw new IllegalAccessError("I found instance of cache provider named '" + instanceName
				+ "', but it is different implementation other than I expect. Expected that the instance should implement interface "
				+ instanceInterfaceClazz.getName() + " but implement " + ArrayUtils.toString(interfaces) + ".");
	}

	public static Set<String> getInstanceNames() {
		return instances.keySet();
	}

	public static void unbindInstanceByManagerName(CacheProviderEnum associatedProvider, String manager) {
		String instanceName = buildInstanceNameBasedOnManagerName(associatedProvider, manager);
		synchronized (lockRegistry) {
			if (instances.containsKey(instanceName)) {
				instances.remove(instanceName);
			}
		}
	}

}
