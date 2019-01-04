package pl.slawas.common.ldap.api;

import java.io.Serializable;

import pl.slawas.common.ldap.provider.ProviderOptions;

/**
 * 
 * _LdapBaseEntry - bardzo podstawowe dane o entry
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public interface ILdapBaseEntry extends Serializable {

	/**
	 * @return the name - nazwa dla entry
	 */
	String getName();

	/**
	 * Ustawianie nazwy dla entry.
	 * 
	 * @param name
	 *            nazwa entry
	 */
	void setName(String name);

	/**
	 * @return the distinguishedName - unikalny klucz dla entry - najczęściej jest
	 *         to adres liścia lokalizacji entry w drzewie LDAP.
	 */
	String getDn();

	/**
	 * Ustawianie unikalnego klucza dla entry - najczęściej jest to adres liścia
	 * lokalizacji entry w drzewie LDAP.
	 * 
	 * @param distinguishedName
	 *            unikalna nazwa entry (klucz).
	 */
	void setDn(String distinguishedName);

	/**
	 * Entry może pochodzić z różnych jednostek organizacyjnych zdefiniowanych w
	 * {@link ProviderOptions#getUsersOrganizationalUnitNames()} (dla użytkowników)
	 * lub {@link ProviderOptions#getGroupsOrganizationalUnitNames()} (dla grup).
	 * Informacja o tym z jakiej jednostki pochodzi entry powinna zostać przekazana
	 * do obiektu w celu optymalizacji ścieżki bazowej wyszukiwania konkretnych
	 * danych.
	 * 
	 * Metoda zwraca dane o jednostce organizacyjnej.
	 * 
	 * @see ILdapAO#getOrganizationalUnitName()
	 * 
	 * @return informacja o jednostce organizacyjnej
	 */
	String getOrgUnitName();
}
