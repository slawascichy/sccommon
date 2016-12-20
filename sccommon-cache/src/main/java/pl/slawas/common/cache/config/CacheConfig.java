package pl.slawas.common.cache.config;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.cache.ehcache.EhCacheConfig;
import pl.slawas.common.cache.ehcache.EhCacheConstants;
import pl.slawas.helpers.Configurations;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * 
 * CacheConfig - klasa zarządzająca parametrami uruchomienia cache (pamięci
 * podręcznej).
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
@Deprecated
public class CacheConfig {

	final protected static Logger logger = LoggerFactory
			.getLogger(CacheConfig.class);

	public static final String FILE_NAME = "ehcache.properties";

	public static final String FILE_DIRECTORY = "";

	private static Map<String, String> _Properties = null;

	static CacheConfig _Instance;

	private static Object instanceLock = new Object();
	private static Object propertyListLock = new Object();

	private boolean isInvalid = true;

	private Properties propertyList;

	/**
	 * Konstruktor domyślny z domyślną ścieżką położenia pliku konfiguracyjnego
	 * {@link #FILE_DIRECTORY}/{@link #FILE_NAME}
	 * 
	 */
	private CacheConfig() {
		loadPropertiesFromFile();
		propertyList = generatePropertyList();
	}

	/**
	 * Konstruktor ze wskazaniem innej lokalizacji położenia pliku
	 * konfiguracyjnego
	 * 
	 * @param configFileName
	 *            pełna ścieżka pliku konfiguracyjnego
	 */
	private CacheConfig(String configFileName) {
		loadPropertiesFromFile(configFileName);
		propertyList = generatePropertyList();
	}

	/**
	 * Metoda ładująca właściwości z pliku
	 * 
	 * @throws Exception
	 */
	private void loadPropertiesFromFile() {
		loadPropertiesFromFile(FILE_DIRECTORY + "/" + FILE_NAME);
	}

	/**
	 * Metoda ładująca właściwości z pliku
	 * 
	 * @throws Exception
	 */
	private void loadPropertiesFromFile(String configFileName) {
		synchronized (propertyListLock) {
			logger.debug("Loading configuration....");

			if (_Properties != null) {
				_Properties.clear();
			}
			_Properties = Configurations.loadHashtable(EhCacheConfig.class,
					configFileName);
		}

	}

	private Properties generatePropertyList() {
		synchronized (propertyListLock) {
			this.propertyList = new Properties();
			Set<Entry<String, String>> entrySet = _Properties.entrySet();
			for (Entry<String, String> entry : entrySet) {
				String key = entry.getKey();
				String value = entry.getValue();
				this.propertyList.put(key, value);
			}

			isInvalid = false;
			return this.propertyList;
		}
	}

	/**
	 * Pobranie instancji konfiguracji
	 * 
	 * @return instancja konfiguracji klienta
	 */
	public static CacheConfig getInstance() {
		synchronized (instanceLock) {
			if (_Instance == null) {
				logger.debug("Get new instance.");
				_Instance = new CacheConfig();
			}
			setSystemPropConfigPath(logger, _Instance);
			return _Instance;
		}
	}

	/**
	 * Pobranie instancji konfiguracji ze wskazaniem nowej lokalizacji pliku
	 * konfiguracyjnego
	 * 
	 * @param configFileName
	 *            pełna ścieżka pliku konfiguracyjnego
	 * @return instancja pliku konfiguracji
	 */
	public static CacheConfig getInstance(String configFileName) {
		synchronized (instanceLock) {
			if (_Instance == null) {
				logger.debug("Get new instance from {}.", configFileName);
				_Instance = new CacheConfig(configFileName);
			}
			setSystemPropConfigPath(logger, _Instance);
			return _Instance;
		}
	}

	public String get(String propertyCode) {
		return (String) _Properties.get(propertyCode);
	}

	public void put(String propertyCode, String value) {
		_Properties.put(propertyCode, value);
		isInvalid = true;
	}

	public Properties getPropertyList() {
		if (this.isInvalid || this.propertyList == null) {
			generatePropertyList();
		}
		return this.propertyList;
	}

	public static boolean statisticsIsDisabled() {
		String disableSatistics = CacheConfig.getInstance().get(
				EhCacheConstants.PROP_DISABLE_STATISTICS);
		if (StringUtils.isNotBlank(disableSatistics)) {
			return Boolean.parseBoolean(disableSatistics);
		}
		return false;
	}

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
