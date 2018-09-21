package pl.slawas.common.cache;

import org.hibernate.cache.ehcache.ScCacheRegionFactoryInstance;

import pl.slawas.common.cache.ehcache.EhCacheInstance;

public enum CacheProviderEnum {

	EhCache, none, ScEhHibernate;

	public Class<?> getCacheProviderInterfaceClass() {
		switch (this) {
		case EhCache:
			return EhCacheInstance.class;
		case ScEhHibernate:
			return ScCacheRegionFactoryInstance.class;
		case none:
		default:
			return null;
		}
	}

	public boolean isAllowed() {
		switch (this) {
		case EhCache:
			return true;
		case ScEhHibernate:
			return true;
		case none:
		default:
			return false;
		}
	}

}
