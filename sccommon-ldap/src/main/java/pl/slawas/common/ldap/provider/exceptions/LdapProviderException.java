package pl.slawas.common.ldap.provider.exceptions;

/**
 * 
 * LdapProviderException - abstrakcja wyjątku dla implementacji security
 * provider'a
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public abstract class LdapProviderException extends Exception {

	private static final long serialVersionUID = 8782485460456861455L;

	public LdapProviderException() {
		super();
	}

	public LdapProviderException(String message, Throwable cause) {
		super(message, cause);
	}

	public LdapProviderException(String message) {
		super(message);
	}

	public LdapProviderException(Throwable cause) {
		super(cause);
	}

}
