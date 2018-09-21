package org.hibernate.cache.ehcache;

import static org.hibernate.cache.ehcache.ConfigSettings.EHCACHE_CONFIGURATION_RESOURCE_NAME;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.hibernate.boot.spi.SessionFactoryOptions;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.cfg.spi.DomainDataRegionBuildingContext;
import org.hibernate.cache.cfg.spi.DomainDataRegionConfig;
import org.hibernate.cache.internal.DefaultCacheKeysFactory;
import org.hibernate.cache.spi.CacheKeysFactory;
import org.hibernate.cache.spi.DomainDataRegion;
import org.hibernate.cache.spi.SecondLevelCacheLogger;
import org.hibernate.cache.spi.support.DomainDataRegionImpl;
import org.hibernate.cache.spi.support.DomainDataStorageAccess;
import org.hibernate.cache.spi.support.RegionFactoryTemplate;
import org.hibernate.cache.spi.support.RegionNameQualifier;
import org.hibernate.cache.spi.support.StorageAccess;
import org.hibernate.engine.spi.SessionFactoryImplementor;

import pl.slawas.common.cache.CacheConstants;
import pl.slawas.common.cache.CacheProviderConfiguration;
import pl.slawas.common.cache.CacheProviderFactory;
import pl.slawas.common.cache.IObjectCache;
import pl.slawas.common.cache.IObjectCacheProvider;
import pl.slawas.common.cache.IObjectCacheStatistics;
import pl.slawas.common.cache.ObjectCacheStatisticsList;

