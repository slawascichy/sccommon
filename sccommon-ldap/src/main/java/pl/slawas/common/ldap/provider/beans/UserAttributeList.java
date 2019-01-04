package pl.slawas.common.ldap.provider.beans;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * UserAttributeList - obiekt listy atrybutów użytkownika
 * 
 * Uwaga! na przeciążenie znaczenia atrybutów LDAP - w rzeczywistości to powinna
 * być lista atrybutów z BPM'a. https://support.ibpm.pro:8443/browse/BPMINT-79
 * 
 * Przymykam oko na błędy w konfiguracji związane z brakiem unikalności pośród
 * atrybutów BPM.
 * 
 * 
 * @author Sławomir Cichy &lt;scichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class UserAttributeList implements Serializable {

	private static final long serialVersionUID = -695077998260486783L;

	/** Mapa nazw atrybutów BPM na obiekty atrybutów użytkownika */
	private Map<String, UserAttributeDefinition> userExtendedAttributiesTW2LDAP = new Hashtable<String, UserAttributeDefinition>();

	/** Lista atrybutów użytkownika w LDAP */
	private Set<String> ldapAttrNames = new HashSet<String>();

	/**
	 * Metoda dodająca definicję atrybutu użytkownika
	 * 
	 * @param attribute
	 */
	public void addUserAttrDefinition(UserAttributeDefinition attribute) {
		String twAttrName = attribute.getTwAttrName();
		this.userExtendedAttributiesTW2LDAP.put(twAttrName, attribute);
		this.ldapAttrNames.add(attribute.getLdapAttrName());
	}

	/**
	 * Czy lista jest pusta?
	 * 
	 * @return {@code true}|{@code false}
	 */
	public boolean isEmpty() {
		return (this.userExtendedAttributiesTW2LDAP == null);
	}

	/**
	 * Pobranie definicji atrybutu na podstawie nazwy atrybutu w TW
	 * 
	 * @param ldapAttrName
	 *            nazwa atrybutu w TW
	 * @return definicja atrybutu użytkownika
	 */
	public UserAttributeDefinition getUserAttrByTwName(String twAttrName) {
		return this.userExtendedAttributiesTW2LDAP.get(twAttrName);
	}

	/**
	 * Lista nazw atrybutów w TW
	 * 
	 * @return lista nazw atrybutów w TW
	 */
	public Enumeration<String> getTwAttrNames() {
		return ((Hashtable<String, UserAttributeDefinition>) this.userExtendedAttributiesTW2LDAP)
				.keys();
	}

	/**
	 * @return the {@link #ldapAttrNames}
	 */
	public Set<String> getLdapAttrNames() {
		return ldapAttrNames;
	}

	/**
	 * W tym przypadku wyciagamy pierwszą z brzegu definicję atrybutu
	 * odpowiadającego atrybutowi LDAP - szukamy po wartościach w {@link
	 * this.userExtendedAttributiesTW2LDAP}
	 * 
	 * @param ldapAttrName
	 * @return
	 */
	public UserAttributeDefinition getUserAttrByLdapName(String ldapAttrName) {
		Set<Entry<String, UserAttributeDefinition>> entries = this.userExtendedAttributiesTW2LDAP
				.entrySet();

		for (Entry<String, UserAttributeDefinition> entry : entries) {
			UserAttributeDefinition uad = entry.getValue();
			if (uad != null
					&& ldapAttrName.equalsIgnoreCase(uad.getLdapAttrName())) {
				/* Znalazłem, zatem zwracam znalezioną definicję atrybutu */
				return uad;
			}
		}
		return null;
	}

}
