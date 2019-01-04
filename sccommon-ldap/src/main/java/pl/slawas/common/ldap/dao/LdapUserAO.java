package pl.slawas.common.ldap.dao;

import java.util.List;

import pl.slawas.common.ldap.api.ILdapUserGroupAO;
import pl.slawas.common.ldap.beans.LdapUser;
import pl.slawas.common.ldap.beans.LdapUserGroup;
import pl.slawas.common.ldap.provider.ProviderOptions;

/**
 * 
 * LdapUserAO
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public class LdapUserAO extends LdapUserAOSupport<LdapUser, LdapUserGroup> {

	private static final long serialVersionUID = -5039858611173250535L;

	public LdapUserAO(ProviderOptions ldapOptions, String organizationalUnitName,
			List<ILdapUserGroupAO<LdapUserGroup>> groupLdapAO) {
		super(ldapOptions, organizationalUnitName, groupLdapAO);
	}

	public LdapUserAO(ProviderOptions ldapOptions, String organizationalUnitName,
			List<ILdapUserGroupAO<LdapUserGroup>> groupLdapAO, String[] additionalAttrs) {
		super(ldapOptions, organizationalUnitName, groupLdapAO, additionalAttrs);
	}

	/**
	 * Utworzenie nowej instancji obiektu reprezentującego entry użytkownika
	 * 
	 * @param login
	 *            nazwa użytkownika
	 * @return nowa instancja obiektu reprezentującego entry użytkownika
	 */
	protected LdapUser createNewInstance(String login) {
		return new LdapUser(login, this.getOrganizationalUnitName());
	}

	/* Overridden (non-Javadoc) */
	@Override
	protected String getEntryNameAttribute() {
		return getLo().getUserNameAttribute();
	}

}
