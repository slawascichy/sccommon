package pl.slawas.common.cache;

import java.util.Properties;

public enum CacheProviderEnum {

	EhCache, none;

	public _IObjectCacheProvider getProvider(Properties props) {
		switch (this) {
		case EhCache:
			return EhCacheProvider.getInstance(props);
		case none:
			return null;
		default:
			throw new UnsupportedOperationException();
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
