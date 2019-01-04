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

import pl.slawas.common.ldap.api.IUserAttribute;

/**
 * UserAttribute - implementacja atrybutu systemowego (BPM) użytkownika.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class UserAttribute implements IUserAttribute {

	private static final long serialVersionUID = -873746835978409011L;

	/** nazwa atrybutu systemowego (po stronie systemu) */
	private String name;

	/** wartość atrybutu */
	private String value;

	/** typ atrybutu, domyślna wartość to {@link UserAttributeType.STRING}. */
	private UserAttributeType type = UserAttributeType.STRING;
	/**
	 * stara wartość atrybutu, potrzebne do realizacji modyfikacji atrybutów
	 */
	private Object oldValue;


	/**
	 * Domyślny konstruktor.
	 */
	public UserAttribute() {
		super();
	}

	/**
	 * Konstruktor pomocniczy (żeby szybko można było robić atrybuty).
	 * 
	 * @param name
	 *            nazwa atrybutu systemowego (po stronie systemu)
	 * @param value
	 *            wartość atrybutu
	 * @param type
	 *            typ atrybutu
	 */
	public UserAttribute(String name, String value, UserAttributeType type) {
		super();
		this.name = name;
		this.value = value;
		this.type = type;
	}

	/**
	 * Konstruktor pomocniczy (żeby szybko można było robić atrybuty). Ustawiana
	 * jest wartość domyślna dla {@link #type}.
	 * 
	 * @param name
	 *            nazwa atrybutu
	 * @param value
	 *            wartość atrybutu
	 */
	public UserAttribute(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the {@link #name} to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the {@link #value}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the {@link #value} to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * @return the {@link #type}
	 */
	public UserAttributeType getType() {
		return type;
	}

	/**
	 * @param type
	 *            the {@link #type} to set
	 */
	public void setType(UserAttributeType type) {
		this.type = type;
	}

	/**
	 * @return the {@link #oldValue}
	 */
	public Object getOldValue() {
		return oldValue;
	}

	/**
	 * @param oldValue
	 *            the {@link #oldValue} to set
	 */
	public void setOldValue(Object oldValue) {
		this.oldValue = oldValue;
	}

}
