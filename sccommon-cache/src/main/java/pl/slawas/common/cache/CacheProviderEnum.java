package pl.slawas.common.cache;

import pl.slawas.common.cache.ehcache.EhCacheInstance;

public enum CacheProviderEnum {

	EhCache, none;

	public Class<?> getCacheProviderInterfaceClass() {
		switch (this) {
		case EhCache:
			return EhCacheInstance.class;
		case none:
		default:
			return null;
		}
	}

	public boolean isAllowed() {
		switch (this) {
		case EhCache:
			return true;
		case none:
		default:
			return false;
		}
	}

}
