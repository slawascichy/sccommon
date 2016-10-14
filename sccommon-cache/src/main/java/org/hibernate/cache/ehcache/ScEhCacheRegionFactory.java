package org.hibernate.cache.ehcache;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.hibernate.management.impl.ProviderMBeanRegistrationHelper;
import net.sf.ehcache.util.Timestamper;

import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.ehcache.internal.nonstop.NonstopAccessStrategyFactory;
import org.hibernate.cache.ehcache.internal.regions.EhcacheCollectionRegion;
import org.hibernate.cache.ehcache.internal.regions.EhcacheEntityRegion;
import org.hibernate.cache.ehcache.internal.regions.EhcacheNaturalIdRegion;
import org.hibernate.cache.ehcache.internal.regions.EhcacheQueryResultsRegion;
import org.hibernate.cache.ehcache.internal.regions.EhcacheTimestampsRegion;
import org.hibernate.cache.ehcache.internal.strategy.EhcacheAccessStrategyFactory;
import org.hibernate.cache.ehcache.internal.strategy.EhcacheAccessStrategyFactoryImpl;
import org.hibernate.cache.ehcache.internal.util.HibernateEhcacheUtils;
import org.hibernate.cache.spi.CacheDataDescription;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.QueryResultsRegion;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.TimestampsRegion;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.service.spi.InjectService;

import pl.slawas.common.cache.CacheProviderConfiguration;
import pl.slawas.common.cache.CacheProviderEnum;
import pl.slawas.common.cache.CacheProviderFactory;
import pl.slawas.common.cache.ObjectCacheStatisticsList;
import pl.slawas.common.cache._IObjectCache;
import pl.slawas.common.cache._IObjectCacheStatistics;
import pl.slawas.common.cache.ehcache.EhCacheConstants;
import pl.slawas.common.cache.ehcache.EhCacheInstance;
import pl.slawas.common.cache.ehcache.EhCacheProvider;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

public class ScEhCacheRegionFactory implements RegionFactory, EhCacheInstance {

	private static final long serialVersionUID = 8498725914796376618L;

	private static final Logger logger = LoggerFactory
			.getLogger(ScEhCacheRegionFactory.class);

	private static final String notInicjalizedMessage = "Wewnętrzny provider jest nie zainicjalizowany. Wcześniej użyj metod init(CacheProviderConfiguration, Properties) albo start(SessionFactoryOptions, Properties)";

	private final ProviderMBeanRegistrationHelper mbeanRegistrationHelper;
	private final EhcacheAccessStrategyFactory accessStrategyFactory;
	private EhCacheInstance internalProvider;
	private static Object initLock = new Object();
	private SessionFactoryOptions settings;
	private ClassLoaderService classLoaderService;

	public ScEhCacheRegionFactory() {
		this.mbeanRegistrationHelper = new ProviderMBeanRegistrationHelper();
		this.accessStrategyFactory = new NonstopAccessStrategyFactory(
				new EhcacheAccessStrategyFactoryImpl());
	}

