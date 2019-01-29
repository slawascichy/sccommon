package pl.slawas.common.ldap.utils;

import pl.slawas.common.ldap.api.ILdapBaseEntry;
import pl.slawas.common.ldap.api.ILdapEntry4Changes;
import pl.slawas.common.ldap.beans.LdapEntry4ChangesSupport;
import pl.slawas.common.ldap.dao.LdapUserAO;

public class EntryUtils {

	private EntryUtils() {
	}

	/**
	 * Domyślna transformacja do instancji będącej podstawą do realizacji zmian. Aby
	 * przygotować instancję do zmian wystarczy mi, że będzie ona spełniała
	 * interfejs {@link ILdapBaseEntry} - <b>powstała instancja będzie miała pustą
	 * mapę zmian</b>.
	 * 
	 * @see LdapUserAO#changePassword(String, String, byte[], byte[])
	 * @see LdapUserAO#changePassword(String, String, String, String)
	 * 
	 * @param ldapEntry
	 *            przekształcana instancja entry użytkownika.
	 * 
	 * @return instancja obiektu pod modyfikację z pustą mapą zmian (tylko
	 *         podstawowe dane {@link ILdapBaseEntry}).
	 */
	public static ILdapEntry4Changes transform4Changes(ILdapBaseEntry ldapEntry) {
		if (ldapEntry instanceof ILdapEntry4Changes) {
			return (ILdapEntry4Changes) ldapEntry;
		}
		ILdapEntry4Changes entry4Changes = new LdapEntry4ChangesSupport(ldapEntry.getName(),
				ldapEntry.getOrgUnitName()) {
			private static final long serialVersionUID = -829323449675905405L;
		};
		entry4Changes.setDn(ldapEntry.getDn());
		return entry4Changes;
	}

}
