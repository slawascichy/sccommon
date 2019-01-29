package pl.slawas.common.ldap.provider;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.ldap.api.Constants;
import pl.slawas.common.ldap.api.ICustomBinaryField;

/**
 * 
 * CustomBinaryField - dodatkowe (znane) pola binarne, które trzeba zdefiniować
 * za pomocą parametru <tt>java.naming.ldap.attributes.binary</tt> podczas
 * nawiązywania połączenia z LDAP. Na podstawie dokumentacji oracle
 * (http://docs.oracle.com/javase/7/docs/technotes/guides/jndi/jndi-ldap-gl.html
 * ) wiemy, że:
 * <dl>
 * <dt><b><tt>java.naming.ldap.attributes.binary</tt></b></dt>
 * </dl>
 * <dl>
 * <dd>
 * <dl>
 * <dt>The value of this property is a string of space-separated attribute
 * names. It specifies attributes which have non-string syntax. It extends the
 * provider's <i>built-in</i> list of non-string attributes (below). The value
 * of an attribute that has non-string syntax is returned as a byte array (
 * <tt>byte[]</tt>) instead of a <tt>String</tt>.<br>
 * No default is defined. If this property is not set then only the following
 * attributes are considered to have non-string syntax:</dt>
 * </dl>
 * 
 * <table summary="attributes with non-string syntax" border="1" cellpadding="3"
 * width="70%">
 * <tbody>
 * <tr>
 * <th>Attribute ID</th>
 * <th>OID</th>
 * <th>Reference</th>
 * </tr>
 * <tr>
 * <td>Any attribute ID with the "<tt>;binary</tt>" option.</td>
 * <td>&nbsp;</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td><tt>photo</tt></td>
 * <td>0.9.2342.19200300.100.1.7</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc1274.txt">RFC 1274</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>personalSignature</tt></td>
 * <td>0.9.2342.19200300.100.1.53</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc1274.txt">RFC 1274</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>audio</tt></td>
 * <td>0.9.2342.19200300.100.1.55</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc1274.txt">RFC 1274</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>jpegPhoto</tt></td>
 * <td>0.9.2342.19200300.100.1.60</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc2798.txt">RFC 2798</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>javaSerializedData</tt></td>
 * <td>1.3.6.1.4.1.42.2.27.4.1.7</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc2713.txt">RFC 2713</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>thumbnailPhoto</tt></td>
 * <td>2.16.128.113533.1.1400.1</td>
 * <td align="center">NAC LIP Schema</td>
 * </tr>
 * <tr>
 * <td><tt>thumbnailLogo</tt></td>
 * <td>2.16.128.113533.1.1400.2</td>
 * <td align="center">NAC LIP Schema</td>
 * </tr>
 * <tr>
 * <td><tt>userPassword</tt></td>
 * <td>2.5.4.35</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc2256.txt">RFC 2256</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>userCertificate</tt></td>
 * <td>2.5.4.36</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc2256.txt">RFC 2256</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>cACertificate</tt></td>
 * <td>2.5.4.37</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc2256.txt">RFC 2256</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>authorityRevocationList</tt></td>
 * <td>2.5.4.38</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc2256.txt">RFC 2256</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>certificateRevocationList</tt></td>
 * <td>2.5.4.39</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc2256.txt">RFC 2256</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>crossCertificatePair</tt></td>
 * <td>2.5.4.40</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc2256.txt">RFC 2256</a>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>x500UniqueIdentifier</tt></td>
 * <td>2.5.4.45</td>
 * <td align="center"><a href="http://www.ietf.org/rfc/rfc2256.txt">RFC 2256</a>
 * </td>
 * </tr>
 * </tbody>
 * </table>
 * 
 * <dl>
 * <dt>For example:</dt>
 * <dd>
 * <dl>
 * <dd>
 * 
 * <pre>
 * env.put(&quot;java.naming.ldap.attributes.binary&quot;, &quot;mpegVideo myspecialkey&quot;);
 * </pre>
 * 
 * </dd>
 * </dl>
 * informs the provider to return values of the <tt>mpegVideo</tt> and
 * <tt>myspecialkey</tt> attributes as <tt>byte[]</tt>.
 * <a name="connect" id="connect"></a></dd>
 * </dl>
 * </dd>
 * 
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public enum CustomBinaryField implements ICustomBinaryField {

	objectGUID(16), objectSid(Constants.UNSPECIFIED_LENGTH);

	public static final String CUSTOM_BINARY_FIELDS;

	static {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for (CustomBinaryField bfName : CustomBinaryField.values()) {
			if (i != 0) {
				sb.append(Constants.SPACE_SEPARATOR);
			}
			sb.append(bfName.name());
			i++;
		}
		CUSTOM_BINARY_FIELDS = sb.toString();
	}

	/**
	 * Oczekiwana długość łańcucha binarnego, jeżeli przyjmuje wartość
	 * {@value ICustomBinaryField#UNSPECIFIED_LENGTH} to znaczy, że długość jest
	 * dowolna
	 */
	private final int lenght;

	private CustomBinaryField(int lenght) {
		this.lenght = lenght;
	}

	/**
	 * Konwersja wartości pola binarnego do String'a
	 * 
	 * @param binaryObject
	 *            wartość atrybutu pola binarnego
	 * @return reprezentacja wartości pola binarnego w postaci String'a
	 */
	public String value2String(byte[] binaryObject) {
		if (binaryObject == null
				|| (this.lenght != Constants.UNSPECIFIED_LENGTH && binaryObject.length != this.lenght)) {
			throw new IllegalArgumentException(this.name() + ": Nieprawidłowa wartość pola binarnego.");
		}
		StringBuilder displayStr = new StringBuilder();
		switch (this) {
		case objectGUID:
			displayStr.append(prefixZeros((int) binaryObject[3] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[2] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[1] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[0] & 0xFF));
			displayStr.append(Constants.VALUE_SEPARATOR);
			displayStr.append(prefixZeros((int) binaryObject[5] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[4] & 0xFF));
			displayStr.append(Constants.VALUE_SEPARATOR);
			displayStr.append(prefixZeros((int) binaryObject[7] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[6] & 0xFF));
			displayStr.append(Constants.VALUE_SEPARATOR);
			displayStr.append(prefixZeros((int) binaryObject[8] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[9] & 0xFF));
			displayStr.append(Constants.VALUE_SEPARATOR);
			displayStr.append(prefixZeros((int) binaryObject[10] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[11] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[12] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[13] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[14] & 0xFF));
			displayStr.append(prefixZeros((int) binaryObject[15] & 0xFF));
			break;
		case objectSid:
			displayStr.append("S-");
			/*
			 * bytes[0] : in the array is the version (must be 1 but might change in the
			 * future)
			 */
			displayStr.append(binaryObject[0]).append(Constants.VALUE_SEPARATOR);

			/* bytes[2..7] : the Authority */
			StringBuilder tmpBuff = new StringBuilder();
			for (int t = 2; t <= 7; t++) {
				String hexString = Integer.toHexString(binaryObject[t] & 0xFF);
				tmpBuff.append(hexString);
			}
			displayStr.append(Long.parseLong(tmpBuff.toString(), 16));

			/* bytes[1] : the sub authorities count */
			int count = binaryObject[1];

			/*
			 * bytes[8..end] : the sub authorities (these are Integers - notice the endian)
			 */
			for (int i = 0; i < count; i++) {
				int currSubAuthOffset = i * 4;
				tmpBuff.setLength(0);
				tmpBuff.append(String.format("%02X%02X%02X%02X", (binaryObject[11 + currSubAuthOffset] & 0xFF),
						(binaryObject[10 + currSubAuthOffset] & 0xFF), (binaryObject[9 + currSubAuthOffset] & 0xFF),
						(binaryObject[8 + currSubAuthOffset] & 0xFF)));

				displayStr.append(Constants.VALUE_SEPARATOR).append(Long.parseLong(tmpBuff.toString(), 16));
			}
			break;
		default:
			for (int i = 0; i < binaryObject.length; i++) {
				displayStr.append(prefixZeros((int) binaryObject[i] & 0xFF));
			}
			break;
		}
		return displayStr.toString();
	}

	private static String prefixZeros(int value) {
		if (value <= 0xF) {
			StringBuilder sb = new StringBuilder("0");
			sb.append(Integer.toHexString(value));

			return sb.toString();

		} else {
			return Integer.toHexString(value);
		}
	}

	/**
	 * Weryfikacja czy atrybut LDAP jest dodatkowym polem binarnym np. objectGUID.
	 * Taki atrybut podlega dodatkowej obróbce podczas przekształcania do postaci
	 * String'a.
	 * 
	 * 
	 * @param ldapAttrName
	 *            nazwa atrybutu LDAP
	 * @return obiekt {@link CustomBinaryField} albo {@code null} gdy obiekt nie
	 *         został znaleziony.
	 */
	public static CustomBinaryField getCustomBinaryField(String ldapAttrName) {
		if (StringUtils.isBlank(ldapAttrName)) {
			/* nie prowadzimy dalszej analizy gdy argument metody jest pusty */
			return null;
		}
		for (CustomBinaryField bfName : CustomBinaryField.values()) {
			if (bfName.name().equalsIgnoreCase(ldapAttrName)) {
				/* wykryliśmy, że atrybut jest dodatkowym polem binarnym */
				return bfName;
			}
		}
		/* pole nie jest dodatkowym polem binarnym */
		return null;
	}

}
