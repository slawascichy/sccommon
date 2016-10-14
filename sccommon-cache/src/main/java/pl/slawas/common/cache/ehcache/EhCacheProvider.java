package pl.slawas.common.cache.ehcache;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.management.MBeanServer;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.management.ManagementService;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.cache.CacheProviderConfiguration;
import pl.slawas.common.cache.CacheProviderEnum;
import pl.slawas.common.cache.CacheProviderFactory;
import pl.slawas.common.cache.ObjectCacheStatisticsList;
import pl.slawas.common.cache._IObjectCache;
import pl.slawas.common.cache._IObjectCacheStatistics;
import pl.slawas.helpers.FileUtils;
import pl.slawas.helpers.Strings;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * 
 * EhCacheProvider - utworzenie instancji CacheManager-a
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.3 $
 * 
 */
public class EhCacheProvider implements EhCacheInstance {

	private static final long serialVersionUID = -6327091572644983790L;

	public static String FILE_CONFIG_PATH = FileUtils.resolveName(
			EhCacheProvider.class, "ehcache-failsafe.xml");

	private static final Logger logger = LoggerFactory
			.getLogger(EhCacheProvider.class);

	public static final Object initLock = new Object();

	private URL configurationFileURL;

	private static URL defaultConfigurationFileURL;

	private CacheManager manager;

	/**
	 * Dodatkowe parametry dotyczące nowo tworzonych regionów pamięci podręcznej
	 */
	private Properties additionalProps;

	private final Map<String, _IObjectCache> caches = new Hashtable<String, _IObjectCache>();

	/**
	 * W konstruktorze ustawiany jest domyślny URL konfiguracji.
	 */
	public EhCacheProvider() {
		super();
		initConfigurationFileURL();
	}

