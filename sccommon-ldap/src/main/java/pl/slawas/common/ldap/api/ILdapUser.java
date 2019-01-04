package pl.slawas.common.ldap.api;

import java.util.List;

/**
 * 
 * _LdapUser interfejs reprezentujący entry użytkownika
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 * @param <G>
 *            implementacja/interfejs reprezentujący obiekt grupy
 */
public interface ILdapUser<G extends ILdapUserGroup> extends ILdapEntry {

	/**
	 * @return the {@link #primaryGroupDN}
	 */
	String getPrimaryGroupDN();

	/**
	 * @param primaryGroupDN
	 *            the {@link #primaryGroupDN} to set
	 */
	void setPrimaryGroupDN(String primaryGroupDN);

	/**
	 * @return the {@link #primaryGroup}
	 */
	G getPrimaryGroup();

	/**
	 * @param primaryGroup
	 *            the {@link #primaryGroup} to set
	 */
	void setPrimaryGroup(G primaryGroup);

	/**
	 * @return the {@link #baseGroups}
	 */
	List<G> getBaseGroups();

	/**
	 * @param baseGroups
	 *            the {@link #baseGroups} to set
	 */
	void setBaseGroups(List<G> baseGroups);

	/**
	 * Metoda, która pokazuje również wartości list
	 * 
	 * @param showLists
	 *            czy mają być prezentowane również listy atrybutów i grup
	 *            podstawowych?
	 * @return łańcuch znakowy charakterystyczny dla danej instancji obiektu
	 *         {@link ILdapUser}.
	 */
	String toString(boolean showLists);

	/**
	 * @return the {@link #avatar}
	 */
	byte[] getAvatar();

	/**
	 * @param avatar
	 *            the {@link #avatar} to set
	 */
	void setAvatar(byte[] avatar);

	/**
	 * @return the {@link #avatarBase64}
	 */
	String getAvatarBase64();

	/**
	 * @param avatarBase64
	 *            the {@link #avatarBase64} to set
	 */
	void setAvatarBase64(String avatarBase64);

	/**
	 * @return the {@link #locale}
	 */
	String getLocale();

	/**
	 * @param locale
	 *            the {@link #locale} to set
	 */
	void setLocale(String locale);

}