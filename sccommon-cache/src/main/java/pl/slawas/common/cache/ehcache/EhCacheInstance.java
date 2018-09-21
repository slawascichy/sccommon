package pl.slawas.common.cache.ehcache;

import java.io.Serializable;

import org.ehcache.CacheManager;
import org.ehcache.xml.XmlConfiguration;

import pl.slawas.common.cache.IObjectCacheProvider;

public interface EhCacheInstance extends IObjectCacheProvider<XmlConfiguration>, Serializable {

	CacheManager getManager();
}