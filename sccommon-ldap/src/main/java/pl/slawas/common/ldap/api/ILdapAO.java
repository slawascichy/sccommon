package pl.slawas.common.ldap.api;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import pl.slawas.common.ldap.provider.ProviderOptions;

/**
 * 
 * WpLdapAO
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 * @param <T>
 */
public interface ILdapAO<T extends ILdapEntry> extends Serializable, ILdapConnectionFactory {

	/**
	 * Loading object form database
	 * 
	 * @param id
	 *            unique value eg. primary key value or unique key value
	 * @return object, jeżeli obiekt nie zostanie znaleziony zwrócona zostanie
	 *         wartość {@code null}
	 * @throws SQLException
	 */
	T load(Object id) throws NamingException;

	/**
	 * Lista wszystkich elementów znalezionych w LDAP.
	 * 
	 * @return lista elementów
	 * @throws NamingException
	 */
	List<T> loadAll() throws NamingException;

	/**
	 * Załadowanie wszystkich znalezionych elementów jako mapę, gdzie kluczem jest
	 * pole z unikalną wartością jednozancznie określającą unikalność obiektu &lt;T>
	 * 
	 * @return mapa znalezionych elementów.
	 * @throws NamingException
	 */
	Map<String, T> loadAllAsMap() throws NamingException;

	/**
	 * Załadowanie wszystkich znalezionych elementów i dodanie ich do istniejącej
	 * mapy występującej w argumencie wejściowym. Kluczem jest pole z unikalną
	 * wartością jednoznacznie określającą unikalność obiektu &lt;T>
	 * 
	 * @param target
	 *            istniejąca mapa elementów
	 * @return zmieniona mapa elementów
	 * @throws NamingException
	 */
	Map<String, T> mergeAllAsMap(Map<String, T> target) throws NamingException;

	void close();

	/**
	 * Entry może pochodzić z różnych jednostek organizacyjnych zdefiniowanych w
	 * {@link ProviderOptions#getUsersOrganizationalUnitNames()} (dla użytkowników)
	 * lub {@link ProviderOptions#getGroupsOrganizationalUnitNames()} (dla grup).
	 * Informacja o tym z jakiej jednostki pochodzi entry powinna zostać przekazana
	 * do obiektu w celu optymalizacji ścieżki bazowej wyszukiwania konkretnych
	 * danych.
	 * 
	 * @return the {@link #organizationalUnitName}
	 */
	String getOrganizationalUnitName();

	/**
	 * @return obiekt wykorzystywanych przez instancję dostępu do danych parametrów.
	 */
	ProviderOptions getLo();

	/**
	 * Lista atrybutów entry wyznaczona na podstawie konfiguracji security
	 * provider'a wraz z atrybutami dodatkowymi, których dodanie jest możliwe w
	 * sposób programistyczny - np. atrybuty związanie z datą ostatniej modyfikacji,
	 * czy też z policy haseł.
	 * 
	 * @return lista atrybutów entry
	 */
	String[] getAttrs();

	/**
	 * lista atrybutów dodatkowych entry, których dodanie jest możliwe w sposób
	 * programistyczny - np. atrybuty związanie z datą ostatniej modyfikacji, czy
	 * też z policy haseł.
	 * 
	 * @return lista dodatkowych atrybutów entry
	 */
	String[] getAdditionalAttrs();

	/**
	 * Wyszukiwanie entry na podstawie zadanego filtru LDAP
	 * 
	 * @param searchFilter
	 *            filtr wyszukiwania
	 * @return lista znalezionych obiektów
	 * @throws NamingException
	 */
	List<T> loadByFilter(String searchFilter) throws NamingException;

	/**
	 * Dodawanie obiektu do LDAP.
	 * 
	 * @param ldapEntry
	 *            dodawany obiekt LDAP
	 * @return
	 * @throws NamingException
	 */
	T insert(ILdapEntry4Changes ldapEntry) throws NamingException;

	/**
	 * Modyfikacja obiektu w LDAP
	 * 
	 * @param ldapEntry
	 *            modyfikowany obiekt LDAP
	 * @return zmodyfikowany obiekt LDAP
	 * @throws NamingException
	 */
	T update(ILdapEntry4Changes ldapEntry) throws NamingException;

	/**
	 * Usunięcie obiektu z LDAP
	 * 
	 * @param entryDN
	 *            DN usuwanego entry
	 * @throws NamingException
	 */
	void delete(String entryDN) throws NamingException;

	/**
	 * Pobranie pomocniczej listy wszystkich atrybutów do weryfikacji czy dany
	 * atrybut został pobrany razem z entry
	 */
	Set<String> getAttrSet();

	/**
	 * Wyszukiwanie pojedynczego entry użytkownika na podstawie filtru LDAP
	 * 
	 * @param searchFilter
	 *            filtr LDAP na podstawie którego zrealizowane będzie wyszukiwanie
	 *            pojedynczego entry użytkownika.
	 * @return
	 * @throws NamingException
	 */
	T loadBySearchFilter(String searchFilter) throws NamingException;

	/**
	 * Lista elementów znalezionych w LDAP, na podstawie wildakrd'owego wyszukiwania
	 * użytkowników po ich nazwach.
	 * 
	 * @param wildcardCredentials
	 *            dodatkowe kryteria wyszukiwania użytkowników po ich nazwach
	 * @return lista elementów
	 * @throws NamingException
	 */
	List<T> loadByWildcard(String wildcardCredentials) throws NamingException;

	/**
	 * Lista elementów znalezionych w LDAP, na podstawie wildakrd'owego wyszukiwania
	 * użytkowników po ich nazwach.
	 * 
	 * @param wildcardCredentials
	 *            dodatkowe kryteria wyszukiwania użytkowników po ich nazwach
	 * @return mapa elementów
	 * @throws NamingException
	 */
	Map<String, T> loadByWildcardAsMap(String wildcardCredentials) throws NamingException;

}
