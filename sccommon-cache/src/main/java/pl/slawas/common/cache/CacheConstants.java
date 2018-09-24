package pl.slawas.common.cache;

/**
 * 
 * EhCacheConstants - klasa z nazwami właściwości czytanymi z pliku
 * konfiguracyjnego cache (pamięci podręcznej).
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class CacheConstants {

	private CacheConstants() {
	}

	/**
	 * Domyślna maksymalna liczba zwracanych rekordów.
	 */
	public static final int DEFAULT_MAX_RESULT_SIZE = 1000;
	/**
	 * Nazwa właściwości definiująca położenie pliku z konfiguracje cache'a (pamięci
	 * podręcznej).
	 */
	public static final String PROP_CONFIG_PATH = "cache.configPath";
	/**
	 * Parametr opisujący ścieżkę do pliku z parametrami pamięci podręcznych, które
	 * są wykorzystywane do tworzenia dynamicznych regionów pamięci podręcznych -
	 * plik *.properties
	 */
	public static final String PROP_DYNAMIC_PARAMETERS_FILE_PATH = "cache.dynamicParametersPath";

	/**
	 * Nazwa właściwości definiująca nazwę używanego providera cache'a (pamięci
	 * podręcznej)
	 */
	public static final String PROP_PROVIDER = "cache.provider";
	public static final String PROP_PROVIDER_NAME = "cache.provider.name";
	public static final String PROP_PROVIDER_IMPL = "cache.provider.implementation";
	public static final String PROP_USE_DEFAULT_REGION = "cache.defaultRegionUsage";
	public static final String PROP_CACHE_MANAGER_REGISTER = "cacheManager.register";
	public static final String PROP_DISABLE_STATISTICS = "cache.statistics.disable";
	public static final String DEFAULT_DISABLE_STATISTICS = "false";
	
	/** Czas życia */
	public static final String PROP_TIME_TO_LIVE = "cache.timeToLive";
	public static final int DEFAULT_TIME_TO_LIVE = 360000;
	public static final long DEFAULT_TIME_TO_LIVE_SECONDS = 300L;
	public static final String DEFAULT_STORE_EVICTION_POLICY = "LRU";

	/**
	 * Nazwa parametru decydującego o tym, czy podczas używania cache'a (pamięci
	 * podręcznej), ma zostać użyty jeden domyślny region. Region ten nazywa się
	 * 'scbpm.Default' ({@link #CACHE_DEFAULT_REGION_NAME}) Parametr przyjmuje jedną
	 * z wartości: {@code true} albo {@code false}. Jeżeli jest ustawiony na
	 * {@code false}, albo nie jest zdefiniowany, to wtedy będą tworzone osobne
	 * regiony o nazwie klasy encji.
	 */
	public static final String CACHE_USE_DEFAULT_REGION = "cache.useDefaultRegion";

	/**
	 * Domyślna nazwa regionu cache
	 */
	public static final String DEFAULT_REGION_NAME = "SCI_Default_Region";
	public static final String DEFAULT_PROVIDER_NAME = "SCI_Single";

}
