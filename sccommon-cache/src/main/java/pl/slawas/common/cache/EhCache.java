package pl.slawas.common.cache;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.cache.config.CacheConstants;
import pl.slawas.common.cache.exceptions.CacheErrorException;
import pl.slawas.helpers.Strings;

/**
 * 
 * EHCache - klasa kesza, wzorowana na keszu hibernate-a
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.3 $
 * 
 */
public class EhCache implements Serializable, _IObjectCache {

	private static final long serialVersionUID = 600977337387514385L;

	private static final Logger logger = LoggerFactory.getLogger(EhCache.class);

	private final Cache ehCache;

	public EhCache(net.sf.ehcache.Cache ehCache, Properties props) {
		this.ehCache = ehCache;

		/** Uzupełnienie konfiguracji */
		CacheConfiguration config = this.ehCache.getCacheConfiguration();
		String originalKey = this.ehCache.getName();
		String param = (String) props.get(originalKey + Strings.DOTChar
				+ CacheConstants.PROP_diskPersistent);
		if (StringUtils.isNotBlank(param)) {
			config.setDiskPersistent(Boolean.parseBoolean(param));
		}
		param = (String) props.get(originalKey + Strings.DOTChar
				+ CacheConstants.PROP_eternal);
		if (StringUtils.isNotBlank(param)) {
			config.setEternal(Boolean.parseBoolean(param));
		}
		param = (String) props.get(originalKey + Strings.DOTChar
				+ CacheConstants.PROP_maxElementsInMemory);
		if (StringUtils.isNotBlank(param)) {
			config.setMaxElementsInMemory(Integer.parseInt(param));
		}
		param = (String) props.get(originalKey + Strings.DOTChar
				+ CacheConstants.PROP_memoryStoreEvictionPolicy);
		if (StringUtils.isNotBlank(param)) {
			config.setMemoryStoreEvictionPolicy(param);
		}
		param = (String) props.get(originalKey + Strings.DOTChar
				+ CacheConstants.PROP_overflowToDisk);
		if (StringUtils.isNotBlank(param)) {
			config.setOverflowToDisk(Boolean.parseBoolean(param));
		}
	}

	public Object get(Object key) throws CacheErrorException {
		try {
			logger.trace("[{}] key: {}",
					new Object[] { ehCache.getName(), key });
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
			return ehCache.calculateInMemorySize();
		} catch (Throwable t) {
			return -1;
		}
	}

	public long getElementCountInMemory() throws CacheErrorException {
		try {
			return ehCache.getMemoryStoreSize();
		} catch (net.sf.ehcache.CacheException ce) {
			throw new CacheErrorException(ce);
		}
	}

	public long getElementCountOnDisk() {
		return ehCache.getDiskStoreSize();
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
		return new EhCacheStatistics(ehCache);
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
}
