package pl.slawas.common.mail;

import javax.mail.Message;

/**
 * 
 * Smtp - klasa narzędziowa wspierająca wysyłanie wiadomości z wykorzystaniem
 * IMAP (automatycznym dodaniem wysłanej wiadomości do odpowiedniego folderu.
 *
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl&gt;
 * @version $Revision: 1.1 $
 *
 */
public class Smtp {

	public static Message sendMail(String smtpServer, String smtpUsername, String smtpPassword, String smtpPort,
			boolean auth, boolean ssl, boolean tls, String from, String replyTo, String to, String cc, String bcc,
			String subject, String text, boolean isHTML, String localFiles2Attach, boolean startTSLEnable,
			String socketFactoryProvider, String socketFactoryClass, String codePage, int connectiontimeout)
			throws Exception {
		return Imap.sendMessage(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, from, replyTo, to, cc,
				bcc, subject, text, isHTML, localFiles2Attach, startTSLEnable, socketFactoryProvider,
				socketFactoryClass, codePage, connectiontimeout);
	}
}
