package pl.slawas.common.ldap.provider.exceptions;

public class LdapObjectFactoryException extends LdapProviderException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2863779655406072070L;

	public LdapObjectFactoryException() {
		super();
	}

	public LdapObjectFactoryException(String message, Throwable cause) {
		super(message, cause);
	}

	public LdapObjectFactoryException(String message) {
		super(message);
	}

	public LdapObjectFactoryException(Throwable cause) {
		super(cause);
	}

}
