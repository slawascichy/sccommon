package pl.slawas.common.ldap.beans;

import java.util.List;
import java.util.Map;

import pl.slawas.common.ldap.api.ILdapAttribute;
import pl.slawas.common.ldap.api.ILdapEntry4Changes;

/**
 * 
 * LdapEntry4ChangesSupport
 *
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl&gt;
 * @version $Revision: 1.1 $
 *
 */
public abstract class LdapEntry4ChangesSupport extends LdapBaseEntrySupport implements ILdapEntry4Changes {

	private static final long serialVersionUID = -6955365592494335886L;

	/**
	 * Mapa zmienionych atrybutów. Zasady ustawiania:
	 * <ul>
	 * <li>dodawanie atrybutu: nowa wartość {@link ILdapAttribute#setValue(Object)},
	 * stara wartość ustawiona na {@code null} za pomocą
	 * {@link ILdapAttribute#setOldValue(Object)}</li>
	 * <li>zmiana atrybutu: nowa wartość {@link ILdapAttribute#setValue(Object)} ,
	 * stara wartość ustawiona za pomocą
	 * {@link ILdapAttribute#setOldValue(Object)}</li>
	 * <li>usunięcie atrybutu: nowa wartość ustawiona na {@code null}
	 * {@link ILdapAttribute#setValue(Object)} , stara wartość ustawiona za pomocą
	 * {@link ILdapAttribute#setOldValue(Object)}</li>
	 * </ul>
	 */
	private Map<String, List<ILdapAttribute>> changesMap;

	public LdapEntry4ChangesSupport(String name, String orgUnitName) {
		super(name, orgUnitName);
	}

	/**
	 * @return the {@link #changesMap}
	 */
	public Map<String, List<ILdapAttribute>> getChangesMap() {
		return changesMap;
	}

	/**
	 * @param changesMap
	 *            the {@link #changesMap} to set
	 */
	public void setChangesMap(Map<String, List<ILdapAttribute>> changesMap) {
		this.changesMap = changesMap;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((changesMap == null) ? 0 : changesMap.hashCode());
		return result;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LdapEntry4ChangesSupport other = (LdapEntry4ChangesSupport) obj;
		if (changesMap == null) {
			if (other.changesMap != null)
				return false;
		} else if (!changesMap.equals(other.changesMap)) {
			return false;
		}
		return true;
	}
}
