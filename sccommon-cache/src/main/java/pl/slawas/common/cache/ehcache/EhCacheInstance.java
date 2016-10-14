package pl.slawas.common.cache.ehcache;

import java.io.Serializable;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import pl.slawas.common.cache._IObjectCacheProvider;

public interface EhCacheInstance extends _IObjectCacheProvider<Configuration>,
		Serializable {

	CacheManager getManager();
}