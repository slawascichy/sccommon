package pl.slawas.common.cache.config;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;

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
public class CacheConfig {

	final protected static Logger logger = LoggerFactory
			.getLogger(CacheConfig.class);

	public static final String FILE_NAME = "cache.properties";

	public static final String FILE_DIRECTORY = "";

	private static Hashtable<String, String> _Properties = null;

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
			_Properties = Configurations.loadHashtable(CacheConfig.class,
					configFileName);
		}

	}

	private Properties generatePropertyList() {
		synchronized (propertyListLock) {
			this.propertyList = new Properties();
			Enumeration<String> props = _Properties.keys();
			while (props.hasMoreElements()) {
				String key = props.nextElement();
				String value = _Properties.get(key);
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
				logger.debug("Get new instance.");
				_Instance = new CacheConfig(configFileName);
			}
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
				CacheConstants.PROP_DISABLE_STATISTICS);
		if (StringUtils.isNotBlank(disableSatistics)) {
			return Boolean.parseBoolean(disableSatistics);
		}
		return false;
	}

}
