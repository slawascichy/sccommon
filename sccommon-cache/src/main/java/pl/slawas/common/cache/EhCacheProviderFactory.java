package pl.slawas.common.cache;

import java.util.Properties;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.cache.config.CacheConstants;

/**
 * 
 * EhCacheProviderFactory obsługa pamięci podręczen opartej o EhCache
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class EhCacheProviderFactory {

	/**
	 * Obiekt instancji managera pamięci podręcznej.
	 */
	private static _IObjectCacheProvider instance;

	/**
	 * Pobieranie nowej instancji dostawcy obsługi pamięci podręcznej EhCache
	 * 
	 * @param props
	 * @return
	 */
	public static _IObjectCacheProvider getInstance(Properties props) {
		if (instance == null) {
			String customProviderImpl = (props
					.getProperty(CacheConstants.PROP_PROVIDER_IMPL) != null ? (String) props
					.getProperty(CacheConstants.PROP_PROVIDER_IMPL) : null);
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
		return instance;
	}

	/**
	 * Zamykanie/usuwanie instancji pamięci podręcznej.
	 */
	static void close() {
		instance = null;
	}
}
