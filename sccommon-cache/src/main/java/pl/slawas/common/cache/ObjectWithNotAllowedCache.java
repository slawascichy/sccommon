package pl.slawas.common.cache;

import java.io.IOException;

public abstract class ObjectWithNotAllowedCache implements IObjectWithCache {

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
	public IObjectCache getCache() {
		return null;
	}

	@Override
	public void close() throws IOException {
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
