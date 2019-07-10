package pl.slawas.common.cache.ehcache;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.EhcacheManager;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.spi.service.Service;
import org.ehcache.xml.XmlConfiguration;

import pl.slawas.common.cache.CacheConstants;
import pl.slawas.common.cache.CacheProviderConfiguration;
import pl.slawas.common.cache.CacheProviderEnum;
import pl.slawas.common.cache.CacheProviderFactory;
import pl.slawas.common.cache.EmptyObjectCacheStatistics;
import pl.slawas.common.cache.IObjectCache;
import pl.slawas.common.cache.IObjectCacheStatistics;
import pl.slawas.common.cache.ObjectCacheStatisticsList;
import pl.slawas.common.cache.exceptions.CacheErrorException;
import pl.slawas.helpers.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * EhCacheProvider - utworzenie instancji CacheManager-a
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.3 $
 * 
 */
public class EhCacheProvider implements EhCacheInstance {

	private static final String DEFAULT_TEMPLATE_NAME = "default";
	private static final String EHCACHE_XML = "/ehcache.xml";
	private static final long serialVersionUID = -6327091572644983790L;
	private static final Logger logger = LoggerFactory.getLogger(EhCacheProvider.class);
	public static final Object initLock = new Object();
	private static URL defaultConfigurationFileURL;

	private URL configurationFileURL;

	private transient CacheManager manager;

	/**
	 * Dodatkowe parametry dotyczące nowo tworzonych regionów pamięci podręcznej
	 */
	private Properties additionalProps;

	private transient CacheProviderConfiguration<XmlConfiguration> configuration;

	private transient StatisticsService statisticsService;

	private final transient Map<String, IObjectCache> caches = new HashMap<>();

	/* Overridden (non-Javadoc) */
	@Override
	public boolean init() {
		synchronized (initLock) {
			if (manager != null) {
				/**
				 * Jeżeli manager jest już zainicjalizowany, to wystawiam tylko ostrzeżenie
				 */
				logger.warn("[init] {} is inicjalized!", EhCacheProvider.class.getSimpleName());
				return false;
			}
			/* tworzę manager'a */
			CacheManager newManager;
			String disableStatistics = additionalProps.getProperty(CacheConstants.PROP_DISABLE_STATISTICS,
					CacheConstants.DEFAULT_DISABLE_STATISTICS);

			XmlConfiguration xmlConfiguration = this.configuration.getConfiguration();
			newManager = createSingleManager(disableStatistics, xmlConfiguration);

			this.manager = newManager;
			this.manager.init();
			return true;
		}
	}

	private CacheManager createSingleManager(String disableStatistics, XmlConfiguration xmlConfiguration) {
		CacheManager newManager;
		if (Boolean.parseBoolean(disableStatistics)) {
			/* statystyki są wyłączone */
			newManager = CacheManagerBuilder.newCacheManager(xmlConfiguration);
		} else {
			this.statisticsService = new EhCacheStatisticsService();
			Set<Service> services = new HashSet<>();
			services.add(this.statisticsService);
			newManager = new EhcacheManager(xmlConfiguration, services);
		}
		return newManager;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public CacheProviderConfiguration<XmlConfiguration> initConfiguration(Properties additionalProperties) {
		synchronized (initLock) {
			setAdditionalProps(additionalProperties);
			XmlConfiguration config;
			String configurationPath = additionalProperties.getProperty(CacheConstants.PROP_CONFIG_PATH);
			URL url = null;
			if (configurationPath != null) {
				File f = new File(configurationPath);
				logger.info("[initConfiguration] Try loading configuration from : {} (exists?: {})",
						new Object[] { configurationPath, f.exists() });
				if (f.exists()) {
					try {
						url = f.toURI().toURL();
					} catch (MalformedURLException e) {
						/* nie ma prawa się zdarzyć, bo URI pochodzi od File */
					}
				} else {
					url = EhCacheProvider.class.getResource(configurationPath);
				}
			}
			if (url == null) {
				/* jeżeli nie jest ustawiony URL, to zostanie ustawiony domyślny */
				url = getConfigurationFileURL();
			} else {
				setConfigurationFileURL(url);
			}
			logger.info("[initConfiguration] Loading configuration from : {}", new Object[] { url });
			config = new XmlConfiguration(url);
			this.configuration = new CacheProviderConfiguration<>(config, additionalProperties);
			return this.configuration;
		}
	}

	public String[] getCacheNames() {
		if (!caches.isEmpty()) {
			String[] result = new String[caches.size()];
			int i = 0;
			for (String cacheName : caches.keySet()) {
				result[i] = cacheName;
				i++;
			}
			return result;
		}
		return new String[] {};
	}

	public IObjectCache getCache(final String name) {
		Object lock = ("EhCacheProvider[" + getName() + "].getCache." + name).intern();
		synchronized (lock) {
			return caches.computeIfAbsent(name, k -> {
				try {
					return createCache(name);
				} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
					throw new IllegalArgumentException(String.format("-->getCache: Error for cache %s.", name));
				}
			});
		}
	}

