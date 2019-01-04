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

import java.sql.Types;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.ldap.api.ILdapAttribute;

/**
 * LdapUserAttribute
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LdapUserAttribute implements ILdapAttribute {

	private static final long serialVersionUID = -873746835978409011L;

	/** nazwa atrybutu */
	private String name;

	/** wartość atrybutu */
	private Object value;

	/** typ atrybutu, domyślna wartość to {@link Types#VARCHAR}. */
	private int type = Types.VARCHAR;

	/** czy atrybut jest wielowartościowy (dla danego entry) */
	private boolean multiValue = false;
	/**
	 * stara wartość atrybutu, potrzebne do realizacji modyfikacji atrybutów
	 */
	private Object oldValue;

	/**
	 * Domyślny konstruktor.
	 */
	public LdapUserAttribute() {
		super();
	}

	/**
	 * Konstruktor pomocniczy (żeby szybko można było robić atrybuty).
	 * 
	 * @param name
	 *            nazwa atrybutu
	 * @param value
	 *            wartość atrybutu
	 * @param type
	 *            typ atrybutu
	 */
	public LdapUserAttribute(String name, Object value, int type) {
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
	public LdapUserAttribute(String name, Object value) {
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
	public Object getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the {@link #value} to set
	 */
	public void setValue(Object value) {
		this.value = value;
	}

	/**
	 * @return the {@link #type}
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type
	 *            the {@link #type} to set
	 */
	public void setType(int type) {
		this.type = type;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + type;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LdapUserAttribute other = (LdapUserAttribute) obj;
		if (StringUtils.isBlank(name)) {
			if (StringUtils.isNotBlank(other.name))
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public Object getOldValue() {
		return this.oldValue;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void setOldValue(Object value) {
		this.oldValue = value;
	}

	/**
	 * @return the {@link #multiValue}
	 */
	public boolean isMultiValue() {
		return multiValue;
	}

	/**
	 * @param multiValue
	 *            the {@link #multiValue} to set
	 */
	public void setMultiValue(boolean multiValue) {
		this.multiValue = multiValue;
	}

}
