package net.sf.ehcache.hibernate;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.hibernate.management.impl.ProviderMBeanRegistrationHelper;
import pl.slawas.common.cache.CacheProviderConfiguration;
import pl.slawas.common.cache.CacheProviderEnum;
import pl.slawas.common.cache.CacheProviderFactory;
import pl.slawas.common.cache.ObjectCacheStatisticsList;
import pl.slawas.common.cache._IObjectCache;
import pl.slawas.common.cache._IObjectCacheStatistics;
import pl.slawas.common.cache.ehcache.EhCacheConstants;
import pl.slawas.common.cache.ehcache.EhCacheInstance;
import pl.slawas.common.cache.ehcache.EhCacheProvider;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * 
 * ScHibernateEhCacheProvider - utworzenie instancji CacheManager-a.
 * 
 * Implementacja nadpisuje oryginalny provider EhCache z klasy
 * net.sf.ehcache.hibernate.EhCacheProvider.
 * 
 * <p>
 * <font style="color:red">Implementacja nie DZIAŁA i już nie będzie
 * działać!</font>
 * </p>
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.3 $
 * 
 */
@Deprecated
public class ScHibernateEhCacheProvider implements EhCacheInstance {

	private static final long serialVersionUID = -9043301647791008352L;

	private static final Logger logger = LoggerFactory
			.getLogger(ScHibernateEhCacheProvider.class);

	private static final String notInicjalizedMessage = "Wewnętrzny provider jest nie zainicjalizowany. Wcześniej użyj metod init(CacheProviderConfiguration, Properties) albo start(CacheProviderConfiguration, Properties)";

	private ProviderMBeanRegistrationHelper mbeanRegistrationHelper;
	private EhCacheInstance internalProvider;
	private Object initLock = new Object();

	static {
		logger.error("Implementation is deprecated! Don't use it!");
	}

	public boolean start(
			CacheProviderConfiguration<Configuration> configuration,
			Properties additionalProps) {
		return init(configuration, additionalProps);
	}

	@Override
	public boolean init(
			CacheProviderConfiguration<Configuration> configuration,
			Properties additionalProps) {
		synchronized (initLock) {
			if (internalProvider != null) {
				logger.warn("{} is already initialized. ",
						ScHibernateEhCacheProvider.class.getName());
				return true;
			}
			String managerName = configuration.getManagerName();
			/* nie każemy rejestrować, to zrobimy za chwilę */
			additionalProps.put(EhCacheConstants.PROP_CACHE_MANAGER_REGISTER,
					Boolean.toString(false));
			EhCacheProvider tempInstance = new EhCacheProvider();
			internalProvider = (EhCacheInstance) CacheProviderFactory
					.registerInstanceByManagerName(configuration, tempInstance,
							managerName);
			/* UWAGA! to może być ta sama instancja !!! */
			if (internalProvider == this) {
				tempInstance.init(configuration, false, additionalProps);
				internalProvider = tempInstance;
			}
			mbeanRegistrationHelper.registerMBean(
					internalProvider.getManager(), additionalProps);
			return (internalProvider != null);
		}
	}

	@Override
	public String[] getCacheNames() {
		logger.trace("-->getCacheNames()");
		if (internalProvider != null) {
			return internalProvider.getCacheNames();
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

	@Override
	public _IObjectCache getCache(String name) {
		logger.trace("-->getCache('{}')", name);
		if (internalProvider != null) {
			return internalProvider.getCache(name);
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

	@Override
	public void removeCache(String name) {
		logger.trace("-->removeCache('{}')", name);
		if (internalProvider != null) {
			internalProvider.removeCache(name);
		} else {
			throw new IllegalAccessError(notInicjalizedMessage);
		}
	}

	@Override
	public void close() {
		logger.trace("-->close()");
		synchronized (initLock) {
			if (mbeanRegistrationHelper != null) {
				mbeanRegistrationHelper.unregisterMBean();
			}
			if (internalProvider != null) {
				internalProvider.close();
				internalProvider = null;
			}
		}
	}

	@Override
	public ObjectCacheStatisticsList getAllStatistics(Integer offset,
			Integer pageSize) {
		logger.trace("-->getAllStatistics({}, {})", new Object[] { offset,
				pageSize });
		if (internalProvider != null) {
			return internalProvider.getAllStatistics(offset, pageSize);
		}
		throw new IllegalAccessError(notInicjalizedMessage);

	}

	@Override
	public void clearCache(String cacheName) {
		logger.trace("-->clearCache('{}')", cacheName);
		if (internalProvider != null) {
			internalProvider.clearCache(cacheName);
		} else {
			throw new IllegalAccessError(notInicjalizedMessage);
		}
	}

	@Override
	public List<String> getKeysList(String cacheName) {
		logger.trace("-->getKeysList('{}')", cacheName);
		if (internalProvider != null) {
			return internalProvider.getKeysList(cacheName);
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void clearStatistics(String cacheName) {
		logger.trace("-->clearStatistics('{}')", cacheName);
		if (internalProvider != null) {
			internalProvider.clearStatistics(cacheName);
		} else {
			throw new IllegalAccessError(notInicjalizedMessage);
		}
	}

	@Override
	public String getName() {
		logger.trace("-->getName()");
		if (internalProvider != null) {
			return internalProvider.getName();
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

	@Override
	public CacheProviderConfiguration<Configuration> initConfiguration(URL url) {
		EhCacheProvider tempInstance = new EhCacheProvider();
		return tempInstance.initConfiguration(url);
	}

	@Override
	public _IObjectCacheStatistics getStatistics(String regionName) {
		logger.trace("-->getName()");
		if (internalProvider != null) {
			return internalProvider.getStatistics(regionName);
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

	@Override
	public CacheProviderEnum getAssociatedProvider() {
		return CacheProviderEnum.EhCache;
	}

	@Override
	public URL getConfigurationFileURL() {
		logger.trace("-->getConfigurationFileURL()");
		if (internalProvider != null) {
			return internalProvider.getConfigurationFileURL();
		}
		EhCacheProvider tempInstance = new EhCacheProvider();
		return tempInstance.getConfigurationFileURL();
	}

	@Override
	public void setAdditionalProps(Properties additionalProps) {
		logger.trace("-->setAdditionalProps()");
		if (internalProvider != null) {
			internalProvider.setAdditionalProps(additionalProps);
		} else {
			throw new IllegalAccessError(notInicjalizedMessage);
		}

	}

	@Override
	public CacheManager getManager() {
		logger.trace("-->getManager()");
		if (internalProvider != null) {
			return internalProvider.getManager();
		}
		throw new IllegalAccessError(notInicjalizedMessage);
	}

}
