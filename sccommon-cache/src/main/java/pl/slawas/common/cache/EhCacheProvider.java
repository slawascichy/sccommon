package pl.slawas.common.cache;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
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
import net.sf.ehcache.config.PersistenceConfiguration;
import net.sf.ehcache.config.PersistenceConfiguration.Strategy;
import net.sf.ehcache.management.ManagementService;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.cache.config.CacheConfig;
import pl.slawas.common.cache.config.CacheConstants;
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
public class EhCacheProvider implements Serializable, _IObjectCacheProvider {

	private static final long serialVersionUID = -6327091572644983790L;

	public static String FILE_CONFIG_PATH = FileUtils.resolveName(
			EhCacheProvider.class, "ehcache.xml");

	private static final Logger logger = LoggerFactory
			.getLogger(EhCacheProvider.class);

	public static final Object initLock = new Object();

	private CacheManager manager;

	private Properties props;

	private Map<String, _IObjectCache> caches = new Hashtable<String, _IObjectCache>();

	/**
	 * Podstawowy konstruktor - manager pamięci nie jest zainicjalizowany -
	 * powinno się później wywołać metodę {@link #init(Properties)}
	 */
	public EhCacheProvider() {

	}

	/**
	 * Konstruktor, który od razu inicjalizuje managera pamięci podręcznej.
	 * 
	 * @param props
	 *            lista właściwości
	 */
	public EhCacheProvider(Properties props) {
		init(props);
	}

	public boolean init(Configuration configuration) {
		if (manager != null) {
			/**
			 * Jeżeli manager jest już zainicjalizowany, to wystawiam tylko
			 * ostrzeżenie
			 */
			logger.warn("[{}] is inicjalized!",
					EhCacheProvider.class.getSimpleName());
			return false;
		}
		synchronized (initLock) {
			manager = new CacheManager(configuration);
			for (String key : getCacheNames()) {
				Cache c = manager.getCache(key);
				caches.put(key, new EhCache(c));
			}
			return true;
		}
	}

	/**
	 * Inicjalizacja managera pamięci podręcznej.
	 * 
	 * @param props
	 */
	public boolean init(Properties props) {

		if (manager != null) {
			/**
			 * Jeżeli manager jest już zainicjalizowany, to wystawiam tylko
			 * ostrzeżenie
			 */
			logger.warn("[{}] is inicjalized!",
					EhCacheProvider.class.getSimpleName());
			return false;
		}
		synchronized (initLock) {
			this.props = props;
			if (logger.isDebugEnabled()) {
				for (Entry<Object, Object> entry : this.props.entrySet()) {
					logger.debug("[init] Laduje prperty {}: '{}'",
							new Object[] { entry.getKey(), entry.getValue() });
				}
			}
			String customConfigPath = (System
					.getProperty(CacheConstants.PROP_CONFIG_PATH) != null ? (String) System
					.getProperty(CacheConstants.PROP_CONFIG_PATH) : null);
			if (StringUtils.isBlank(customConfigPath)) {
				/* Ustawiony parametr systemowy ma pierwszeństwo */
				customConfigPath = (props
						.getProperty(CacheConstants.PROP_CONFIG_PATH) != null ? (String) props
						.getProperty(CacheConstants.PROP_CONFIG_PATH) : null);
			}
			if (StringUtils.isNotBlank(customConfigPath)) {

				URL resource = EhCacheProvider.class
						.getResource(customConfigPath);
				logger.info("Laduje konfiguracje z pliku {} classpath: {}",
						new Object[] { (resource == null ? "spoza" : "z"),
								customConfigPath });
				if (resource == null) {
					manager = new CacheManager(customConfigPath);
				} else {
					manager = new CacheManager(
							EhCacheProvider.class
									.getResourceAsStream(customConfigPath));
				}
			} else {
				logger.info("Laduje domyslna konfiguracje.");
				manager = new CacheManager(
						EhCacheProvider.class
								.getResourceAsStream(FILE_CONFIG_PATH));
			}
			if (!CacheConfig.statisticsIsDisabled()) {
				MBeanServer mBeanServer = ManagementFactory
						.getPlatformMBeanServer();
				ManagementService.registerMBeans(manager, mBeanServer, false,
						false, false, true);
			}
			for (String key : getCacheNames()) {
				Cache c = manager.getCache(key);
				caches.put(key, new EhCache(c));
			}
			return true;
		}
	}

	public String[] getCacheNames() {
		return manager.getCacheNames();
	}

	public _IObjectCache getCache(String name) {
		String lock = ("EhCacheProvider.getCache." + name).intern();
		synchronized (lock) {
			_IObjectCache cache = caches.get(name);
			if (cache == null) {
				/** Uzupełnienie konfiguracji */
				CacheConfiguration config;
				String param;
				String originalKey = name;
				param = (String) props.get(originalKey + Strings.DOTChar
						+ CacheConstants.PROP_maxElementsInMemory);
				if (StringUtils.isNotBlank(param)) {
					config = new CacheConfiguration(name,
							Integer.parseInt(param));
				} else {
					config = new CacheConfiguration(name,
							CacheConstants.DEFAULT_maxElementsInMemory);
				}
				param = (String) props.get(originalKey + Strings.DOTChar
						+ CacheConstants.PROP_eternal);
				if (StringUtils.isNotBlank(param)) {
					config.setEternal(Boolean.parseBoolean(param));
				}
				param = (String) props.get(originalKey + Strings.DOTChar
						+ CacheConstants.PROP_memoryStoreEvictionPolicy);
				if (StringUtils.isNotBlank(param)) {
					config.setMemoryStoreEvictionPolicy(param);
				}

				param = (String) props.get(originalKey + Strings.DOTChar
						+ CacheConstants.PROP_strategy);
				PersistenceConfiguration pc;
				if (StringUtils.isNotBlank(param)) {
					pc = new PersistenceConfiguration();
					pc.setStrategy(param);
				} else {
					pc = new PersistenceConfiguration()
							.strategy(Strategy.LOCALTEMPSWAP);
				}
				config.persistence(pc);
				logger.debug("Tworze nowy region '{}'", name);
				manager.addCache(new Cache(config));
				cache = new EhCache(manager.getCache(name));
				caches.put(name, cache);
			}
			return cache;
		}
	}

	public void close() {
		manager.shutdown();
		EhCacheProviderFactory.close();
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
	public List<_IObjectCacheStatistics> getAllStatistics(Integer maxRecords) {
		String[] keys = manager.getCacheNames();
		List<_IObjectCacheStatistics> result = new ArrayList<_IObjectCacheStatistics>();
		int recordCount = 0;
		for (String key : keys) {
			if (!(maxRecords == null || recordCount < maxRecords.intValue())) {
				// przerywam, ze względu na założone limity.
				break;
			}
			/* pobieram statystyki cache */
			_IObjectCacheStatistics row = getCache(key).getStatistics();
			result.add(row);
			recordCount++;
		}
		return result;

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

}
