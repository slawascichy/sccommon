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

	/**
	 * 
	 * @param path2fileConfiguration
	 *            ścieżka do pliku z parametrami pamięci podręcznej
	 *            wykorzystywanymi do tworzenia dynamicznych regionów. zobacz
	 *            też {@link CacheConstants#PROP_DYNAMIC_PARAMETERS_FILE_PATH}
	 */
	public CacheConfigLoader(String path2fileConfiguration) {
		CacheConfig.getInstance(path2fileConfiguration);
	}

}
