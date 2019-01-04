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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.ldap.api.ILdapEntry;
import pl.slawas.common.ldap.api.IUserAttribute;

/**
 * LdapEntrySupport
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
@SuppressWarnings("serial")
public abstract class LdapEntrySupport extends LdapBaseEntrySupport implements
		ILdapEntry {

	/**
	 * Np. adres poczty elektronicznej użytkownika
	 */
	private String email;

	/**
	 * Np. pełna nazwa użytkownika, z reguły kombinacja imienia i nazwiska.
	 */
	private String displayName;

	/**
	 * Zdefiniowane atrybuty użytkownika
	 */
	private List<IUserAttribute> attributes;

	/**
	 * Mapa atrybutów użytkownika, pomocniczo, by szybko można było pobrać
	 * odpowiednie wartości atrybutu po jego nazwie.
	 */
	private Hashtable<String, IUserAttribute> attrMap;

	/**
	 * 
	 * @param name
	 *            nazwa użytkownika !parametr wymagany!
	 * @param orgUnitName
	 *            jednostka organizacyjna entry {@link #orgUnitName}
	 */
	public LdapEntrySupport(String name, String orgUnitName) {
		super(name, orgUnitName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.scbpm.lib.ldap._LdapEntry#getAttribute(java.lang.String)
	 */
	public IUserAttribute getAttribute(String attrName) {
		if (this.attrMap != null) {
			return this.attrMap.get(attrName);
		} else {
			return null;
		}
	}

	/**
	 * @return the {@link #email}
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email
	 *            the {@link #email} to set
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * @return the {@link #displayName}
	 */
	public String getDisplayName() {
		return StringUtils.isBlank(displayName) ? this.getName() : displayName;
	}

	/**
	 * @param displayName
	 *            the {@link #displayName} to set
	 */
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the {@link #attributes}
	 */
	public List<IUserAttribute> getAttributes() {
		return attributes;
	}

	/**
	 * <font color="#dd0000">Uwaga! użycie metody nadpisze dodane przy użyciu
	 * tej metody {@link #addAttribute(IUserAttribute)} wartości.</font>
	 * 
	 * @param attributes
	 *            the {@link #attributes} to set
	 */
	public void setAttributes(List<IUserAttribute> attributes) {
		this.attributes = attributes;
		if (this.attributes != null && !this.attributes.isEmpty()) {
			// ustawianie mapy atrybutów, tak aby łatwo można było pobrać
			// wartość atrybutu po jego nazwie.
			this.attrMap = new Hashtable<String, IUserAttribute>();
			for (IUserAttribute attr : this.attributes) {
				this.attrMap.put(attr.getName(), attr);
			}
		}
	}

	/**
	 * Dodawanie atrybutu, wykorzystywane przez mechanizmy zbierające dodatkowe
	 * informacje o użytkowniku. Jeżeli {@link #attributes} lub {@link #attrMap}
	 * są puste ({@code null}), to zostaną zainicjalizowane.
	 * <p>
	 * <font color="#dd0000">Uwaga! użycie metody {@link #setAttributes(List)}
	 * nadpisze dodane przy użyciu tej metody wartości.</font>
	 * </p>
	 * 
	 * 
	 * @param attr
	 *            the attributes to set
	 */
	public void addAttribute(IUserAttribute attr) {
		if (this.attrMap == null) {
			this.attrMap = new Hashtable<String, IUserAttribute>();
		}
		if (this.attributes == null) {
			this.attributes = new ArrayList<IUserAttribute>();
		}
		this.attrMap.put(attr.getName(), attr);
		this.attributes.add(attr);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LdapEntrySupport [name=" + getName() + ", email=" + email
				+ ", displayName=" + displayName + ", dn=" + getDn() + "]";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result
				+ ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((getDn() == null) ? 0 : getDn().hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result
				+ ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LdapEntrySupport other = (LdapEntrySupport) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (other.attributes == null) {
			return false;
		} else if (attributes.isEmpty() != other.attributes.isEmpty()) {
			return false;
		} else if (attributes.size() != other.attributes.size()) {
			return false;
		} else {
			IUserAttribute[] thisArray = attributes
					.toArray(new IUserAttribute[attributes.size()]);
			IUserAttribute[] otherArray = other.attributes
					.toArray(new IUserAttribute[other.attributes.size()]);
			if (!Arrays.equals(thisArray, otherArray)) {
				return false;
			}
		}
		if (StringUtils.isBlank(displayName)) {
			if (StringUtils.isNotBlank(other.displayName)) {
				return false;
			}
		} else if (!displayName.equals(other.displayName)) {
			return false;
		}
		if (StringUtils.isBlank(getDn())) {
			if (StringUtils.isNotBlank(other.getDn())) {
				return false;
			}
		} else if (!getDn().equalsIgnoreCase(other.getDn())) {
			return false;
		}
		if (StringUtils.isBlank(email)) {
			if (StringUtils.isNotBlank(other.email)) {
				return false;
			}
		} else if (!email.equalsIgnoreCase(other.email)) {
			return false;
		}
		if (StringUtils.isBlank(getName())) {
			if (StringUtils.isNotBlank(other.getName())) {
				return false;
			}
		} else if (!getName().equals(other.getName())) {
			return false;
		}
		return true;
	}

}
