package pl.slawas.common.ldap.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;

/**
 * 
 * SecurityPassword
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public class SecurityPassword {

	private static Logger logger = Logger.getLogger(SecurityPassword.class.getName());

	private static SecurityPassword instance;
	private final String messageDigest;

	private SecurityPassword(String messageDigest) {
		super();
		this.messageDigest = messageDigest;
	}

	/**
	 * 
	 * @param messageDigest
	 *            metoda kodowania np. "SHA1", "SHA2"
	 * @return
	 */
	public static SecurityPassword getInstance(String messageDigest) {
		if (instance == null) {
			instance = new SecurityPassword(messageDigest);
		}
		return instance;
	}

	/**
	 * Zakodowanie hasła
	 * 
	 * @param passwordToHash
	 *            niezakodowane hasło
	 * @return zakodowane hasło
	 */
	public String getSecurePasswordAsHex(String passwordToHash) {
		String generatedPassword = null;
		byte[] bytes = getSecurePasswordAsByte(passwordToHash);
		// Convert it to hexadecimal format
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}
		// Get complete hashed password in hex format
		generatedPassword = sb.toString();
		return generatedPassword;
	}

	/**
	 * Zakodowanie hasła
	 * 
	 * @param passwordToHash
	 *            niezakodowane hasło
	 * @return zakodowane hasło
	 */
	public byte[] getSecurePasswordAsLdapFormat(String passwordToHash) {
		byte[] bytes = getSecurePasswordAsByte(passwordToHash);
		// Convert it to LDAP format
		String encodedNewPasswdWithPrefix;
		if (this.messageDigest.equals("SHA1") || this.messageDigest.equals("SHA-1")) {
			encodedNewPasswdWithPrefix = "{SHA}" + DatatypeConverter.printBase64Binary(bytes);
		} else {
			encodedNewPasswdWithPrefix = "{SSHA}" + DatatypeConverter.printBase64Binary(bytes);
		}
		String encodedNewPasswdWithPrefixBase64 = DatatypeConverter
				.printBase64Binary(encodedNewPasswdWithPrefix.getBytes());
		return DatatypeConverter.parseBase64Binary(encodedNewPasswdWithPrefixBase64);
	}

	/**
	 * Zakodowanie hasła
	 * 
	 * @param passwordToHash
	 *            niezakodowane hasło
	 * @return zakodowane hasło
	 */
	public byte[] getSecurePasswordAsByte(String passwordToHash) {
		try {
			return securePassword(passwordToHash);
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			logger.error("getSecurePassword", e);
		}
		logger.warn("getSecurePassword: returned not secured password");
		return passwordToHash.getBytes();
	}

	private byte[] securePassword(String passwordToHash) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		// Create MessageDigest instance for MD5
		MessageDigest md = MessageDigest.getInstance(this.messageDigest);
		// Get the hash's bytes
		return md.digest(passwordToHash.getBytes("UTF-8"));
	}

}
