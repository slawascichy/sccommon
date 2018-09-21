package org.hibernate.cache.ehcache;

import java.io.Serializable;
import java.util.Properties;

import pl.slawas.common.cache.IObjectCacheProvider;

public interface ScCacheRegionFactoryInstance<C> extends IObjectCacheProvider<Properties>, Serializable {

	IObjectCacheProvider<C> getManager();

}
