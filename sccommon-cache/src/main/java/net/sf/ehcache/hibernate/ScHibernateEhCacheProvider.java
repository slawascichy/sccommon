package net.sf.ehcache.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.hibernate.management.impl.ProviderMBeanRegistrationHelper;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.cache.EhCache;
import pl.slawas.common.cache._IObjectCache;
import pl.slawas.common.cache._IObjectCacheProvider;
import pl.slawas.common.cache._IObjectCacheStatistics;
import pl.slawas.common.cache.config.CacheConfig;
import pl.slawas.common.cache.config.CacheConstants;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * 
 * ScHibernateEhCacheProvider - utworzenie instancji CacheManager-a.
 * 
 * Implementacja nadpisuje oryginalny provider EhCache z klasy
 * net.sf.ehcache.hibernate.EhCacheProvider.
 * 
 * @author Mariusz Barwikowski &lt;mariusz.barwikowski@ibpm.pro&gt;
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.3 $
 * 
 */
public class ScHibernateEhCacheProvider extends AbstractEhcacheProvider
		implements Serializable, _IObjectCacheProvider {

	private static final long serialVersionUID = -6327091572644983790L;
	private static final Logger logger = LoggerFactory
			.getLogger(ScHibernateEhCacheProvider.class);

	public static final Object initLock = new Object();
	public static final String ORIG_MANAGER_EXISTS_MSG = "Attempt to restart an already started EhCacheProvider. Use sessionFactory.close()  between repeated calls to buildSessionFactory. Using previously created EhCacheProvider. If this behaviour is required, consider using SingletonEhCacheProvider.";

	private static _IObjectCacheProvider instance;

	private Hashtable<String, _IObjectCache> caches;

	private final ProviderMBeanRegistrationHelper mbeanRegistrationHelper;

	public ScHibernateEhCacheProvider() {
		synchronized (initLock) {
			if (instance == null) {
				logger.info("Inicjuje {}...",
						new Object[] { ScHibernateEhCacheProvider.class
								.getSimpleName() });
				this.caches = new Hashtable<String, _IObjectCache>();
				this.mbeanRegistrationHelper = new ProviderMBeanRegistrationHelper();
				instance = this;
			} else {
				this.caches = ((ScHibernateEhCacheProvider) instance).caches;
				this.mbeanRegistrationHelper = ((ScHibernateEhCacheProvider) instance).mbeanRegistrationHelper;
				this.manager = ((ScHibernateEhCacheProvider) instance).manager;
			}
		}
	}

	public String[] getCacheNames() {
		return manager.getCacheNames();
	}

	@Override
	public void start(Properties properties) throws CacheException {
		synchronized (initLock) {
			if (this.manager != null) {
				logger.warn(ORIG_MANAGER_EXISTS_MSG);
				return;
			}
			String customConfigPath = CacheConfig.getInstance().get(
					CacheConstants.PROP_CONFIG_PATH);
			if (StringUtils.isNotBlank(customConfigPath)) {
				logger.info("Ładuję konfigurację z pliku: {} ",
						customConfigPath);
				manager = new CacheManager(customConfigPath);
			} else {
				logger.info("Ładuję konfigurację domyślną. FILE_CONFIG_PATH="
						+ pl.slawas.common.cache.EhCacheProvider.FILE_CONFIG_PATH);
				manager = new CacheManager(
						ScHibernateEhCacheProvider.class
								.getResourceAsStream(pl.slawas.common.cache.EhCacheProvider.FILE_CONFIG_PATH));
			}

			for (String key : getCacheNames()) {
				caches.put(key, new EhCache(manager.getCache(key), CacheConfig
						.getInstance().getPropertyList()));
			}

			this.mbeanRegistrationHelper
					.registerMBean(this.manager, properties);
		}
	}

	@Override
	public final void stop() {
		if (this.manager != null) {
			this.manager.shutdown();
			this.manager = null;
		}
	}

	public _IObjectCache getCache(String name) {
		_IObjectCache result = null;

		Cache cache = manager.getCache(name);

		if (cache == null) {
			/*
			 * jeśli nie ma pamięci podręcznej w net.sf to tworzymy w net.sf
			 * oraz w prywatnej mapie
			 */
			logger.debug("Tworze nowy region o nazwie '{}'", name);
			manager.addCache(name);
			result = new EhCache(manager.getCache(name), CacheConfig
					.getInstance().getPropertyList());
			caches.put(name, result);

		} else {
			/*
			 * jeśli jest w net.sf to sprawdzamy czy jest w prywatnej mapie i
			 * ewentualnie go tam dodajemy, chyba że już jest to zwracamy ten
			 * który jest w mapie
			 */
			result = caches.get(name);
			if (result == null) {
				logger.debug("Tworze we własnej mapie region o nazwie '{}'",
						name);
				result = new EhCache(manager.getCache(name), CacheConfig
						.getInstance().getPropertyList());
				caches.put(name, result);
			}
		}
		return result;
	}

	public void close() {
		synchronized (initLock) {
			manager.shutdown();
			instance = null;
		}
	}

	public void removeCache(String name) {
		synchronized (initLock) {
			_IObjectCache cache = caches.get(name);
			if (cache != null) {
				logger.debug("Usuwam region o nazwie '{}'", name);
				manager.removeCache(name);
				caches.remove(name);
			}
		}
	}

	/* Overridden (non-Javadoc) */
	public void clearCache(String cacheName) {
		synchronized (initLock) {
			if (StringUtils.isNotBlank(cacheName)) {
				Cache ch = manager.getCache(cacheName);
				if (ch != null) {
					ch.removeAll();
				}
			}
		}
	}

	/* Overridden (non-Javadoc) */
	@SuppressWarnings("unchecked")
	public List<String> getKeysList(String cacheName) {
		List<String> result = new ArrayList<String>();
		List<Object> keys = manager.getCache(cacheName).getKeys();

		for (Object key : keys) {
			result.add(key.toString());
		}
		return result;
	}

	/**
	 * Zwraca statystyki o cache-ach.
	 * 
	 * @param maxRecords
	 *            maksymalna ilość wyników
	 * @return
	 */
	public List<_IObjectCacheStatistics> getAllStatistics(Integer maxRecords) {
		List<_IObjectCacheStatistics> result = null;

		if (maxRecords != null && maxRecords <= 0) {
			/*
			 * jeśli podano zakres ujemny bądź równy zero to zwracamy pustą
			 * listę
			 */
			result = new ArrayList<_IObjectCacheStatistics>();

		} else {

			String[] keys = manager.getCacheNames();
			String[] cachesNames = null;

			if (maxRecords == null) {
				/*
				 * jeśli nie podano ograniczenia na ilość regionów to zwracamy
				 * wszystkie
				 */
				cachesNames = keys;
			} else {
				/* podano dodatnie ograniczenie na ilość regionów */
				Integer from = 0;
				Integer to = Math.min(maxRecords, keys.length);
				cachesNames = Arrays.copyOfRange(keys, from, to);
			}

			/* obliczamy statystyki dla przedziału regionów */
			result = getAllStatistics(cachesNames);
		}
		return result;
	}

	/**
	 * Pobiera statystyki dla wybranych cache.
	 * 
	 * @param cacheNames
	 *            nazwy cach-y dla których chcemy pobrać statystyki
	 * @return
	 */
	public List<_IObjectCacheStatistics> getAllStatistics(String[] cacheNames) {
		List<_IObjectCacheStatistics> result = new ArrayList<_IObjectCacheStatistics>();
		if (cacheNames != null) {
			for (String key : cacheNames) {
				/* pobieram statystyki cache */
				_IObjectCacheStatistics row = getCache(key).getStatistics();
				result.add(row);
			}
		}
		return result;
	}

}
