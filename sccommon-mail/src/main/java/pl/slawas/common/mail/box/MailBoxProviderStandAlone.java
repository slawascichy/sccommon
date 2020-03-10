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
package pl.slawas.common.mail.box;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Properties;

import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;

import org.apache.commons.lang.StringUtils;
import org.eclipse.wst.common.internal.emf.utilities.EncoderDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.mail.box.api.IMailBoxProviderStandAlone;
import pl.slawas.common.mail.helpers.MailProtocol;
import pl.slawas.common.mail.helpers.MessageHelper;
import pl.slawas.common.mail.smtp.api.MailConstants;
import pl.slawas.security.PasswordEncoder;

/**
 * MailBoxProviderStandAlone - implementacja samodzielnej instancji dostawcy
 * poczty elektronicznej IMAP.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class MailBoxProviderStandAlone extends MailConstants implements IMailBoxProviderStandAlone, Serializable {

	private static final long serialVersionUID = 7203553866919752293L;

	private static final Logger logger = LoggerFactory.getLogger(MailBoxProviderStandAlone.class.getName());

	/** protokół komunikacji ze skrzynką poczty elektronicznej */
	private final MailProtocol protocol;
	/** nazwa serwera (razem z portem, jeżeli trzeba) */
	private final String server;
	/** nazwa użytkownika/właściciela skrzynki */
	private final String username;

	/** zakodowane hasło użytkownika/właściciela skrzynki */
	private String password;

	private boolean startTSLEnable = false;
	private String socketFactoryProvider = null;
	private String socketFactoryClass = null;
	private String passwordEncoderClass = PasswordEncoder.DEFAULT_ENCODER_CLASS;

	/**
	 * Socket connection timeout value in milliseconds. This timeout is implemented
	 * by java.net.Socket.
	 */
	private int connectiontimeout = defaultimapConnectiontimeout;

	public MailBoxProviderStandAlone(MailProtocol protocol, String server, String username,
			String passwordEncoderClass) {
		super();
		this.protocol = protocol;
		this.server = server;
		this.username = username;
		this.passwordEncoderClass = passwordEncoderClass;
	}

	/**
	 * @param password
	 *            the {@link #password} to set
	 */
	@Override
	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public Store getStore(boolean partialfetch) throws NoSuchProviderException {
		Properties props = System.getProperties();
		props.setProperty(imapPartialFetch, Boolean.toString(partialfetch));
		props.setProperty(mailMimeDecodetextStrict, mailMimeDecodetextStrictDefaultValue);
		if (this.connectiontimeout > 0) {
			final String timeout = Integer.toString(this.connectiontimeout);
			props.setProperty(imapConnectiontimeout, timeout);
			props.setProperty(imapTimeout, timeout);
			props.setProperty(imapWritetimeout, timeout);
		}
		props.setProperty(MAIL_MIME_IGNOREUNKNOWNENCODING, Boolean.toString(true));
		props.setProperty(imapStartTSLEnable, Boolean.toString(this.startTSLEnable));
		if (StringUtils.isNotBlank(this.socketFactoryProvider)) {
			props.setProperty(sslSocketFactoryProvider, this.socketFactoryProvider);
		}
		if (StringUtils.isNotBlank(this.socketFactoryClass)) {
			props.setProperty(imapSocketFactoryClass, this.socketFactoryClass);
		}
		Session session = Session.getDefaultInstance(props, null);

		StringBuilder url = new StringBuilder(128);

		PasswordEncoder encoder = new PasswordEncoder();
		encoder.setEncoderClass(passwordEncoderClass);
		EncoderDecoder passwdEncoderDecoder = encoder.getEncoderDecoder();

		/*
		 * wywalamy wszystkie niebezpieczne dla URL'a znaki z nazwy użytkownika,
		 * ponieważ budujemy później URL'a
		 */
		String escapedUserName;
		try {
			escapedUserName = URLEncoder.encode(this.username, MessageHelper.DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			logger.warn("Problem z enkodowaniem nazwy użytkownika: " + this.username, e);
			escapedUserName = this.username;
		}
		/*
		 * wywalamy wszystkie niebezpieczne dla URL'a znaki z hasła użytkownika,
		 * ponieważ budujemy później URL'a
		 */
		String escapedPassword;
		try {
			escapedPassword = URLEncoder.encode(passwdEncoderDecoder.decode(this.password),
					MessageHelper.DEFAULT_CHARSET);
		} catch (UnsupportedEncodingException e) {
			logger.warn("Problem z enkodowaniem hasła użytkownika: " + this.username, e);
			escapedPassword = passwdEncoderDecoder.decode(this.password);
		}
		url.append(this.protocol).append("://").append(escapedUserName).append(":").append(escapedPassword).append("@")
				.append(this.server);
		URLName urlName = new URLName(url.toString());

		if (logger.isDebugEnabled()) {
			StringBuilder url4Debug = new StringBuilder(128);
			url4Debug.append(this.protocol).append("://").append(escapedUserName).append(":").append("***********")
					.append("@").append(this.server);
			logger.debug("url={} connectiontimeout={}", new Object[] { url4Debug, this.connectiontimeout });
		}
		return session.getStore(urlName);
	}

	/**
	 * @return the {@link #protocol}
	 */
	public MailProtocol getProtocol() {
		return protocol;
	}

	/**
	 * @return the {@link #server}
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @return the {@link #username}
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return the {@link #startTSLEnable}
	 */
	public boolean isStartTSLEnable() {
		return startTSLEnable;
	}

	/**
	 * @param startTSLEnable
	 *            the {@link #startTSLEnable} to set
	 */
	public void setStartTSLEnable(boolean startTSLEnable) {
		this.startTSLEnable = startTSLEnable;
	}

	/**
	 * @return the {@link #socketFactoryProvider}
	 */
	public String getSocketFactoryProvider() {
		return socketFactoryProvider;
	}

	/**
	 * @param socketFactoryProvider
	 *            the {@link #socketFactoryProvider} to set
	 */
	public void setSocketFactoryProvider(String socketFactoryProvider) {
		this.socketFactoryProvider = socketFactoryProvider;
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

	public String getPasswordEncoderClass() {
		return passwordEncoderClass;
	}

	public void setPasswordEncoderClass(String passwordEncoderClass) {
		this.passwordEncoderClass = passwordEncoderClass;
	}

}
