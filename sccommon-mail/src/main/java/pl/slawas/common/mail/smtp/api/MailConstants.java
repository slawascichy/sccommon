package pl.slawas.common.mail.smtp.api;

import pl.slawas.common.mail.box.ExchangeSSLSocketFactory;

/**
 * MailConstants
 * 
 * * <a href=
 * "https://javamail.java.net/nonav/docs/api/com/sun/mail/imap/package-summary.html"
 * >Package com.sun.mail.imap</a>
 * 
 * 
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl&gt;
 * @version $Revision: 1.1 $
 *
 */
public class MailConstants {

	protected MailConstants() {
	}

	public static final String smtpProtocol = "mail.smtp.protocol";
	public static final String smtpServer = "mail.smtp.host";
	public static final String smtpUsername = "mail.smtp.user";
	public static final String smtpPassword = "mail.smtp.password";
	public static final String smtpPort = "mail.smtp.port";
	public static final String smtpAuth = "mail.smtp.auth";
	public static final String smtpSslTrust = "mail.smtp.ssl.trust";
	public static final String smtpStartTSL = "mail.smtp.starttls.enable";
	public static final String smtpStartSSL = "mail.smtp.startssl.enable";
	public static final String smtpStartSSLClazz = "mail.smtp.socketFactory.class";
	public static final String smtpStartSSLPort = "mail.smtp.socketFactory.port";
	public static final String smtpCodePage = "mail.smtp.codePage";

	/** Socket connection timeout value in milliseconds. */
	public static final String smtpConnectiontimeout = "mail.smtp.connectiontimeout";
	public static final String smtpTimeout = "mail.smtp.timeout";
	public static final String smtpWritetimeout = "mail.smtp.writetimeout";
	/** Default socket connection timeout - 10min. */
	public static final int defaultimapConnectiontimeout = 1000 * 60 * 10;

	public static final String imapServer = "mail.imap.server";
	public static final String imapUsername = "mail.imap.username";
	public static final String imapPassword = "mail.imap.password";
	public static final String imapMailboxIn = "mail.imap.mailbox.in";
	public static final String imapMailboxSend = "mail.imap.mailbox.send";
	public static final String imapMailboxTrash = "mail.imap.mailbox.trash";
	public static final String imapMailboxDraft = "mail.imap.mailbox.draft";
	public static final String imapFlgReturnAtt = "mail.imap.flgReturnAtt";
	public static final String imapFlgScanNarrative = "mail.imap.flgScanNarrative";
	public static final String imapFlgDeleteRead = "mail.imap.flgDeleteRead";
	public static final String imapPartialFetch = "mail.imap.partialfetch";
	public static final String imapStartTSLEnable = "mail.imap.starttls.enable";

	/** Socket connection timeout value in milliseconds. */
	public static final String imapConnectiontimeout = "mail.imap.connectiontimeout";
	public static final String imapTimeout = "mail.imap.timeout";
	public static final String imapWritetimeout = "mail.imap.writetimeout";
	public static final String imapSocketFactoryClass = "mail.imap.socketFactory.class";
	public static final String mailMimeDecodetextStrict = "mail.mime.decodetext.strict";
	public static final String mailMimeDecodetextStrictDefaultValue = Boolean.toString(false);

	public static final String sslSocketFactoryProvider = "ssl.SocketFactory.provider";
	public static final String EXHANGE_SOCKET_FACTORY_PROVIDER = ExchangeSSLSocketFactory.class.getName();
	public static final String MAIL_MIME_IGNOREUNKNOWNENCODING = "mail.mime.ignoreunknownencoding";

	public static final String FORWARD_SUBJECT_PREFIX = "FW: ";
	public static final String FORWARD_CONTENT_TYPE = "message/rfc822";
	public static final String FORWARD_HEADER_SENDER = "Sender";
	public static final String FORWARD_HEADER_ORIG_SENDER = "Orig-Sender";
	public static final String FORWARD_HEADER_REFERENCES = "References";
	public static final String FORWARD_HEADER_IN_REPLY_TO = "In-Reply-To";
	public static final String CONTENT_TYPE_HTML_PREFIX = "text/htm";
	public static final String CONTENT_TYPE_TEXT_PREFIX = "text/pla";

}
