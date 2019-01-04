package pl.slawas.common.ldap.provider.exceptions;

/**
 * 
 * ProviderOptionsNotInicjalized błąd inicjalizacji opcji provider'a
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public class ProviderOptionsNotInicjalized extends LdapProviderException {

	private static final long serialVersionUID = -8679329846083716599L;

	public ProviderOptionsNotInicjalized(String message) {
		super(message);
	}

	public ProviderOptionsNotInicjalized(String message, Throwable cause) {
		super(message, cause);
	}

}
