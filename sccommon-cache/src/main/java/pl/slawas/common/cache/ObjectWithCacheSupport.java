package pl.slawas.common.cache;

import java.io.Serializable;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.cache.config.CacheConstants;
import pl.slawas.common.cache.exceptions.CacheErrorException;
import pl.slawas.helpers.Strings;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * 
 * ObjectWithCacheSupport - abstrakcja obiektów wykorzystujących pamięć
 * podręczną.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.2 $
 * 
 */
@SuppressWarnings("serial")
public abstract class ObjectWithCacheSupport implements Serializable,
		_IObjectWithCache {

	final static private Logger logger = LoggerFactory
			.getLogger(ObjectWithCacheSupport.class);

	/** nazwa regionu używanego cache */
	private final String cacheRegionName;

	private final transient _IObjectCache cache;

	private final CacheSharing cacheSharing;
	/** obiekt dostawcy pamięci podręcznej */
	private final CacheProviderEnum provider;

	protected CacheUsage cacheUsage;

	private long cacheTimeToLive = CacheConstants.DEFAULT_timeToLive;

	/**
	 * @param cacheUsage
	 *            informacja o tym czy ma być używana pamięć podręczna. Jako, że
	 *            jest to inicjalizacja obiektu, to parametr powinien przyjmować
	 *            jedną z dwóch wartości: {@link CacheUsage#TO_USE} oraz
	 *            {@link CacheUsage#NONE}
	 * @param cacheRegionName
	 *            nazwa regionu pamięci podręcznej
	 * @param cacheSharing
	 *            informacja o współdzielenia cache'a ({@link CacheSharing}.
	 * @param useDefaultRegion
	 *            informacja o tym czy ma być użyty domyślny region. Jeżeli
	 *            podamy ten parametr jako wartość {@code null}, to wtedy
	 *            informacja ta zostanie pobrana z wartości
	 *            {@link CacheProviderEnum#CACHE_USE_DEFAULT_REGION}.
	 * @param props
	 *            parametry pamięci podręcznej.
	 */
	protected ObjectWithCacheSupport(CacheUsage cacheUsage,
			String cacheRegionName, CacheSharing cacheSharing,
			Boolean useDefaultRegion, Properties props) {

		/** obiekt tworzonego cache'a */
		_IObjectCache preparedCache = null;
		/** nazwa regionu tworzonego cache'a */
		String preparedCacheRegionName = null;
		this.cacheUsage = cacheUsage;
		CacheProviderEnum lPprovider = null;
		if (cacheUsage != null && !cacheUsage.equals(CacheUsage.NONE)) {
			String pProvider = null;
			try {
				/* ustawienie parametrów cache'a */
				pProvider = (String) props.get(CacheConstants.PROP_PROVIDER);
				if (StringUtils.isNotBlank(pProvider)) {
					lPprovider = CacheProviderEnum.valueOf(pProvider);
				} else {
					lPprovider = CacheProviderEnum.none;
				}
				if (lPprovider.isAllowed()) {

					_IObjectCacheProvider cacheManager = lPprovider
							.getProvider(props);
					logger.trace(
							"[ {} ] Zdefiniowano uzycie cache'a '{}' (impl: {}).",
							new Object[] { getClass().getSimpleName(),
									lPprovider.toString(),
									cacheManager.getClass().getName() });

					/* Rozwiązywanie nazwy regionu pamięci podręcznej */
					preparedCacheRegionName = CacheConstants.DEFAULT_REGION_NAME;
					boolean customUseDefaultRegion = true;
					if (props.get(CacheConstants.PROP_USE_DEFAULT_REGION) != null) {
						customUseDefaultRegion = Boolean
								.parseBoolean((String) props
										.get(CacheConstants.PROP_USE_DEFAULT_REGION));
					}
					customUseDefaultRegion = (useDefaultRegion == null ? customUseDefaultRegion
							: useDefaultRegion);
					if (customUseDefaultRegion) {
						logger.trace(
								"[ {} ] Zdefiniowano uzycie domyslnego regionu '{}'.",
								new Object[] { getClass().getSimpleName(),
										CacheConstants.DEFAULT_REGION_NAME });
					} else {
						preparedCacheRegionName = cacheRegionName;
						logger.trace(
								"[ {} ] Zdefiniowano uzycie regionu '{}'.",
								new Object[] { getClass().getSimpleName(),
										preparedCacheRegionName });
					}

					/* Utworzenie obiektu pamięci podręcznej */
					preparedCache = cacheManager
							.getCache(preparedCacheRegionName);
					if (customUseDefaultRegion) {
						/** Wczytuje domyślne parametry pamięci podręcznej */
						preparedCache.setTimeToLiveSeconds(cacheTimeToLive);
					} else {
						String originalKey = preparedCacheRegionName;
						String param = (String) props.get(originalKey
								+ Strings.DOTChar
								+ CacheConstants.PROP_timeToLive);
						if (StringUtils.isNotBlank(param)) {
							preparedCache.setTimeToLiveSeconds(Integer
									.parseInt(param));
						} else {
							preparedCache.setTimeToLiveSeconds(cacheTimeToLive);
						}
					}
				} else {
					if (logger.isTraceEnabled()) {
						logger.warn("[ {} ] Nie zdefiniowano uzycia kesz-a.",
								new Object[] { getClass().getSimpleName() });
					}
					preparedCache = null;
				}
			} catch (IllegalArgumentException e) {
				logger.error("[ {} ] Zla nazwa provider'a cache'a: {}.",
						new Object[] { getClass().getSimpleName(), pProvider });
				logger.warn("[ {} ] Definicja uzycia cache'a pominięta.",
						new Object[] { getClass().getSimpleName() });
				preparedCache = null;
			}
		}
		this.cache = preparedCache;
		this.cacheRegionName = preparedCacheRegionName;
		this.cacheSharing = cacheSharing;
		this.provider = lPprovider;
	}

	public boolean cacheIsAllowed() {
		return (this.cache != null && !cacheUsage.equals(CacheUsage.NONE));
	}

	public _IObjectCache getCache() {
		return this.cache;
	}

	public String getCacheRegionName() {
		return this.cacheRegionName;
	}

	public long getCacheTimeToLive() {
		return this.cacheTimeToLive;
	}

	public void setCacheTimeToLive(long cacheTimeToLive) {
		if (this.cacheIsAllowed()) {
			logger.debug(
					"[{}] Ustawiam niedomyśny cacheTimeToLive: {}",
					new Object[] { getClass().getSimpleName(), cacheTimeToLive });
			this.cacheTimeToLive = cacheTimeToLive;
			this.cache.setTimeToLiveSeconds(cacheTimeToLive);
		}

	}

	public boolean cacheIsShared() {
		return this.cacheSharing.isShared();
	}

	public void close() {
		if (cacheIsAllowed() && !cacheIsShared()) {
			try {
				this.cache.destroy();
				logger.trace("Zniszczylem cache {}", this.cache.getRegionName());
			} catch (CacheErrorException ignore) {
				logger.warn("Ooops... nie udalo sie niszczenie cache'a '"
						+ this.cache.getRegionName() + "'", ignore);
			}
		}

	}

	/**
	 * @return the {@link #cacheUsage}
	 */
	public CacheUsage getCacheUsage() {
		return cacheUsage;
	}

	/**
	 * @param cacheUsage
	 *            the {@link #cacheUsage} to set
	 */
	public void setCacheUsage(CacheUsage cacheUsage) {
		this.cacheUsage = cacheUsage;
	}

	@Override
	public CacheProviderEnum getCacheProvider() {
		return this.provider;
	}

}
