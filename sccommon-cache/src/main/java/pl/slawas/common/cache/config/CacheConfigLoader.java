package pl.slawas.common.cache.config;

/**
 * 
 * CacheConfigLoader obiekt pomocniczy zmieniający domyślną lokalizację
 * parametrów cache (pamięci podręcznej).
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class CacheConfigLoader {

	public CacheConfigLoader(String path2fileConfiguration) {
		CacheConfig.getInstance(path2fileConfiguration);
	}

}
