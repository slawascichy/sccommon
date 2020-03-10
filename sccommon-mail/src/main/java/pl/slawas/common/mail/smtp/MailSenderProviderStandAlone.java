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
package pl.slawas.common.mail.smtp;

import java.util.Properties;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;

import org.apache.commons.lang.StringUtils;
import org.eclipse.wst.common.internal.emf.utilities.EncoderDecoder;

import pl.slawas.common.mail.exceptions.CommonMailException;
import pl.slawas.common.mail.helpers.MailProtocol;
import pl.slawas.common.mail.smtp.api.IMailSenderProviderStandAlone;
import pl.slawas.common.mail.smtp.api.MailConstants;
import pl.slawas.security.PasswordEncoder;

/**
 * MailSenderProviderStandAlone
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class MailSenderProviderStandAlone implements IMailSenderProviderStandAlone {

	private final String host;

	private final String username;

	private String socketFactoryClass;

	private String port;

	private boolean auth = false;

	private boolean tls = false;

	private boolean ssl = false;

	private String password;

	private String passwordEncoderClass = PasswordEncoder.DEFAULT_ENCODER_CLASS;

	/**
	 * Socket connection timeout value in milliseconds. This timeout is implemented
	 * by java.net.Socket.
	 */
	private int connectiontimeout = MailConstants.defaultimapConnectiontimeout;

	public MailSenderProviderStandAlone(String host, String username) {
		super();
		this.host = host;
		this.username = username;
	}

	/**
	 * Ustawienie dostawcy poczty - zwraca sesję
	 */
	public Session getSession() throws CommonMailException {

		// Get system properties
		Properties properties = System.getProperties();

		// Setup mail server
		properties.put(MailConstants.smtpProtocol, MailProtocol.smtp.name());
		properties.setProperty(MailConstants.smtpServer, host);
		if (StringUtils.isNotBlank(port)) {
			properties.put(MailConstants.smtpPort, port);
			properties.put(MailConstants.smtpStartSSLPort, port);
		}
		properties.put(MailConstants.smtpStartTSL, Boolean.toString(tls));
		if (tls) {
			if (!ssl) {
				ssl = true;
			}
		}
		if (!ssl) {
			properties.put(MailConstants.smtpStartSSLClazz, "");
			properties.put(MailConstants.smtpSslTrust, "");
		} else {
			if (StringUtils.isNotBlank(socketFactoryClass)) {
				properties.put(MailConstants.smtpStartSSLClazz, socketFactoryClass);
			}
			properties.put(MailConstants.smtpSslTrust, host);
		}
		properties.put(MailConstants.smtpAuth, Boolean.toString(auth));
		if (this.connectiontimeout > 0) {
			final String timeout = Integer.toString(this.connectiontimeout);
			properties.put(MailConstants.smtpConnectiontimeout, timeout);
			properties.put(MailConstants.smtpTimeout, timeout);
			properties.put(MailConstants.smtpWritetimeout, timeout);
		}

		if (!auth) {
			return Session.getDefaultInstance(properties);
		}

		return Session.getInstance(properties, new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				PasswordEncoder encoder = new PasswordEncoder();
				encoder.setEncoderClass(passwordEncoderClass);
				EncoderDecoder passwdEncoderDecoder = encoder.getEncoderDecoder();
				return new PasswordAuthentication(username, passwdEncoderDecoder.decode(password));
			}
		});
	}

	/**
	 * @param password
	 *            the {@link #password} to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the {@link #username}
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the {@link #port}
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the {@link #port} to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the {@link #auth}
	 */
	public boolean isAuth() {
		return auth;
	}

	/**
	 * @param auth
	 *            the {@link #auth} to set
	 */
	public void setAuth(boolean auth) {
		this.auth = auth;
	}

	/**
	 * @return the {@link #tls}
	 */
	public boolean isTls() {
		return tls;
	}

	/**
	 * @param tls
	 *            the {@link #tls} to set
	 */
	public void setTls(boolean tls) {
		this.tls = tls;
	}

	/**
	 * @return the {@link #ssl}
	 */
	public boolean isSsl() {
		return ssl;
	}

	/**
	 * @param ssl
	 *            the {@link #ssl} to set
	 */
	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	/**
	 * @return the {@link #host}
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @return the {@link #connectiontimeout}
	 */
	public int getConnectiontimeout() {
		return connectiontimeout;
	}

	/**
	 * @param connectiontimeout
	 *            the {@link #connectiontimeout} to set
	 */
	public void setConnectiontimeout(int connectiontimeout) {
		this.connectiontimeout = connectiontimeout;
	}

	/**
	 * @return the {@link #socketFactoryClass}
	 */
	public String getSocketFactoryClass() {
		return socketFactoryClass;
	}

	/**
	 * @param socketFactoryClass
	 *            the {@link #socketFactoryClass} to set
	 */
	public void setSocketFactoryClass(String socketFactoryClass) {
		this.socketFactoryClass = socketFactoryClass;
	}

	public String getPasswordEncoderClass() {
		return passwordEncoderClass;
	}

	public void setPasswordEncoderClass(String passwordEncoderClass) {
		this.passwordEncoderClass = passwordEncoderClass;
	}

}
