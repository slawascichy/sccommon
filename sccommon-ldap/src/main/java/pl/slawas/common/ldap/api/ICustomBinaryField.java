package pl.slawas.common.ldap.api;

/**
 * 
 * ICustomBinaryField
 *
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl&gt;
 * @version $Revision: 1.1 $
 *
 */
public interface ICustomBinaryField {

	/**
	 * Konwersja wartości pola binarnego do String'a
	 * 
	 * @param objectUID
	 *            wartość atrybutu pola binarnego
	 * @return reprezentacja wartości pola binarnego w postaci String'a
	 */
	String value2String(byte[] objectUID);

}
