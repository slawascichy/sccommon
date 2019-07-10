package pl.slawas.common.cache.ehcache;

import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.ehcache.Cache;
import org.ehcache.Cache.Entry;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.core.spi.service.StatisticsService;
import org.ehcache.expiry.ExpiryPolicy;

import pl.slawas.common.cache.IObjectCache;
import pl.slawas.common.cache.IObjectCacheStatistics;
import pl.slawas.common.cache.exceptions.CacheErrorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * EHCache - klasa kesza, wzorowana na keszu hibernate-a
 * 
 * http://www.ehcache.org/documentation/2.7/apis/explicitlocking.html
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.3 $
 * 
 */
public class EhCache implements Serializable, IObjectCache {

	private static final long serialVersionUID = 600977337387514385L;

	private static final Logger logger = LoggerFactory.getLogger(EhCache.class);

	private final transient CacheManager cacheManager;
	private final transient Cache<String, Element> localCache;
	private final transient ExpiryPolicy<String, Element> expiryPolicy;
	private final transient StatisticsService statisticsService;
	private final String associatedCacheName;
	private final String associatedProviderName;

	@SuppressWarnings("unchecked")
	public EhCache(String associatedProviderName, String associatedCacheName, final CacheManager cacheManager,
			final StatisticsService statisticsService, CacheConfiguration<String, Element> cacheConfig,
			boolean isCreated) {
		this.associatedProviderName = associatedProviderName;
		this.associatedCacheName = associatedCacheName;
		this.cacheManager = cacheManager;
		this.statisticsService = statisticsService;
		if (!isCreated) {
			this.localCache = cacheManager.createCache(associatedCacheName, cacheConfig);
		} else {
			this.localCache = cacheManager.getCache(associatedCacheName, String.class, Element.class);
		}
		this.expiryPolicy = (ExpiryPolicy<String, Element>) cacheConfig.getExpiryPolicy();
	}

	public Object get(Object key) throws CacheErrorException {
		try {
			if (key == null) {
				return null;
			} else {
				final Element element = this.localCache.get(key2String(key));
				if (element == null) {
					return null;
				} else {
					return element.getObjectValue();
				}
			}
		} catch (Exception e) {
			throw new CacheErrorException(
					String.format("-->get: Error for key type: %s and value: %s", key.getClass(), key), e);
		}
	}

	public Object read(Object key) throws CacheErrorException {
		return get(key);
	}

	public void update(Object key, Object value) throws CacheErrorException {
		put(key, value);
	}

	public void put(Object key, Object value) throws CacheErrorException {
		try {
			localCache.put(key2String(key), new Element((Serializable) value));
		} catch (Exception e) {
			throw new CacheErrorException(
					String.format("-->put: Error for key type: %s and value: %s", key.getClass(), key), e);
		}

	}

	public void remove(Object key) throws CacheErrorException {
		try {
			localCache.remove(key2String(key));
		} catch (Exception e) {
			throw new CacheErrorException(
					String.format("-->remove: Error for key type: %s and value: %s", key.getClass(), key), e);
		}
	}

	public void clear() throws CacheErrorException {
		try {
			localCache.clear();
		} catch (Exception e) {
			throw new CacheErrorException(e);
		}
	}

	public void destroy() throws CacheErrorException {
		try {
			this.cacheManager.removeCache(associatedCacheName);
		} catch (IllegalStateException e) {
			/*
			 * When Spring and Hibernate are both involved this will happen in normal
			 * shutdown operation. Do not throw an exception, simply log this one.
			 */
			logger.debug("This can happen if multiple frameworks both try to shutdown ehcache", e);
		} catch (Exception e) {
			throw new CacheErrorException(e);

		}
	}

	public String getRegionName() {
		return associatedCacheName;
	}

	public long getSizeInMemory() {
		/* brak implementacji */
		if (logger.isDebugEnabled()) {
			logger.warn("-->getSizeInMemory: implementation lack.");
		}
		return -1L;
	}

	public long getElementCountInMemory() throws CacheErrorException {
		/* brak implementacji */
		if (logger.isDebugEnabled()) {
			logger.warn("-->getElementCountInMemory: implementation lack.");
		}
		return -1L;
	}

	public long getElementCountOnDisk() {
		/* brak implementacji */
		if (logger.isDebugEnabled()) {
			logger.warn("-->getElementCountOnDisk: implementation lack.");
		}
		return -1L;
	}

	public Map<String, Object> toMap() throws CacheErrorException {
		try {
			Map<String, Object> result = new HashMap<>();
			Iterator<Entry<String, Element>> iter = localCache.iterator();
			while (iter.hasNext()) {
				Entry<String, Element> entry = iter.next();
				Serializable value = entry.getValue().getObjectValue();
				result.put(entry.getKey(), value);
			}
			return result;
		} catch (Exception e) {
			throw new CacheErrorException(e);
		}
	}

	public String toString() {
		return "EHCache(" + getRegionName() + ')';
	}

	public IObjectCacheStatistics getStatistics() {
		return new EhCacheStatistics(this.associatedProviderName, this);
	}

	public long getTimeToLiveSeconds() {
		if (this.expiryPolicy instanceof CustomExpiry) {
			return ((CustomExpiry<String, Element>) this.expiryPolicy).getTimeToLiveExpiration().getSeconds();
		}
		return -1L;
	}

	public void setTimeToLiveSeconds(long timeToLiveSeconds) {
		if (this.expiryPolicy instanceof CustomExpiry) {
			((CustomExpiry<String, Element>) this.expiryPolicy)
					.setTimeToLiveExpiration(Duration.ofSeconds(timeToLiveSeconds));
		}
	}

	/**
	 * @return the {@link #localCache}
	 */
	public Cache<String, Element> getEhCache() {
		return localCache;
	}

	/**
	 * @return the {@link #statisticsService}
	 */
	public StatisticsService getStatisticsService() {
		return statisticsService;
	}

	@Override
	public boolean contains(Object key) {
		return localCache.containsKey(key2String(key));
	}

	private String key2String(Object key) {
		if (key instanceof String) {
			return (String) key;
		}
		return key.toString();
	}

}
