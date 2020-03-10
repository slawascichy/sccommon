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
package pl.slawas.common.mail.helpers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.activation.DataHandler;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MailDateFormat;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.mail.box.MailBoxVersion;

/**
 * MessageHelper
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class MessageHelper {

	private static final Logger logger = LoggerFactory.getLogger(MessageHelper.class.getName());

	public static final String MAIL_RECEIVED_DATE_COLUMN = "receivedDate";
	public static final String MAIL_MESSAGE_ID_COLUMN = "Message-ID";
	public static final String MAIL_SENT_DATE_COLUMN = "sentDate";
	public static final String MAIL_NUMBER_COLUMN = "mailNumber";
	public static final String MAIL_SIZE = "size";
	public static final String MAIL_IS_READED_COLUMN = "isRead";
	public static final String MAIL_PART_CONTENT_COLUMN = "content";
	public static final String MAIL_PART_LOCAL_FILE_PATH = "localFilePath";
	public static final String MAIL_PART_HTTP_FILE_PATH = "httpFilePath";
	public static final String MAIL_SOURCE_EML_LOCAL_FILE_PATH = "localSourceEmlFilePath";
	public static final String MAIL_SOURCE_EML_HTTP_FILE_PATH = "httpSourceEmlFilePath";
	public static final String MAIL_PART_CONTENT_TYPE = "contentType";
	public static final String MAIL_PART_CHARSET = "charset";
	public static final String MAIL_PART_CONTENT_TENCODING_COLUMN = "Content-Transfer-Encoding";
	public static final String MAIL_PART_CONTENT_DISPOSITION = "contentDisposition";
	public static final String MAIL_HEADER_RECEIVE_DATE = "Receive-Date";
	public static final String MAIL_HEADER_REPLY_TO = "Reply-To";

	/** lista pól/kolumn obiektu folderu */
	public enum FolderFields {
		fullName, name, parentName, type, mode, newMessageCount, unreadMessageCount, allMessageCount;
	}

	/** Folder zawiera inne foldery */
	public static final String FOLDER_TYPE_HOLD_FOLDERS = "HOLD_FOLDERS";
	/** Folder zawiera wiadomości */
	public static final String FOLDER_TYPE_HOLD_MESSAGES = "HOLD_MESSAGES";
	/** Folder zawiera inne foldery oraz wiadomości */
	public static final String FOLDER_TYPE_HOLD_FOLDERS_AND_MESSAGES = "HOLD_FOLDERS_AND_MESSAGES";
	/** Folder tylko do odczytu */
	public static final String FOLDER_MODE_RO = "READ_ONLY";
	/** Folder folder do odczytu i zapisu */
	public static final String FOLDER_MODE_RW = "READ_WRITE";

	public static final String CONTENT_TEXT_PREFIX = "text";
	public static final String CONTENT_TEXT = CONTENT_TEXT_PREFIX + "/plain";
	public static final String CONTENT_HTML = CONTENT_TEXT_PREFIX + "/html";
	public static final String SYSTEM_MAIL_MIME_CHARSET_PROPERTY_NAME = "mail.mime.charset";
	public static final String SYSTEM_MAIL_MIME_ENCODE_FILENAME_PROPERTY_NAME = "mail.mime.encodefilename";
	public static final String SYSTEM_MAIL_MIME_DECODE_FILENAME_PROPERTY_NAME = "mail.mime.decodefilename";
	public static final String CONTENT_TYPE_CHARSET_PREFIX = "; charset=";
	public static final String DEFAULT_CHARSET = "UTF-8";

	public static final String DEFAULT_CONTENT_TENCODING = "quoted-printable";
	public static final String DEFAULT_MULTIPART_SUBTYPE = "alternative";
	public static final String MIXED_MULTIPART_SUBTYPE = "mixed";
	public static final String RELATED_MULTIPART_SUBTYPE = "related";
	public static final String FORWARD_SUBTYPE = "rfc822";
	public static final String DEFAULT_FORWARD_ATTACHED_MSG = "<span style=\"font-family: Courier New, Courier, monospace;font-size: 14px;\">Forwarded message is as attachment.</span>";
	/** format daty do prezentacji czasu wystąpienia wiadomości */
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static BodyPart attachLogo;
	private static String DEFAULT_FORWARD_FOOTER_HTML_MSG = null;
	private static String DEFAULT_FORWARD_FOOTER_TEXT_MSG = null;
	/**
	 * separator dodawany pomiędzy wieloma wartościami sklejonymi w jeden String
	 */
	public static char MANY_VALUES_SEPARATOR = '|';

	/** obiekt format'era daty */
	public static final SimpleDateFormat getLongDateFormater() {
		return new SimpleDateFormat(DATE_FORMAT);
	}

	private static final MailDateFormat getMailDateFormat() {
		return new MailDateFormat();
	}

	public static final String EMPTY_SUBJECT = "[Empty subject] Brak tematu...";

	/**
	 * Przygotowanie obiektu wiadomości do zapisu.
	 * 
	 * @param from
	 *            od
	 * @param replyTo
	 *            adres "odpowiedz Do"
	 * @param to
	 *            do
	 * @param cc
	 *            do wiadomości
	 * @param bcc
	 *            adres ukryty do wiadomości
	 * @param subject
	 *            temat
	 * @param text
	 *            treść wiadomości
	 * @param isHTML
	 *            czy wiadomość jest w HTML'u
	 * @param localFiles2Attach
	 *            lista nazw plików na serwerze (lokalnych). które zostaną załączone
	 *            do wiadomości.
	 * @param codePage
	 *            strona kodowa na jaką wiadomość zostanie zamieniona przed
	 *            wysłaniem.
	 * @param receiveDate
	 *            niedomyślna data przesłania wiadomości przez SMTP
	 * @param sentDate
	 *            data wysłania wiadomości przez użytkownika
	 * @return obiekt wiadomości
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public static Message prepareMsgForSave(String from, String replyTo, String to, String cc, String bcc,
			String subject, String text, boolean isHtml, String[] localFiles2Attach, String codePage,
			final String receiveDate, final String sentDate) throws UnsupportedEncodingException, MessagingException {

		if (StringUtils.isBlank(sentDate)) {
			throw new IllegalArgumentException("Pole '" + MAIL_SENT_DATE_COLUMN + "' nie może być puste.");
		}
		if (StringUtils.isBlank(receiveDate)) {
			throw new IllegalArgumentException("Pole '" + MAIL_RECEIVED_DATE_COLUMN + "' nie może być puste.");
		}
		Date localReceiveDate = prepareDate(MAIL_RECEIVED_DATE_COLUMN, receiveDate);
		Date localSentDate = prepareDate(MAIL_SENT_DATE_COLUMN, sentDate);
		return prepareMsg(/* session */null, from, replyTo, to, cc, bcc, subject, text, isHtml, localFiles2Attach,
				codePage, localReceiveDate, localSentDate);
	}

	/**
	 * Metoda pomocnicza pozwalająca na przekształcenie daty ze {@link String} do
	 * postaci obiektu {@link Date}
	 * 
	 * @param dateFieldName
	 *            nazwa pola wiadomości - wartość pomocnicza, pozwalająca na
	 *            poprawne sformułowanie komunikatu błędu gdyby np. format daty był
	 *            nieprawidłowy.
	 * @param date
	 *            data w postaci {@link String}
	 * @return obiekt daty.
	 */
	private static Date prepareDate(String dateFieldName, String date) {
		try {
			return MessageHelper.getLongDateFormater().parse(date);
		} catch (ParseException ex) {
			final String msg = "Wartość pola '" + dateFieldName + "'='" + date + "' nie jest datą wg formatu: '"
					+ MessageHelper.DATE_FORMAT + "'.";
			throw new IllegalArgumentException(msg);
		}
	}

	/**
	 * Przygotowanie obiektu wiadomości do wysłania.
	 * 
	 * @param session
	 *            sesja
	 * @param from
	 *            od
	 * @param replyTo
	 *            adres "odpowiedz Do"
	 * @param to
	 *            do
	 * @param cc
	 *            do wiadomości
	 * @param bcc
	 *            adres ukryty do wiadomości
	 * @param subject
	 *            temat
	 * @param text
	 *            treść wiadomości
	 * @param isHTML
	 *            czy wiadomość jest w HTML'u
	 * @param localFiles2Attach
	 *            lista nazw plików na serwerze (lokalnych). które zostaną załączone
	 *            do wiadomości.
	 * @param codePage
	 *            strona kodowa na jaką wiadomość zostanie zamieniona przed
	 *            wysłaniem.
	 * @return obiekt wiadomości
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public static Message prepareMsgForSend(Session session, String from, String replyTo, String to, String cc,
			String bcc, String subject, String text, boolean isHtml, String[] localFiles2Attach, String codePage)
			throws UnsupportedEncodingException, MessagingException {
		return prepareMsg(session, from, replyTo, to, cc, bcc, subject, text, isHtml, localFiles2Attach, codePage,
				/* receiveDate */null, /* sentDate */
				null);
	}

	/**
	 * Przygotowanie obiektu wiadomości - uniwersalna metoda wykorzystywana to
	 * tworzenia obiektu wiadomości, zarówno dla wiadomości które mają być wysłane,
	 * jak i przekształcania treści wiadomości "jednej w drugą".
	 * 
	 * @param session
	 *            sesja
	 * @param from
	 *            od
	 * @param replyTo
	 *            adres "odpowiedz Do"
	 * @param to
	 *            do
	 * @param cc
	 *            do wiadomości
	 * @param bcc
	 *            adres ukryty do wiadomości
	 * @param subject
	 *            temat
	 * @param text
	 *            treść wiadomości
	 * @param isHTML
	 *            czy wiadomość jest w HTML'u
	 * @param localFiles2Attach
	 *            lista nazw plików na serwerze (lokalnych). które zostaną załączone
	 *            do wiadomości.
	 * @param codePage
	 *            strona kodowa na jaką wiadomość zostanie zamieniona przed
	 *            wysłaniem.
	 * @param receiveDate
	 *            niedomyślna data przesłania wiadomości przez SMTP - parametr
	 *            wykorzystywany do budowania instancji obiektu wiadomości, która
	 *            powstaje na podstawie już istniejącej. Dla tworzonych wiadomości
	 *            do wysłania parametr powinien być {@code null}
	 * @param sentDate
	 *            data wysłania wiadomości przez użytkownika - parametr
	 *            wykorzystywany do budowania instancji obiektu wiadomości, która
	 *            powstaje na podstawie już istniejącej. Dla tworzonych wiadomości
	 *            do wysłania parametr powinien być {@code null}
	 * @return obiekt wiadomości
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private static Message prepareMsg(Session session, String from, String replyTo, String to, String cc, String bcc,
			String subject, String text, boolean isHtml, String[] localFiles2Attach, String codePage,
			final Date receiveDate, final Date sentDate) throws MessagingException, UnsupportedEncodingException {

		if (StringUtils.isBlank(codePage)) {
			codePage = DEFAULT_CHARSET;
		}

		if (!Charset.isSupported(codePage)) {
			final String msg = "Wartość pola 'codePage'='" + codePage + "' nie jest obsługiwaną stroną kodową.";
			throw new IllegalArgumentException(msg);
		}

		if (StringUtils.isBlank(from)) {
			throw new IllegalArgumentException("Pole 'Od' (nadawcy) nie może być puste.");
		}

		if (StringUtils.isBlank(to)) {
			throw new IllegalArgumentException("Pole 'Do' (adresata) nie może być puste.");
		}

		System.setProperty(SYSTEM_MAIL_MIME_CHARSET_PROPERTY_NAME, codePage);
		final MimeMessage message = new MimeMessage(session);
		message.setHeader(MAIL_PART_CONTENT_TENCODING_COLUMN, DEFAULT_CONTENT_TENCODING);

		/* Create a multi-part message */
		final Multipart multipart;
		if ((localFiles2Attach != null) && (localFiles2Attach.length > 0)) {
			multipart = new MimeMultipart(MIXED_MULTIPART_SUBTYPE);
		} else {
			multipart = new MimeMultipart(DEFAULT_MULTIPART_SUBTYPE);
		}

		/* Create the message part */
		final BodyPart messageBodyPart = new MimeBodyPart();

		messageBodyPart.setDisposition(MimeBodyPart.INLINE);

		/* Fill the message */
		messageBodyPart.setContent(text,
				(String) (isHtml ? CONTENT_HTML : CONTENT_TEXT) + CONTENT_TYPE_CHARSET_PREFIX + codePage);

		/* Set text message part */
		multipart.addBodyPart(messageBodyPart);
		/* Add attachments */
		if ((localFiles2Attach != null) && (localFiles2Attach.length > 0)) {
			for (final String file2Attach : localFiles2Attach) {
				final BodyPart attach = new MimeBodyPart();
				final String filename = file2Attach;
				final AttachDataSource source = new AttachDataSource(filename);
				attach.setDataHandler(new DataHandler(source));
				attach.setFileName(MimeUtility.encodeText(source.getShortName(), codePage, null));
				multipart.addBodyPart(attach);
			}
		}

		/* Send the complete message parts */
		message.setContent(multipart);

		/* Set From: header field of the header. */
		message.setFrom(prepareRecipientsAddresses(from, codePage));

		Address[] recipientsArray;
		/* Set To: header field of the header. */
		logger.debug("Dodaje To: {}", to);
		recipientsArray = prepareRecipientsAddressesArray(to, codePage);
		message.setRecipients(Message.RecipientType.TO, recipientsArray);

		if (StringUtils.isNotBlank(replyTo)) {
			logger.debug("Dodaje ReplyTo: {}", replyTo);
			recipientsArray = prepareRecipientsAddressesArray(replyTo, codePage);
			message.setReplyTo(recipientsArray);
		}

		if (StringUtils.isNotBlank(cc)) {
			logger.debug("Dodaje CC: {}", cc);
			recipientsArray = prepareRecipientsAddressesArray(cc, codePage);
			message.setRecipients(Message.RecipientType.CC, recipientsArray);
		}
		if (StringUtils.isNotBlank(bcc)) {
			logger.debug("Dodaje BCC: {}", bcc);
			recipientsArray = prepareRecipientsAddressesArray(bcc, codePage);
			message.setRecipients(Message.RecipientType.BCC, recipientsArray);
		}

		/* Set Subject: header field */
		message.setSubject(StringUtils.isBlank(subject) ? EMPTY_SUBJECT : subject, codePage);

		/* Ustawianie dat wiadomości - data wysłania wiadomości */
		message.setSentDate((sentDate == null ? new Date() : sentDate));
		/* Ustawianie dat wiadomości - data przesłania wiadomości */
		if (receiveDate != null) {
			message.setHeader(MAIL_HEADER_RECEIVE_DATE,
					MimeUtility.encodeText(getMailDateFormat().format(receiveDate), codePage, null));
		}
		return message;
	}

	public static Address[] prepareRecipientsAddressesArray(String recipientsListString, String codePage)
			throws UnsupportedEncodingException {
		String[] tmpRecipientsArray;
		Address[] recipientsArray;
		tmpRecipientsArray = recipientsListString.split("\\,");
		recipientsArray = new Address[tmpRecipientsArray.length];
		for (int i = 0; i < tmpRecipientsArray.length; i++) {
			recipientsArray[i] = prepareRecipientsAddresses(tmpRecipientsArray[i], codePage);
			if (logger.isDebugEnabled()) {
				logger.debug("prepareRecipientsAddressesArray: {}", tmpRecipientsArray[i]);
			}
		}
		return recipientsArray;
	}

	public static Address prepareRecipientsAddresses(String recipientsString, String codePage)
			throws UnsupportedEncodingException {
		String personal;
		String email;

		String[] tmpRecipientArray = recipientsString.split("\\<");
		if (tmpRecipientArray.length > 1) {
			personal = lrtrim(tmpRecipientArray[0]);
			email = lrtrim(tmpRecipientArray[1].replaceAll(">", ""));
		} else if (tmpRecipientArray.length != 0) {
			personal = lrtrim(tmpRecipientArray[0]);
			email = lrtrim(tmpRecipientArray[0]);
		} else {
			throw new IllegalStateException("Dziwne...");
		}

		InternetAddress adres = new InternetAddress(email, personal, codePage);
		if (logger.isDebugEnabled()) {
			logger.debug("prepareRecipientsAddresses: {}", adres);
		}
		return adres;
	}

	/**
	 * Dopasowanie własności wiadomości do pola obiektu dto pierwszy znak jest małą
	 * literą oraz pominięcie separatorów
	 * 
	 * @param mailProperty
	 *            nazwa parametru/własności maila
	 * @return nazwa symulowanej kolumny
	 */
	public static String mailProperty2ColumnName(final String mailProperty) {
		return Character.toLowerCase(mailProperty.charAt(0)) + mailProperty.substring(1).replaceAll("-", "");
	}

	public static String buildDefaultForwardFooterInHTMLMsg() {
		if (StringUtils.isBlank(DEFAULT_FORWARD_FOOTER_HTML_MSG)) {
			StringBuilder sb = new StringBuilder();
			sb.append("<br/><br/><table>");
			sb.append("<tr>");
			sb.append("<td><a title=\"").append(MailBoxVersion.MailBoxOrgName).append("\" href=\"")
					.append(MailBoxVersion.MailBoxOrgUrl).append("\" target=\"blank\"><img title=\"")
					.append(MailBoxVersion.MailBoxOrgName)
					.append("\" src=\"cid:" + MailBoxVersion.SCISOFTWARE_LOGO_CID + "\" alt=\"")
					.append(MailBoxVersion.MailBoxOrgName).append(" logotype\"></a></td>");
			sb.append("<td style=\"vertical-align:bottom\">");
			sb.append(
					"<span style=\"font-size:12px;font-family:'Arial',sans-serif;color:#1F497D;mso-fareast-language:EN-US\">Mail forwarded by ")
					.append(MailBoxVersion.MailBoxName).append(" version ").append(MailBoxVersion.MailVersion)
					.append("</span>");
			sb.append(
					"<br/><span style=\"font-size:10px;font-family:'Arial',sans-serif;color:gray;mso-fareast-language:EN-US\"><i>Copyright &copy; "
							+ MailBoxVersion.MailBoxCopyrightYear + " <a href='")
					.append(MailBoxVersion.MailBoxOrgUrl).append("'>").append(MailBoxVersion.MailBoxOrgName)
					.append("</span>").append("");
			sb.append("</td>");
			sb.append("</tr>");
			sb.append("</table>");
			DEFAULT_FORWARD_FOOTER_HTML_MSG = sb.toString();

		}
		return DEFAULT_FORWARD_FOOTER_HTML_MSG;
	}

	public static String buildDefaultForwardFooterInTextMsg() {
		if (StringUtils.isBlank(DEFAULT_FORWARD_FOOTER_TEXT_MSG)) {
			StringBuilder sb = new StringBuilder();
			sb.append("\n\n");
			sb.append("Mail forwarded by ").append(MailBoxVersion.MailBoxName).append(" version ")
					.append(MailBoxVersion.MailVersion).append(".");
			sb.append("\nCopyright © " + MailBoxVersion.MailBoxCopyrightYear + " ")
					.append(MailBoxVersion.MailBoxOrgName).append(" ").append(MailBoxVersion.MailBoxOrgUrl);
			DEFAULT_FORWARD_FOOTER_TEXT_MSG = sb.toString();
		}
		return DEFAULT_FORWARD_FOOTER_TEXT_MSG;
	}

	public static BodyPart getAttachLogo() throws MessagingException {
		if (attachLogo == null) {
			attachLogo = new MimeBodyPart();
			final LogoDataSource source = new LogoDataSource();
			attachLogo.setDataHandler(new DataHandler(source));
			attachLogo.setDisposition("inline; filename=\"" + MailBoxVersion.SCISOFTWARE_LOGO_FILE_NAME + "\";");
			attachLogo.setDescription(MailBoxVersion.SCISOFTWARE_LOGO_FILE_NAME);
			attachLogo.setHeader("Content-ID", "<" + MailBoxVersion.SCISOFTWARE_LOGO_CID + ">");
		}
		return attachLogo;
	}

	public static String ltrim(String source) {
		return source.replaceAll("^\\s+", "");
	}

	public static String rtrim(String source) {
		return source.replaceAll("\\s+$", "");
	}

	public static String lrtrim(String source) {
		return ltrim(rtrim(source));
	}

	/**
	 * 
	 * FileNameStringValidator - wyciągnięta z schelper'a klasa walidacji znaków w
	 * nazwach plików.
	 *
	 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
	 * @version $Revision: 1.1 $
	 *
	 */
	public static class FileNameStringValidator {

		public static final char charReplacement = 95;

		public static boolean isInvalidChar(int c) {
			return ((c < 32) || ((c > 126) && (c < 160)) || (c == 34) || (c == 42) || (c == 47) || (c == 58)
					|| (c == 62) || (c == 63) || (c == 92) || (c == 124));
		}

		public static char getCharReplacement() {
			return charReplacement;
		}

		public static int[] invalidCharOff(String input) {
			int[] ret = { -1, -1 };
			if (input == null) {
				return ret;
			}
			int i = 0;
			int k = input.length();
			while (i < k) {
				int c = input.codePointAt(i);
				if (isInvalidChar(c)) {
					ret[0] = i;
					ret[1] = c;
					return ret;
				}
				++i;
			}
			return ret;
		}

		public static String invalidCharsRemoved(String input) {
			int[] s = invalidCharOff(input);
			if (s[0] == -1) {
				return input;
			}
			int i = s[0];
			int k = input.length();

			StringBuilder stringBuilder = new StringBuilder(input);
			stringBuilder.deleteCharAt(i);
			--k;
			while (i < k) {
				int c = stringBuilder.codePointAt(i);
				if (!(isInvalidChar(c))) {
					++i;
				}
				stringBuilder.deleteCharAt(i);
				--k;
			}

			return stringBuilder.toString();
		}

		public static String invalidCharsReplaced(String input) {
			int[] s = invalidCharOff(input);
			if (s[0] == -1) {
				return input;
			}
			int i = s[0];
			int k = input.length();
			char[] chars = input.toCharArray();
			char r = getCharReplacement();
			chars[(i++)] = r;
			while (i < k) {
				if (isInvalidChar(chars[i])) {
					chars[i] = r;
				}
				++i;
			}
			return new String(chars);
		}
	}

	/**
	 * Składa pełną ścieżkę do pliku wiadomości.
	 * 
	 * @param rootLocalDir
	 *            pełna ścieżka do lokalnego katalogu (na serwerze)
	 * @param dir
	 *            podkatalog (na serwerze) jeśli podany
	 * @param defaultFileName
	 *            domyślna nazwa pliku (gdy część wiadomości nie ma nazwy, to nazwa
	 *            pliku jest generowana w oparciu o wartość tu zadeklarowaną).
	 * @param prefix
	 *            przedrostek nazwy pliku lub null
	 * @param postfix
	 *            rozszerzenie nazwy pliku lub null
	 * @param encodePrefix
	 *            czy enkodować prefix nazwy pliku? wartość true dla ścieżek do
	 *            pliku "remote" (dostęp po HTTP(S)), dla ścieżek lokalnych false
	 * @param encoding
	 *            strona kodowa enkodowania np. 'UTF-8', może być null gdy
	 *            encodePrefix=false
	 * @return ścieżka do pliku z wiadomością
	 */
	public static String getPathMsg(final String rootLocalDir, final String dir, final String defaultFileName,
			final String prefix, final String postfix, boolean encodePrefix, String encoding) {
		StringBuilder sb = new StringBuilder().append(rootLocalDir).append(AttachDataSource.UNIX_SEPARATOR);
		if (dir != null) {
			sb.append(FileNameStringValidator.invalidCharsReplaced(dir)).append(AttachDataSource.UNIX_SEPARATOR);
		}
		if (prefix != null) {
			String separator = "#";
			try {
				separator = (encodePrefix ? URLEncoder.encode(separator, encoding) : separator);
			} catch (UnsupportedEncodingException e) {
				logger.warn("Nie udało się encodować separatora dla '" + encoding + "'", e);
			}
			sb.append(prefix).append(separator);
		}
		sb.append(FileNameStringValidator.invalidCharsReplaced(defaultFileName));
		if (postfix != null) {
			sb.append(postfix);
		}
		return sb.toString();
	}

	/**
	 * Pobranie ścieżki pliku wiadomości eml.
	 * 
	 * @param rootLocalDir
	 *            pełna ścieżka do lokalnego katalogu (na serwerze)
	 * @param dir
	 *            podkatalog (na serwerze) jeśli podany
	 * @param defaultFileName
	 *            domyślna nazwa pliku.
	 * @return nazwa pliku wiadomości
	 */
	public static String getPathMsgEml(final String rootLocalDir, final String dir, final String defaultFileName) {
		return MessageHelper.getPathMsg(rootLocalDir, dir, defaultFileName, null, ".eml", false, null);
	}
}
