package pl.slawas.common.cache.ehcache;

/**
 * 
 * EhCacheConfigLoader obiekt pomocniczy zmieniający domyślną lokalizację
 * parametrów cache (pamięci podręcznej).
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class EhCacheConfigLoader {

	/**
	 * 
	 * @param path2fileConfiguration
	 *            ścieżka do pliku z parametrami pamięci podręcznej
	 *            wykorzystywanymi do tworzenia dynamicznych regionów. zobacz
	 *            też {@link EhCacheConstants#PROP_DYNAMIC_PARAMETERS_FILE_PATH}
	 */
	public EhCacheConfigLoader(String path2fileConfiguration) {
		EhCacheConfig.getInstance(path2fileConfiguration);
	}

}
