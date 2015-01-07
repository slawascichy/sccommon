package pl.slawas.common.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.commons.lang.StringUtils;

import pl.slawas.helpers.FileUtils;
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

	private static _IObjectCacheProvider instance;

	private static CacheManager manager;

	private final Properties props;

	private Hashtable<String, _IObjectCache> caches = new Hashtable<String, _IObjectCache>();

	private EhCacheProvider(Properties props) {

		this.props = props;
		String customConfigPath = (props
				.getProperty(CacheConstants.PROP_CONFIG_PATH) != null ? (String) props
				.getProperty(CacheConstants.PROP_CONFIG_PATH) : null);
		if (StringUtils.isNotBlank(customConfigPath)) {
			logger.info("Laduje konfiguracje z pliku: {}", customConfigPath);
			manager = new CacheManager(customConfigPath);
		} else {
			logger.info("Laduje domyslna konfiguracje.");
			manager = new CacheManager(
					EhCacheProvider.class.getResourceAsStream(FILE_CONFIG_PATH));
		}
		for (String key : getCacheNames()) {
			caches.put(key, new EhCache(manager.getCache(key), props));
		}
	}

	public static _IObjectCacheProvider getInstance(Properties props) {
		if (instance == null) {
			String customProviderImpl = (props
					.getProperty(CacheConstants.PROP_PROVIDER_IMPL) != null ? (String) props
					.getProperty(CacheConstants.PROP_PROVIDER_IMPL) : null);

			if (StringUtils.isNotBlank(customProviderImpl)) {
				try {
					Class<?> impl = Class.forName(customProviderImpl);
					instance = (_IObjectCacheProvider) impl.newInstance();
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException(e);
				} catch (IllegalAccessException e) {
					throw new IllegalArgumentException(e);
				} catch (InstantiationException e) {
					throw new IllegalArgumentException(e);
				}
			} else {
				instance = new EhCacheProvider(props);
			}
		}
		return instance;
	}

	public String[] getCacheNames() {
		return manager.getCacheNames();
	}

	public _IObjectCache getCache(String name) {
		_IObjectCache cache = caches.get(name);
		if (cache == null) {
			logger.debug("Tworze nowy kesz o nazwie '{}'", name);
			manager.addCache(name);
			cache = new EhCache(manager.getCache(name), this.props);
			caches.put(name, cache);
		}
		return cache;
	}

	public void close() {
		manager.shutdown();
		instance = null;
	}

	public void removeCache(String name) {
		_IObjectCache cache = caches.get(name);
		if (cache != null) {
			logger.debug("Usuwam kesz o nazwie '{}'", name);
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

}