	/**
	 * Inna metoda inicjalizacji managera pamięci podręcznej.
	 * 
	 * @param configuration
	 *            konfiguracja
	 * @param register
	 *            czy ma być zarejestrowany w register MBeans
	 * @param additionalProps
	 *            dodatkowe parametry związane z regionami pamięci
	 * @return czy nastąpiła inicjalizacja
	 */
	public boolean init(
			CacheProviderConfiguration<Configuration> configuration,
			boolean register, Properties additionalProps) {
		synchronized (initLock) {
			if (manager != null) {
				/**
				 * Jeżeli manager jest już zainicjalizowany, to wystawiam tylko
				 * ostrzeżenie
				 */
				logger.warn("[init] {} is inicjalized!",
						EhCacheProvider.class.getSimpleName());
				return false;
			}
			if (additionalProps != null) {
				/* dodatkowe parametry */
				if (logger.isDebugEnabled()) {
					for (Entry<Object, Object> entry : additionalProps
							.entrySet()) {
						logger.debug(
								"[init] Laduje dodatkowe parametr {}: '{}'",
								new Object[] { entry.getKey(), entry.getValue() });
					}
				}
				if (this.additionalProps == null) {
					this.additionalProps = additionalProps;
				} else if (!this.additionalProps.equals(additionalProps)) {
					/* dodaję parametry tylko gdy obiekty są różne */
					this.additionalProps.putAll(additionalProps);
				}
			}
			/* tworzę manager'a */
			manager = new CacheManager(configuration.getConfiguration());
			if (register) {
				/* rejestruję manager'a w MBeans */
				MBeanServer mBeanServer = ManagementFactory
						.getPlatformMBeanServer();
				ManagementService.registerMBeans(manager, mBeanServer, false,
						false, false, true);
			} else if (logger.isDebugEnabled()) {
				logger.debug("[init] Pomijam rejestrację managera w MBeans.");
			}
			for (String key : getCacheNames()) {
				Cache c = manager.getCache(key);
				caches.put(key, new EhCache(manager.getName(), c));
			}
			return true;
		}
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean init(CacheProviderConfiguration<Configuration> config,
			Properties additionalProps) {
		String registerValue = (String) additionalProps
				.get(EhCacheConstants.PROP_CACHE_MANAGER_REGISTER);
		boolean register = (StringUtils.isNotBlank(registerValue) ? Boolean
				.parseBoolean(registerValue) : true);
		return init((CacheProviderConfiguration<Configuration>) config,
				register, additionalProps);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public CacheProviderConfiguration<Configuration> initConfiguration(
			URL resource) {
		synchronized (initLock) {
			Configuration config;
			URL url;
			if (resource == null) {
				url = getConfigurationFileURL();
			} else {
				url = resource;
				setConfigurationFileURL(url);
			}
			logger.info(
					"[initConfiguration] Laduje konfiguracje z lokalizacji : {}",
					new Object[] { url });
			config = ConfigurationFactory.parseConfiguration(url);
			return new CacheProviderConfiguration<Configuration>(
					config.getName(), config);
		}
	}

	public String[] getCacheNames() {
		return manager.getCacheNames();
	}

	public _IObjectCache getCache(String name) {
		String lock = ("EhCacheProvider[" + getName() + "].getCache." + name)
				.intern();
		synchronized (lock) {
			_IObjectCache cache = caches.get(name);
			if (cache == null) {
				/** Uzupełnienie konfiguracji */
				CacheConfiguration config;
				String param;
				String originalKey = name;
				param = (String) additionalProps.get(originalKey
						+ Strings.DOTChar
						+ EhCacheConstants.PROP_maxElementsInMemory);
				if (StringUtils.isNotBlank(param)) {
					config = new CacheConfiguration(name,
							Integer.parseInt(param));
				} else {
					config = new CacheConfiguration(name,
							EhCacheConstants.DEFAULT_maxElementsInMemory);
				}
				param = (String) additionalProps.get(originalKey
						+ Strings.DOTChar + EhCacheConstants.PROP_eternal);
				if (StringUtils.isNotBlank(param)) {
					config.setEternal(Boolean.parseBoolean(param));
				}
				param = (String) additionalProps.get(originalKey
						+ Strings.DOTChar
						+ EhCacheConstants.PROP_memoryStoreEvictionPolicy);
				if (StringUtils.isNotBlank(param)) {
					config.setMemoryStoreEvictionPolicy(param);
				}

				param = (String) additionalProps.get(originalKey
						+ Strings.DOTChar + EhCacheConstants.PROP_strategy);
				PersistenceConfiguration pc;
				if (StringUtils.isNotBlank(param)) {
					pc = new PersistenceConfiguration();
					pc.setStrategy(param);
				} else {
					pc = new PersistenceConfiguration()
							.strategy(Strategy.LOCALTEMPSWAP);
				}
				config.persistence(pc);
				logger.debug("[{}.getCache] Tworze nowy region '{}'",
						new Object[] { getName(), name });
				manager.addCache(new Cache(config));
				cache = new EhCache(manager.getName(), manager.getCache(name));
				caches.put(name, cache);
			}
			return cache;
		}
	}

	public void close() {
		manager.shutdown();
		CacheProviderFactory.unbindInstanceByManagerName(
				getAssociatedProvider(), getName());
	}

	public void removeCache(String name) {
		_IObjectCache cache = caches.get(name);
		if (cache != null) {
			logger.debug("Usuwam region o nazwie '{}'", name);
			manager.removeCache(name);
			caches.remove(name);
		}
	}

	@Override
	public ObjectCacheStatisticsList getAllStatistics(Integer offset,
			Integer pageSize) {
		String[] keys = manager.getCacheNames();
		List<_IObjectCacheStatistics> result = new ArrayList<_IObjectCacheStatistics>();
		int lOffset = (offset == null ? 0 : offset.intValue());
		int lPageSize = (pageSize == null ? EhCacheConstants.DEFAULT_MAX_RESULT_SIZE
				: pageSize.intValue());
		if (lOffset >= keys.length) {
			return new ObjectCacheStatisticsList(result, keys.length, lOffset,
					lPageSize);
		}
		try {
			Strings.sort(keys);
		} catch (Exception e) {
			logger.warn(
					"["
							+ getName()
							+ ".getAllStatistics]  Nieudana próba posortowania nazw regionów.",
					e);
		}

		for (int i = lOffset; i < keys.length; i++) {
			if (!(i < lPageSize)) {
				/* przerywam, ze względu na założone limity. */
				break;
			}
			/* pobieram statystyki cache */
			_IObjectCacheStatistics row = getStatistics(keys[i]);
			result.add(row);
		}
		return new ObjectCacheStatisticsList(result, keys.length, lOffset,
				lPageSize);

	}

	/* Overridden (non-Javadoc) */
	@Override
	public _IObjectCacheStatistics getStatistics(String regionName) {
		Cache ch = manager.getCache(regionName);
		if (ch != null) {
			return getCache(regionName).getStatistics();
		}
		return new EhCacheStatistics(getName(), regionName);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void clearCache(String cacheName) {
		if (StringUtils.isNotBlank(cacheName)) {
			Cache ch = manager.getCache(cacheName);
			if (ch != null) {
				ch.removeAll();
			}
		}
	}

	/* Overridden (non-Javadoc) */
	@SuppressWarnings("unchecked")
	@Override
	public List<String> getKeysList(String cacheName) {
		List<String> result = new ArrayList<String>();
		List<Object> keys = manager.getCache(cacheName).getKeys();

		for (Object key : keys) {
			result.add(key.toString());
		}
		return result;
	}

	/**
	 * @return the {@link #manager}
	 */
	public CacheManager getManager() {
		return manager;
	}

	@Override
	public void clearStatistics(String cacheName) {
		logger.warn("--> clearStatistics('{}'): Method is deprecated",
				cacheName);
	}

	@Override
	public String getName() {
		return manager.getName();
	}

	/* Overridden (non-Javadoc) */
	@Override
	public CacheProviderEnum getAssociatedProvider() {
		return CacheProviderEnum.EhCache;
	}

	/**
	 * Odczytanie ścieżki konfiguracji ehcache.xml
	 * 
	 * @return the {@link #configurationFileURL}
	 */
	public URL getConfigurationFileURL() {
		if (configurationFileURL == null) {
			configurationFileURL = initConfigurationFileURL();
		}
		return configurationFileURL;
	}

	private static URL initConfigurationFileURL() {
		if (defaultConfigurationFileURL == null) {
			/* Ustawiony parametr systemowy ma pierwszeństwo */
			String customConfigPath = (System
					.getProperty(EhCacheConstants.PROP_CONFIG_PATH) != null ? (String) System
					.getProperty(EhCacheConstants.PROP_CONFIG_PATH) : null);
			if (StringUtils.isBlank(customConfigPath)) {
				customConfigPath = "/ehcache.xml";
			}
			URL url = EhCacheProvider.class.getResource(customConfigPath);
			if (url == null) {
				File file = new File(customConfigPath);
				if (file.exists() && file.isFile()) {
					try {
						url = file.toURI().toURL();
					} catch (MalformedURLException e) {
						logger.warn("Error with conversion file name '"
								+ customConfigPath + "' to URL.", e);
					}
				}
			}
			if (url == null) {
				url = ConfigurationFactory.class.getResource("/ehcache.xml");
			}
			if (url == null) {
				url = EhCacheProvider.class.getResource("ehcache-failsafe.xml");
				logger.warn(
						"No configuration found. Configuring ehcache from ehcache-failsafe.xml  found in the classpath: {}",
						url);
			} else if (logger.isDebugEnabled()) {
				logger.debug(new StringBuilder()
						.append("Configuring ehcache from ehcache.xml found in the classpath: ")
						.append(url).toString());
			}
			defaultConfigurationFileURL = url;
		}
		return defaultConfigurationFileURL;
	}

	/**
	 * @param configurationFilePath
	 *            ścieżka do konfiguracji EhCache (np. ehcache.xml);
	 */
	public void setConfigurationFileURL(URL configurationFileURL) {
		this.configurationFileURL = configurationFileURL;
	}

	/**
	 * @param props
	 *            ustawianie ścieżki do konfiguracji EhCache na podstawie
	 *            parametrów (parametry muszą zawierać właściwość o nazwie
	 *            {@link EhCacheConstants#PROP_CONFIG_PATH} aby wartość była
	 *            ustawione.
	 */
	public void setConfigurationFileURL(Properties props) {
		String customConfigPath = (props
				.getProperty(EhCacheConstants.PROP_CONFIG_PATH) != null ? (String) props
				.getProperty(EhCacheConstants.PROP_CONFIG_PATH) : null);
		if (StringUtils.isBlank(customConfigPath)) {
			logger.warn("No property {} found.",
					EhCacheConstants.PROP_CONFIG_PATH);
			return;
		}
		URL url = EhCacheProvider.class.getResource(customConfigPath);
		if (url == null) {
			logger.warn(
					"Wrong value of property {} found. Can't find resource: '{}'",
					new Object[] { EhCacheConstants.PROP_CONFIG_PATH,
							customConfigPath });
			return;
		}
		this.configurationFileURL = url;
	}

	/**
	 * @return the {@link #additionalProps}
	 */
	public Properties getAdditionalProps() {
		return additionalProps;
	}

	/**
	 * @param additionalProps
	 *            the {@link #additionalProps} to set
	 */
	public void setAdditionalProps(Properties additionalProps) {
		this.additionalProps = additionalProps;
	}
}
