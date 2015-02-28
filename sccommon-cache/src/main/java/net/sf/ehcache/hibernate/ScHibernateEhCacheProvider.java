package net.sf.ehcache.hibernate;

import java.io.Serializable;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.Ehcache;
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

	private ProviderMBeanRegistrationHelper mbeanRegistrationHelper;
	private pl.slawas.common.cache.EhCacheProvider internalProvider;
	private Object initLock = new Object();

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
			netEhcache
					.setStatisticsEnabled(!CacheConfig.statisticsIsDisabled());
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
			if (this.internalProvider != null) {
				logger.warn("{} is already initialized. ",
						ScHibernateEhCacheProvider.class.getName());
				return true;
			}
			this.internalProvider = new pl.slawas.common.cache.EhCacheProvider(
					props);
			this.mbeanRegistrationHelper = new ProviderMBeanRegistrationHelper();
			this.mbeanRegistrationHelper.registerMBean(
					internalProvider.getManager(), props);
			return (this.internalProvider != null);
		}
	}

	@Override
	public String[] getCacheNames() {
		logger.trace("-->getCacheNames()");
		if (this.internalProvider != null) {
			return this.internalProvider.getCacheNames();
		}
		throw new NullPointerException(
				"Wewnętrzny provider jest nie zainicjalizowany. Wcześniej użyj metod init(Properties) albo start(Properties)");
	}

	@Override
	public _IObjectCache getCache(String name) {
		logger.trace("-->getCache('{}')", name);
		if (this.internalProvider != null) {
			return this.internalProvider.getCache(name);
		}
		throw new NullPointerException(
				"Wewnętrzny provider jest nie zainicjalizowany. Wcześniej użyj metod init(Properties) albo start(Properties)");
	}

	@Override
	public void removeCache(String name) {
		logger.trace("-->removeCache('{}')", name);
		if (this.internalProvider != null) {
			this.internalProvider.removeCache(name);
		} else {
			throw new NullPointerException(
					"Wewnętrzny provider jest nie zainicjalizowany. Wcześniej użyj metod init(Properties) albo start(Properties)");
		}
	}

	@Override
	public void close() {
		logger.trace("-->close()");
		synchronized (initLock) {
			if (this.mbeanRegistrationHelper != null) {
				this.mbeanRegistrationHelper.unregisterMBean();
				this.mbeanRegistrationHelper = null;
			}
			if (this.internalProvider != null) {
				this.internalProvider.close();
				this.internalProvider = null;
			}
		}
	}

	@Override
	public List<_IObjectCacheStatistics> getAllStatistics(
			Integer getAllStatistics) {
		logger.trace("-->getAllStatistics('{}')", getAllStatistics);
		if (this.internalProvider != null) {
			return this.internalProvider.getAllStatistics(getAllStatistics);
		}
		throw new NullPointerException(
				"Wewnętrzny provider jest nie zainicjalizowany. Wcześniej użyj metod init(Properties) albo start(Properties)");

	}

	@Override
	public void clearCache(String cacheName) {
		logger.trace("-->clearCache('{}')", cacheName);
		if (this.internalProvider != null) {
			this.internalProvider.clearCache(cacheName);
		} else {
			throw new NullPointerException(
					"Wewnętrzny provider jest nie zainicjalizowany. Wcześniej użyj metod init(Properties) albo start(Properties)");
		}
	}

	@Override
	public List<String> getKeysList(String cacheName) {
		logger.trace("-->getKeysList('{}')", cacheName);
		if (this.internalProvider != null) {
			return this.internalProvider.getKeysList(cacheName);
		}
		throw new NullPointerException(
				"Wewnętrzny provider jest nie zainicjalizowany. Wcześniej użyj metod init(Properties) albo start(Properties)");

	}

	/* Overridden (non-Javadoc) */
	@Override
	public void clearStatistics(String cacheName) {
		logger.trace("-->clearStatistics('{}')", cacheName);
		if (this.internalProvider != null) {
			this.internalProvider.clearStatistics(cacheName);
		} else {
			throw new NullPointerException(
					"Wewnętrzny provider jest nie zainicjalizowany. Wcześniej użyj metod init(Properties) albo start(Properties)");
		}
	}

}
