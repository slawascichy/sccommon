/*
 * Slawas.pl Copyright &copy; 2011-2012 
 * http://slawas.pl 
 * All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL SŁAWOMIR CICHY BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package pl.slawas.common.ldap.config;

import org.apache.log4j.lf5.LogLevel;

import pl.slawas.common.cache.CacheConstants;
import pl.slawas.common.ldap.provider.ProviderOptions;
import pl.slawas.security.PasswordEncoder;

/**
 * LdapConfigOptions - definicja stałych wykorzystywanych w konfiguracji
 * kontekstu LDAP oraz synchronizacji informacji pomiędzy LDAP a IBM BPM.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LdapConfigOptions {

	protected LdapConfigOptions() {
	}

	/** Otwarcie nawiasu (ze spacją na początku) */
	public static final String OPEN_BRACKED = " (";
	/** Otwarcie nawiasu kwadratowego */
	public static final String OPEN_SQUARE_BRACKED = "[";
	/** Zamknięcie nawiasu */
	public static final String CLOSE_BRACKED = ")";
	/** Zamknięcie nawiasu kwadratowego (ze spacją na końcu) */
	public static final String CLOSE_SQUARE_BRACKED = "] ";
	/** Wartość reprezentująca brak dostępu do wartości */
	public static final String NOT_ALLOWED_VALUE = "n/a";
	/** Wartość reprezentująca spację */
	public static final String SPACE = " ";
	/** Reprezentacja związku pomiędzy dwoma obiektami */
	public static final String RELATIONSHIP = " -> ";
	/** Reprezentacja znaku podkreślenia */
	public static final String UNDERSCORE = "_";
	/** Reprezentacja kropki */
	public static final String DOT = ".";
	/** Reprezentacja dwukropka (sam) */
	public static final String DOUBLE_DOT_ALONE = ":";
	/** Reprezentacja dwukropka (ze spacją na końcu) */
	public static final String DOUBLE_DOT = ": ";
	/** Reprezentacja przecinka (ze spacją na końcu) */
	public static final String COMMA = ", ";
	/** Reprezentacja znaku równości */
	public static final String EQUALS_CHR = "=";
	/** cudzysłów pojedynczy */
	public static final String SINGLE_QUOTATION_MARK = "'";
	/** cudzysłów podwójny */
	public static final String DOUBLE_QUOTATION_MARK = "\"";
	public static final String ZERO = "0";
	public static final Long ZEROL = 0L;
	/** gwiazdka */
	public static final char STAR = '*';
	/** znak zapytania */
	public static final char QUESTION_MARK = '?';

	/**
	 * separator oddzielający poszczególne definicje atrybutów użytkownika.
	 */
	public static final String ATTRIBUTE_SEPARATOR = ",";

	/**
	 * separator rozdzielający różne nazwy atrybutów gdzie pierwsza to nazwa
	 * atrybutu w TW, druga w LDAP
	 */
	public static final String ATTRIBUTE_NAME_MAP_SEPARATOR = "|";

	/**
	 * Separator używany do oddzielania par wartości do zbudowania mapy wartości,
	 * różnic pomiędzy TW i LDAP.
	 */
	public static final String ATTRIBUTE_VALUE_PAIR_MAP_SEPARATOR = ":";

	/**
	 * Separator używana do rozdzielenia wartości w parze.
	 */
	public static final String ATTRIBUTE_VALUE_MAP_SEPARATOR = "@";

	public static final String FILE_LOCATION_PREFIX = "file:";

	/** Domyślna liczba zagnieżdżenia ról */
	public static final String DEFAILT_ROLE_RECURSION = "10";

	/**
	 * Domyślny period synchronizacji użytkowników [ms] ustawiony na 7 dni.
	 */
	public static final long DEFAULT_SYNC_PRIOD = 1000 * 60 * 60 * 24 * 7L;

	/**
	 * Domyślna nazwa klasy obiektu użytkownika w LDAP/AD
	 */
	public static final String DEFAULT_USER_OBJECT_CLASS_NAME = "user";

	/**
	 * Domyślna nazwa klasy obiektu grupy w LDAP/AD
	 */
	public static final String DEFAULT_GROUP_OBJECT_CLASS_NAME = "group";

	/**
	 * Nazwa atrybutu klasy obiektu w LDAP/AD
	 */
	public static final String OBJECT_CLASS = "objectClass";

	/**
	 * Domyślna lokalizacja pliku z dynamicznymi parametrami pamięci podręcznych
	 */
	public static final String DEFAULT_CACHE_DYNAMIC_PARAMETERS_PATH = "/pro/ibpm/security/cache/cache.properties";

	/* Niezbędne elementy do nawiązania połączenia */
	/** Nazwa opcji dla: Provider URL */
	public static final String option_providerUrl = "providerUrl";
	/**
	 * Nazwa opcji dla: Adres entry wykorzystywanego podczas uwierzytelniania
	 * nawiązywanego połączenia - z reguły jakiś użytkownik techniczny.
	 */
	public static final String option_bindDN = "bindDN";
	/**
	 * Nazwa opcji dla: Hasło wykorzystywane podczas uwierzytelniania nawiązywanego
	 * połączenia - z reguły hasło użytkownika technicznego. Hasło zakodowane metodą
	 * dostępną w {@link PasswordEncoder}. Zobacz również parametr
	 * 'credentialEncoderClass'.
	 */
	public static final String option_bindCredential = "bindCredential";

	/* uwierzytelnianie użytkownika */
	/**
	 * Nazwa opcji dla: Podstawowe drzewo LDAP wyszukiwania użytkownika. Wartość
	 * wykorzystywana jest również do wyszukiwania informacji o użytkownikach:
	 * 
	 * @see #usersFilter
	 * @see #userNameAttribute
	 * @see #userDisplayNameAttribute
	 * @see #userEmailAttribute
	 * @see #userDNAttribute
	 * 
	 */
	public static final String option_baseCtxDN = "baseCtxDN";
	/**
	 * Nazwa opcji dla: Podstawowy filtr wyciągający informacje o użytkowniku o
	 * podanej nazwie.
	 * <p>
	 * Przykład: <br/>
	 * (&amp;(sAMAccountName={0})(objectClass=user)(isVisible=true))
	 * </p>
	 * gdzie {0} jest zamieniane na nazwę użytkownika.
	 */
	public static final String option_baseFilter = "baseFilter";

	/**
	 * lista nazw jednostek organizacyjnych odseparowanych znakiem przecinka (może
	 * być pusta, czyli {@code null}), wtedy będzie korzystać z domyślnych ctxDN
	 * zdefiniowanych w {@link ProviderOptions#getBaseCtxDN()}.
	 * 
	 * Podczas tworzenia kontekstów (otwartych połączeń do LDAP) wartość jest
	 * split'owana, a do {@code baseCtxDN} jest doklejany prefix
	 * {@code "OU=" + organizationalUnitName} w trakcie wyszukiwania użytkowników.
	 */
	public static final String option_usersOrganizationalUnitNames = "userOrganizationalUnitNames";

	/**
	 * lista nazw jednostek organizacyjnych odseparowanych znakiem przecinka (może
	 * być pusta, czyli {@code null}), wtedy będzie korzystać z domyślnych ctxDN
	 * zdefiniowanych w {@link ProviderOptions#getRolesCtxDN()}.
	 * 
	 * Podczas tworzenia kontekstów (otwartych połączeń do LDAP) wartość jest
	 * split'owana, a do {@code rolesCtxDN} jest doklejany prefix
	 * {@code "OU=" + organizationalUnitName} w trakcie wyszukiwania grup.
	 */
	public static final String option_groupsOrganizationalUnitNames = "groupOrganizationalUnitNames";

	/**
	 * Parametr definiujący kontekst drzewa ze strukturą firmy, wsparcie dla
	 * odróżnienia jednostek organizacji od grup/ról pocztowych i innych. Przykład
	 * wartości: '{@code OU=Struktura,DC=ibpm,DC=pro}' Parametr opcjonalnym domyślna
	 * wartość '{@code n/a}'
	 */
	public static final String option_structureCtxDN = "structureCtxDN";

	/* autoryzacja użytkownika */
	/**
	 * Nazwa opcji dla: Podstawowe drzewo LDAP wyszukiwania ról użytkownika. Wartość
	 * wykorzystywana jest również do wyszukiwania informacji o grupach:
	 * 
	 * @see #groupsFilter
	 * @see #groupNameAttribute
	 * @see #groupDisplayNameAttribute
	 * @see #groupMemberAttribute
	 * @see #group2GroupTokenAttribute
	 * @see #groupDNAttribute
	 */
	public static final String option_rolesCtxDN = "rolesCtxDN";
	/**
	 * Nazwa opcji dla: Podstawowy filtr wyciągający informacje rolach użytkowników
	 * o podanej 'distinguishedName' użytkownika.
	 * <p>
	 * Przykład: <br/>
	 * (&amp;(objectClass=group)(member={1}))
	 * </p>
	 * gdzie {1} jest zamieniane na wartość atrybutu wskazującego
	 * 'distinguishedName' użytkownika ({@link #userDNAttribute}).
	 */
	public static final String option_roleFilter = "roleFilter";
	/**
	 * Nazwa opcji dla: Atrybut jednoznacznie identyfikujący entry roli.
	 */
	public static final String option_roleAttributeID = "roleAttributeID";
	/**
	 * Nazwa opcji dla: Czy wartość atrybutu {@link #roleAttributeID} wskazuje na
	 * 'distinguishedName' roli? Wartości jakie przyjmuje to pole to {@code 'true'}
	 * albo {@code 'false'}.
	 */
	public static final String option_roleAttributeIsDN = "roleAttributeIsDN";
	/**
	 * Nazwa opcji dla: Atrybut definiujący nazwę roli.
	 */
	public static final String option_roleNameAttributeID = "roleNameAttributeID";
	/**
	 * Nazwa opcji dla: Maksymalna liczba poziomów zagnieżdżenia pomiędzy rolami
	 * jaka ma być wykorzystana do zbudowania listy ról użytkownika.
	 */
	public static final String option_roleRecursion = "roleRecursion";

	/* informacje o użytkowniku */
	/**
	 * Nazwa opcji dla: Podstawowy filtr wyciągający użytkowników.
	 * <p>
	 * Przykład:<br/>
	 * (&amp;(objectClass=user)(isVisible=true))
	 * </p>
	 */
	public static final String option_usersFilter = "usersFilter";
	/**
	 * Nazwa opcji dla: Nazwa atrybutu definiującego unikalną nazwę/login
	 * użytkownika. Dla AD to {@code sAMAccountName}.
	 */
	public static final String option_userNameAttribute = "userNameAttribute";
	/**
	 * Nazwa opcji dla: Nazwa atrybutu definiującego nazwę prezentacyjną
	 * użytkownika, najczęściej jest to 'Nazwisko i Imię'.
	 */
	public static final String option_userDisplayNameAttribute = "userDisplayNameAttribute";
	/** Nazwa opcji dla: Nazwa atrybutu definiującego adres email użytkownika */
	public static final String option_userEmailAttribute = "userEmailAttribute";
	/**
	 * Nazwa opcji dla: Nazwa atrybutu definiującego 'distinguishedName' użytkownika
	 * (lokalizację entry w drzewie katalogowym LDAP'a) - dla AD jest to
	 * 'distinguishedName'.
	 */
	public static final String option_userDNAttribute = "userDNAttribute";
	/**
	 * Nazwa opcji dla: Nazwa atrybutu użytkownika, w którym przechowywana jest
	 * informacja o głównej grupie użytkownika.
	 */
	public static final String option_userPrimaryGroupAttribute = "userPrimaryGroupAttribute";
	/**
	 * Nazwa opcji dla: Dodatkowy filtr wyciągający wartość dodatkowego atrybutu
	 * użytkownika zdefiniowanego w IBM BPM atrybutu o nazwie 'Primary Group'
	 * <p>
	 * Przykład:<br/>
	 * (&amp;(|(CN=*Team)(CN=*Manager))(!(CN=WP*))(member={1}))
	 * </p>
	 * gdzie {1} jest zamieniane na wartość atrybutu wskazującego
	 * 'distinguishedName' użytkownika ({@link #userDNAttribute}).
	 */
	public static final String option_userPrimaryGroupFilter = "userPrimaryGroupFilter";
	/**
	 * Nazwa opcji dla: Lista klas definiujących obiekt użytkownika. Domyślnie
	 * jednoelementowa lista zawierająca {@link #DEFAULT_USER_OBJECT_CLASS_NAME} . W
	 * konfiguracji jest to lista elementów odseparowana przecinkami (
	 * {@link #ATTRIBUTE_SEPARATOR}).
	 */
	public static final String option_userObjectClasses = "userObjectClasses";

	/* informacje o grupie */
	/**
	 * Nazwa opcji dla: Podstawowy filtr wyciągający grupy.
	 * <p>
	 * Przykład:<br/>
	 * (&amp;(objectClass=group)(isVisible=true))
	 * </p>
	 */
	public static final String option_groupsFilter = "groupsFilter";
	/** Nazwa opcji dla: Nazwa atrybutu definiującego unikalną nazwę grupy */
	public static final String option_groupNameAttribute = "groupNameAttribute";
	/** Nazwa opcji dla: Nazwa atrybutu definiującego nazwę prezentacyjną grupy. */
	public static final String option_groupDisplayNameAttribute = "groupDisplayNameAttribute";
	/**
	 * Nazwa opcji dla: Nazwa atrybutu wskazującego na entry członka grupy
	 * najczęściej {@code member}.
	 */
	public static final String option_groupMemberAttribute = "groupMemberAttribute";
	/**
	 * Nazwa opcji dla: Nazwa atrybutu wskazującego na entry grupy, której członkiem
	 * jest dana grupa (w celu zbudowania hierarchii grup). Najczęściej
	 * {@code memberOf}
	 */
	public static final String option_group2GroupTokenAttribute = "group2GroupTokenAttribute";
	/**
	 * Nazwa opcji dla: Nazwa atrybutu definiującego 'distinguishedName' grupy
	 * (lokalizację entry w drzewie katalogowym LDAP'a) - dla AD jest to
	 * 'distinguishedName'.
	 */
	public static final String option_groupDNAttribute = "groupDNAttribute";
	/**
	 * Nazwa opcji dla: Lista klas definiujących obiekt grupy. Domyślnie
	 * jednoelementowa lista zawierająca {@link #DEFAULT_GROUP_OBJECT_CLASS_NAME}. W
	 * konfiguracji jest to lista elementów odseparowana przecinkami
	 * ({@link #ATTRIBUTE_SEPARATOR} ).
	 */
	public static final String option_groupObjectClasses = "groupObjectClasses";

	/* Parametry dodatkowe */
	/**
	 * Nazwa opcji dla: Mapowana lista atrybutów dodatkowych, które można
	 * synchronizować pomiędzy LDAP a IBM BPM).
	 */
	public static final String option_extendedAttributies = "extendedAttributies";

	/**
	 * Nazwa opcji dla: Rozmiar cyklu synchronizacji danych o użytkownikach i
	 * grupach pomiędzy LDAP i IBM BPM.
	 */
	public static final String option_syncPeriod = "syncPeriod";

	/**
	 * Nazwa opcji dla: Domyślna wartość flagi powiadamiania użytkownika o zadaniach
	 * drogą elektroniczną (na adres email użytkownika).
	 */
	public static final String option_defaultTaskNotification = "defaultTaskNotification";

	/**
	 * Nazwa opcji dla: Domyślna wartość flagi dla ostrzegania użytkownika przed
	 * podjęciem zadania kierowanego do grupy ("Zadanie kierowane jest do grupy i
	 * zostanie przypisane do Ciebie"). ALERT_ON_ASSIGN_AND_RUN
	 */
	public static final String option_defaultAlertOnAssignAndRun = "defaultAlertOnAssignAndRun";

	/**
	 * Nazwa opcji dla: Bardzo ważna flaga, mówiąca o tym, czy konfiguracja ról/grup
	 * użytkownika została zdefiniowana.
	 */
	public static final String option_userGroupOptionsAreDefinded = "userGroupOptionsAreDefinded";

	/**
	 * Nazwa opcji dla: Rozmiar strony dla wyniku wyszukiwania w LDAP.
	 */
	public static final String option_ldapResultPageSize = "ldapResultPageSize";

	/**
	 * Nazwa opcji dla: Rozmiar strony dla wyniku wyszukiwania w LDAP.
	 */
	public static final String option_userLocaleAttr = "userLocaleAttr";

	/**
	 * Nazwa opcji dla: Czy nadpisać ustawienia użytkownika ustawieniami domyślnymi?
	 * Dotyczy tylko parametru {@link ProviderOptions#getDefaultTaskNotification()}.
	 */
	public static final String option_useDefaultParams = "useDefaultParams";

	/**
	 * Nazwa opcji dla opisy grupy.
	 */
	public static final String option_groupDescriptionAttribute = "groupDescriptionAttribute";

	/**
	 * Nazwa opcji, w której przechowywana jest flaga informująca o tym czy atrybut
	 * wskazujący na "rodzica" jest zdefiniowany jako DN. Jeżeli nie, to wtedy
	 * uznamy, że przechowywana jest wartość taka jak zostało to zdefiniowane dla
	 * {@link LdapConfigOptions#option_roleAttributeID} oraz
	 * {@link LdapConfigOptions#option_roleAttributeIsDN}.
	 */
	public static final String option_group2GroupTokenAttributeIsDN = "group2GroupTokenAttributeIsDN";

	/**
	 * Nazwa opcji, która definiuje poziom logowania komunikatów.
	 * 
	 * @see LogLevel
	 */
	public static final String option_logLevel = "logLevel";

	/**
	 * Nazwa opcji, która definiuje ścieżkę z plikiem zawierającym konfigurację
	 * parametrów pamięci podręcznych (dynamicznych + parametr wskazujący na nazwę
	 * statycznego pliku).
	 * 
	 * @see CacheConstants#PROP_DYNAMIC_PARAMETERS_FILE_PATH
	 * 
	 */
	public static final String option_cacheDynamicParametersPath = "cacheDynamicParametersPath";

	/**
	 * Nazwa opcji, która definiuje klasę implementacji kodera haseł,
	 * przechowywanych w pliku z konfiguracją. Opcjonalnie, w przypadku gdy nie
	 * zostanie zdefiniowana nazwa klasy zostanie użyty domyślny enkoder.
	 */
	public static final String option_credentialEncoderClass = "credentialEncoderClass";

}
