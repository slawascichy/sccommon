package org.hibernate.cache.ehcache;

import java.util.Properties;

import org.ehcache.xml.XmlConfiguration;
import org.hibernate.cache.spi.CacheKeysFactory;

import pl.slawas.common.cache.CacheConstants;
import pl.slawas.common.cache.CacheProviderConfiguration;
import pl.slawas.common.cache.CacheProviderEnum;
import pl.slawas.common.cache.ehcache.EhCacheProvider;

public class ScEhCacheRegionFactory extends ScCacheRegionFactorySupport<XmlConfiguration>
		implements ScCacheRegionFactoryInstance<XmlConfiguration> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2610741223331309861L;

	public ScEhCacheRegionFactory() {
		super();
	}

	public ScEhCacheRegionFactory(CacheKeysFactory cacheKeysFactory) {
		super(cacheKeysFactory);
	}

	@Override
	public CacheProviderEnum getAssociatedProvider() {
		return CacheProviderEnum.ScEhHibernate;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public CacheProviderConfiguration<Properties> initConfiguration(Properties additionalProps) {
		additionalProps.put(CacheConstants.PROP_PROVIDER, CacheProviderEnum.EhCache.name());
		additionalProps.put(CacheConstants.PROP_PROVIDER_IMPL, EhCacheProvider.class.getName());
		return super.initConfiguration(additionalProps);
	}

}
