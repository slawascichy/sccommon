package pl.slawas.common.cache;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.cache.config.CacheConfig;
import pl.slawas.common.cache.ehcache.EhCacheConstants;
import pl.slawas.twl4j.Logger;

@Deprecated
public class EhCacheProviderFactory {

	/**
	 * Ustawianie parametru systemowego
	 * {@link EhCacheConstants#PROP_CONFIG_PATH} o położeniu statycznej
	 * konfiguracji pamięci podręcznej.
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
				.getProperty(EhCacheConstants.PROP_CONFIG_PATH))) {
			/*
			 * parametr systemowy nie jest ustawiony, próbuję przeczytać z
			 * konfiguracji...
			 */
			if (StringUtils.isNotBlank(cc
					.get(EhCacheConstants.PROP_CONFIG_PATH))) {
				/*
				 * Konfiguracja ma ustawiony parametr, zatem ustawiam go jako
				 * parametr systemowy
				 */
				System.setProperty(EhCacheConstants.PROP_CONFIG_PATH,
						cc.get(EhCacheConstants.PROP_CONFIG_PATH));
			}
		} else if (StringUtils.isNotBlank(cc
				.get(EhCacheConstants.PROP_CONFIG_PATH))) {
			logger.warn(
					"Używam parametru systemowego: {} = '{}'. Pomijam lokalne ustawienia '{}'.",
					new Object[] {
							EhCacheConstants.PROP_CONFIG_PATH,
							System.getProperty(EhCacheConstants.PROP_CONFIG_PATH),
							cc.get(EhCacheConstants.PROP_CONFIG_PATH) });
		}
	}

}
