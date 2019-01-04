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

import pl.slawas.common.ldap.beans.UserAttributeType;

/**
 * _UserAttribute - interfejs atrybutu systemowego (BPM) użytkownika.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface IUserAttribute extends Serializable {

	/**
	 * @return nazwa atrybutu systemowego (po stronie systemu).
	 */
	String getName();

	/**
	 * Ustawia nazwę atrybutu systemowego (po stronie systemu).
	 * 
	 * @param name
	 *            nazwa atrybutu jaka ma być ustawiona.
	 */
	void setName(String name);

	/**
	 * @return wartość atrybutu w postaci obiektu {@link String}.
	 */
	String getValue();

	/**
	 * Ustawia wartość atrybutu.
	 * 
	 * @param value
	 *            wartość jaka ma być ustawiona.
	 */
	void setValue(String value);

	/**
	 * Typ zawartej w polu wartości
	 */
	UserAttributeType getType();

	/**
	 * Ustawianie typu zawartej w polu wartości
	 * 
	 * @param type
	 *            typ
	 */
	void setType(UserAttributeType type);
	
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

}
