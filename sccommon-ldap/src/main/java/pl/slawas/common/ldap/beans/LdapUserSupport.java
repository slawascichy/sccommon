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

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.ldap.api.ILdapAttribute;
import pl.slawas.common.ldap.api.ILdapEntry4Changes;
import pl.slawas.common.ldap.api.ILdapUser;
import pl.slawas.common.ldap.api.ILdapUserGroup;
import pl.slawas.common.ldap.api.IUserAttribute;

/**
 * LdapUserSupport - implementacja obiektu reprezentującego entry użytkownika.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LdapUserSupport<G extends ILdapUserGroup> extends LdapEntrySupport
		implements ILdapUser<G>, ILdapEntry4Changes {

	private static final long serialVersionUID = 6033888446721608744L;

	/** klasa parametru */
	private final Class<G> persistentClass;

	/**
	 * Grupa domyślna użytkownika.
	 */
	private G primaryGroup;

	/**
	 * DN domyślnej grupy użytkownika. . Pole pomocnicze dla wyznaczenia wartości
	 * pola {@link #primaryGroup}.
	 */
	private String primaryGroupDN;

	/**
	 * Lista grup, do których użytkownik jest bezpośrednio zapisany np. za
	 * pośrednictwem atrybutu {@code member} albo {@code uniqueMember} w LDAP. Lista
	 * zawiera również grupę domyślną reprezentowaną przez pole
	 * {@link #primaryGroup}.
	 */
	private List<G> baseGroups;

	/**
	 * Preferowane ustawienia językowe użytkownika.
	 */
	private String locale;

	/** Avatar użytkownika */
	private byte[] avatar;

	/** Avatar użytkownika zakodowany w Base64 */
	private String avatarBase64;

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
	 * 
	 * @param name
	 *            nazwa użytkownika !parametr wymagany!
	 * @param orgUnitName
	 *            jednostka organizacyjna entry {@link #getOrgUnitName()}
	 */
	@SuppressWarnings("unchecked")
	public LdapUserSupport(String name, String orgUnitName) {
		super(name, orgUnitName);
		Class<?> tmpClass = getClass();
		while (!(tmpClass.getGenericSuperclass() instanceof ParameterizedType)) {
			tmpClass = tmpClass.getSuperclass();
		}
		this.persistentClass = (Class<G>) ((ParameterizedType) tmpClass.getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	/* Overridden (non-Javadoc) */
	@Override
	public String getPrimaryGroupDN() {
		return primaryGroupDN;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setPrimaryGroupDN(String primaryGroupDN) {
		this.primaryGroupDN = primaryGroupDN;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public G getPrimaryGroup() {
		return primaryGroup;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setPrimaryGroup(G primaryGroup) {
		this.primaryGroup = primaryGroup;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public List<G> getBaseGroups() {
		return baseGroups;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setBaseGroups(List<G> baseGroups) {
		this.baseGroups = baseGroups;
	}

	@Override
	public String toString() {
		return toString(false);
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
	public String toString(boolean showLists) {
		StringBuilder userDescription = new StringBuilder()
				.append("\n  LdapUser:\n --------------------------------------").append("\n DN : ")
				.append(this.getDn()).append("\n Name : ").append(this.getName()).append("\n Display Name : ")
				.append(this.getDisplayName()).append("\n E-mail : ").append(this.getEmail())
				.append("\n Primary Group : ")
				.append((this.primaryGroup == null ? "unknown" : this.primaryGroup.getName()));
		if (showLists) {
			List<IUserAttribute> attributes = this.getAttributes();
			if (attributes != null && !attributes.isEmpty()) {
				userDescription.append("\n Attributes:");
				for (IUserAttribute attr : attributes) {
					userDescription.append("\n\t").append(attr.getName()).append(" : ").append(attr.getValue());
				}
			}
			if (baseGroups != null && !baseGroups.isEmpty()) {
				userDescription.append("\n Base Groups:");
				for (ILdapUserGroup attr : baseGroups) {
					userDescription.append("\n\t").append(attr.getName()).append(" (").append(attr.getDisplayName())
							.append(")");
				}
			}
		}
		userDescription.append("\n --------------------------------------");
		return userDescription.toString();

	}

	/* Overridden (non-Javadoc) */
	@Override
	public byte[] getAvatar() {
		return avatar;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setAvatar(byte[] avatar) {
		if (avatar != null) {
			this.avatar = new byte[avatar.length];
			for (int i = 0; i < avatar.length; i++) {
				this.avatar[i] = avatar[i];
			}
		}
	}

	/* Overridden (non-Javadoc) */
	@Override
	public String getAvatarBase64() {
		return avatarBase64;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setAvatarBase64(String avatarBase64) {
		this.avatarBase64 = avatarBase64;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public String getLocale() {
		return locale;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setLocale(String locale) {
		this.locale = locale;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(avatar);
		result = prime * result + ((avatarBase64 == null) ? 0 : avatarBase64.hashCode());
		result = prime * result + ((baseGroups == null) ? 0 : baseGroups.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + ((primaryGroup == null) ? 0 : primaryGroup.hashCode());
		result = prime * result + ((primaryGroupDN == null) ? 0 : primaryGroupDN.hashCode());
		return result;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		@SuppressWarnings("rawtypes")
		LdapUserSupport other = (LdapUserSupport) obj;
		if (!persistentClass.equals(other.persistentClass)) {
			return false;
		}
		if (!Arrays.equals(avatar, other.avatar)) {
			return false;
		}
		if (avatarBase64 == null) {
			if (other.avatarBase64 != null) {
				return false;
			}
		} else if (!avatarBase64.equals(other.avatarBase64)) {
			return false;
		}
		if (baseGroups == null) {
			if (other.baseGroups != null) {
				return false;
			}
		} else if (!baseGroups.equals(other.baseGroups)) {
			return false;
		}
		if (StringUtils.isBlank(locale)) {
			if (StringUtils.isNotBlank(other.locale)) {
				return false;
			}
		} else if (!locale.equals(other.locale)) {
			return false;
		}
		if (primaryGroup == null) {
			if (other.primaryGroup != null) {
				return false;
			}
		} else if (!primaryGroup.equals(other.primaryGroup)) {
			return false;
		}
		if (StringUtils.isBlank(primaryGroupDN)) {
			if (StringUtils.isNotBlank(other.primaryGroupDN)) {
				return false;
			}
		} else if (!primaryGroupDN.equals(other.primaryGroupDN)) {
			return false;
		}
		return true;
	}

}