	/* Overridden (non-Javadoc) */
	@Override
	public CollectionRegion buildCollectionRegion(String regionName,
			Properties properties, CacheDataDescription metadata)
			throws CacheException {
		logger.debug("[buildCollectionRegion] regionName: {}", regionName);
		return new EhcacheCollectionRegion(this.accessStrategyFactory,
				getEhCaheCache(regionName), this.settings, metadata, properties);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public EntityRegion buildEntityRegion(String regionName,
			Properties properties, CacheDataDescription metadata)
			throws CacheException {
		logger.debug("[buildEntityRegion] regionName: {}", regionName);
		return new EhcacheEntityRegion(this.accessStrategyFactory,
				getEhCaheCache(regionName), this.settings, metadata, properties);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public NaturalIdRegion buildNaturalIdRegion(String regionName,
			Properties properties, CacheDataDescription metadata)
			throws CacheException {
		logger.debug("[buildNaturalIdRegion] regionName: {}", regionName);
		return new EhcacheNaturalIdRegion(this.accessStrategyFactory,
				getEhCaheCache(regionName), this.settings, metadata, properties);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public QueryResultsRegion buildQueryResultsRegion(String regionName,
			Properties properties) throws CacheException {
		logger.debug("[buildQueryResultsRegion] regionName: {}", regionName);
		return new EhcacheQueryResultsRegion(this.accessStrategyFactory,
				getEhCaheCache(regionName), properties);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public TimestampsRegion buildTimestampsRegion(String regionName,
			Properties properties) throws CacheException {
		logger.debug("[buildTimestampsRegion] regionName: {}", regionName);
		return new EhcacheTimestampsRegion(this.accessStrategyFactory,
				getEhCaheCache(regionName), properties);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public AccessType getDefaultAccessType() {
		return AccessType.READ_WRITE;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isMinimalPutsEnabledByDefault() {
		return true;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public long nextTimestamp() {
		return Timestamper.next();
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void start(SessionFactoryOptions settings, Properties properties)
			throws CacheException {
		this.settings = settings;
		if (internalProvider != null) {
			logger.warn("{} is already started. ",
					ScEhCacheRegionFactory.class.getName());
			return;
		}
		try {
			EhCacheProvider tempInstance = new EhCacheProvider();
			tempInstance.setAdditionalProps(properties);

			String configurationResourceName = null;
			if (properties != null) {
				configurationResourceName = (String) properties
						.get("net.sf.ehcache.configurationResourceName");
			}
			/* nie każemy rejestrować, to zrobimy za chwilę */
			Properties additionalProps = new Properties();
			additionalProps.put(EhCacheConstants.PROP_CACHE_MANAGER_REGISTER,
					Boolean.toString(false));

			if ((configurationResourceName == null)
					|| (configurationResourceName.length() == 0)) {
				URL url = tempInstance.getConfigurationFileURL();
				CacheProviderConfiguration<Configuration> configuration = tempInstance
						.initConfiguration(url);
				String managerName = configuration.getManagerName();
				internalProvider = (EhCacheInstance) CacheProviderFactory
						.registerInstanceByManagerName(configuration,
								tempInstance, managerName);
				internalProvider.init(configuration, additionalProps);
			} else {
				/* mam inną ścieżkę do konfiguracji */
				URL url = loadResource(configurationResourceName);
				tempInstance.setConfigurationFileURL(url);
				Configuration configuration = HibernateEhcacheUtils
						.loadAndCorrectConfiguration(url);
				String managerName = configuration.getName();
				internalProvider = (EhCacheInstance) CacheProviderFactory
						.registerInstanceByManagerName(
								new CacheProviderConfiguration<Configuration>(
										managerName, configuration),
								tempInstance, managerName);
				internalProvider.init(
						new CacheProviderConfiguration<Configuration>(
								managerName, configuration), additionalProps);
			}
			this.mbeanRegistrationHelper.registerMBean(
					internalProvider.getManager(), properties);
		} catch (net.sf.ehcache.CacheException e) {
			if (e.getMessage()
					.startsWith(
							"Cannot parseConfiguration CacheManager. Attempt to create a new instance of CacheManager using the diskStorePath")) {
				throw new org.hibernate.cache.CacheException(
						"Attempt to restart an already started EhCacheRegionFactory. Use sessionFactory.close() between repeated calls to buildSessionFactory. Consider using SingletonEhCacheRegionFactory. Error from ehcache was: "
								+ e.getMessage());
			}

			throw new org.hibernate.cache.CacheException(e);
		} catch (Exception e) {
			throw new org.hibernate.cache.CacheException(e);
		}
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void stop() {
		close();
	}

	private Ehcache getEhCaheCache(String name)
			throws org.hibernate.cache.CacheException {
		try {
			logger.trace("-->getEhCaheCache('{}')", name);
			_IObjectCache cache = getCache(name);
			return ((pl.slawas.common.cache.ehcache.EhCache) cache)
					.getEhCache();
		} catch (net.sf.ehcache.CacheException e) {
			throw new org.hibernate.cache.CacheException(e);
		}
	}

	@InjectService
	public void setClassLoaderService(ClassLoaderService classLoaderService) {
		this.classLoaderService = classLoaderService;
	}

	protected URL loadResource(String configurationResourceName) {
		URL url = null;
		if (this.classLoaderService != null) {
			url = this.classLoaderService
					.locateResource(configurationResourceName);
		}
		if (url == null) {
			ClassLoader standardClassloader = Thread.currentThread()
					.getContextClassLoader();
			if (standardClassloader != null) {
				url = standardClassloader
						.getResource(configurationResourceName);
			}
			if (url == null) {
				url = AbstractEhcacheRegionFactory.class
						.getResource(configurationResourceName);
			}
			if (url == null) {
				try {
					url = new URL(configurationResourceName);
				} catch (MalformedURLException localMalformedURLException) {
				}
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug(
					"Creating EhCacheRegionFactory from a specified resource: {}.  Resolved to URL: {}",
					new Object[] { configurationResourceName, url });
		}

		if (url == null) {
			logger.warn("{} unable to load configuration from ''{}. ",
					new Object[] { ScEhCacheRegionFactory.class.getName(),
							configurationResourceName });
		}
		return url;
	}

	@Override
	public boolean init(
			CacheProviderConfiguration<Configuration> configuration,
			Properties additionalProps) {
		synchronized (initLock) {
			if (internalProvider != null) {
				logger.warn("{} is already initialized. ",
						ScEhCacheRegionFactory.class.getName());
				return true;
			}
			String managerName = configuration.getManagerName();
			/* nie każemy rejestrować, to zrobimy za chwilę */
			additionalProps.put(EhCacheConstants.PROP_CACHE_MANAGER_REGISTER,
					Boolean.toString(false));
			EhCacheProvider tempInstance = new EhCacheProvider();
			internalProvider = (EhCacheInstance) CacheProviderFactory
					.registerInstanceByManagerName(configuration, tempInstance,
							managerName);
			/* UWAGA! to może być ta sama instancja !!! */
			if (internalProvider == this) {
				tempInstance.init(configuration, false, additionalProps);
				internalProvider = tempInstance;
			}
			mbeanRegistrationHelper.registerMBean(
					internalProvider.getManager(), additionalProps);
			return (internalProvider != null);
		}
	}

	@Override
	public String[] getCacheNames() {
		logger.trace("-->getCacheNames()");
		if (internalProvider != null) {
			return internalProvider.getCacheNames();
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

	@Override
	public _IObjectCache getCache(String name) {
		logger.trace("-->getCache('{}')", name);
		if (internalProvider != null) {
			return internalProvider.getCache(name);
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

	@Override
	public void removeCache(String name) {
		logger.trace("-->removeCache('{}')", name);
		if (internalProvider != null) {
			internalProvider.removeCache(name);
		} else {
			throw new IllegalAccessError(notInicjalizedMessage);
		}
	}

	@Override
	public void close() {
		logger.trace("-->close()");
		synchronized (initLock) {
			if (mbeanRegistrationHelper != null) {
				mbeanRegistrationHelper.unregisterMBean();
			}
			if (internalProvider != null) {
				internalProvider.close();
				internalProvider = null;
			}
		}
	}

	@Override
	public ObjectCacheStatisticsList getAllStatistics(Integer offset,
			Integer pageSize) {
		logger.trace("-->getAllStatistics({}, {})", new Object[] { offset,
				pageSize });
		if (internalProvider != null) {
			return internalProvider.getAllStatistics(offset, pageSize);
		}
		throw new IllegalAccessError(notInicjalizedMessage);

	}

	@Override
	public void clearCache(String cacheName) {
		logger.trace("-->clearCache('{}')", cacheName);
		if (internalProvider != null) {
			internalProvider.clearCache(cacheName);
		} else {
			throw new IllegalAccessError(notInicjalizedMessage);
		}
	}

	@Override
	public List<String> getKeysList(String cacheName) {
		logger.trace("-->getKeysList('{}')", cacheName);
		if (internalProvider != null) {
			return internalProvider.getKeysList(cacheName);
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

	/* Overridden (non-Javadoc) */
	@SuppressWarnings("deprecation")
	@Override
	public void clearStatistics(String cacheName) {
		logger.trace("-->clearStatistics('{}')", cacheName);
		if (internalProvider != null) {
			internalProvider.clearStatistics(cacheName);
		} else {
			throw new IllegalAccessError(notInicjalizedMessage);
		}
	}

	@Override
	public String getName() {
		logger.trace("-->getName()");
		if (internalProvider != null) {
			return internalProvider.getName();
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

	@Override
	public CacheProviderConfiguration<Configuration> initConfiguration(URL url) {
		EhCacheProvider tempInstance = new EhCacheProvider();
		return tempInstance.initConfiguration(url);
	}

	@Override
	public _IObjectCacheStatistics getStatistics(String regionName) {
		logger.trace("-->getName()");
		if (internalProvider != null) {
			return internalProvider.getStatistics(regionName);
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

	@Override
	public CacheProviderEnum getAssociatedProvider() {
		return CacheProviderEnum.EhCache;
	}

	@Override
	public URL getConfigurationFileURL() {
		logger.trace("-->getConfigurationFileURL()");
		if (internalProvider != null) {
			return internalProvider.getConfigurationFileURL();
		}
		EhCacheProvider tempInstance = new EhCacheProvider();
		return tempInstance.getConfigurationFileURL();
	}

	@Override
	public void setAdditionalProps(Properties additionalProps) {
		logger.trace("-->setAdditionalProps()");
		if (internalProvider != null) {
			internalProvider.setAdditionalProps(additionalProps);
		} else {
			throw new IllegalAccessError(notInicjalizedMessage);
		}

	}

	@Override
	public CacheManager getManager() {
		logger.trace("-->getManager()");
		if (internalProvider != null) {
			return internalProvider.getManager();
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

}