public abstract class ScCacheRegionFactorySupport<C> extends RegionFactoryTemplate
		implements ScCacheRegionFactoryInstance<C> {

	private static final long serialVersionUID = 8498725914796376618L;

	private final transient CacheKeysFactory cacheKeysFactory;
	private transient volatile IObjectCacheProvider<C> cacheManager;
	private transient volatile MissingCacheStrategy missingCacheStrategy;

	private Properties properties;

	protected ScCacheRegionFactorySupport() {
		this(DefaultCacheKeysFactory.INSTANCE);
	}

	protected ScCacheRegionFactorySupport(CacheKeysFactory cacheKeysFactory) {
		this.cacheKeysFactory = cacheKeysFactory;
	}

	@Override
	protected CacheKeysFactory getImplicitCacheKeysFactory() {
		return cacheKeysFactory;
	}

	@Override
	public DomainDataRegion buildDomainDataRegion(DomainDataRegionConfig regionConfig,
			DomainDataRegionBuildingContext buildingContext) {
		return new DomainDataRegionImpl(regionConfig, this,
				createDomainDataStorageAccess(regionConfig, buildingContext), cacheKeysFactory, buildingContext);
	}

	@Override
	protected DomainDataStorageAccess createDomainDataStorageAccess(DomainDataRegionConfig regionConfig,
			DomainDataRegionBuildingContext buildingContext) {
		return new ScStorageAccessImpl(
				getOrCreateCache(regionConfig.getRegionName(), buildingContext.getSessionFactory()));
	}

	@Override
	protected StorageAccess createQueryResultsRegionStorageAccess(String regionName,
			SessionFactoryImplementor sessionFactory) {
		String defaultedRegionName = defaultRegionName(regionName, sessionFactory,
				DEFAULT_QUERY_RESULTS_REGION_UNQUALIFIED_NAME, LEGACY_QUERY_RESULTS_REGION_UNQUALIFIED_NAMES);
		return new ScStorageAccessImpl(getOrCreateCache(defaultedRegionName, sessionFactory));
	}

	@Override
	protected StorageAccess createTimestampsRegionStorageAccess(String regionName,
			SessionFactoryImplementor sessionFactory) {
		String defaultedRegionName = defaultRegionName(regionName, sessionFactory,
				DEFAULT_UPDATE_TIMESTAMPS_REGION_UNQUALIFIED_NAME, LEGACY_UPDATE_TIMESTAMPS_REGION_UNQUALIFIED_NAMES);
		return new ScStorageAccessImpl(getOrCreateCache(defaultedRegionName, sessionFactory));
	}

	protected final String defaultRegionName(String regionName, SessionFactoryImplementor sessionFactory,
			String defaultRegionName, List<String> legacyDefaultRegionNames) {
		if (defaultRegionName.equals(regionName) && !cacheExists(regionName, sessionFactory)) {
			// Maybe the user configured caches explicitly with legacy names; try them and
			// use the first that exists

			for (String legacyDefaultRegionName : legacyDefaultRegionNames) {
				if (cacheExists(legacyDefaultRegionName, sessionFactory)) {
					SecondLevelCacheLogger.INSTANCE.usingLegacyCacheName(defaultRegionName, legacyDefaultRegionName);
					return legacyDefaultRegionName;
				}
			}
		}

		return regionName;
	}

	protected IObjectCache getOrCreateCache(String unqualifiedRegionName, SessionFactoryImplementor sessionFactory) {
		verifyStarted();
		assert !RegionNameQualifier.INSTANCE.isQualified(unqualifiedRegionName,
				sessionFactory.getSessionFactoryOptions());

		final String qualifiedRegionName = RegionNameQualifier.INSTANCE.qualify(unqualifiedRegionName,
				sessionFactory.getSessionFactoryOptions());

		final IObjectCache cache = cacheManager.getCache(qualifiedRegionName);
		if (cache == null) {
			return createCache(qualifiedRegionName);
		}
		return cache;
	}

	protected IObjectCache createCache(String regionName) {
		switch (missingCacheStrategy) {
		case CREATE_WARN:
			SecondLevelCacheLogger.INSTANCE.missingCacheCreated(regionName, ConfigSettings.MISSING_CACHE_STRATEGY,
					MissingCacheStrategy.CREATE.getExternalRepresentation());
			return cacheManager.getCache(regionName);
		case CREATE:
			return cacheManager.getCache(regionName);
		case FAIL:
			throw new CacheException(
					"On-the-fly creation of Ehcache Cache objects is not supported [" + regionName + "]");
		default:
			throw new IllegalStateException("Unsupported missing cache strategy: " + missingCacheStrategy);
		}
	}

	protected boolean cacheExists(String unqualifiedRegionName, SessionFactoryImplementor sessionFactory) {
		final String qualifiedRegionName = RegionNameQualifier.INSTANCE.qualify(unqualifiedRegionName,
				sessionFactory.getSessionFactoryOptions());
		return cacheManager.getCache(qualifiedRegionName) != null;
	}

	@Override
	protected boolean isStarted() {
		return super.isStarted() && cacheManager != null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected void prepareForUse(SessionFactoryOptions settings, Map configValues) {
		synchronized (this) {
			this.cacheManager = useNormalCacheManager(configValues);
			if (this.cacheManager == null) {
				throw new CacheException("Could not start Ehcache CacheManager");
			}
			this.missingCacheStrategy = MissingCacheStrategy
					.interpretSetting(configValues.get(ConfigSettings.MISSING_CACHE_STRATEGY));
		}
	}

	/**
	 * Locate the CacheManager during start-up. protected to allow for subclassing
	 * such as SingletonEhcacheRegionFactory
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected IObjectCacheProvider<C> useNormalCacheManager(Map properties) {
		String configurationResourceName = null;
		Properties cacheProperties = new Properties();
		if (properties != null) {
			cacheProperties.putAll(properties);
			configurationResourceName = (String) properties.get(EHCACHE_CONFIGURATION_RESOURCE_NAME);
		}
		if (StringUtils.isNotBlank(configurationResourceName)) {
			cacheProperties.put(CacheConstants.PROP_CONFIG_PATH, configurationResourceName);
		}
		return (IObjectCacheProvider<C>) CacheProviderFactory.getInstance(cacheProperties);
	}

	@Override
	protected void releaseFromUse() {
		/* do nothing */
	}

	@Override
	public CacheProviderConfiguration<Properties> initConfiguration(Properties additionalProps) {
		this.properties = new Properties();
		this.properties.putAll(additionalProps);
		return new CacheProviderConfiguration<>(this.properties, /* additionalProperties */ null);
	}

	@Override
	public boolean init() {
		prepareForUse(/* settings */ null, properties);
		return true;
	}

	@Override
	public String getName() {
		return this.cacheManager.getName();
	}

	@Override
	public String[] getCacheNames() {
		return this.cacheManager.getCacheNames();
	}

	@Override
	public IObjectCache getCache(String name) {
		return this.cacheManager.getCache(name);
	}

	@Override
	public void removeCache(String name) {
		this.cacheManager.removeCache(name);
	}

	@Override
	public void close() {
		if (this.cacheManager != null) {
			try {
				this.cacheManager.close();
				this.cacheManager = null;
			} catch (IllegalStateException e) {
				/* ignore */
			}
		}
	}

	@Override
	public ObjectCacheStatisticsList getAllStatistics(Integer offset, Integer pageSize) {
		return this.cacheManager.getAllStatistics(offset, pageSize);
	}

	@Override
	public IObjectCacheStatistics getStatistics(String regionName) {
		return this.cacheManager.getStatistics(regionName);
	}

	@Override
	public void clearCache(String cacheName) {
		this.cacheManager.clearCache(cacheName);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void clearStatistics(String cacheName) {
		this.cacheManager.clearStatistics(cacheName);
	}

	@Override
	public URL getConfigurationFileURL() {
		return this.cacheManager.getConfigurationFileURL();
	}

	@Override
	public void setAdditionalProps(Properties additionalProps) {
		if (additionalProps != null) {
			this.properties.putAll(additionalProps);
		}
	}

	@Override
	public IObjectCacheProvider<C> getManager() {
		return this.cacheManager;
	}
}
