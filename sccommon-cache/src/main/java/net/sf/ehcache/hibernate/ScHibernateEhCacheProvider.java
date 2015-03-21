package net.sf.ehcache.hibernate;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.hibernate.management.impl.ProviderMBeanRegistrationHelper;
import net.sf.ehcache.util.Timestamper;

import org.hibernate.cache.CacheProvider;

import pl.slawas.common.cache._IObjectCache;
import pl.slawas.common.cache._IObjectCacheProvider;
import pl.slawas.common.cache._IObjectCacheStatistics;
import pl.slawas.common.cache.config.CacheConfig;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * 
 * ScHibernateEhCacheProvider - utworzenie instancji CacheManager-a.
 * 
 * Implementacja nadpisuje oryginalny provider EhCache z klasy
 * net.sf.ehcache.hibernate.EhCacheProvider.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.3 $
 * 
 */
public class ScHibernateEhCacheProvider implements CacheProvider, Serializable,
		_IObjectCacheProvider {

	private static final long serialVersionUID = -9043301647791008352L;

	private static final Logger logger = LoggerFactory
			.getLogger(ScHibernateEhCacheProvider.class);

	private static final String notInicjalizedMessage = "Wewnętrzny provider jest nie zainicjalizowany. Wcześniej użyj metod init(Properties) albo start(Properties)";

	private static ProviderMBeanRegistrationHelper mbeanRegistrationHelper;
	private static pl.slawas.common.cache.EhCacheProvider internalProvider;
	private static Object initLock = new Object();

	public ScHibernateEhCacheProvider() {
		super();
	}

	/* Overridden (non-Javadoc) */
	/**
	 * Uwaga! @param paramProperties to aktualne parametry pobierane z systemu
	 * oraz ustawień JPA (plik persistence.xml). <b>Niestety parametry JPA mają
	 * pierwszeństwo nad systemowymi</b>, więc lepiej nie ustawiać:
	 * 
	 * <pre>
	 * <property name="cache.configPath" value="/pro/ibpm/mercury/cache/ehcache.xml" />
	 * </pre>
	 * 
	 * TODO Zdefiniowanego parametru JPA nie nadpiszemy w żaden sposób, chyba,
	 * ze jakoś dynamicznie zawładniemy nad tymi parametrami.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public org.hibernate.cache.Cache buildCache(String name,
			Properties paramProperties)
			throws org.hibernate.cache.CacheException {
		try {
			_IObjectCache cache = getCache(name);
			Ehcache netEhcache = ((pl.slawas.common.cache.EhCache) cache)
					.getEhCache();
			if (!CacheConfig.statisticsIsDisabled()) {
				netEhcache.setStatisticsEnabled(true);
				netEhcache
						.setStatisticsAccuracy(Statistics.STATISTICS_ACCURACY_GUARANTEED);
			}
			HibernateUtil.validateEhcache(netEhcache);
			return new EhCache(netEhcache);
		} catch (net.sf.ehcache.CacheException e) {
			throw new org.hibernate.cache.CacheException(e);
		}
	}

	/* Overridden (non-Javadoc) */
	@Override
	public long nextTimestamp() {
		return Timestamper.next();
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isMinimalPutsEnabledByDefault() {
		return true;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void start(Properties paramProperties)
			throws org.hibernate.cache.CacheException {
		init(paramProperties);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void stop() {
		close();
	}

	@Override
	public boolean init(Properties props) {
		synchronized (initLock) {
			if (internalProvider != null) {
				logger.warn("{} is already initialized. ",
						ScHibernateEhCacheProvider.class.getName());
				return true;
			}
			internalProvider = new pl.slawas.common.cache.EhCacheProvider(props);
			mbeanRegistrationHelper = new ProviderMBeanRegistrationHelper();
			mbeanRegistrationHelper.registerMBean(
					internalProvider.getManager(), props);
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
				mbeanRegistrationHelper = null;
			}
			if (internalProvider != null) {
				internalProvider.close();
				internalProvider = null;
			}
		}
	}

	@Override
	public List<_IObjectCacheStatistics> getAllStatistics(
			Integer getAllStatistics) {
		logger.trace("-->getAllStatistics('{}')", getAllStatistics);
		if (internalProvider != null) {
			return internalProvider.getAllStatistics(getAllStatistics);
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
	@Override
	public void clearStatistics(String cacheName) {
		logger.trace("-->clearStatistics('{}')", cacheName);
		if (internalProvider != null) {
			internalProvider.clearStatistics(cacheName);
		} else {
			throw new IllegalAccessError(notInicjalizedMessage);
		}
	}

}
