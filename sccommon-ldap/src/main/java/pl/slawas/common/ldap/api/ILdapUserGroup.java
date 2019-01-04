package pl.slawas.common.ldap.api;

import java.util.Set;

public interface ILdapUserGroup extends ILdapEntry {

	/**
	 * @return the {@link #memberOf}
	 */
	public abstract String getMainMemberOf();

	/**
	 * @param memberOf
	 *            the {@link #memberOf} to set
	 */
	public abstract void setMainMemberOf(String memberOf);

	/**
	 * @return the {@link #members}
	 */
	public abstract Set<String> getMembers();

	/**
	 * @param members
	 *            the {@link #members} to set
	 */
	public abstract void setMembers(Set<String> members);

	/**
	 * Metoda, która pokazuje również wartości list
	 * 
	 * @param showLists
	 *            czy mają być prezentowane również listy atrybutów i członków
	 *            grupy?
	 * @return łańcuch znakowy charakterystyczny dla danej instancji obiektu
	 *         {@link ILdapUserGroup}.
	 */
	public abstract String toString(boolean showLists);

	/**
	 * @return the {@link #description}
	 */
	public abstract String getDescription();

	/**
	 * @param description
	 *            the {@link #description} to set
	 */
	public abstract void setDescription(String description);

	/**
	 * @return the {@link #managerGroupName}
	 */
	public abstract String getManagerGroupName();

	/**
	 * @param managerGroupName
	 *            the {@link #managerGroupName} to set
	 */
	public abstract void setManagerGroupName(String managerGroupName);

	public abstract boolean isManagerGroup();

	public abstract boolean isWorkersGroup();

	/**
	 * @return the {@link #isStructureUnit}
	 */
	public abstract boolean isStructureUnit();

	/**
	 * @param isStructureUnit
	 *            the {@link #isStructureUnit} to set
	 */
	public abstract void setStructureUnit(boolean isStructureUnit);

	/**
	 * @return the {@link #managerSuffix}
	 */
	String getManagerSuffix();

	/**
	 * @return the {@link #workerSuffix}
	 */
	String getWorkerSuffix();

	/**
	 * pozostali rodzice z pominięciem rodzica głównego (jeżeli taki istnieje)
	 */
	Set<String> getOtherMembersOf();

	/**
	 * Ustawianie pozostałych rodziców z pominięciem rodzica głównego (jeżeli
	 * taki istnieje)
	 */
	void setOtherMembersOf(Set<String> otherMembersOf);

}