	@SuppressWarnings({ "unchecked" })
	private IObjectCache createCache(String name)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		/** Uzupełnienie konfiguracji */
		XmlConfiguration xmlConfiguration = this.configuration.getConfiguration();
		CacheConfiguration<String, Element> cacheConfig = (CacheConfiguration<String, Element>) xmlConfiguration
				.getCacheConfigurations().get(name);
		if (cacheConfig == null) {
			CacheConfigurationBuilder<String, Element> configurationBuilder;
			configurationBuilder = createConfigurationBuilder(name, xmlConfiguration);
			cacheConfig = configurationBuilder.build();
		}
		Cache<String, Element> localCache = this.manager.getCache(name, String.class, Element.class);
		return new EhCache(getName(), name, this.manager, this.statisticsService, cacheConfig, (localCache != null));
	}

	private CacheConfigurationBuilder<String, Element> createConfigurationBuilder(String name,
			XmlConfiguration xmlConfiguration)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		final String defaultTemplate = DEFAULT_TEMPLATE_NAME;
		CacheConfigurationBuilder<String, Element> configurationBuilder;
		configurationBuilder = xmlConfiguration.newCacheConfigurationBuilderFromTemplate(defaultTemplate, String.class,
				Element.class);
		if (configurationBuilder == null) {
			CustomExpiry<String, Element> expiryPolicy = createExpiryPolicy(name);
			ResourcePoolsBuilder rBuilder = createResourcePoolBuilder(name);
			ResourcePools rPool = rBuilder.build();
			if (expiryPolicy != null) {
				configurationBuilder = CacheConfigurationBuilder
						.newCacheConfigurationBuilder(String.class, Element.class, rPool).withExpiry(expiryPolicy);
			} else {
				configurationBuilder = CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class,
						Element.class, rPool);
			}
		}
		return configurationBuilder;
	}

	private CustomExpiry<String, Element> createExpiryPolicy(String name) {
		/* Expiry Policy - start */
		CustomExpiry<String, Element> expiryPolicy = null;
		if (getProperty(name, EhCacheConfig.PROP_CACHE_TIME_TO_LIVE_SECONDS) != null) {
			long value = Long.parseLong(getProperty(name, EhCacheConfig.PROP_CACHE_TIME_TO_LIVE_SECONDS));
			expiryPolicy = new CustomExpiry<>();
			expiryPolicy.setTimeToLiveExpiration(java.time.Duration.ofSeconds(value));
		}
		if (getProperty(name, EhCacheConfig.PROP_CACHE_TIME_TO_IDLE_SECONDS) != null) {
			long value = Long.parseLong(getProperty(name, EhCacheConfig.PROP_CACHE_TIME_TO_IDLE_SECONDS));
			if (expiryPolicy == null) {
				expiryPolicy = new CustomExpiry<>();
			}
			expiryPolicy.setTimeToIdleExpiration(java.time.Duration.ofSeconds(value));
		}
		/* Expiry Policy - end */
		return expiryPolicy;
	}

	private ResourcePoolsBuilder createResourcePoolBuilder(String name) {
		long heap = Long.parseLong(
				getProperty(name, EhCacheConfig.PROP_CACHE_HEAP, Integer.toString(EhCacheConfig.DEFAULT_HEAP)));
		ResourcePoolsBuilder rBuilder;
		if (getProperty(name, EhCacheConfig.PROP_CACHE_OFFHEAP) != null) {
			long offHeap = Long.parseLong(getProperty(name, EhCacheConfig.PROP_CACHE_OFFHEAP));
			if (getProperty(name, EhCacheConfig.PROP_CACHE_DISK) != null) {
				long disk = Long.parseLong(getProperty(name, EhCacheConfig.PROP_CACHE_DISK));
				rBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder().heap(heap, EntryUnit.ENTRIES)
						.offheap(offHeap, MemoryUnit.MB).disk(disk, MemoryUnit.MB, true);
			} else {
				rBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder().heap(heap, EntryUnit.ENTRIES).offheap(offHeap,
						MemoryUnit.MB);
			}
		} else {
			rBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder().heap(heap, EntryUnit.ENTRIES);
		}
		return rBuilder;
	}

	private String getProperty(String cacheName, String propertyName, String defaultValue) {
		String key = cacheName + "." + propertyName;
		return this.additionalProps.getProperty(key, defaultValue);
	}

	private String getProperty(String cacheName, String propertyName) {
		String key = cacheName + "." + propertyName;
		return this.additionalProps.getProperty(key);
	}

	public void close() {
		manager.close();
		CacheProviderFactory.unbindInstanceByManagerName(getAssociatedProvider(), getName());
	}

	public void removeCache(String name) {
		IObjectCache cache = caches.get(name);
		if (cache != null) {
			logger.debug("Usuwam region o nazwie '{}'", name);
			manager.removeCache(name);
			caches.remove(name);
		}
	}

	@Override
	public ObjectCacheStatisticsList getAllStatistics(Integer offset, Integer pageSize) {
		String[] keys = getCacheNames();
		List<IObjectCacheStatistics> result = new ArrayList<>();
		int lOffset = (offset == null ? 0 : offset.intValue());
		int lPageSize = (pageSize == null ? CacheConstants.DEFAULT_MAX_RESULT_SIZE : pageSize.intValue());
		if (lOffset >= keys.length) {
			return new ObjectCacheStatisticsList(result, keys.length, lOffset, lPageSize);
		}
		try {
			Strings.sort(keys);
		} catch (Exception e) {
			logger.warn("[" + getName() + ".getAllStatistics]  Nieudana próba posortowania nazw regionów.", e);
		}

		for (int i = lOffset; i < keys.length; i++) {
			if ((i >= lPageSize)) {
				/* przerywam, ze względu na założone limity. */
				break;
			}
			/* pobieram statystyki cache */
			IObjectCacheStatistics row = getStatistics(keys[i]);
			result.add(row);
		}
		return new ObjectCacheStatisticsList(result, keys.length, lOffset, lPageSize);

	}

	/* Overridden (non-Javadoc) */
	@Override
	public IObjectCacheStatistics getStatistics(final String regionName) {
		IObjectCache cache = caches.get(regionName);
		if (cache != null) {
			return cache.getStatistics();
		}
		return new EmptyObjectCacheStatistics(getName(), regionName, getAssociatedProvider());
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void clearCache(final String cacheName) {
		IObjectCache cache = caches.get(cacheName);
		if (cache != null) {
			try {
				cache.clear();
			} catch (CacheErrorException e) {
				logger.error(String.format("-->clearCache: Cache '%s' clear error!", cacheName), e);
			}
		}
	}

	/**
	 * @return the {@link #manager}
	 */
	public CacheManager getManager() {
		return manager;
	}

	@Override
	public void clearStatistics(String cacheName) {
		logger.warn("--> clearStatistics('{}'): Method is deprecated", cacheName);
	}

	@Override
	public String getName() {
		if (this.configuration == null) {
			throw new IllegalArgumentException("-->getName: Provider not initilized!");
		}
		return this.configuration.getManagerName();
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
			String customConfigPath = (System.getProperty(CacheConstants.PROP_CONFIG_PATH) != null
					? (String) System.getProperty(CacheConstants.PROP_CONFIG_PATH)
					: null);
			if (StringUtils.isBlank(customConfigPath)) {
				customConfigPath = EHCACHE_XML;
			}
			URL url = EhCacheProvider.class.getResource(customConfigPath);
			if (url == null) {
				File file = new File(customConfigPath);
				if (file.exists() && file.isFile()) {
					try {
						url = file.toURI().toURL();
					} catch (MalformedURLException e) {
						logger.warn("Error with conversion file name '" + customConfigPath + "' to URL.", e);
					}
				}
			}
			if (url == null) {
				url = EhCacheProvider.class.getResource("ehcache-failsafe.xml");
				logger.warn(
						"No configuration found. Configuring ehcache from ehcache-failsafe.xml  found in the classpath: {}",
						url);
			} else if (logger.isDebugEnabled()) {
				logger.debug(new StringBuilder().append("Configuring ehcache from ehcache.xml found in the classpath: ")
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
	 *            ustawianie ścieżki do konfiguracji EhCache na podstawie parametrów
	 *            (parametry muszą zawierać właściwość o nazwie
	 *            {@link CacheConstants#PROP_CONFIG_PATH} aby wartość była
	 *            ustawione.
	 */
	public void setConfigurationFileURL(Properties props) {
		String customConfigPath = (props.getProperty(CacheConstants.PROP_CONFIG_PATH) != null
				? (String) props.getProperty(CacheConstants.PROP_CONFIG_PATH)
				: null);
		if (StringUtils.isBlank(customConfigPath)) {
			logger.warn("No property {} found.", CacheConstants.PROP_CONFIG_PATH);
			return;
		}
		URL url = EhCacheProvider.class.getResource(customConfigPath);
		if (url == null) {
			logger.warn("Wrong value of property {} found. Can't find resource: '{}'",
					new Object[] { CacheConstants.PROP_CONFIG_PATH, customConfigPath });
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
		if (additionalProps == null) {
			this.additionalProps = new Properties();
		}
		this.additionalProps = additionalProps;
	}

}
