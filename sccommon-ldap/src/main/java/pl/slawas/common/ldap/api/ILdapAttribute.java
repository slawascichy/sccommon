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

import java.io.Serializable;
import java.sql.Types;

/**
 * _LdapAttribute - reprezentacja atrybutu entry w LDAP.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface ILdapAttribute extends Serializable {

	/** Domyślna nazwa atrybutu reprezentującego adres e-mail */
	String DEFAULT_EMAIL_ATTR_NAME = "mail";

	/**
	 * @return nazwa atrybutu.
	 */
	String getName();

	/**
	 * Ustawia nazwę atrybutu.
	 * 
	 * @param name
	 *            nazwa atrybutu jaka ma być ustawiona.
	 */
	void setName(String name);

	/**
	 * @return wartość atrybutu w postaci obiektu {@link Object}.
	 */
	Object getValue();

	/**
	 * Ustawia wartość atrybutu.
	 * 
	 * @param value
	 *            wartość jaka ma być ustawiona.
	 */
	void setValue(Object value);

	/**
	 * Typ, zdefiniowany jako liczba. Słownikiem typów jest {@link Types} np.
	 * {@link Types#OTHER}, {@link Types#ARRAY}, {@link Types#INTEGER} itp.
	 * 
	 * @return "identyfikator" typu, jakim jest reprezentowana wartość atrybutu.
	 */
	int getType();

	/**
	 * Ustawianie "identyfikatora" typu, jakim jest reprezentowana wartość
	 * atrybutu
	 * 
	 * @param type
	 *            "identyfikator" typu, jakim jest reprezentowana wartość
	 *            atrybutu. Słownikiem typów jest {@link Types} np.
	 *            {@link Types#OTHER}, {@link Types#ARRAY},
	 *            {@link Types#INTEGER} itp.
	 */
	void setType(int type);

	/**
	 * Pobranie poprzedniej, starej wartości atrybutu, potrzebne do realizacji
	 * modyfikacji atrybutów.
	 * 
	 * @return stara wartość atrybutu w postaci obiektu {@link Object}.
	 */
	Object getOldValue();

	/**
	 * Ustawia poprzednią, starą wartość atrybutu, potrzebne do realizacji
	 * modyfikacji atrybutów.
	 * 
	 * @param value
	 *            wartość jaka ma być ustawiona.
	 */
	void setOldValue(Object value);

	/**
	 * Informacja czy atrybut jest wielowartościowy.
	 * 
	 * @return
	 */
	boolean isMultiValue();

	/**
	 * Informacja czy atrybut jest wielowartościowy.
	 * 
	 * @param multiValue
	 *            wartość flagi
	 */
	void setMultiValue(boolean multiValue);

}
