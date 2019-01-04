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

import java.util.List;

/**
 * _LdapEntry - ogólny interfejs związany z entry przechowywania grup i
 * użytkowników.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface ILdapEntry extends ILdapBaseEntry {

	/**
	 * @return the email - adres poczty elektronicznej entry (adres grupy,
	 *         użytkownika)
	 */
	String getEmail();

	/**
	 * Ustawianie adresu poczty elektronicznej dla użytkownika lub grupy, które
	 * reprezentowane są przez entry.
	 * 
	 * @param email
	 *            adres poczty elektronicznej entry
	 */
	void setEmail(String email);

	/**
	 * @return the displayName - nazwa prezentacyjna związana z entry
	 */
	String getDisplayName();

	/**
	 * Ustawianie nazwy prezentacyjnej entry.
	 * 
	 * @param displayName
	 *            nazwa prezentacyjna
	 */
	void setDisplayName(String displayName);

	/**
	 * @return lista wszystkich atrybutów systemowych (po mapowaniu z atrybutów
	 *         LDAP) "załadowanych" z LDAP(wraz z mapowanymi do ich
	 *         odpowiedników systemowych wartościami) powiązanych z entry.
	 */
	List<IUserAttribute> getAttributes();

	/**
	 * Pobranie obiektu atrybutu systemowego entry o konkretnej nazwie atrybutu
	 * w systemie.
	 * 
	 * @param attrName
	 *            nazwa atrybutu w systemie.
	 * @return obiekt atrybutu o konkretnej nazwie.
	 */
	IUserAttribute getAttribute(String attrName);

	/**
	 * <font color="#dd0000">Uwaga! użycie metody nadpisze dodane przy użyciu
	 * tej metody {@link #addAttribute(IUserAttribute)} wartości.</font>
	 * 
	 * @param attributes
	 *            the {@link #attributes} to set
	 */
	void setAttributes(List<IUserAttribute> attributes);

}
