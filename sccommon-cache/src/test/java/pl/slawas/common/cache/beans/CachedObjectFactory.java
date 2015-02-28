package pl.slawas.common.cache.beans;

import java.util.Properties;

import pl.slawas.common.cache.CacheSharing;
import pl.slawas.common.cache.CacheUsage;
import pl.slawas.common.cache.ObjectWithCacheSupport;
import pl.slawas.common.cache.exceptions.CacheErrorException;
import pl.slawas.entities.NameValuePair;

public class CachedObjectFactory extends ObjectWithCacheSupport {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4795177687749610587L;
	public static final String cachedObjectRegionName = CachedObjectFactory.class
			.getSimpleName();

	protected CachedObjectFactory(CacheUsage cacheUsage, Properties props) {
		super(cacheUsage, cachedObjectRegionName, CacheSharing.NONE, false,
				props);
	}

	public static CachedObjectResult get(NameValuePair object,
			CacheUsage cacheUsage, Properties props) throws CacheErrorException {

		CachedObjectFactory obj = new CachedObjectFactory(cacheUsage, props);
		String key = object.getName();
		Object cachedObject = obj.getCache().get(key);
		if (cachedObject != null && cachedObject instanceof NameValuePair) {
			switch (cacheUsage) {
			case TO_REMOVE:
				obj.getCache().remove(key);
				return new CachedObjectResult(null, true);
			case REFRESH:
				obj.getCache().put(key, object);
				return new CachedObjectResult(object, false);
			default:
				return new CachedObjectResult((NameValuePair) cachedObject,
						true);
			}
		} else {
			obj.getCache().put(key, object);
			return new CachedObjectResult(object, false);
		}
	}

}
