package pl.slawas.common.cache;

import pl.slawas.common.cache.CacheProviderEnum;
import pl.slawas.common.cache._IObjectCache;
import pl.slawas.common.cache._IObjectWithCache;

public abstract class ObjectWithNotAllowedCache implements _IObjectWithCache {

	@Override
	public void setCacheTimeToLive(long arg0) {
		/* nie implementujemy! */
	}

	@Override
	public long getCacheTimeToLive() {
		return 0;
	}

	@Override
	public String getCacheRegionName() {
		return CacheProviderEnum.none.name();
	}

	@Override
	public CacheProviderEnum getCacheProvider() {
		return CacheProviderEnum.none;
	}

	@Override
	public _IObjectCache getCache() {
		return null;
	}

	@Override
	public void close() {
		/* nie implementujemy! */
	}

	@Override
	public boolean cacheIsShared() {
		return false;
	}

	@Override
	public boolean cacheIsAllowed() {
		return false;
	}

}
