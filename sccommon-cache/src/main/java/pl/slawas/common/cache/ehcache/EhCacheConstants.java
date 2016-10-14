package pl.slawas.common.cache.ehcache;

/**
 * 
 * EhCacheConstants - klasa z nazwami właściwości czytanymi z pliku
 * konfiguracyjnego cache (pamięci podręcznej).
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface EhCacheConstants {

	/**
	 * Domyślna maksymalna liczba zwracanych rekordów.
	 */
	int DEFAULT_MAX_RESULT_SIZE = 1000;
	/**
	 * Nazwa właściwości definiująca położenie pliku z konfiguracje cache'a
	 * (pamięci podręcznej).
	 */
	String PROP_CONFIG_PATH = "cache.configPath";
	/**
	 * Parametr opisujący ścieżkę do pliku z parametrami pamięci podręcznych,
	 * które są wykorzystywane do tworzenia dynamicznych regionów pamięci
	 * podręcznych - plik *.properties
	 */
	String PROP_DYNAMIC_PARAMETERS_FILE_PATH = "cache.dynamicParametersPath";

	/**
	 * Nazwa właściwości definiująca nazwę używanego providera cache'a (pamięci
	 * podręcznej)
	 */
	String PROP_PROVIDER = "cache.provider";
	String PROP_PROVIDER_IMPL = "cache.provider.implementation";
	String PROP_USE_DEFAULT_REGION = "cache.defaultRegionUsage";
	String PROP_CACHE_MANAGER_REGISTER = "cacheManager.register";
	String PROP_DISABLE_STATISTICS = "cache.statistics.disable";

	/** Czas życia */
	String PROP_timeToLive = "cache.timeToLive";
	int DEFAULT_timeToLive = 360000;
	long DEFAULT_TIME_TO_LIVE_SECONDS = 300L;
	String DEFAULT_STORE_EVICTION_POLICY = "LRU";

	/**
	 * Nazwa parametru decydującego o tym, czy podczas używania cache'a (pamięci
	 * podręcznej), ma zostać użyty jeden domyślny region. Region ten nazywa się
	 * 'scbpm.Default' ({@link #CACHE_DEFAULT_REGION_NAME}) Parametr przyjmuje
	 * jedną z wartości: {@code true} albo {@code false}. Jeżeli jest ustawiony
	 * na {@code false}, albo nie jest zdefiniowany, to wtedy będą tworzone
	 * osobne regiony o nazwie klasy encji.
	 */
	public static String CACHE_USE_DEFAULT_REGION = "cache.useDefaultRegion";

	/**
	 * Domyślna nazwa regionu cache
	 */
	public static String DEFAULT_REGION_NAME = "scbpm.Default";

	String PROP_memoryStoreEvictionPolicy = "cache.memoryStoreEvictionPolicy";
	int DEFAULT_maxElementsInMemory = 10000;
	String PROP_maxElementsInMemory = "cache.maxEntriesLocalHeap";
	String PROP_eternal = "cache.eternal";
	String PROP_strategy = "cache.strategy";
	String PROP_strategy_sync = "cache.strategy.synchronousWrites";
	String PROP_timeToIdleSeconds = "cache.timeToIdleSeconds";
	String PROP_timeToLiveSeconds = "cache.timeToLiveSeconds";

}
