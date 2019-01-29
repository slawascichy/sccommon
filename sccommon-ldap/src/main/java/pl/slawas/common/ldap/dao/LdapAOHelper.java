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

package pl.slawas.common.ldap.dao;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.ldap.api.Constants;
import pl.slawas.common.ldap.provider.LdapResult;
import pl.slawas.common.ldap.provider.ProviderOptions;
import pl.slawas.helpers.Strings;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * 
 * LdapAOHelper - klasa pomocnicza obsługująca pulę kontekstów (połączeń) LDAP.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LdapAOHelper implements Serializable {

	private static final long serialVersionUID = 6652495401454679123L;

	private final  static Logger log = LoggerFactory.getLogger(LdapAOHelper.class.getName());

	public static final String DEFAULT_LDAP_CONTEXT_NAME = "DEFAULT";

	/**
	 * Bindowanie argumentów do definicji filtra używanego do wyszukiwania w LDAP.
	 * Zasada jest prosta. Łańcuch filtra (argument {@code filter}) zawiera klauzule
	 * '{nr_pozycji_w_macierzy}' np.
	 * 
	 * <pre>
	 * (&(sAMAccountName={0})(objectClass=user))
	 * </pre>
	 * 
	 * Ta klauzula jest podmieniana wartością zdefiniowaną na odpowiedniej pozycji w
	 * macierzy argumentów {@code args} i tak dla powyższego przykładu wartość '{0}'
	 * zostanie podmieniona odpowiednią wartością {@code args[0]}.
	 * 
	 * @param args
	 *            macierz argumentów do bindowania
	 * @param filter
	 *            klauzula filtra
	 * @return zbudowane odpowiednio zapytanie LDAP
	 */
	public static String bindFilterVariables(String[] args, String filter) {

		String localFilter = filter;
		if (args != null) {
			int i = 0;
			for (String arg : args) {
				log.debug("local Filter arg{}: {}", new Object[] { i, arg });
				localFilter = Strings.replaceAll(localFilter, "{" + i++ + "}", arg);
			}
		}
		return localFilter;
	}

	/**
	 * Pobranie nazwy atrybutu wskazującego na ścieżkę przechowywania entry w LDAP.
	 * 
	 * @see Constants#DEFAULT_DISTINGUISHED_NAME_ATTR
	 * @param ldapOptions
	 *            parametry inicjalizacyjne kontekstu LDAP, zdefiniowane w dostawcy
	 *            opcji.
	 * @return nazwa atrybutu
	 */
	public static String getDistinguishedNameAttrName(ProviderOptions ldapOptions) {
		String distinguishedName = Constants.DEFAULT_DISTINGUISHED_NAME_ATTR;
		if (ldapOptions != null) {
			return ldapOptions.getUserDNAttribute();
		}
		return distinguishedName;
	}

	/**
	 * Czytanie (rozkodowanie) wartości atrybutu z wyniku LDAP.
	 * 
	 * @param result
	 *            obiekt rezultatu (wyniku) z LDAP
	 * @param attrName
	 *            nazwa atrybutu LDAP
	 * @return wartość atrybutu w postaci string'a
	 */
	public static String readValue(LdapResult result, String attrName) {
		if (result.get(attrName) != null) {
			log.trace("readValue: {}={}", new Object[] { attrName, result.get(attrName).get(0).getValue() });
			return (String) result.get(attrName).get(0).getValue();
		}
		log.trace("readValue: {}={}", new Object[] { attrName, result.get(attrName) });
		return null;

	}

	/**
	 * Zwrócenie nazwy kontekstu na podstawie nazwy jednostki organizacyjnej.
	 * 
	 * @param organizationalUnitName
	 *            nazwa jednostki organizacyjnej
	 * @return jeżeli nazwa jednostki organizacyjnej jest pusta ({@code null}) to
	 *         {@link #DEFAULT_LDAP_CONTEXT_NAME}.
	 */
	public static String getContextName(String organizationalUnitName) {
		if (StringUtils.isBlank(organizationalUnitName)) {
			return DEFAULT_LDAP_CONTEXT_NAME;
		}
		return organizationalUnitName;
	}

}
