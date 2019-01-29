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

import javax.management.RuntimeErrorException;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.ldap.api.ILdapAO;
import pl.slawas.common.ldap.api.ILdapBaseEntry;
import pl.slawas.common.ldap.provider.ProviderOptions;

/**
 * LdapEntrySupport
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
@SuppressWarnings("serial")
public abstract class LdapBaseEntrySupport implements ILdapBaseEntry {

	/**
	 * Login/nazwa jednoznacznie identyfikująca użytkownika/grupę
	 */
	private final String name;

	/**
	 * DN (distinguishedName) użytkownika/grupy
	 */
	private String dn;

	/**
	 * Entry może pochodzić z różnych jednostek organizacyjnych zdefiniowanych w
	 * {@link ProviderOptions#getUsersOrganizationalUnitNames()} (dla użytkowników)
	 * lub {@link ProviderOptions#getGroupsOrganizationalUnitNames()} (dla grup).
	 * Informacja o tym z jakiej jednostki pochodzi entry powinna zostać przekazana
	 * do obiektu w celu optymalizacji ścieżki bazowej wyszukiwania konkretnych
	 * danych.
	 * 
	 * Jednostka organizacyjna entry doklejana do kontekstu bazowego podczas
	 * wyszukiwania entry po jego identyfikatorze (pobieranie konkretnego entry).
	 * 
	 * @see ILdapAO#getOrganizationalUnitName()
	 */
	private final String orgUnitName;

	/**
	 * 
	 * @param name
	 *            nazwa użytkownika !parametr wymagany!
	 * @param orgUnitName
	 *            jednostka organizacyjna entry {@link #orgUnitName}
	 */
	public LdapBaseEntrySupport(String name, String orgUnitName) {
		super();
		if (StringUtils.isBlank(name)) {
			throw new RuntimeErrorException(new Error("Nazwa entry nie może być pusta!"));
		}
		this.name = name;
		this.orgUnitName = orgUnitName;
	}

	/**
	 * @return the {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @deprecated Pole 'name' jest ustawiane tylko i wyłącznie za pośrednictwem
	 *             konstruktora. Metoda istnieje ze względu na spełnienie wymogów
	 *             interfejsu. Użycie metody spowoduje wyjątek
	 *             {@link UnsupportedOperationException}.
	 * @see org.scbpm.lib.ldap._LdapEntry#setName(java.lang.String)
	 */
	@Deprecated
	public void setName(String name) {
		throw new UnsupportedOperationException(
				"Pole 'name' jest ustawiane tylko i wyłącznie za pośrednictwem konstruktora.");
	}

	/**
	 * @return the {@link #dn}
	 */
	public String getDn() {
		return dn;
	}

	/**
	 * @param distinguishedName
	 *            the {@link #dn} to set
	 */
	public void setDn(String distinguishedName) {
		this.dn = distinguishedName;
	}

	/**
	 * @return the {@link #orgUnitName}
	 */
	public String getOrgUnitName() {
		return orgUnitName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "LdapBaseEntrySupport [name=" + name + ", dn=" + dn + "]";
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
		result = prime * result + ((dn == null) ? 0 : dn.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		LdapBaseEntrySupport other = (LdapBaseEntrySupport) obj;

		if (StringUtils.isBlank(dn)) {
			if (StringUtils.isNotBlank(other.dn)) {
				return false;
			}
		} else if (!dn.equalsIgnoreCase(other.dn)) {
			return false;
		}
		if (StringUtils.isBlank(name)) {
			if (StringUtils.isNotBlank(other.name)) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

}
