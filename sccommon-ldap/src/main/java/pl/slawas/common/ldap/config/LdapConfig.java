package pl.slawas.common.ldap.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.lf5.LogLevel;

import pl.slawas.common.ldap.api.Constants;
import pl.slawas.common.ldap.provider.ProviderOptions;
import pl.slawas.security.PasswordEncoder;

/**
 * 
 * LdapConfig - klasa zarządzająca parametrami konfiguracji provider'a LDAP'a.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.4 $
 * 
 */
@XmlRootElement(name = "ldap-options")
public class LdapConfig extends LdapConfigOptions implements Serializable {

	private static final long serialVersionUID = 1302128333610279616L;

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
	private String usersOrganizationalUnitNames;
	private OptionsList usersOrganizationalUnitNamesOptions;
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
	 * Bardzo ważna flaga, mówiąca o tym, czy konfiguracja ról/grup użytkownika
	 * została zdefiniowana. Przyjmuje dwie wartości: {@code true} albo
	 * {@code false}. Jeżeli ma wartość {@code true} to definicje opcji dla
	 * poniższych pól są wymagane!:
	 * 
	 * <p>
	 * Role
	 * </p>
	 * <ul>
	 * <li>{@link #rolesCtxDN}</li>
	 * <li>{@link #roleFilter}</li>
	 * <li>{@link #roleAttributeID}</li>
	 * <li>{@link #roleAttributeIsDN}</li>
	 * <li>{@link #roleNameAttributeID}</li>
	 * <li>{@link #roleRecursion}</li>
	 * </ul>
	 * <p>
	 * Grupy
	 * </p>
	 * <ul>
	 * <li>{@link #groupsFilter}</li>
	 * <li>{@link #groupNameAttribute}</li>
	 * <li>{@link #groupDisplayNameAttribute}</li>
	 * <li>{@link #groupMemberAttribute}</li>
	 * <li>{@link #group2GroupTokenAttribute}</li>
	 * <li>{@link #group2GroupTokenAttributeIsDN}</li>
	 * <li>{@link #groupDNAttribute}</li>
	 * </ul>
	 */
	private String userGroupOptionsAreDefinded = "true";

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
	private String groupsOrganizationalUnitNames;
	private OptionsList groupsOrganizationalUnitNamesOptions;

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
	 * Atrybut jednoznacznie identyfikujący entry roli. <b>Atrybutem może być DN
	 * ('distinguishedName') albo nazwa grupy.</b>
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
	 * Dodatkowy filtr wyciągający wartość dodatkowego atrybutu użytkownika
	 * zdefiniowanego w IBM BPM atrybutu o nazwie 'Primary Group'
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
	private String userObjectClasses;
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
	private String structureCtxDN;
	private OptionsList structureCtxDNOptions;

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
	 * Atrybut grupy opisujący grupę.
	 */
	private String groupDescriptionAttribute;
	/**
	 * Lista klas definiujących obiekt grupy. Domyślnie jednoelementowa lista
	 * zawierająca {@link #DEFAULT_GROUP_OBJECT_CLASS_NAME}. W konfiguracji jest to
	 * lista elementów odseparowana przecinkami ({@link #ATTRIBUTE_SEPARATOR} ).
	 */
	private String groupObjectClasses;

	/* Parametry dodatkowe */
	/**
	 * Mapowana lista atrybutów dodatkowych, które można synchronizować pomiędzy
	 * LDAP a IBM BPM).
	 * 
	 * @deprecated Mechanizm dodatkowego mapowania miał zastosowanie tylko i
	 *             wyłącznie w IBM BPM
	 */
	@Deprecated
	private String extendedAttributies;
	private OptionsList extendedAttributiesOptions;

	/**
	 * Rozmiar cyklu synchronizacji danych o użytkownikach i grupach pomiędzy LDAP i
	 * IBM BPM.
	 */
	private String syncPeriod;
	/**
	 * Domyślna wartość flagi powiadamiania użytkownika o zadaniach drogą
	 * elektroniczną (na adres email użytkownika). Domyślna wartość
	 * {@link Constants.DEFAULT_TASK_NOTIFICATION}.
	 */
	private String defaultTaskNotification = Constants.DEFAULT_TASK_NOTIFICATION;
	/**
	 * Domyślna wartość flagi dla ostrzegania użytkownika przed podjęciem zadania
	 * kierowanego do grupy ("Zadanie kierowane jest do grupy i zostanie przypisane
	 * do Ciebie"). Domyślna wartość
	 * {@link Constants.DEFAULT_ATTR_ALERT_ON_ASSIGN_AND_RUN}.
	 */
	private String defaultAlertOnAssignAndRun = Constants.DEFAULT_ATTR_ALERT_ON_ASSIGN_AND_RUN;
	/**
	 * Rozmiar strony dla wyniku wyszukiwania w LDAP.
	 */
	private String ldapResultPageSize;
	/**
	 * Nazwa opcji dla: Czy nadpisać ustawienia użytkownika ustawieniami domyślnymi?
	 * Dotyczy tylko parametru {@link ProviderOptions#getDefaultTaskNotification()}.
	 */
	private String useDefaultParams;

	/**
	 * Poziom logowania komunikatów.
	 * 
	 * @see LogLevel
	 * @deprecated zmieniono mechanizmy logowania operacji.
	 */
	@Deprecated
	private String logLevel;

	/**
	 * Nazwa opcji, która definiuje ścieżkę do konfiguracji parametrów pamięci
	 * podręcznych.
	 * 
	 */
	private String cacheDynamicParametersPath = DEFAULT_CACHE_DYNAMIC_PARAMETERS_PATH;

	/**
	 * Nazwa klasy implementacji kodera haseł, przechowywanych w pliku z
	 * konfiguracją. Opcjonalnie, w przypadku gdy nie zostanie zdefiniowana nazwa
	 * klasy zostanie użyty domyślny enkoder.
	 */
	private String credentialEncoderClass;

	/**
	 * Podstawowy konstruktor
	 */
	public LdapConfig() {

	}

	/**
	 * Konstruktor konfiguracji na podstawie mapy parametrów i ich wartości.
	 * 
	 * @param options
	 *            mapa opcji (parametrów).
	 */
	public LdapConfig(Map<String, String> options) {

		providerUrl = (String) options.get(option_providerUrl);
		bindDN = (String) options.get(option_bindDN);
		bindCredential = (String) options.get(option_bindCredential);
		baseCtxDN = (String) options.get(option_baseCtxDN);
		baseFilter = (String) options.get(option_baseFilter);
		rolesCtxDN = (String) options.get(option_rolesCtxDN);
		roleFilter = (String) options.get(option_roleFilter);
		roleAttributeID = (String) options.get(option_roleAttributeID);
		roleAttributeIsDN = (String) options.get(option_roleAttributeIsDN);
		roleNameAttributeID = (String) options.get(option_roleNameAttributeID);
		roleRecursion = (String) options.get(option_roleRecursion);
		usersFilter = (String) options.get(option_usersFilter);
		userNameAttribute = (String) options.get(option_userNameAttribute);
		userDisplayNameAttribute = (String) options.get(option_userDisplayNameAttribute);
		userEmailAttribute = (String) options.get(option_userEmailAttribute);
		userDNAttribute = (String) options.get(option_userDNAttribute);
		userPrimaryGroupAttribute = (String) options.get(option_userPrimaryGroupAttribute);
		userPrimaryGroupFilter = (String) options.get(option_userPrimaryGroupFilter);
		userObjectClasses = (String) options.get(option_userObjectClasses);
		groupsFilter = (String) options.get(option_groupsFilter);
		groupNameAttribute = (String) options.get(option_groupNameAttribute);
		groupDisplayNameAttribute = (String) options.get(option_groupDisplayNameAttribute);
		groupMemberAttribute = (String) options.get(option_groupMemberAttribute);
		group2GroupTokenAttribute = (String) options.get(option_group2GroupTokenAttribute);
		groupDNAttribute = (String) options.get(option_groupDNAttribute);
		groupObjectClasses = (String) options.get(option_groupObjectClasses);

		/* Ustawianie dodatkowych atrybutów */
		String extAttributies = (String) options.get(option_extendedAttributies);
		if (StringUtils.isNotBlank(extAttributies)) {
			extendedAttributies = extAttributies;
			extendedAttributiesOptions = transformString2OptinsList(extAttributies, ATTRIBUTE_SEPARATOR);
		}

		syncPeriod = (String) options.get(option_syncPeriod);
		defaultTaskNotification = (String) options.get(option_defaultTaskNotification);
		defaultAlertOnAssignAndRun = (String) options.get(option_defaultAlertOnAssignAndRun);
		userGroupOptionsAreDefinded = (String) options.get(option_userGroupOptionsAreDefinded);
		ldapResultPageSize = (String) options.get(option_ldapResultPageSize);

		/* Ustawianie jednostek organizacyjnych użytkowników */
		String uOrganizationalUnitNames = (String) options.get(option_usersOrganizationalUnitNames);
		if (StringUtils.isNotBlank(uOrganizationalUnitNames)) {
			usersOrganizationalUnitNames = uOrganizationalUnitNames;
			usersOrganizationalUnitNamesOptions = transformString2OptinsList(uOrganizationalUnitNames,
					ATTRIBUTE_NAME_MAP_SEPARATOR);
		}

		/* Ustawianie jednostek organizacyjnych grup */
		String gOrganizationalUnitNames = (String) options.get(option_groupsOrganizationalUnitNames);
		if (StringUtils.isNotBlank(gOrganizationalUnitNames)) {
			groupsOrganizationalUnitNames = gOrganizationalUnitNames;
			groupsOrganizationalUnitNamesOptions = transformString2OptinsList(gOrganizationalUnitNames,
					ATTRIBUTE_NAME_MAP_SEPARATOR);
		}

		/* Ustawianie jednostek organizacyjnych grup */
		String structureCtxDNs = (String) options.get(option_structureCtxDN);
		if (StringUtils.isNotBlank(structureCtxDNs)) {
			structureCtxDN = structureCtxDNs;
			structureCtxDNOptions = transformString2OptinsList(structureCtxDNs, ATTRIBUTE_NAME_MAP_SEPARATOR);
		}

		userLocaleAttr = (String) options.get(option_userLocaleAttr);
		useDefaultParams = (String) options.get(option_useDefaultParams);
		groupDescriptionAttribute = (String) options.get(option_groupDescriptionAttribute);
		group2GroupTokenAttributeIsDN = (String) options.get(option_group2GroupTokenAttributeIsDN);
		logLevel = (String) options.get(option_logLevel);
		cacheDynamicParametersPath = (String) options.get(option_cacheDynamicParametersPath);

		String extCredentialEncoderClass = (String) options.get(option_credentialEncoderClass);
		if (StringUtils.isNotBlank(extCredentialEncoderClass)) {
			credentialEncoderClass = extCredentialEncoderClass;
		}
	}

	/**
	 * 
	 * @param optionsListStr
	 * @param separator
	 * @return
	 */
	private OptionsList transformString2OptinsList(String optionsListStr, String separator) {
		OptionsList oList = new OptionsList();
		List<String> ops = new ArrayList<>();
		String[] elements = optionsListStr.split("\\" + separator);
		for (String element : elements) {
			if (StringUtils.isNotBlank(element)) {
				ops.add(element);
			}
		}
		oList.setOption(ops);
		return oList;
	}

	/**
	 * Transformacja obiektu do mapy parametrów.
	 * 
	 * @return mapa parametrów.
	 */
	public Map<String, String> toOptions() {
		Map<String, String> out = new HashMap<>();
		out.put(option_providerUrl, providerUrl);
		out.put(option_bindDN, bindDN);
		out.put(option_bindCredential, bindCredential);
		out.put(option_baseCtxDN, baseCtxDN);
		out.put(option_baseFilter, baseFilter);
		out.put(option_rolesCtxDN, rolesCtxDN);
		out.put(option_roleFilter, roleFilter);
		out.put(option_roleAttributeID, roleAttributeID);
		out.put(option_roleAttributeIsDN, roleAttributeIsDN);
		out.put(option_roleNameAttributeID, roleNameAttributeID);
		out.put(option_roleRecursion, roleRecursion);
		out.put(option_usersFilter, usersFilter);
		out.put(option_userNameAttribute, userNameAttribute);
		out.put(option_userDisplayNameAttribute, userDisplayNameAttribute);
		out.put(option_userEmailAttribute, userEmailAttribute);
		out.put(option_userDNAttribute, userDNAttribute);
		out.put(option_userPrimaryGroupAttribute, userPrimaryGroupAttribute);
		out.put(option_userPrimaryGroupFilter, userPrimaryGroupFilter);
		out.put(option_userObjectClasses, userObjectClasses);
		out.put(option_groupsFilter, groupsFilter);
		out.put(option_groupNameAttribute, groupNameAttribute);
		out.put(option_groupDisplayNameAttribute, groupDisplayNameAttribute);
		out.put(option_groupMemberAttribute, groupMemberAttribute);
		out.put(option_group2GroupTokenAttribute, group2GroupTokenAttribute);
		out.put(option_groupDNAttribute, groupDNAttribute);
		out.put(option_groupObjectClasses, groupObjectClasses);

		String extAttributies = null;
		if (extendedAttributiesOptions != null) {
			extAttributies = transformOptionsList2String(extendedAttributiesOptions, ATTRIBUTE_SEPARATOR);
		} else {
			extAttributies = extendedAttributies;
		}
		out.put(option_extendedAttributies, extAttributies);

		out.put(option_syncPeriod, syncPeriod);
		out.put(option_defaultTaskNotification, defaultTaskNotification);
		out.put(option_defaultAlertOnAssignAndRun, defaultAlertOnAssignAndRun);
		out.put(option_userGroupOptionsAreDefinded, userGroupOptionsAreDefinded);
		out.put(option_ldapResultPageSize, ldapResultPageSize);

		String uOrganizationalUnitNames = null;
		if (usersOrganizationalUnitNamesOptions != null) {
			uOrganizationalUnitNames = transformOptionsList2String(usersOrganizationalUnitNamesOptions,
					ATTRIBUTE_NAME_MAP_SEPARATOR);
		} else {
			uOrganizationalUnitNames = usersOrganizationalUnitNames;
		}
		out.put(option_usersOrganizationalUnitNames, uOrganizationalUnitNames);

		String gOrganizationalUnitNames = null;
		if (groupsOrganizationalUnitNamesOptions != null) {
			gOrganizationalUnitNames = transformOptionsList2String(groupsOrganizationalUnitNamesOptions,
					ATTRIBUTE_NAME_MAP_SEPARATOR);
		} else {
			gOrganizationalUnitNames = groupsOrganizationalUnitNames;
		}
		out.put(option_groupsOrganizationalUnitNames, gOrganizationalUnitNames);

		/* Ustawianie jednostek organizacyjnych grup */
		String structureCtxDNs = null;
		if (structureCtxDNOptions != null) {
			structureCtxDNs = transformOptionsList2String(structureCtxDNOptions, ATTRIBUTE_NAME_MAP_SEPARATOR);
		} else {
			structureCtxDNs = structureCtxDN;
		}
		out.put(option_structureCtxDN, structureCtxDNs);

		out.put(option_userLocaleAttr, userLocaleAttr);
		out.put(option_useDefaultParams, useDefaultParams);
		out.put(option_groupDescriptionAttribute, groupDescriptionAttribute);
		out.put(option_group2GroupTokenAttributeIsDN, group2GroupTokenAttributeIsDN);
		out.put(option_logLevel, logLevel);
		out.put(option_cacheDynamicParametersPath, cacheDynamicParametersPath);

		if (StringUtils.isNotBlank(credentialEncoderClass)) {
			out.put(option_credentialEncoderClass, credentialEncoderClass);
		}

		return out;
	}

	/**
	 * 
	 * @param oList
	 * @param separator
	 * @return
	 */
	private String transformOptionsList2String(OptionsList oList, String separator) {
		String oListString;
		StringBuilder sb = new StringBuilder();
		List<String> ops = oList.getOption();
		if (ops != null && !ops.isEmpty()) {
			int i = 0;
			for (String element : ops) {
				if (i != 0) {
					sb.append(separator);
				}
				sb.append(element);
				i++;
			}
		}
		oListString = sb.toString();
		return oListString;
	}

	/**
	 * @return the {@link #providerUrl}
	 */
	public String getProviderUrl() {
		return providerUrl;
	}

	/**
	 * @param providerUrl
	 *            the {@link #providerUrl} to set
	 */
	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}

	/**
	 * @return the {@link #bindDN}
	 */
	public String getBindDN() {
		return bindDN;
	}

	/**
	 * @param bindDN
	 *            the {@link #bindDN} to set
	 */
	public void setBindDN(String bindDN) {
		this.bindDN = bindDN;
	}

	/**
	 * @return the {@link #bindCredential}
	 */
	public String getBindCredential() {
		return bindCredential;
	}

	/**
	 * @param bindCredential
	 *            the {@link #bindCredential} to set
	 */
	public void setBindCredential(String bindCredential) {
		this.bindCredential = bindCredential;
	}

	/**
	 * @return the {@link #baseCtxDN}
	 */
	public String getBaseCtxDN() {
		return baseCtxDN;
	}

	/**
	 * @param baseCtxDN
	 *            the {@link #baseCtxDN} to set
	 */
	public void setBaseCtxDN(String baseCtxDN) {
		this.baseCtxDN = baseCtxDN;
	}

	/**
	 * @return the {@link #baseFilter}
	 */
	public String getBaseFilter() {
		return baseFilter;
	}

	/**
	 * @param baseFilter
	 *            the {@link #baseFilter} to set
	 */
	public void setBaseFilter(String baseFilter) {
		this.baseFilter = baseFilter;
	}

	/**
	 * @return the {@link #rolesCtxDN}
	 */
	public String getRolesCtxDN() {
		return rolesCtxDN;
	}

	/**
	 * @param rolesCtxDN
	 *            the {@link #rolesCtxDN} to set
	 */
	public void setRolesCtxDN(String rolesCtxDN) {
		this.rolesCtxDN = rolesCtxDN;
	}

	/**
	 * @return the {@link #roleFilter}
	 */
	public String getRoleFilter() {
		return roleFilter;
	}

	/**
	 * @param roleFilter
	 *            the {@link #roleFilter} to set
	 */
	public void setRoleFilter(String roleFilter) {
		this.roleFilter = roleFilter;
	}

	/**
	 * @return the {@link #roleAttributeID}
	 */
	public String getRoleAttributeID() {
		return roleAttributeID;
	}

	/**
	 * @param roleAttributeID
	 *            the {@link #roleAttributeID} to set
	 */
	public void setRoleAttributeID(String roleAttributeID) {
		this.roleAttributeID = roleAttributeID;
	}

	/**
	 * @return the {@link #roleAttributeIsDN}
	 */
	public String getRoleAttributeIsDN() {
		return roleAttributeIsDN;
	}

	/**
	 * @param roleAttributeIsDN
	 *            the {@link #roleAttributeIsDN} to set
	 */
	public void setRoleAttributeIsDN(String roleAttributeIsDN) {
		this.roleAttributeIsDN = roleAttributeIsDN;
	}

	/**
	 * @return the {@link #roleNameAttributeID}
	 */
	public String getRoleNameAttributeID() {
		return roleNameAttributeID;
	}

	/**
	 * @param roleNameAttributeID
	 *            the {@link #roleNameAttributeID} to set
	 */
	public void setRoleNameAttributeID(String roleNameAttributeID) {
		this.roleNameAttributeID = roleNameAttributeID;
	}

	/**
	 * @return the {@link #roleRecursion}
	 */
	public String getRoleRecursion() {
		return roleRecursion;
	}

	/**
	 * @param roleRecursion
	 *            the {@link #roleRecursion} to set
	 */
	public void setRoleRecursion(String roleRecursion) {
		this.roleRecursion = roleRecursion;
	}

	/**
	 * @return the {@link #usersFilter}
	 */
	public String getUsersFilter() {
		return usersFilter;
	}

	/**
	 * @param usersFilter
	 *            the {@link #usersFilter} to set
	 */
	public void setUsersFilter(String usersFilter) {
		this.usersFilter = usersFilter;
	}

	/**
	 * @return the {@link #userNameAttribute}
	 */
	public String getUserNameAttribute() {
		return userNameAttribute;
	}

	/**
	 * @param userNameAttribute
	 *            the {@link #userNameAttribute} to set
	 */
	public void setUserNameAttribute(String userNameAttribute) {
		this.userNameAttribute = userNameAttribute;
	}

	/**
	 * @return the {@link #userDisplayNameAttribute}
	 */
	public String getUserDisplayNameAttribute() {
		return userDisplayNameAttribute;
	}

	/**
	 * @param userDisplayNameAttribute
	 *            the {@link #userDisplayNameAttribute} to set
	 */
	public void setUserDisplayNameAttribute(String userDisplayNameAttribute) {
		this.userDisplayNameAttribute = userDisplayNameAttribute;
	}

	/**
	 * @return the {@link #userEmailAttribute}
	 */
	public String getUserEmailAttribute() {
		return userEmailAttribute;
	}

	/**
	 * @param userEmailAttribute
	 *            the {@link #userEmailAttribute} to set
	 */
	public void setUserEmailAttribute(String userEmailAttribute) {
		this.userEmailAttribute = userEmailAttribute;
	}

	/**
	 * @return the {@link #userDNAttribute}
	 */
	public String getUserDNAttribute() {
		return userDNAttribute;
	}

	/**
	 * @param userDNAttribute
	 *            the {@link #userDNAttribute} to set
	 */
	public void setUserDNAttribute(String userDNAttribute) {
		this.userDNAttribute = userDNAttribute;
	}

	/**
	 * @return the {@link #userPrimaryGroupAttribute}
	 */
	public String getUserPrimaryGroupAttribute() {
		return userPrimaryGroupAttribute;
	}

	/**
	 * @param userPrimaryGroupAttribute
	 *            the {@link #userPrimaryGroupAttribute} to set
	 */
	public void setUserPrimaryGroupAttribute(String userPrimaryGroupAttribute) {
		this.userPrimaryGroupAttribute = userPrimaryGroupAttribute;
	}

	/**
	 * @return the {@link #userPrimaryGroupFilter}
	 */
	public String getUserPrimaryGroupFilter() {
		return userPrimaryGroupFilter;
	}

	/**
	 * @param userPrimaryGroupFilter
	 *            the {@link #userPrimaryGroupFilter} to set
	 */
	public void setUserPrimaryGroupFilter(String userPrimaryGroupFilter) {
		this.userPrimaryGroupFilter = userPrimaryGroupFilter;
	}

	/**
	 * @return the {@link #userObjectClasses}
	 */
	public String getUserObjectClasses() {
		return userObjectClasses;
	}

	/**
	 * @param userObjectClasses
	 *            the {@link #userObjectClasses} to set
	 */
	public void setUserObjectClasses(String userObjectClasses) {
		this.userObjectClasses = userObjectClasses;
	}

	/**
	 * @return the {@link #groupsFilter}
	 */
	public String getGroupsFilter() {
		return groupsFilter;
	}

	/**
	 * @param groupsFilter
	 *            the {@link #groupsFilter} to set
	 */
	public void setGroupsFilter(String groupsFilter) {
		this.groupsFilter = groupsFilter;
	}

	/**
	 * @return the {@link #groupNameAttribute}
	 */
	public String getGroupNameAttribute() {
		return groupNameAttribute;
	}

	/**
	 * @param groupNameAttribute
	 *            the {@link #groupNameAttribute} to set
	 */
	public void setGroupNameAttribute(String groupNameAttribute) {
		this.groupNameAttribute = groupNameAttribute;
	}

	/**
	 * @return the {@link #groupDisplayNameAttribute}
	 */
	public String getGroupDisplayNameAttribute() {
		return groupDisplayNameAttribute;
	}

	/**
	 * @param groupDisplayNameAttribute
	 *            the {@link #groupDisplayNameAttribute} to set
	 */
	public void setGroupDisplayNameAttribute(String groupDisplayNameAttribute) {
		this.groupDisplayNameAttribute = groupDisplayNameAttribute;
	}

	/**
	 * @return the {@link #groupMemberAttribute}
	 */
	public String getGroupMemberAttribute() {
		return groupMemberAttribute;
	}

	/**
	 * @param groupMemberAttribute
	 *            the {@link #groupMemberAttribute} to set
	 */
	public void setGroupMemberAttribute(String groupMemberAttribute) {
		this.groupMemberAttribute = groupMemberAttribute;
	}

	/**
	 * @return the {@link #group2GroupTokenAttribute}
	 */
	public String getGroup2GroupTokenAttribute() {
		return group2GroupTokenAttribute;
	}

	/**
	 * @param group2GroupTokenAttribute
	 *            the {@link #group2GroupTokenAttribute} to set
	 */
	public void setGroup2GroupTokenAttribute(String group2GroupTokenAttribute) {
		this.group2GroupTokenAttribute = group2GroupTokenAttribute;
	}

	/**
	 * @return the {@link #groupDNAttribute}
	 */
	public String getGroupDNAttribute() {
		return groupDNAttribute;
	}

	/**
	 * @param groupDNAttribute
	 *            the {@link #groupDNAttribute} to set
	 */
	public void setGroupDNAttribute(String groupDNAttribute) {
		this.groupDNAttribute = groupDNAttribute;
	}

	/**
	 * @return the {@link #groupObjectClasses}
	 */
	public String getGroupObjectClasses() {
		return groupObjectClasses;
	}

	/**
	 * @param groupObjectClasses
	 *            the {@link #groupObjectClasses} to set
	 * 
	 */
	public void setGroupObjectClasses(String groupObjectClasses) {
		this.groupObjectClasses = groupObjectClasses;
	}

	/**
	 * @return the {@link #extendedAttributies}
	 * @deprecated Mechanizm dodatkowego mapowania miał zastosowanie tylko i
	 *             wyłącznie w IBM BPM
	 */
	@Deprecated
	public String getExtendedAttributies() {
		return extendedAttributies;
	}

	/**
	 * @param extendedAttributies
	 *            the {@link #extendedAttributies} to set
	 * @deprecated Mechanizm dodatkowego mapowania miał zastosowanie tylko i
	 *             wyłącznie w IBM BPM
	 */
	@Deprecated
	public void setExtendedAttributies(String extendedAttributies) {
		this.extendedAttributies = extendedAttributies;
	}

	/**
	 * @return the {@link #syncPeriod}
	 */
	public String getSyncPeriod() {
		return syncPeriod;
	}

	/**
	 * @param syncPeriod
	 *            the {@link #syncPeriod} to set
	 */
	public void setSyncPeriod(String syncPeriod) {
		this.syncPeriod = syncPeriod;
	}

	/**
	 * @return the {@link #defaultTaskNotification}
	 */
	public String getDefaultTaskNotification() {
		return defaultTaskNotification;
	}

	/**
	 * @param defaultTaskNotification
	 *            the {@link #defaultTaskNotification} to set
	 */
	public void setDefaultTaskNotification(String defaultTaskNotification) {
		this.defaultTaskNotification = defaultTaskNotification;
	}

	/**
	 * @return the {@link #defaultAlertOnAssignAndRun}
	 */
	public String getDefaultAlertOnAssignAndRun() {
		return defaultAlertOnAssignAndRun;
	}

	/**
	 * @param defaultAlertOnAssignAndRun
	 *            the {@link #defaultAlertOnAssignAndRun} to set
	 */
	public void setDefaultAlertOnAssignAndRun(String defaultAlertOnAssignAndRun) {
		this.defaultAlertOnAssignAndRun = defaultAlertOnAssignAndRun;
	}

	/**
	 * @return the {@link #userGroupOptionsAreDefinded}
	 */
	public String getUserGroupOptionsAreDefinded() {
		return userGroupOptionsAreDefinded;
	}

	/**
	 * @param userGroupOptionsAreDefinded
	 *            the {@link #userGroupOptionsAreDefinded} to set
	 */
	public void setUserGroupOptionsAreDefinded(String userGroupOptionsAreDefinded) {
		this.userGroupOptionsAreDefinded = userGroupOptionsAreDefinded;
	}

	/**
	 * @return the {@link #ldapResultPageSize}
	 */
	public String getLdapResultPageSize() {
		return ldapResultPageSize;
	}

	/**
	 * @param ldapResultPageSize
	 *            the {@link #ldapResultPageSize} to set
	 */
	public void setLdapResultPageSize(String ldapResultPageSize) {
		this.ldapResultPageSize = ldapResultPageSize;
	}

	/**
	 * @return the {@link #usersOrganizationalUnitNames}
	 * @deprecated Metoda powstała ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	public String getUsersOrganizationalUnitNames() {
		return usersOrganizationalUnitNames;
	}

	/**
	 * @param usersOrganizationalUnitNames
	 *            the {@link #usersOrganizationalUnitNames} to set
	 * @deprecated Metoda powstała ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	public void setUsersOrganizationalUnitNames(String usersOrganizationalUnitNames) {
		this.usersOrganizationalUnitNames = usersOrganizationalUnitNames;
	}

	/**
	 * @return the {@link #groupsOrganizationalUnitNames}
	 * @deprecated Metoda powstała ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	public String getGroupsOrganizationalUnitNames() {
		return groupsOrganizationalUnitNames;
	}

	/**
	 * @param groupsOrganizationalUnitNames
	 *            the {@link #groupsOrganizationalUnitNames} to set
	 * @deprecated Metoda powstała ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	public void setGroupsOrganizationalUnitNames(String groupsOrganizationalUnitNames) {
		this.groupsOrganizationalUnitNames = groupsOrganizationalUnitNames;
	}

	/**
	 * @return the {@link #structureCtxDN}
	 * @deprecated Metoda powstała ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	public String getStructureCtxDN() {
		return structureCtxDN;
	}

	/**
	 * @param structureCtxDN
	 *            the {@link #structureCtxDN} to set
	 * @deprecated Metoda powstała ze względu na możliwość obsługi wielu
	 *             repozytoriów LDAP w IBM BPM. W obecnej implementacji jest zbędna.
	 *             Trzeba pomyśleć o zmianie w konfiguracji.
	 */
	@Deprecated
	public void setStructureCtxDN(String structureCtxDN) {
		this.structureCtxDN = structureCtxDN;
	}

	/**
	 * @return the {@link #userLocaleAttr}
	 */
	public String getUserLocaleAttr() {
		return userLocaleAttr;
	}

	/**
	 * @param userLocaleAttr
	 *            the {@link #userLocaleAttr} to set
	 */
	public void setUserLocaleAttr(String userLocaleAttr) {
		this.userLocaleAttr = userLocaleAttr;
	}

	/**
	 * @return the {@link #useDefaultParams}
	 */
	public String getUseDefaultParams() {
		return useDefaultParams;
	}

	/**
	 * @param useDefaultParams
	 *            the {@link #useDefaultParams} to set
	 */
	public void setUseDefaultParams(String useDefaultParams) {
		this.useDefaultParams = useDefaultParams;
	}

	/**
	 * @return the {@link #groupDescriptionAttribute}
	 */
	public String getGroupDescriptionAttribute() {
		return groupDescriptionAttribute;
	}

	/**
	 * @param groupDescriptionAttribute
	 *            the {@link #groupDescriptionAttribute} to set
	 */
	public void setGroupDescriptionAttribute(String groupDescriptionAttribute) {
		this.groupDescriptionAttribute = groupDescriptionAttribute;
	}

	/**
	 * @return the {@link #group2GroupTokenAttributeIsDN}
	 */
	public String getGroup2GroupTokenAttributeIsDN() {
		return group2GroupTokenAttributeIsDN;
	}

	/**
	 * @param group2GroupTokenAttributeIsDN
	 *            the {@link #group2GroupTokenAttributeIsDN} to set
	 */
	public void setGroup2GroupTokenAttributeIsDN(String group2GroupTokenAttributeIsDN) {
		this.group2GroupTokenAttributeIsDN = group2GroupTokenAttributeIsDN;
	}

	/**
	 * @return the {@link #logLevel}
	 * @deprecated zmieniono mechanizmy logowania operacji.
	 */
	@Deprecated
	public String getLogLevel() {
		return logLevel;
	}

	/**
	 * @param logLevel
	 *            the {@link #logLevel} to set
	 * @deprecated zmieniono mechanizmy logowania operacji.
	 */
	@Deprecated
	public void setLogLevel(String logLevel) {
		this.logLevel = logLevel;
	}

	/**
	 * @return the {@link #cacheDynamicParametersPath}
	 */
	public String getCacheDynamicParametersPath() {
		return cacheDynamicParametersPath;
	}

	/**
	 * @param cacheDynamicParametersPath
	 *            the {@link #cacheDynamicParametersPath} to set
	 */
	public void setCacheDynamicParametersPath(String cacheDynamicParametersPath) {
		this.cacheDynamicParametersPath = cacheDynamicParametersPath;
	}

	/**
	 * @return the {@link #usersOrganizationalUnitNamesOptions}
	 */
	public OptionsList getUsersOrganizationalUnitNamesOptions() {
		return usersOrganizationalUnitNamesOptions;
	}

	/**
	 * @param usersOrganizationalUnitNamesOptions
	 *            the {@link #usersOrganizationalUnitNamesOptions} to set
	 */
	public void setUsersOrganizationalUnitNamesOptions(OptionsList usersOrganizationalUnitNamesOptions) {
		this.usersOrganizationalUnitNamesOptions = usersOrganizationalUnitNamesOptions;
	}

	/**
	 * @return the {@link #groupsOrganizationalUnitNamesOptions}
	 */
	public OptionsList getGroupsOrganizationalUnitNamesOptions() {
		return groupsOrganizationalUnitNamesOptions;
	}

	/**
	 * @param groupsOrganizationalUnitNamesOptions
	 *            the {@link #groupsOrganizationalUnitNamesOptions} to set
	 */
	public void setGroupsOrganizationalUnitNamesOptions(OptionsList groupsOrganizationalUnitNamesOptions) {
		this.groupsOrganizationalUnitNamesOptions = groupsOrganizationalUnitNamesOptions;
	}

	/**
	 * @return the {@link #structureCtxDNOptions}
	 */
	public OptionsList getStructureCtxDNOptions() {
		return structureCtxDNOptions;
	}

	/**
	 * @param structureCtxDNOptions
	 *            the {@link #structureCtxDNOptions} to set
	 */
	public void setStructureCtxDNOptions(OptionsList structureCtxDNOptions) {
		this.structureCtxDNOptions = structureCtxDNOptions;
	}

	/**
	 * @return the {@link #extendedAttributiesOptions}
	 */
	public OptionsList getExtendedAttributiesOptions() {
		return extendedAttributiesOptions;
	}

	/**
	 * @param extendedAttributiesOptions
	 *            the {@link #extendedAttributiesOptions} to set
	 */
	public void setExtendedAttributiesOptions(OptionsList extendedAttributiesOptions) {
		this.extendedAttributiesOptions = extendedAttributiesOptions;
	}

	/**
	 * @return the {@link #credentialEncoderClass}
	 */
	public String getCredentialEncoderClass() {
		return credentialEncoderClass;
	}

	/**
	 * @param credentialEncoderClass
	 *            the {@link #credentialEncoderClass} to set
	 */
	public void setCredentialEncoderClass(String credentialEncoderClass) {
		this.credentialEncoderClass = credentialEncoderClass;
	}

}
