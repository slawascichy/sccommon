package pl.slawas.common.ldap.dao;

import pl.slawas.common.ldap.beans.LdapUserGroup;
import pl.slawas.common.ldap.provider.LdapObjectFactoryBean;
import pl.slawas.common.ldap.provider.ProviderOptions;

/**
 * 
 * LdapUserGroupAO
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LdapUserGroupAO extends LdapUserGroupAOSupport<LdapUserGroup> {

	private static final long serialVersionUID = 715770566979293239L;

	public LdapUserGroupAO(ProviderOptions ldapOptions, String organizationalUnitName) {
		super(ldapOptions, organizationalUnitName);
	}

	public LdapUserGroupAO(ProviderOptions ldapOptions, String organizationalUnitName, String[] additionalAttrs) {
		super(ldapOptions, organizationalUnitName, additionalAttrs);
	}

	/**
	 * Utworzenie nowej instancji obiektu reprezentującego entry grupy
	 * 
	 * @param login
	 *            nazwa grupy
	 * @return nowa instancja obiektu reprezentującego entry użytkownika
	 */
	protected LdapUserGroup createNewInstance(String name) {
		return new LdapUserGroup(name, this.getOrganizationalUnitName(),
				LdapObjectFactoryBean.getInstance().getManagerGroupSuffix(),
				LdapObjectFactoryBean.getInstance().getTeamGroupSuffix());
	}

	/* Overridden (non-Javadoc) */
	@Override
	protected String getEntryNameAttribute() {
		return getLo().getGroupNameAttribute();
	}
}
