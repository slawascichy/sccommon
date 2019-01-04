package pl.slawas.common.ldap.api;

import pl.slawas.common.ldap.provider.ProviderOptions;

/**
 * Stałe na potrzeby działania provider'a
 * 
 * @author slawas
 * 
 */
public class Constants {

	private Constants() {
	}

	/**
	 * Domyślna nazwa atrybutu wskazującego na ścieżkę przechowywania entry w LDAP
	 */
	public static final String DEFAULT_DISTINGUISHED_NAME_ATTR = "distinguishedName";

	/**
	 * pomocnicza wartość do identyfikacji wartości {@code null} w stringu, który
	 * może przyjąć wartość "{@code n/a}" (not allowed)
	 */
	public static final String NOT_ALLOWED = "n/a";

	/**
	 * Domyślna domena dla adresów email.
	 */
	public static final String DEFAULT_EMAIL_SUFFIX = "@scisoftware.pl";
	/**
	 * Domyślne ustawienie flagi związanej z wysyłaniem domyślnych notyfikacji
	 * mailowych przez system IBM BPM.
	 */
	public static final String DEFAULT_TASK_NOTIFICATION = "false";
	/**
	 * Domyślne ustawienie flagi mówiącej o tym, cy użytkownik ma być ostrzegany o
	 * tym, ze pobieraz adanie z grupy.
	 */
	public static final String DEFAULT_ATTR_ALERT_ON_ASSIGN_AND_RUN = "true";
	/**
	 * Domyślna wartość dla opcji: Czy nadpisać ustawienia użytkownika ustawieniami
	 * domyślnymi? Dotyczy tylko parametru
	 * {@link ProviderOptions#getDefaultTaskNotification()}.
	 */
	public static final String DEFAULT_USE_DEFAULT_PARAMS = "false";

	public static final String DEFAULT_USER_GROUP_OPTIONS_ARE_DEFINED = "true";

	public static final String DEFAULT_USER_LOCALE = "pl";

	/**
	 * Następca {@link #PRIMARY_ROLE_ATTRIBUTE_NAME}.
	 */
	public static final String PRIMARY_GROUP_ATTRIBUTE_NAME = "Primary Group";

	/**
	 * Końcówka podstawowej grupy użytkownika będącego członkiem zespołu
	 */
	public static final String DEFAULT_WORKERS_PRIMARY_GROUP_SUFFIX = "-Team";

	/**
	 * Końcówka podstawowej grupy użytkownika będącego managerem zespołu
	 */
	public static final String DEFAULT_MANAGER_PRIMARY_GROUP_SUFFIX = "-Manager";

	/**
	 * pomocnicza wartosc do identyfikacji wartości {@code null} w stringu, który
	 * może przyjąć wartość "{@code null}"
	 */
	public static final String NULL_STRING = "null";

}
