package pl.slawas.common.mail.smtp;

import javax.mail.Session;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import pl.slawas.common.mail.exceptions.CommonMailException;
import pl.slawas.common.mail.smtp.api.IMailSenderProvider;

public class MailSenderProviderEmbedded implements IMailSenderProvider {

	private final String resourceJndiName;

	public MailSenderProviderEmbedded(String resourceJndiName) {
		super();
		this.resourceJndiName = resourceJndiName;
	}

	@Override
	public Session getSession() throws CommonMailException {
		InitialContext ctx;
		try {
			ctx = new InitialContext();
			return (Session) ctx.lookup(this.resourceJndiName);
		} catch (NamingException e) {
			throw new CommonMailException(e);
		}
	}

	@Override
	public String getPasswordEncoderClass() {
		/* nie implementujemy */
		return null;
	}

	@Override
	public void setPasswordEncoderClass(String passwordEncoderClass) {
		/* nie implementujemy */
	}

}
