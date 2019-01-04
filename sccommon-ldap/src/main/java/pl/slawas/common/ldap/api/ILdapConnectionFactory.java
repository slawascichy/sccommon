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

package pl.slawas.common.ldap.api;

import pl.slawas.common.ldap.provider.ProviderOptions;

/**
 * 
 * _LdapConnectionFactory interfejs obsługi wyszukiwania w LDAP. Obiekt
 * zarządzający parametrami połączenia do repozytorium LDAP.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 * @param <T>
 *            obiekt entry, z którym związany jest dany kontekst.
 */
public interface ILdapConnectionFactory {

	/**
	 * @return the {@link #ldapFactory}
	 */
	ILdapContextFactory getLdapContextFactory();

	/**
	 * @return the {@link #isNotInicjalized}
	 */
	boolean isInicjalized();

	void close();

	/**
	 * @return the {@link #organizationalUnitName}
	 */
	String getOrganizationalUnitName();

	/**
	 * Informacja czy fabryka używa domyślnego kontekstu wyszukiwania czy nie.
	 * 
	 * @return {@code true} jeżeli {@link #organizationalUnitName} jest pusty.
	 */
	boolean isDefaultCtxDN();

	/**
	 * @return the {@link #ldapOptions}
	 */
	ProviderOptions getLdapOptions();

}