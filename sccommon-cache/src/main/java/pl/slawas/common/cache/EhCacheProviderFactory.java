package pl.slawas.common.cache;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.cache.config.CacheConfig;
import pl.slawas.common.cache.config.CacheConstants;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * 
 * EhCacheProviderFactory obsługa pamięci podręczen opartej o EhCache
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class EhCacheProviderFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(EhCacheProviderFactory.class);

	/**
	 * Obiekt instancji managera pamięci podręcznej.
	 */
	private static _IObjectCacheProvider instance;

	private static Object initLock = new Object();

	/**
	 * Pobieranie nowej instancji dostawcy obsługi pamięci podręcznej EhCache
	 * 
	 * @param props
	 * @return
	 */
	public static _IObjectCacheProvider getInstance(Properties props) {
		synchronized (initLock) {
			if (instance == null) {
				String customProviderImpl = (props
						.getProperty(CacheConstants.PROP_PROVIDER_IMPL) != null ? (String) props
						.getProperty(CacheConstants.PROP_PROVIDER_IMPL) : null);
				logger.info(
						"\n***************\n* Incjalizuje nowego provider'a: {}\n***************",
						(StringUtils.isNotBlank(customProviderImpl) ? customProviderImpl
								: EhCacheProvider.class.getName()));
				if (StringUtils.isNotBlank(customProviderImpl)) {

					try {
						Class<?> impl = Class.forName(customProviderImpl);
						instance = (_IObjectCacheProvider) impl.newInstance();
						instance.init(props);
					} catch (ClassNotFoundException e) {
						throw new IllegalArgumentException(e);
					} catch (IllegalAccessException e) {
						throw new IllegalArgumentException(e);
					} catch (InstantiationException e) {
						throw new IllegalArgumentException(e);
					}
				} else {
					instance = new EhCacheProvider(props);
				}
			}
		}
		logger.trace(
				"\n***************\n* Pobrałem instancje provider'a: {}\n***************",
				instance.getClass().getName());
		return instance;
	}

	/**
	 * Zamykanie/usuwanie instancji pamięci podręcznej.
	 */
	static void close() {
		instance = null;
	}

	/**
	 * Ustawianie parametru systemowego o położeniu statycznej konfiguracji
	 * pamięci podręcznej
	 * 
	 * @param logger
	 *            obiekt logger'a do którego przekazany będzie ewentualne
	 *            ostrzeżenie o tym, że parametr lokalny został pominięty.
	 * @param cc
	 *            lokalna konfiguracja (parametry) pamięci podręcznej.
	 */
	public static void setSystemPropConfigPath(Logger logger, CacheConfig cc) {
		/*
		 * Pierwszy załadowany system ma pierwszeństwo - pozwoli nam to na
		 * sterowanie jaki parametr ma być użyty.
		 */
		if (StringUtils.isBlank(System
				.getProperty(CacheConstants.PROP_CONFIG_PATH))) {
			if (StringUtils.isNotBlank(cc.get(CacheConstants.PROP_CONFIG_PATH))) {
				System.setProperty(CacheConstants.PROP_CONFIG_PATH,
						cc.get(CacheConstants.PROP_CONFIG_PATH));
			}
		} else if (StringUtils.isNotBlank(cc
				.get(CacheConstants.PROP_CONFIG_PATH))) {
			logger.warn(
					"Używam parametru systemowego: {} = '{}'. Pomijam lokalne ustawienia '{}'.",
					new Object[] {
							CacheConstants.PROP_CONFIG_PATH,
							System.getProperty(CacheConstants.PROP_CONFIG_PATH),
							cc.get(CacheConstants.PROP_CONFIG_PATH) });
		}
	}
}
