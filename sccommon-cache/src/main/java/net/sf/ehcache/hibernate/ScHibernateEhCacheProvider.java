package net.sf.ehcache.hibernate;

import java.util.Properties;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.hibernate.management.impl.ProviderMBeanRegistrationHelper;
import net.sf.ehcache.util.Timestamper;

import org.hibernate.cache.CacheProvider;

import pl.slawas.common.cache.EhCacheProviderFactory;
import pl.slawas.common.cache._IObjectCache;
import pl.slawas.common.cache.config.CacheConfig;
import pl.slawas.common.cache.config.CacheConstants;

/**
 * 
 * ScHibernateEhCacheProvider - utworzenie instancji CacheManager-a.
 * 
 * Implementacja nadpisuje oryginalny provider EhCache z klasy
 * net.sf.ehcache.hibernate.EhCacheProvider.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.3 $
 * 
 */
public class ScHibernateEhCacheProvider implements CacheProvider {

	private ProviderMBeanRegistrationHelper mbeanRegistrationHelper;
	private pl.slawas.common.cache.EhCacheProvider provider;

	public ScHibernateEhCacheProvider() {
		super();
	}

	/* Overridden (non-Javadoc) */
	/**
	 * Uwaga! @param paramProperties to aktualne parametry pobierane z systemu
	 * oraz ustawień JPA (plik persistence.xml). <b>Niestety parametry JPA mają
	 * pierwszeństwo nad systemowymi</b>, więc lepiej nie ustawiać:
	 * 
	 * <pre>
	 * <property name="cache.configPath" value="/pro/ibpm/mercury/cache/ehcache.xml" />
	 * </pre>
	 * 
	 * TODO Zdefiniowanego parametru JPA nie nadpiszemy w żaden sposób, chyba,
	 * ze jakoś dynamicznie zawładniemy nad tymi parametrami.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public org.hibernate.cache.Cache buildCache(String name,
			Properties paramProperties)
			throws org.hibernate.cache.CacheException {
		try {
			_IObjectCache cache = provider.getCache(name);
			Ehcache netEhcache = ((pl.slawas.common.cache.EhCache) cache)
					.getEhCache();
			netEhcache
					.setStatisticsEnabled(!CacheConfig.statisticsIsDisabled());
			HibernateUtil.validateEhcache(netEhcache);
			return new EhCache(netEhcache);
		} catch (net.sf.ehcache.CacheException e) {
			throw new org.hibernate.cache.CacheException(e);
		}
	}

	/* Overridden (non-Javadoc) */
	@Override
	public long nextTimestamp() {
		return Timestamper.next();
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isMinimalPutsEnabledByDefault() {
		return true;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void start(Properties paramProperties)
			throws org.hibernate.cache.CacheException {

		paramProperties.put(CacheConstants.PROP_PROVIDER_IMPL,
				pl.slawas.common.cache.EhCacheProvider.class.getName());
		this.mbeanRegistrationHelper = new ProviderMBeanRegistrationHelper();
		this.provider = (pl.slawas.common.cache.EhCacheProvider) EhCacheProviderFactory
				.getInstance(paramProperties);
		this.mbeanRegistrationHelper.registerMBean(provider.getManager(),
				paramProperties);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void stop() {
		this.mbeanRegistrationHelper = null;
		this.provider = null;
	}

}
