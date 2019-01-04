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

package pl.slawas.common.ldap.beans;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

import pl.slawas.common.ldap.api.ILdapAttribute;
import pl.slawas.common.ldap.api.ILdapEntry4Changes;
import pl.slawas.common.ldap.api.ILdapUserGroup;
import pl.slawas.common.ldap.api.IUserAttribute;
import pl.slawas.common.ldap.provider.ProviderOptions;

/**
 * LdapUserGroup - implementacja obiektu reprezentującego entry grupy.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LdapUserGroup extends LdapEntrySupport implements ILdapUserGroup, ILdapEntry4Changes {

	private static final long serialVersionUID = 6900485007409600915L;

	/**
	 * W celu zbudowania drzewa hierarchii organizacji potrzebne jest pole
	 * wskazujące na jednoznacznego rodzica. Może być problem, gdy grupa jest
	 * członkiem wielu grup. Aby rozwiązać ten problem robimy założenie, że właściwa
	 * grupa (dla której budowane jest drzewo hierarchii, znajduje się tej samej
	 * gałęzi.
	 */
	private String mainMemberOf;

	private Set<String> otherMembersOf;

	private Set<String> members;

	private String description;

	private String managerGroupName;

	private final String managerSuffix;

	private final String workerSuffix;

	/**
	 * Czy grupa/rola jest zdefiniowana jako jednostka struktury firmy? Pole
	 * ustawiane w {@link UserGroupLdapAO} na podstawie parametru
	 * {@link ProviderOptions#getWpStructureCtxDN()} oraz {@link #dn} grupy.
	 * Domyślna wartość {@code false}.
	 * 
	 * @see ProviderOptions#checkStructureUnit(String)
	 */
	private boolean isStructureUnit = false;

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

	/**
	 * Podstawowy konstruktor.
	 * 
	 * @param name
	 *            nazwa grupy
	 * @param orgUnitName
	 *            jednostka organizacyjna entry {@link #getOrgUnitName()}
	 * @param managerSuffix
	 *            suffix nazwy dla grupy manager'a
	 * @param workerSuffix
	 *            suffix nazwy dla grupy zespołu
	 */
	public LdapUserGroup(String name, String orgUnitName, String managerSuffix, String workerSuffix) {
		super(name, orgUnitName);
		this.managerSuffix = managerSuffix;
		this.workerSuffix = workerSuffix;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public String getMainMemberOf() {
		return mainMemberOf;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setMainMemberOf(String memberOf) {
		this.mainMemberOf = memberOf;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public Set<String> getMembers() {
		return members;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setMembers(Set<String> members) {
		this.members = members;
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

	@Override
	public String toString() {
		return toString(false);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public String toString(boolean showLists) {
		StringBuilder userDescription = new StringBuilder()
				.append("\n  LdapUserGroup:\n --------------------------------------").append("\n DN : ")
				.append((this.getDn() == null ? "unknown" : this.getDn())).append("\n Name : ").append(this.getName())
				.append("\n Display Name : ").append(this.getDisplayName()).append("\n E-mail : ")
				.append(this.getEmail()).append("\n Manager Group Name : ")
				.append((this.managerGroupName == null ? "unknown" : this.managerGroupName)).append("\n Member Of : ")
				.append((this.mainMemberOf == null ? "unknown" : this.mainMemberOf));
		if (showLists) {
			List<IUserAttribute> attributes = this.getAttributes();
			if (attributes != null && !attributes.isEmpty()) {
				userDescription.append("\n Attributes:");
				for (IUserAttribute attr : attributes) {
					userDescription.append("\n\t").append(attr.getName()).append(" : ").append(attr.getValue());
				}
			}
			if (members != null && !members.isEmpty()) {
				userDescription.append("\n Members:");
				for (String attr : members) {
					userDescription.append("\n\t").append(attr);
				}
			}
		}
		userDescription.append("\n --------------------------------------");
		return userDescription.toString();

	}

	/* Overridden (non-Javadoc) */
	@Override
	public String getDescription() {
		return description;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public String getManagerGroupName() {
		if (StringUtils.isBlank(managerGroupName) && isWorkersGroup()) {
			String baseName = getName().substring(0, getName().length() - this.workerSuffix.length());
			return new StrBuilder().append(baseName).append(this.managerSuffix).toString();
		}
		if (StringUtils.isBlank(managerGroupName) && isStructureUnit() && !isWorkersGroup() && !isManagerGroup()) {
			return new StrBuilder().append(getName()).append(this.managerSuffix).toString();
		}
		return managerGroupName;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setManagerGroupName(String managerGroupName) {
		this.managerGroupName = managerGroupName;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isManagerGroup() {
		return getName().endsWith(this.managerSuffix);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isWorkersGroup() {
		return getName().endsWith(this.workerSuffix);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isStructureUnit() {
		return isStructureUnit;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setStructureUnit(boolean isStructureUnit) {
		this.isStructureUnit = isStructureUnit;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + (isStructureUnit ? 1231 : 1237);
		result = prime * result + ((managerGroupName == null) ? 0 : managerGroupName.hashCode());
		result = prime * result + ((managerSuffix == null) ? 0 : managerSuffix.hashCode());
		result = prime * result + ((mainMemberOf == null) ? 0 : mainMemberOf.hashCode());
		result = prime * result + ((members == null) ? 0 : members.hashCode());
		result = prime * result + ((otherMembersOf == null) ? 0 : otherMembersOf.hashCode());
		result = prime * result + ((workerSuffix == null) ? 0 : workerSuffix.hashCode());
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
		LdapUserGroup other = (LdapUserGroup) obj;
		if (StringUtils.isBlank(description)) {
			if (StringUtils.isNotBlank(other.description))
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (isStructureUnit != other.isStructureUnit)
			return false;
		if (StringUtils.isBlank(managerGroupName)) {
			if (StringUtils.isNotBlank(other.managerGroupName))
				return false;
		} else if (!managerGroupName.equals(other.managerGroupName))
			return false;
		if (StringUtils.isBlank(managerSuffix)) {
			if (StringUtils.isNotBlank(other.managerSuffix))
				return false;
		} else if (!managerSuffix.equals(other.managerSuffix))
			return false;
		if (StringUtils.isBlank(mainMemberOf)) {
			if (StringUtils.isNotBlank(other.mainMemberOf))
				return false;
		} else if (!mainMemberOf.equals(other.mainMemberOf))
			return false;
		if (members == null) {
			if (other.members != null)
				return false;
		} else if (!members.equals(other.members))
			return false;
		if (otherMembersOf == null) {
			if (other.otherMembersOf != null)
				return false;
		} else if (!otherMembersOf.equals(other.otherMembersOf))
			return false;
		if (StringUtils.isBlank(workerSuffix)) {
			if (StringUtils.isNotBlank(other.workerSuffix))
				return false;
		} else if (!workerSuffix.equals(other.workerSuffix))
			return false;
		return true;
	}

	/**
	 * @return the {@link #managerSuffix}
	 */
	public String getManagerSuffix() {
		return managerSuffix;
	}

	/**
	 * @return the {@link #workerSuffix}
	 */
	public String getWorkerSuffix() {
		return workerSuffix;
	}

	/**
	 * @return the {@link #otherMembersOf}
	 */
	public Set<String> getOtherMembersOf() {
		return otherMembersOf;
	}

	/**
	 * @param otherMembersOf
	 *            the {@link #otherMembersOf} to set
	 */
	public void setOtherMembersOf(Set<String> otherMembersOf) {
		this.otherMembersOf = otherMembersOf;
	}
}
