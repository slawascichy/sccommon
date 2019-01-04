package pl.slawas.common.ldap.provider;

import java.io.UnsupportedEncodingException;
import java.sql.Types;

/**
 * 
 * LdapValue klasa pomocnicza reprezentująca wartość atrybutu przechowywanego w LDAP
 *
 * @author Sławomir Cichy &lt;scichy@ibpm.pro&gt;
 * @version $Revision: 1.2 $
 *
 */
public class LdapValue {

	private Object value;

	private int type;

	public LdapValue(Object value, int type) {
		this.value = value;
		this.type = type;
	}

	public LdapValue(String value) {
		this.value = value;
		this.type = Types.VARCHAR;
	}

	/**
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}

	/**
	 * @return the value jako string
	 * @throws UnsupportedEncodingException
	 */
	public String getString() throws UnsupportedEncodingException {
		if (Types.BLOB == type) {
			byte[] b = (byte[]) this.value;
			return new String(b, 0, b.length, "UTF-8");
		} else if (Types.VARCHAR == type) {
			return (String) value;
		} else {
			return value.toString();
		}
	}

}
