package pl.slawas.common.mail.box;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import pl.slawas.common.mail.box.api.IMailBoxProvider;

public class MailBoxProviderEmbedded implements IMailBoxProvider {

	private static final long serialVersionUID = -573974428425963986L;
	private final String resourceJndiName;
	private String server;
	private String userName;

	public MailBoxProviderEmbedded(String resourceJndiName) {
		super();
		this.resourceJndiName = resourceJndiName;
	}

	@Override
	public String getServer() {
		return (server == null ? this.resourceJndiName : server);
	}

	@Override
	public String getUsername() {
		return (userName == null ? this.resourceJndiName : userName);
	}

	@Override
	public Store getStore(boolean partialfetch) throws NoSuchProviderException {
		try {
			InitialContext ctx = new InitialContext();
			Session mailSession = (Session) ctx.lookup(this.resourceJndiName);
			Store store = mailSession.getStore();
			URLName urlName = store.getURLName();
			this.server = urlName.getHost();
			this.userName = urlName.getUsername();
			return store;
		} catch (NamingException e) {
			throw new NoSuchProviderException(e.getMessage());
		}
	}

}
