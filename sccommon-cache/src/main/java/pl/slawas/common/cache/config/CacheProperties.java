package pl.slawas.common.cache.config;

public class CacheProperties {

	/**
	 * Nazwa właściwości definiująca położenie pliku z konfiguracje cache'a
	 * (pamięci podręcznej).
	 */
	public static String CACHE_CONFIG_PATH = "cache.config.path";

	/**
	 * Nazwa właściwości definiująca nazwę używanego providera cache'a (pamięci
	 * podręcznej)
	 */
	public static String CACHE_PROVIDER = "cache.provider";

	/**
	 * Suffix dla właściwości określających czas życia elementów w cache'u
	 * (pamięci podręcznej)
	 */
	public static String CACHE_TIME_TO_LIVE_SUFFIX = "timeToLiveSeconds";

	/**
	 * Nazwa właściwości określająca domyślny czas życia elementów w cache
	 * (pamięci podręcznej)
	 */
	public static String CACHE_TIME_TO_LIVE = "cache."
			+ CACHE_TIME_TO_LIVE_SUFFIX;

	/**
	 * Nazwa parametru decydującego o tym, czy podczas używania cache'a (pamięci
	 * podręcznej), ma zostać użyty jeden domyślny region. Region ten nazywa się
	 * 'jdbc.ResultRowMapper' ({@link #CACHE_DEFAULT_REGION_NAME}) Parametr
	 * przyjmuje jedną z wartości: {@code true} albo {@code false}. Jeżeli jest
	 * ustawiony na {@code false}, albo nie jest zdefiniowany, to wtedy będą
	 * tworzone osobne regiony o nazwie klasy encji.
	 */
	public static String CACHE_USE_DEFAULT_REGION = "cache.useDefaultRegion";

	/**
	 * Domyślna nazwa regionu cache
	 */
	public static String CACHE_DEFAULT_REGION_NAME = "scbpm.Default";

}
