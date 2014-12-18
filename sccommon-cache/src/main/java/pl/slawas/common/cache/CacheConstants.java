package pl.slawas.common.cache;

/**
 * 
 * CacheConfigConst - klasa z nazwami właściwości czytanymi z pliku
 * konfiguracyjnego cache (pamięci podręcznej).
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface CacheConstants {

	/**
	 * Nazwa właściwości definiująca położenie pliku z konfiguracje cache'a
	 * (pamięci podręcznej).
	 */
	String PROP_CONFIG_PATH = "cache.configPath";

	/**
	 * Nazwa właściwości definiująca nazwę używanego providera cache'a (pamięci
	 * podręcznej)
	 */
	String PROP_PROVIDER = "cache.provider";
	String PROP_PROVIDER_IMPL = "cache.provider.implementation";
	String PROP_USE_DEFAULT_REGION = "cache.defaultRegionUsage";

	/** Czas życia */
	String PROP_timeToLive = "cache.timeToLive";
	int DEFAULT_timeToLive = 360000;

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

	String PROP_diskPersistent = "cache.diskPersistent";
	String PROP_eternal = "cache.eternal";
	String PROP_maxElementsInMemory = "cache.maxElementsInMemory";
	String PROP_memoryStoreEvictionPolicy = "cache.memoryStoreEvictionPolicy";
	String PROP_overflowToDisk = "cache.overflowToDisk";

}
