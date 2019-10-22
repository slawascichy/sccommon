package pl.slawas.common.ldap.provider;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.management.RuntimeErrorException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.lf5.LogLevel;
import org.apache.log4j.lf5.LogLevelFormatException;
import org.eclipse.wst.common.internal.emf.utilities.EncoderDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.cache.ehcache.EhCacheConfig;
import pl.slawas.common.ldap.api.Constants;
import pl.slawas.common.ldap.config.LdapConfig;
import pl.slawas.common.ldap.config.LdapConfigOptions;
import pl.slawas.common.ldap.config.LoadLdapConfiguration;
import pl.slawas.common.ldap.provider.beans.UserAttributeDefinition;
import pl.slawas.common.ldap.provider.beans.UserAttributeList;
import pl.slawas.common.ldap.provider.exceptions.LdapProviderException;
import pl.slawas.common.ldap.provider.exceptions.ProviderOptionsNotInicjalized;
import pl.slawas.common.ldap.provider.helpers.ProviderOptionsHelper;
import pl.slawas.common.ldap.utils.UserAttributeUtils;
import pl.slawas.helpers.Configurations;
import pl.slawas.helpers.Strings;
import pl.slawas.security.PasswordEncoder;

/**
 * 
 * ProviderOptions - opcje provider'a realizującego synchronizację danych
 * użytkowników i grup pomiędzy LDAP a IBM BPM. Obiekt jest singleton'em i
 * pobiera się jego wartości za pomocą metod statycznych.
 * <p>
 * Zasada budowania filtrów:
 * </p>
 * <ul>
 * <li>{0} - oznacza nazwę entry (wartość odpowiedniego atrybutu użytkownika
 * albo grupy definiującego nazwę).</li>
 * <li>{1} - oznacza unikalny identyfikator lokalizacji entry w drzewie LDAP
 * ('distinguishedName' użytkownika albo grupy)</li>
 * </ul>
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class ProviderOptions extends LdapConfigOptions implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3804155547293539009L;

	private static final Logger log = LoggerFactory.getLogger(ProviderOptions.class.getName());

	/** pełna ścieżka do pliku konfiguracyjnego */
	public static final String configFilePath;
	/**
	 * przełącznik ustawiany podczas inicjalizacji modułu. Jeżeli nie jest ustawiona
	 * ścieżka konfiguracyjna (odpowiedni parametr systemowy) to biblioteka robi za
	 * "proxy" metod oryginalnych - wyzwalane są metody z klasy {@code SecurityCore}
	 */
	public static final boolean offLine;
	/**
	 * Nazwa parametru systemowego pod nazwą którego kryć się będzie ścieżka
	 * kierująca do pliku systemowego
	 */
	public static final String configFilePathParamName = "ibpm.ldap.config.file";

	static {
		// Ścieżkę do pliku konfiguracyjnego ustawiamy przez parametr systemowy
		configFilePath = System.getProperty(configFilePathParamName);
		offLine = StringUtils.isBlank(configFilePath);
		System.out.println(
				"External LDAP Login Module 'LdapSecurityCore' is loaded and is " + (offLine ? "OFFLINE" : "ONLINE"));
	}

	private static Object providerOptionsFromURLLock = new Object();
	private static Map<String, ProviderOptions> providerOptionsFromURL = new HashMap<>();

	private EncoderDecoder encoderDecoder;

	/**
	 * Mapa opcji (parametrów).
	 */
	private Map<String, String> options;

	/* Niezbędne elementy do nawiązania połączenia */
	/** Provider URL */
	private String providerUrl;
	/**
	 * Adres entry wykorzystywanego podczas uwierzytelniania nawiązywanego
	 * połączenia - z reguły jakiś użytkownik techniczny.
	 */
	private String bindDN;
	/**
	 * Hasło wykorzystywane podczas uwierzytelniania nawiązywanego połączenia - z
	 * reguły hasło użytkownika technicznego. Hasło zakodowane metodą dostępną w
	 * {@link PasswordEncoder}. Zobacz również parametr
	 * {@link #credentialEncoderClass}.
	 */
	private String bindCredential;

	/* uwierzytelnianie użytkownika */
	/**
	 * Podstawowe drzewo LDAP wyszukiwania użytkownika. Wartość wykorzystywana jest
	 * również do wyszukiwania informacji o użytkownikach:
	 * 
	 * @see #usersFilter
	 * @see #userNameAttribute
	 * @see #userDisplayNameAttribute
	 * @see #userEmailAttribute
	 * @see #userDNAttribute
	 * 
	 */
	private String baseCtxDN;
	/**
	 * Podstawowy filtr wyciągający informacje o użytkowniku o podanej nazwie.
	 * <p>
	 * Przykład: <br/>
	 * (&amp;(sAMAccountName={0})(objectClass=user)(isVisible=true))
	 * </p>
	 * gdzie {0} jest zamieniane na nazwę użytkownika.
	 */
	private String baseFilter;

	/**
	 * lista nazw jednostek organizacyjnych odseparowanych znakiem przecinka (może
	 * być pusta, czyli {@code null}), wtedy będzie korzystać z domyślnych ctxDN
	 * zdefiniowanych w {@link ProviderOptions#getBaseCtxDN()}.
	 * 
	 * Podczas tworzenia kontekstów (otwartych połączeń do LDAP) wartość jest
	 * split'owana, a do {@code baseCtxDN} jest doklejany prefix
	 * {@code "OU=" + organizationalUnitName} w trakcie wyszukiwania użytkowników.
	 * 
	 * @deprecated parametr powstał ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	private String[] usersOrganizationalUnitNames = null;

	/**
	 * lista nazw jednostek organizacyjnych odseparowanych znakiem przecinka (może
	 * być pusta, czyli {@code null}), wtedy będzie korzystać z domyślnych ctxDN
	 * zdefiniowanych w {@link ProviderOptions#getRolesCtxDN()}.
	 * 
	 * Podczas tworzenia kontekstów (otwartych połączeń do LDAP) wartość jest
	 * split'owana, a do {@code rolesCtxDN} jest doklejany prefix
	 * {@code "OU=" + organizationalUnitName} w trakcie wyszukiwania grup.
	 * 
	 * @deprecated parametr powstał ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	private String[] groupsOrganizationalUnitNames = null;

	/**
	 * Parametr definiujący kontekst drzewa ze strukturą firmy, wsparcie dla
	 * odróżnienia jednostek organizacji od grup/ról pocztowych i innych. Przykład
	 * wartości: '{@code OU=Struktura,DC=ibpm,DC=pro}' Parametr opcjonalnym domyślna
	 * wartość '{@code n/a}'
	 * 
	 * @deprecated parametr powstał ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	private String structureCtxDN = "n/a";

	/* autoryzacja użytkownika */
	/**
	 * Podstawowe drzewo LDAP wyszukiwania ról użytkownika. Wartość wykorzystywana
	 * jest również do wyszukiwania informacji o grupach:
	 * 
	 * @see #groupsFilter
	 * @see #groupNameAttribute
	 * @see #groupDisplayNameAttribute
	 * @see #groupMemberAttribute
	 * @see #group2GroupTokenAttribute
	 * @see #groupDNAttribute
	 */
	private String rolesCtxDN;
	/**
	 * Podstawowy filtr wyciągający informacje rolach użytkowników o podanej
	 * 'distinguishedName' użytkownika.
	 * <p>
	 * Przykład: <br/>
	 * (&amp;(objectClass=group)(member={1}))
	 * </p>
	 * gdzie {1} jest zamieniane na wartość atrybutu wskazującego
	 * 'distinguishedName' użytkownika ({@link #userDNAttribute}).
	 */
	private String roleFilter;
	/**
	 * Atrybut jednoznacznie identyfikujący entry roli.
	 */
	private String roleAttributeID;
	/**
	 * Czy wartość atrybutu {@link #roleAttributeID} wskazuje na 'distinguishedName'
	 * roli? Wartości jakie przyjmuje to pole to {@code 'true'} albo
	 * {@code 'false'}.
	 */
	private String roleAttributeIsDN;
	/**
	 * Atrybut definiujący nazwę roli.
	 */
	private String roleNameAttributeID;
	/**
	 * Maksymalna liczba poziomów zagnieżdżenia pomiędzy rolami jaka ma być
	 * wykorzystana do zbudowania listy ról użytkownika.
	 */
	private String roleRecursion;

	/* informacje o użytkowniku */
	/**
	 * Podstawowy filtr wyciągający użytkowników.
	 * <p>
	 * Przykład:<br/>
	 * (&amp;(objectClass=user)(isVisible=true))
	 * </p>
	 */
	private String usersFilter;
	/**
	 * Nazwa atrybutu definiującego unikalną nazwę/login użytkownika. Dla AD to
	 * {@code sAMAccountName}.
	 */
	private String userNameAttribute;
	/**
	 * Nazwa atrybutu definiującego nazwę prezentacyjną użytkownika, najczęściej
	 * jest to 'Nazwisko i Imię'.
	 */
	private String userDisplayNameAttribute;
	/** Nazwa atrybutu definiującego adres email użytkownika */
	private String userEmailAttribute;
	/**
	 * Nazwa atrybutu definiującego 'distinguishedName' użytkownika (lokalizację
	 * entry w drzewie katalogowym LDAP'a) - dla AD jest to 'distinguishedName'.
	 */
	private String userDNAttribute;
	/**
	 * Nazwa atrybutu użytkownika, w którym przechowywana jest informacja o głównej
	 * grupie użytkownika.
	 */
	private String userPrimaryGroupAttribute;
	/**
	 * Nie raz nie można wyciągnąć nazwy roli użytkownika bezpośrednio z entry
	 * użytkownika, zatem trzeba użyć filtra. Pole definiuje dodatkowy filtr
	 * wyciągający wartość dodatkowego atrybutu użytkownika zdefiniowanego w IBM BPM
	 * atrybutu o nazwie 'Primary Group'
	 * <p>
	 * Przykład:<br/>
	 * (&amp;(|(CN=*Team)(CN=*Manager))(!(CN=WP*))(member={1}))
	 * </p>
	 * gdzie {1} jest zamieniane na wartość atrybutu wskazującego
	 * 'distinguishedName' użytkownika ({@link #userDNAttribute}).
	 */
	private String userPrimaryGroupFilter;
	/**
	 * Lista klas definiujących obiekt użytkownika. Domyślnie jednoelementowa lista
	 * zawierająca {@link #DEFAULT_USER_OBJECT_CLASS_NAME}. W konfiguracji jest to
	 * lista elementów odseparowana przecinkami ( {@link #ATTRIBUTE_SEPARATOR}).
	 */
	private String[] userObjectClasses = { DEFAULT_USER_OBJECT_CLASS_NAME };
	/**
	 * Nazwa opcji dla: Rozmiar strony dla wyniku wyszukiwania w LDAP.
	 */
	private String userLocaleAttr;

	/* informacje o grupie */
	/**
	 * Podstawowy filtr wyciągający grupy.
	 * <p>
	 * Przykład:<br/>
	 * (&amp;(objectClass=group)(isVisible=true))
	 * </p>
	 */
	private String groupsFilter;
	/** Nazwa atrybutu definiującego unikalną nazwę grupy */
	private String groupNameAttribute;
	/** Nazwa atrybutu definiującego nazwę prezentacyjną grupy. */
	private String groupDisplayNameAttribute;
	/**
	 * Nazwa atrybutu wskazującego na entry członka grupy najczęściej
	 * {@code member}.
	 */
	private String groupMemberAttribute;
	/**
	 * Nazwa atrybutu wskazującego na entry grupy, której członkiem jest dana grupa
	 * (w celu zbudowania hierarchii grup). Najczęściej {@code memberOf}
	 */
	private String group2GroupTokenAttribute;
	/**
	 * Nazwa opcji w której przechowywana jest flaga informująca o tym czy atrybut
	 * wskazujący na "rodzica" jest zdefiniowany jako DN. Jeżeli nie, to wtedy
	 * uznamy, że przechowywana jest wartość taka jak zostało to zdefiniowane dla
	 * {@link #roleAttributeID} oraz {@link #roleAttributeIsDN} .
	 */
	private String group2GroupTokenAttributeIsDN;
	/**
	 * Nazwa atrybutu definiującego 'distinguishedName' grupy (lokalizację entry w
	 * drzewie katalogowym LDAP'a) - dla AD jest to 'distinguishedName'.
	 */
	private String groupDNAttribute;
	/**
	 * Nazwa atrybutu opisującego grupę;
	 */
	private String groupDescriptionAttribute;
	/**
	 * Lista klas definiujących obiekt grupy. Domyślnie jednoelementowa lista
	 * zawierająca {@link #DEFAULT_GROUP_OBJECT_CLASS_NAME}. W konfiguracji jest to
	 * lista elementów odseparowana przecinkami ({@link #ATTRIBUTE_SEPARATOR} ).
	 */
	private String[] groupObjectClasses = { DEFAULT_GROUP_OBJECT_CLASS_NAME };

	/* Parametry dodatkowe */
	/**
	 * Mapowana lista atrybutów dodatkowych, które można synchronizować pomiędzy
	 * LDAP a IBM BPM).
	 */
	private UserAttributeList extendedAttributies = null;
	/**
	 * jeden z rozszerzonych atrybutów - nazwa atrybutu LDAP z avatar użytkownika
	 */
	private String userAvatarAttribute = null;
	/**
	 * Rozmiar cyklu synchronizacji danych o użytkownikach i grupach pomiędzy LDAP i
	 * IBM BPM.
	 */
	private long syncPeriod = DEFAULT_SYNC_PRIOD;
	/**
	 * Domyślna wartość flagi powiadamiania użytkownika o zadaniach drogą
	 * elektroniczną (na adres email użytkownika). Domyślna wartość
	 * {@link Constants.DEFAULT_TASK_NOTIFICATION}.
	 */
	private Boolean defaultTaskNotification = Boolean.parseBoolean(Constants.DEFAULT_TASK_NOTIFICATION);
	/**
	 * Domyślna wartość flagi dla ostrzegania użytkownika przed podjęciem zadania
	 * kierowanego do grupy ("Zadanie kierowane jest do grupy i zostanie przypisane
	 * do Ciebie"). Domyślna wartość
	 * {@link Constants.DEFAULT_ATTR_ALERT_ON_ASSIGN_AND_RUN}.
	 */
	private Boolean defaultAlertOnAssignAndRun = Boolean.parseBoolean(Constants.DEFAULT_ATTR_ALERT_ON_ASSIGN_AND_RUN);

	private Boolean userGroupOptionsAreDefinded = Boolean
			.parseBoolean(Constants.DEFAULT_USER_GROUP_OPTIONS_ARE_DEFINED);

	/**
	 * Rozmiar strony dla wyniku wyszukiwania w LDAP.
	 */
	private Integer ldapResultPageSize = null;
	/**
	 * Nazwa opcji dla: Czy nadpisać ustawienia użytkownika ustawieniami domyślnymi?
	 * Dotyczy tylko parametru {@link ProviderOptions#getDefaultTaskNotification()}.
	 */
	private Boolean useDefaultParams = Boolean.parseBoolean(Constants.DEFAULT_USE_DEFAULT_PARAMS);

	/**
	 * Poziom logowania komunikatów. Domyslnie INFO.
	 * 
	 * @see LogLevel
	 * @deprecated zmieniono mechanizmy logowania operacji.
	 */
	@Deprecated
	private LogLevel logLevel = LogLevel.INFO;

	/**
	 * Nazwa klasy implementacji kodera haseł, przechowywanych w pliku z
	 * konfiguracją. Opcjonalnie, w przypadku gdy nie zostanie zdefiniowana nazwa
	 * klasy zostanie użyty domyślny enkoder.
	 */
	private String credentialEncoderClass;

	/**
	 * Obiekt statyczny instancji {@link ProviderOptions}.
	 */
	private static ProviderOptions instance;

	/** Konfiguracja połączenia/parametrów odczytu danych o użytkownikach w LDAP */
	private LdapConfig config;

	/** Konfiguracja pamięci podręcznych */
	private transient EhCacheConfig cc;

	private ProviderOptions() {
	}

	/**
	 * Inicjalizacja obiektu {@link ProviderOptions} jako singleton. Tak
	 * zainicjalizowany obiekt dostępny jest za pomocą metody
	 * {@link #getSingleton()}.
	 * 
	 * 
	 * @param ldapOptions
	 *            XML konfiguracyjny w postaci łańcucha znakowego (zobacz
	 *            {@link LdapConfig}).
	 * @return instancja obiektu {@link ProviderOptions}.
	 * @throws IOException
	 */
	public static synchronized ProviderOptions init(String ldapOptions) throws IOException {
		LoadLdapConfiguration llc = new LoadLdapConfiguration(ldapOptions);
		LdapConfig ldapConfig = (LdapConfig) llc.makeIndexConfig();
		return init(ldapConfig.toOptions());
	}

	/**
	 * Inicjalizacja obiektu {@link ProviderOptions} jako singleton. Tak
	 * zainicjalizowany obiekt dostępny jest za pomocą metody
	 * {@link #getSingleton()}.
	 * 
	 * @param url
	 *            nowa, zewnętrzna lokalizacja pliku konfiguracyjnego
	 * @param readConfigFromJar
	 *            czy plik znajduje się w bibliotece *.jar
	 * @return instancja obiektu {@link ProviderOptions}.
	 * @throws IOException
	 */
	public static ProviderOptions init(String url, boolean readConfigFromJar) throws IOException {
		LoadLdapConfiguration llc = new LoadLdapConfiguration(url, readConfigFromJar);
		LdapConfig ldapConfig = llc.makeIndexConfig();
		return init(ldapConfig.toOptions());
	}

	/**
	 * Inicjalizacja obiektu {@link ProviderOptions} jako singleton. Tak
	 * zainicjalizowany obiekt dostępny jest za pomocą metody
	 * {@link #getSingleton()}.
	 * 
	 * @param options
	 *            mapa opcji: nazwa parametry - wartość
	 * @return instancja obiektu {@link ProviderOptions}.
	 * @throws IOException
	 */
	public static ProviderOptions init(Map<String, String> options) throws IOException {
		Object lock = (new StringBuilder(ProviderOptions.class.getName())).toString().intern();
		synchronized (lock) {
			if (instance == null) {
				log.debug("Create new instance");
				instance = new ProviderOptions();
			}
			return init(instance, options);
		}
	}

	private static ProviderOptions init(ProviderOptions instance, Map<String, String> options) throws IOException {
		Object lock = (new StringBuilder(ProviderOptions.class.getName())).toString().intern();
		synchronized (lock) {
			instance.setOptions(options);
			/** Załadowanie parametrów obsługi pamięci podręcznych - START */
			instance.cc = EhCacheConfig.getInstance();
			/*
			 * Parametry z osobnego pliku konfiguracyjnego - nadpisują parametry z pliku
			 * głównego
			 */
			String cachePropertiesFileName = options.get(LdapConfigOptions.option_cacheDynamicParametersPath);
			if (StringUtils.isNotBlank(cachePropertiesFileName)) {
				Map<String, String> cacheProps = null;
				cacheProps = Configurations.loadHashtable(ProviderOptions.class, cachePropertiesFileName);
				if (cacheProps != null && !cacheProps.isEmpty()) {
					for (Entry<String, String> entry : cacheProps.entrySet()) {
						String propertyCode = (String) entry.getKey();
						String value = (String) entry.getValue();
						instance.addCacheParam(propertyCode, value);
					}
				}
			}
			/* Ustawienie parametru systemowego konfiguracji statycznej */
			/** Załadowanie parametrów obsługi pamięci podręcznych - KONIEC */
			return instance;
		}
	}

	/**
	 * @param propertyCode
	 * @param value
	 */
	private void addCacheParam(String propertyCode, String value) {
		if (this.cc.get(propertyCode) != null) {
			log.warn("[CacheConfig] Parametr {} o wartości '{}' zostanie nadpisany wartością '{}'",
					new Object[] { propertyCode, this.cc.get(propertyCode), value });

		}
		this.cc.put(propertyCode, value);
	}

	/**
	 * {@link ProviderOptions} jest implementowany jako singleton. Za pomocą tej
	 * metody pobierana jest pojedyncza instancja obiektu {@link ProviderOptions}. W
	 * odróżnieniu od metody {@link #getInstance()} metoda sprawdza czy instancja
	 * istnieje i jeżeli tak nie jest wyrzuca odpowiedni wyjątek.
	 * 
	 * @return instancja obiektu {@link ProviderOptions}.
	 * @throws ProviderOptionsNotInicjalized
	 */
	public static ProviderOptions getSingleton() throws ProviderOptionsNotInicjalized {
		if (instance == null) {
			throw new ProviderOptionsNotInicjalized("LDAP options instance is not inicjalized");
		}
		return instance;
	}

	/**
	 * Prosta metoda sprawdzająca czy singleton {@link ProviderOptions} został już
	 * zainicjalizowany.
	 * 
	 * @return {@code true} to znaczy, że zainicjalizowany i spokojnie można używać
	 *         metody {@link #getSingleton()}, a jeżeli {@code false}, to trzeba
	 *         zainicjalizować za pomocą jednej z metod: {@link #init(Map)} albo
	 *         {@link #init(String, boolean)}
	 */
	public static boolean isInicjalized() {
		return instance != null;
	}

	/**
	 * 
	 * @param ldapOptionsXML
	 *            parametry konfiguracji LDAP w postaci XML'a
	 * @return
	 * @throws IOException
	 */
	public static ProviderOptions getNewInstance(String ldapOptionsXML) throws IOException {
		LoadLdapConfiguration llc = new LoadLdapConfiguration(ldapOptionsXML);
		LdapConfig ldapConfig = (LdapConfig) llc.makeIndexConfig();
		return getNewInstance(ldapConfig.toOptions());
	}

	/**
	 * Pobranie (utworzenie) nowej instancji obiektu {@link ProviderOptions} na
	 * podstawie lokalizacji konfiguracji w pliku. Aby zminimalizować liczbę
	 * odczytów z pliku konfiguracje są przechowywane w mapie url-konfiguracja (
	 * {@link #providerOptionsFromURL}.
	 * 
	 * @param url
	 *            nowa, zewnętrzna lokalizacja pliku konfiguracyjnego
	 * @param readConfigFromJar
	 *            czy plik znajduje się w bibliotece *.jar
	 * @return instancja obiektu {@link ProviderOptions}.
	 * @throws IOException
	 */
	public static ProviderOptions getNewInstance(String url, boolean readConfigFromJar) throws IOException {
		return getNewInstance(url, readConfigFromJar, false);
	}

	/**
	 * Pobranie (utworzenie) nowej instancji obiektu {@link ProviderOptions}.
	 * 
	 * @param url
	 *            nowa, zewnętrzna lokalizacja pliku konfiguracyjnego
	 * @param readConfigFromJar
	 *            czy plik znajduje się w bibliotece *.jar
	 * @param force
	 * 
	 * @return instancja obiektu {@link ProviderOptions}.
	 * @throws IOException
	 */
	public static ProviderOptions getNewInstance(String url, boolean readConfigFromJar, boolean force)
			throws IOException {
		synchronized (providerOptionsFromURLLock) {
			ProviderOptions providerOptions = providerOptionsFromURL.get(url);
			if (providerOptions == null || force) {
				LoadLdapConfiguration llc = new LoadLdapConfiguration(url, readConfigFromJar);
				LdapConfig ldapConfig = (LdapConfig) llc.makeIndexConfig();
				providerOptions = getNewInstance(ldapConfig.toOptions());
				providerOptionsFromURL.put(url, providerOptions);
			}
			return providerOptions;
		}
	}

	/**
	 * Pobranie (utworzenie) nowej instancji obiektu {@link ProviderOptions}.
	 * 
	 * @param options
	 *            mapa opcji: nazwa parametry - wartość
	 * @return instancja obiektu {@link ProviderOptions}.
	 * @throws IOException
	 */
	public static ProviderOptions getNewInstance(Map<String, String> options) throws IOException {
		ProviderOptions newInstance = new ProviderOptions();
		return init(newInstance, options);
	}

	/**
	 * Ustawianie parametrów obiektu {@link ProviderOptions} - można je zmieniać w
	 * trakcie życia instancji.
	 * 
	 * @param options
	 *            mapa opcji: nazwa parametry - wartość
	 * @throws IOException
	 */
	private void setOptions(Map<String, String> options) throws IOException {

		this.options = options;
		/* Niezbędne elementy do nawiązania połączenia */
		this.providerUrl = requiredOption(option_providerUrl);
		this.bindDN = requiredOption(option_bindDN);
		this.bindCredential = requiredOption(option_bindCredential);
		if (this.bindCredential.startsWith(FILE_LOCATION_PREFIX)) {
			String fileName = this.bindCredential.substring(FILE_LOCATION_PREFIX.length());
			this.bindCredential = ProviderOptionsHelper.loadPasswordWord(ProviderOptions.class, fileName);
		}

		/* mechanizm uwierzytelniania użytkownika */
		this.baseCtxDN = requiredOption(option_baseCtxDN);
		this.baseFilter = requiredOption(option_baseFilter);

		this.usersOrganizationalUnitNames = convert2Array(optionalOption(option_usersOrganizationalUnitNames));
		this.groupsOrganizationalUnitNames = convert2Array(optionalOption(option_groupsOrganizationalUnitNames));

		if (optionalOption(option_structureCtxDN) != null) {
			this.structureCtxDN = optionalOption(option_structureCtxDN);
		}
		String lUserGroupOptionsAreDefinded = requiredOption(option_userGroupOptionsAreDefinded);
		this.userGroupOptionsAreDefinded = Boolean.parseBoolean(lUserGroupOptionsAreDefinded);

		/* autoryzacja użytkownika */
		if (this.userGroupOptionsAreDefinded) {
			this.rolesCtxDN = requiredOption(option_rolesCtxDN);
			this.roleFilter = requiredOption(option_roleFilter);
			this.roleAttributeID = requiredOption(option_roleAttributeID);
			this.roleAttributeIsDN = requiredOption(option_roleAttributeIsDN);
			this.roleNameAttributeID = requiredOption(option_roleNameAttributeID);
			this.roleRecursion = requiredOption(option_roleRecursion);
		} else {
			this.rolesCtxDN = optionalOption(option_rolesCtxDN);
			this.roleFilter = optionalOption(option_roleFilter);
			this.roleAttributeID = optionalOption(option_roleAttributeID);
			this.roleAttributeIsDN = optionalOption(option_roleAttributeIsDN);
			this.roleNameAttributeID = optionalOption(option_roleNameAttributeID);
			this.roleRecursion = optionalOption(option_roleRecursion);
		}

		/*
		 * informacje o użytkowniku
		 * 
		 * @see #usersFilter
		 * 
		 * @see #userNameAttribute
		 * 
		 * @see #userDisplayNameAttribute
		 * 
		 * @see #userEmailAttribute
		 * 
		 * @see #userDNAttribute
		 * 
		 * @see #userPrimaryGroupAttribute
		 * 
		 * @see #userPrimaryGroupFilter
		 */
		this.usersFilter = requiredOption(option_usersFilter);
		this.userNameAttribute = requiredOption(option_userNameAttribute);
		this.userDisplayNameAttribute = requiredOption(option_userDisplayNameAttribute);
		this.userEmailAttribute = optionalOption(option_userEmailAttribute);
		/** jest tylko potrzebne gdy mamy do czynienia z AD */
		this.userDNAttribute = optionalOption(option_userDNAttribute);
		this.userPrimaryGroupAttribute = optionalOption(option_userPrimaryGroupAttribute);
		this.userPrimaryGroupFilter = optionalOption(option_userPrimaryGroupFilter);
		this.userLocaleAttr = optionalOption(option_userLocaleAttr);

		String userObjectClass = optionalOption(option_userObjectClasses);
		if (StringUtils.isNotBlank(userObjectClass)) {
			this.userObjectClasses = userObjectClass.split("\\" + ATTRIBUTE_SEPARATOR);
		}

		/*
		 * informacje o grupach
		 * 
		 * @see #groupsFilter
		 * 
		 * @see #groupNameAttribute
		 * 
		 * @see #groupDisplayNameAttribute
		 * 
		 * @see #groupMemberAttribute
		 * 
		 * @see #group2GroupTokenAttribute
		 * 
		 * @see #group2GroupTokenAttributeIsDN
		 * 
		 * @see #groupDNAttribute
		 */
		if (this.userGroupOptionsAreDefinded) {
			this.groupsFilter = requiredOption(option_groupsFilter);
			this.groupNameAttribute = requiredOption(option_groupNameAttribute);
			this.groupDisplayNameAttribute = requiredOption(option_groupDisplayNameAttribute);
			this.groupMemberAttribute = requiredOption(option_groupMemberAttribute);
			this.group2GroupTokenAttribute = requiredOption(option_group2GroupTokenAttribute);
			this.group2GroupTokenAttributeIsDN = requiredOption(option_group2GroupTokenAttributeIsDN);
			this.groupDNAttribute = optionalOption(option_groupDNAttribute);
			this.groupDescriptionAttribute = optionalOption(option_groupDescriptionAttribute);
		} else {
			this.groupsFilter = optionalOption(option_groupsFilter);
			this.groupNameAttribute = optionalOption(option_groupNameAttribute);
			this.groupDisplayNameAttribute = optionalOption(option_groupDisplayNameAttribute);
			this.groupMemberAttribute = optionalOption(option_groupMemberAttribute);
			this.group2GroupTokenAttribute = optionalOption(option_group2GroupTokenAttribute);
			this.group2GroupTokenAttributeIsDN = optionalOption(option_group2GroupTokenAttributeIsDN);
			this.groupDNAttribute = optionalOption(option_groupDNAttribute);
			this.groupDescriptionAttribute = optionalOption(option_groupDescriptionAttribute);
		}
		String groupObjectClass = optionalOption(option_groupObjectClasses);
		if (StringUtils.isNotBlank(groupObjectClass)) {
			this.groupObjectClasses = groupObjectClass.split("\\" + ATTRIBUTE_SEPARATOR);
		}

		/* Parametry dodatkowe */
		/* Mapowanie dodatkowych atrybutów użytkowników */
		String lWpUserExtendedAttributies = optionalOption(option_extendedAttributies);
		if (lWpUserExtendedAttributies != null) {
			String[] extendedAttributiesMap = lWpUserExtendedAttributies.split("\\" + ATTRIBUTE_SEPARATOR);
			log.debug("extendedAttributiesMap={}", extendedAttributiesMap);
			this.extendedAttributies = new UserAttributeList();
			for (String attributeMap : extendedAttributiesMap) {
				log.debug("attributeMap={}", attributeMap);
				String[] mapRow = attributeMap.split("\\" + ATTRIBUTE_NAME_MAP_SEPARATOR);
				log.debug("mapRow={}", mapRow);
				UserAttributeDefinition uad = new UserAttributeDefinition();
				if (mapRow.length > 1) {
					uad.setLdapAttrName(mapRow[0]);
					uad.setTwAttrName(mapRow[1]);
					if (mapRow.length > 2) {
						Map<String, String> parsedValues = UserAttributeUtils.valueMapParser(mapRow[2]);
						for (Entry<String, String> entry : parsedValues.entrySet()) {
							String ldapValue = entry.getKey();
							String twValue = entry.getValue();
							uad.addValueMap(ldapValue, twValue);
						}
					}
				} else {
					uad.setTwAttrName(mapRow[0]);
					uad.setLdapAttrName(mapRow[0]);
				}
				if (UserAttributeDefinition.TW_ATTR_IMAGE.equals(uad.getTwAttrName())) {
					this.userAvatarAttribute = uad.getLdapAttrName();
				}
				log.debug("uad={}", uad);
				this.extendedAttributies.addUserAttrDefinition(uad);
			}

		}
		/* Cykl synchronizacji */
		String periodSync = optionalOption(option_syncPeriod);
		if (StringUtils.isNotBlank(periodSync)) {
			if (periodSync.contains("*")) {
				String[] vals = periodSync.split("\\*");
				long value = 1L;
				for (String val : vals) {
					long longVal = Long.parseLong(Strings.lrtrim(val));
					value = value * longVal;
				}
				this.syncPeriod = value;
			} else {
				this.syncPeriod = Long.parseLong(Strings.lrtrim(periodSync));
			}
		}
		String lLogLevel = optionalOption(option_logLevel);
		if (StringUtils.isNotBlank(lLogLevel)) {
			try {
				this.logLevel = LogLevel.valueOf(lLogLevel.toUpperCase());
			} catch (LogLevelFormatException e) {
				/* ignoruję błąd i tak już przestarzałej akcji */
			}
		}

		String lDefaultTaskNotification = optionalOption(option_defaultTaskNotification);
		if (StringUtils.isNotBlank(lDefaultTaskNotification)) {
			this.defaultTaskNotification = Boolean.parseBoolean(lDefaultTaskNotification);
		}
		String lDefaultAlertOnAssignAndRun = optionalOption(option_defaultAlertOnAssignAndRun);
		if (StringUtils.isNotBlank(lDefaultAlertOnAssignAndRun)) {
			this.defaultAlertOnAssignAndRun = Boolean.parseBoolean(lDefaultAlertOnAssignAndRun);
		}
		String lLdapResultPageSize = optionalOption(option_ldapResultPageSize);
		if (StringUtils.isNotBlank(lLdapResultPageSize)) {
			this.ldapResultPageSize = Integer.parseInt(lLdapResultPageSize);
		}
		String lUseDefaultParams = optionalOption(option_useDefaultParams);
		if (StringUtils.isNotBlank(lUseDefaultParams)) {
			this.useDefaultParams = Boolean.parseBoolean(lUseDefaultParams);
		}

		String extCredentialEncoderClass = optionalOption(option_credentialEncoderClass);
		if (StringUtils.isNotBlank(extCredentialEncoderClass)) {
			this.credentialEncoderClass = extCredentialEncoderClass;
		}

		this.config = new LdapConfig(options);
	}

	private String requiredOption(String optionName) {
		String optionValue = (String) options.get(optionName);
		if (StringUtils.isBlank(optionValue) || Constants.NULL_STRING.equals(optionValue)) {
			optionValue = null;
		}
		if (optionValue == null) {
			throw new RuntimeErrorException(new Error("Opcja " + optionName + " jest wymagana w konfiguracji"));
		}
		return optionValue;
	}

	private String optionalOption(String optionName) {
		String optionValue = (String) options.get(optionName);
		if (StringUtils.isBlank(optionValue) || Constants.NULL_STRING.equals(optionValue)) {
			optionValue = null;
		}
		return optionValue;
	}

	private String bindFilterVariables(String[] args, String filter) {

		String localFilter = filter;
		if (args != null) {
			int i = 0;
			for (String arg : args) {
				log.debug("local Filter arg{}: {}", new Object[] { i, arg });
				localFilter = Strings.replaceAll(localFilter, "{" + i++ + "}", escape(arg));
			}
		}
		return localFilter;
	}

	private static String escape(final String input) {

		String s = input;
		if (s.indexOf('\\') >= 0) {
			// escape backslash
			s = Strings.replaceAll(s, "\\", "\\\\");
		}
		if (s.indexOf('(') >= 0) {
			// escape left parenthesis
			s = Strings.replaceAll(s, "(", "\\(");
		}
		if (s.indexOf(')') >= 0) {
			// escape right parenthesis
			s = Strings.replaceAll(s, ")", "\\)");
		}
		return s;
	}

	/**
	 * @return the {@link #options}
	 */
	public Map<String, String> getOptions() {
		return options;
	}

	/**
	 * @return the {@link #providerUrl}
	 */
	public String getProviderUrl() {
		return providerUrl;
	}

	/**
	 * @return the {@link #bindDN}
	 */
	public String getBindDN() {
		return bindDN;
	}

	/**
	 * @return the {@link #bindCredential}
	 */
	public String getBindCredential() {
		if (encoderDecoder == null) {
			PasswordEncoder encoder = new PasswordEncoder();
			if (StringUtils.isNotBlank(this.credentialEncoderClass)) {
				encoder.setEncoderClass(credentialEncoderClass);
			}
			encoderDecoder = encoder.getEncoderDecoder();
		}
		return encoderDecoder.decode(bindCredential);
	}

	/**
	 * @return the {@link #baseCtxDN}
	 */
	public String getBaseCtxDN() {
		return baseCtxDN;
	}

	/**
	 * @param args
	 *            wartości jakie mają być załadowane do filtra: args[0] - nazwa,
	 *            args[1] - DN.
	 * @return the {@link #baseFilter}
	 */
	public String getBaseFilter(String[] args) {
		return bindFilterVariables(args, this.baseFilter);
	}

	/**
	 * @return the {@link #rolesCtxDN}
	 */
	public String getRolesCtxDN() {
		return rolesCtxDN;
	}

	/**
	 * @param args
	 *            wartości jakie mają być załadowane do filtra: args[0] - nazwa,
	 *            args[1] - DN.
	 * @return the {@link #roleFilter}
	 */
	public String getRoleFilter(String[] args) {
		return bindFilterVariables(args, roleFilter);
	}

	/**
	 * @return the {@link #roleAttributeID}
	 */
	public String getRoleAttributeID() {
		return roleAttributeID;
	}

	/**
	 * @return the {@link #roleAttributeIsDN}
	 */
	public Boolean getRoleAttributeIsDN() {
		return Boolean.valueOf(roleAttributeIsDN);
	}

	/**
	 * @return the {@link #roleNameAttributeID}
	 */
	public String getRoleNameAttributeID() {
		return roleNameAttributeID;
	}

	/**
	 * @return the {@link #roleRecursion}
	 */
	public String getRoleRecursion() {
		return roleRecursion;
	}

	/**
	 * @return the {@link #usersFilter}
	 */
	public String getUsersFilter() {
		return this.usersFilter;
	}

	/**
	 * @param args
	 *            wartości jakie mają być załadowane do filtra: args[0] - nazwa,
	 *            args[1] - DN.
	 * @return the {@link #usersFilter}
	 */
	public String getUsersFilter(String[] args) {
		return bindFilterVariables(args, this.usersFilter);
	}

	/**
	 * @return the {@link #userNameAttribute}
	 */
	public String getUserNameAttribute() {
		return userNameAttribute;
	}

	/**
	 * @return the {@link #userDisplayNameAttribute}
	 */
	public String getUserDisplayNameAttribute() {
		return userDisplayNameAttribute;
	}

	/**
	 * @return the {@link #userEmailAttribute}
	 */
	public String getUserEmailAttribute() {
		return userEmailAttribute;
	}

	/**
	 * @return the {@link #userDNAttribute}
	 */
	public String getUserDNAttribute() {
		return userDNAttribute;
	}

	/**
	 * @return the {@link #userPrimaryGroupAttribute}
	 */
	public String getUserPrimaryGroupAttribute() {
		return userPrimaryGroupAttribute;
	}

	/**
	 * @param args
	 *            wartości jakie mają być załadowane do filtra: args[0] - nazwa,
	 *            args[1] - DN.
	 * @return the {@link #userPrimaryGroupFilter}
	 */
	public String getUserPrimaryGroupFilter(String[] args) {
		if (StringUtils.isNotBlank(this.userPrimaryGroupFilter)) {
			return bindFilterVariables(args, this.userPrimaryGroupFilter);
		}
		return null;
	}

	/**
	 * @return the {@link #userObjectClasses}
	 */
	public String[] getUserObjectClasses() {
		return userObjectClasses;
	}

	/**
	 * @return the {@link #groupsFilter}
	 */
	public String getGroupsFilter() {
		return this.groupsFilter;
	}

	/**
	 * @param args
	 *            wartości jakie mają być załadowane do filtra: args[0] - nazwa,
	 *            args[1] - DN.
	 * @return the {@link #groupsFilter}
	 */
	public String getGroupsFilter(String[] args) {
		return bindFilterVariables(args, this.groupsFilter);
	}

	/**
	 * @return the {@link #groupNameAttribute}
	 */
	public String getGroupNameAttribute() {
		return groupNameAttribute;
	}

	/**
	 * @return the {@link #groupDisplayNameAttribute}
	 */
	public String getGroupDisplayNameAttribute() {
		return groupDisplayNameAttribute;
	}

	/**
	 * @return the {@link #groupMemberAttribute}
	 */
	public String getGroupMemberAttribute() {
		return groupMemberAttribute;
	}

	/**
	 * @return the {@link #group2GroupTokenAttribute}
	 */
	public String getGroup2GroupTokenAttribute() {
		return group2GroupTokenAttribute;
	}

	/**
	 * @return the {@link #groupDNAttribute}
	 */
	public String getGroupDNAttribute() {
		return groupDNAttribute;
	}

	/**
	 * @return the {@link #groupObjectClasses}
	 */
	public String[] getGroupObjectClasses() {
		return groupObjectClasses;
	}

	/**
	 * @return the {@link #extendedAttributies}
	 */
	public UserAttributeList getExtendedAttributies() {
		return extendedAttributies;
	}

	/**
	 * @return the {@link #syncPeriod}
	 */
	public long getSyncPeriod() {
		return syncPeriod;
	}

	/**
	 * @return the {@link #defaultTaskNotification}
	 */
	public Boolean getDefaultTaskNotification() {
		return defaultTaskNotification;
	}

	/**
	 * @return the {@link #defaultAlertOnAssignAndRun}
	 */
	public Boolean getDefaultAlertOnAssignAndRun() {
		return defaultAlertOnAssignAndRun;
	}

	/**
	 * @return the {@link #userGroupOptionsAreDefinded}
	 */
	public boolean isUserGroupOptionsAreDefinded() {
		return userGroupOptionsAreDefinded.booleanValue();
	}

	/**
	 * Niszczenie (usuwanie) instancji opcji. Po wydaniu tego polecenia konieczna
	 * jest ponowna inicjalizacja instancji za pomocą metody {@link #init(Map)} albo
	 * {@link #init(String, boolean)}.
	 */
	public static void destroyInstance() {
		instance = null;
	}

	private Object getValue(Object value) {
		Object ret = null;
		if (value == null) {
			ret = Constants.NOT_ALLOWED;
		} else {
			ret = value;
		}
		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ProviderOptions [");
		builder.append("providerUrl=");
		builder.append(getValue(providerUrl));
		builder.append(", bindDN=");
		builder.append(getValue(bindDN));
		builder.append(", bindCredential=");
		builder.append(getValue(bindCredential));
		builder.append(", baseCtxDN=");
		builder.append(getValue(baseCtxDN));
		builder.append(", baseFilter=");
		builder.append(getValue(baseFilter));
		builder.append(", rolesCtxDN=");
		builder.append(getValue(rolesCtxDN));
		builder.append(", roleFilter=");
		builder.append(getValue(roleFilter));
		builder.append(", roleAttributeID=");
		builder.append(getValue(roleAttributeID));
		builder.append(", roleAttributeIsDN=");
		builder.append(getValue(roleAttributeIsDN));
		builder.append(", roleNameAttributeID=");
		builder.append(getValue(roleNameAttributeID));
		builder.append(", roleRecursion=");
		builder.append(getValue(roleRecursion));
		builder.append(", usersFilter=");
		builder.append(getValue(usersFilter));
		builder.append(", userNameAttribute=");
		builder.append(getValue(userNameAttribute));
		builder.append(", userDisplayNameAttribute=");
		builder.append(getValue(userDisplayNameAttribute));
		builder.append(", userEmailAttribute=");
		builder.append(getValue(userEmailAttribute));
		builder.append(", userDNAttribute=");
		builder.append(getValue(userDNAttribute));
		builder.append(", userPrimaryGroupAttribute=");
		builder.append(getValue(userPrimaryGroupAttribute));
		builder.append(", userPrimaryGroupFilter=");
		builder.append(getValue(userPrimaryGroupFilter));
		builder.append(", userObjectClasses=");
		builder.append(getValue(Arrays.toString(userObjectClasses)));
		builder.append(", groupsFilter=");
		builder.append(getValue(groupsFilter));
		builder.append(", groupNameAttribute=");
		builder.append(getValue(groupNameAttribute));
		builder.append(", groupDisplayNameAttribute=");
		builder.append(getValue(groupDisplayNameAttribute));
		builder.append(", groupMemberAttribute=");
		builder.append(getValue(groupMemberAttribute));
		builder.append(", group2GroupTokenAttribute=");
		builder.append(getValue(group2GroupTokenAttribute));
		builder.append(", groupDNAttribute=");
		builder.append(getValue(groupDNAttribute));
		builder.append(", groupObjectClasses=");
		builder.append(getValue(Arrays.toString(groupObjectClasses)));
		builder.append(", extendedAttributies=");
		builder.append(getValue(extendedAttributies));
		builder.append(", syncPeriod=");
		builder.append(getValue(syncPeriod));
		builder.append(", defaultTaskNotification=");
		builder.append(getValue(defaultTaskNotification));
		builder.append(", defaultAlertOnAssignAndRun=");
		builder.append(getValue(defaultAlertOnAssignAndRun));
		builder.append(", userGroupOptionsAreDefinded=");
		builder.append(getValue(userGroupOptionsAreDefinded));
		builder.append(", credentialEncoderClass=");
		builder.append(getValue(credentialEncoderClass));
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return the {@link #userAvatarAttribute}
	 */
	public String getUserAvatarAttribute() {
		return userAvatarAttribute;
	}

	/**
	 * @return the {@link #ldapResultPageSize}
	 */
	public Integer getLdapResultPageSize() {
		return ldapResultPageSize;
	}

	public String toXMLStr() {
		OutputStream output = new OutputStream() {

			private StringBuilder string = new StringBuilder();

			/**
			 * dodanie znaku do strumienia.
			 */
			public void write(int b) throws IOException {
				string.append((char) b);
			}

			/**
			 * Nadpisanie metody przekształcenia strumienia do łańcucha.
			 */
			@Override
			public String toString() {
				return string.toString();
			}

		};
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(LdapConfig.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty("jaxb.formatted.output", Boolean.TRUE);
			jaxbMarshaller.marshal(this.config, output);
		} catch (JAXBException e) {
			log.error("Błąd przekształcenia konfiguracji do postaci XML", e);
		}
		return output.toString();
	}

	/**
	 * @return the {@link #log}
	 */
	public static Logger getLog() {
		return log;
	}

	/**
	 * @return the {@link #baseFilter}
	 */
	public String getBaseFilter() {
		return baseFilter;
	}

	/**
	 * @return the {@link #usersOrganizationalUnitNames}
	 * @deprecated parametr powstał ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	public String[] getUsersOrganizationalUnitNames() {
		return usersOrganizationalUnitNames;
	}

	/**
	 * @return the {@link #groupsOrganizationalUnitNames}
	 * @deprecated parametr powstał ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	public String[] getGroupsOrganizationalUnitNames() {
		return groupsOrganizationalUnitNames;
	}

	/**
	 * @return the {@link #roleFilter}
	 */
	public String getRoleFilter() {
		return roleFilter;
	}

	/**
	 * @return the {@link #userGroupOptionsAreDefinded}
	 */
	public Boolean getUserGroupOptionsAreDefinded() {
		return userGroupOptionsAreDefinded;
	}

	/**
	 * @return the {@link #instance}
	 */
	public static ProviderOptions getInstance() {
		return instance;
	}

	/**
	 * @return the {@link #config}
	 */
	public LdapConfig getConfig() {
		return config;
	}

	/**
	 * Sprawdzenie czy grupa mająca dany DN (podany jako argument) jest zdefiniowana
	 * jako jednostka strukturalna firmy.
	 * 
	 * @param groupDN
	 *            DN badanej grupy
	 * @return {@code true} jeżeli grupa jest zdefiniowana jako jednostka
	 *         strukturalna firmy.
	 */
	public boolean checkStructureUnit(String groupDN) {
		if (StringUtils.isBlank(groupDN)) {
			return false;
		}
		String[] structures = convert2Array(this.structureCtxDN.toUpperCase());
		if (structures.length > 0) {
			for (String str : structures) {
				if (StringUtils.isNotBlank(str) && groupDN.toUpperCase().endsWith(str)) {
					return true;
				}
			}
		} else {
			return false;
		}
		return groupDN.toUpperCase().endsWith(this.structureCtxDN.toUpperCase());
	}

	/**
	 * @return the {@link #structureCtxDN}
	 * @deprecated parametr powstał ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	public String getStructureCtxDN() {
		return structureCtxDN;
	}

	/**
	 * @return the {@link #userLocaleAttr}
	 */
	public String getUserLocaleAttr() {
		return userLocaleAttr;
	}

	/**
	 * @return the {@link #useDefaultParams}
	 */
	public Boolean getUseDefaultParams() {
		return useDefaultParams;
	}

	/**
	 * @return the {@link #groupDescriptionAttribute}
	 */
	public String getGroupDescriptionAttribute() {
		return groupDescriptionAttribute;
	}

	/**
	 * @return the {@link #group2GroupTokenAttributeIsDN}
	 */
	public Boolean getGroup2GroupTokenAttributeIsDN() {
		return Boolean.valueOf(group2GroupTokenAttributeIsDN);
	}

	/**
	 * @return the {@link #logLevel}
	 * @deprecated zmieniono mechanizmy logowania operacji.
	 */
	@Deprecated
	public LogLevel getLogLevel() {
		return logLevel;
	}

	/**
	 * Kolejna metoda pobierania opcji, tym razem odczytanych z pliku, którego
	 * ścieżka została podana jako parametr systemowy.
	 * 
	 * @see #configFilePath
	 * @see #configFilePathParamName
	 * @see #offLine
	 * @return UWAGA! może być {@code null}
	 * @throws LdapProviderException
	 */
	public static ProviderOptions getProviderOptionsFromConfigFile() throws LdapProviderException {
		ProviderOptions po = null;
		if (!ProviderOptions.offLine) {
			if (!ProviderOptions.isInicjalized()) {
				/* jeszcze nie zostały zainicjalizowane, więc je inicjalizuję */
				try {
					ProviderOptions.init(configFilePath, false);
				} catch (IOException e1) {
					throw new ProviderOptionsNotInicjalized("Blad inicjalizacji opcji", e1);
				}
				if (!ProviderOptions.isInicjalized()) {
					throw new ProviderOptionsNotInicjalized(
							"Blad inicjalizacji opcji: opcje provider'a nie zostały zainicjalizowane.");
				}
				po = ProviderOptions.getInstance();

			} else {
				po = ProviderOptions.getInstance();
			}
		}
		return po;
	}

	public static String[] convert2Array(String organizationalUnitNames) {
		String[] organizationalUnitArray;
		if (StringUtils.isBlank(organizationalUnitNames)) {
			organizationalUnitArray = new String[1];
		} else {
			organizationalUnitArray = organizationalUnitNames.split("\\" + ATTRIBUTE_NAME_MAP_SEPARATOR);
			for (int i = 0; i < organizationalUnitArray.length; i++) {
				organizationalUnitArray[i] = StringUtils.trim(organizationalUnitArray[i]);
			}
		}
		return organizationalUnitArray;
	}

	/**
	 * @return the {@link #cc}
	 */
	public EhCacheConfig getCacheConfig() {
		return cc;
	}

}
