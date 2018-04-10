package pl.slawas.common.cache.ehcache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import pl.slawas.common.cache._IObjectCache;
import pl.slawas.common.cache._IObjectCacheStatistics;
import pl.slawas.common.cache.exceptions.CacheErrorException;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

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
public class EhCache implements Serializable, _IObjectCache {

	private static final long serialVersionUID = 600977337387514385L;

	private static final Logger logger = LoggerFactory.getLogger(EhCache.class);

	private final Cache ehCache;
	private final String associatedManagerName;

	public EhCache(String associatedManagerName, Cache ehCache) {
		this.ehCache = ehCache;
		this.associatedManagerName = associatedManagerName;
	}

	public Object get(Object key) throws CacheErrorException {
		try {
			logger.trace("[{}] key: {}", new Object[] { ehCache.getName(), key });
			if (key == null) {
				return null;
			} else {
				Element element = ehCache.get(key);
				if (element == null) {
					return null;
				} else {
					return element.getObjectValue();
				}
			}
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheErrorException(e);
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
			Element element = new Element(key, value);
			ehCache.put(element);
		} catch (IllegalArgumentException e) {
			throw new CacheErrorException(e);
		} catch (IllegalStateException e) {
			throw new CacheErrorException(e);
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheErrorException(e);
		}

	}

	public void remove(Object key) throws CacheErrorException {
		try {
			ehCache.remove(key);
		} catch (ClassCastException e) {
			throw new CacheErrorException(e);
		} catch (IllegalStateException e) {
			throw new CacheErrorException(e);
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheErrorException(e);
		}
	}

	public void clear() throws CacheErrorException {
		try {
			ehCache.removeAll();
		} catch (IllegalStateException e) {
			throw new CacheErrorException(e);
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheErrorException(e);
		}
	}

	public void destroy() throws CacheErrorException {
		try {
			ehCache.getCacheManager().removeCache(ehCache.getName());
		} catch (IllegalStateException e) {
			throw new CacheErrorException(e);
		} catch (net.sf.ehcache.CacheException e) {
			throw new CacheErrorException(e);
		}
	}

	public String getRegionName() {
		return ehCache.getName();
	}

	public long getSizeInMemory() {
		try {
			return ehCache.getStatistics().getLocalHeapSizeInBytes();
		} catch (Exception e) {
			logger.warn("Nie udało się przeliczyć rozmiaru zajmowanej pamięci przez dany region.", e);
			return -1;
		}
	}

	public long getElementCountInMemory() throws CacheErrorException {
		try {
			return ehCache.getStatistics().getLocalHeapSize();
		} catch (net.sf.ehcache.CacheException ce) {
			throw new CacheErrorException(ce);
		}
	}

	public long getElementCountOnDisk() {
		return ehCache.getStatistics().getLocalDiskSize();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Map toMap() throws CacheErrorException {
		try {
			Map result = new HashMap();
			Iterator iter = ehCache.getKeys().iterator();
			while (iter.hasNext()) {
				Object key = iter.next();
				result.put(key, ehCache.get(key).getObjectValue());
			}
			return result;
		} catch (Exception e) {
			throw new CacheErrorException(e);
		}
	}

	public String toString() {
		return "EHCache(" + getRegionName() + ')';
	}

	public _IObjectCacheStatistics getStatistics() {
		return new EhCacheStatistics(this.associatedManagerName, ehCache);
	}

	public long getTimeToLiveSeconds() {
		return ehCache.getCacheConfiguration().getTimeToLiveSeconds();
	}

	public void setTimeToLiveSeconds(long timeToLiveSeconds) {
		CacheConfiguration config = this.ehCache.getCacheConfiguration();
		config.setTimeToLiveSeconds(timeToLiveSeconds);
		config.setTimeToIdleSeconds(timeToLiveSeconds / 2);
		config.setDiskExpiryThreadIntervalSeconds(timeToLiveSeconds);
	}

	public List<?> getKeys() {
		return this.ehCache.getKeysWithExpiryCheck();
	}

	/**
	 * @return the {@link #ehCache}
	 */
	public Ehcache getEhCache() {
		return ehCache;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void acquireReadLockOnKey(Object key) {
		this.ehCache.acquireReadLockOnKey(key);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void acquireWriteLockOnKey(Object key) {
		this.ehCache.acquireWriteLockOnKey(key);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean tryReadLockOnKey(Object key, long timeout) throws InterruptedException {
		return this.ehCache.tryReadLockOnKey(key, timeout);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean tryWriteLockOnKey(Object key, long timeout) throws InterruptedException {
		return this.ehCache.tryWriteLockOnKey(key, timeout);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void releaseReadLockOnKey(Object key) {
		this.ehCache.releaseReadLockOnKey(key);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void releaseWriteLockOnKey(Object key) {
		this.ehCache.releaseWriteLockOnKey(key);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isReadLockedByCurrentThread(Object key) {
		return this.ehCache.isReadLockedByCurrentThread(key);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isWriteLockedByCurrentThread(Object key) {
		return this.ehCache.isWriteLockedByCurrentThread(key);
	}
}
