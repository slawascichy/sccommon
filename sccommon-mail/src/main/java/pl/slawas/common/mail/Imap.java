package pl.slawas.common.mail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.mail.box.MailBox;
import pl.slawas.common.mail.box.MailBoxVersion;
import pl.slawas.common.mail.box.api.IMailBox;
import pl.slawas.common.mail.box.api.IMailBoxProvider;
import pl.slawas.common.mail.box.api.IMailBoxProviderStandAlone;
import pl.slawas.common.mail.exceptions.CommonMailException;
import pl.slawas.common.mail.helpers.MailProtocol;
import pl.slawas.common.mail.helpers.MessageHelper;
import pl.slawas.common.mail.smtp.MailSender;
import pl.slawas.common.mail.smtp.api.IMailSender;
import pl.slawas.common.mail.smtp.api.IMailSenderProvider;
import pl.slawas.common.mail.smtp.api.IMailSenderProviderStandAlone;
import pl.slawas.common.mail.smtp.api.MailConstants;
import pl.slawas.common.mail.sql.PartMetaData;
import pl.slawas.common.mail.sql.ResultSetHelper;
import pl.slawas.security.PasswordEncoder;

/**
 * Imap - Integracja do komunikacji z IMAP'em
 * 
 * @author Sławomir Cichy &lt;scichy@ibpm.pro&gt;
 * 
 * @version $Revision: 1.23 $
 */
public class Imap extends MailConstants {

	private static final Logger logger = LoggerFactory.getLogger(Imap.class.getName());
	private static String passwordEncoderClass = PasswordEncoder.DEFAULT_ENCODER_CLASS;
	private static final Object passwordEncoderClassLock = new Object();

	public static String getPasswordEncoderClass() {
		synchronized (passwordEncoderClassLock) {
			return passwordEncoderClass;
		}
	}

	public static void setPasswordEncoderClass(String passwordEncoderClass) {
		synchronized (passwordEncoderClassLock) {
			Imap.passwordEncoderClass = passwordEncoderClass;
		}
	}

	/**
	 * 
	 * Metoda pobierania listy wiadomości ze skrzynki odbiorczej o podanej nazwie.
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param flgDeleteRead
	 *            czy kasować wiadomość po przeczytaniu?
	 * @param limit
	 *            maksymalna liczba wiadomości jaka ma być zwrócona podczas
	 *            pobierania.
	 * @param start
	 *            numer pierwszej z pobieranych wiadomości licząc od jeden.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @return lista zawierająca listę wiadomości
	 * @throws CommonMailException
	 */
	public static ResultSet getMessages(final String server, final String username, final String password,
			final String mailBox, final boolean flgDeleteRead, final Integer limit, final int start,
			final boolean startTSLEnable, final String socketFactoryProvider, final String socketFactoryClass)
			throws CommonMailException {
		return getMessages(server, username, password, mailBox, flgDeleteRead, limit, start, startTSLEnable,
				socketFactoryProvider, socketFactoryClass, defaultimapConnectiontimeout);
	}

	/**
	 * 
	 * Metoda pobierania listy wiadomości ze skrzynki odbiorczej o podanej nazwie.
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param flgDeleteRead
	 *            czy kasować wiadomość po przeczytaniu?
	 * @param limit
	 *            maksymalna liczba wiadomości jaka ma być zwrócona podczas
	 *            pobierania.
	 * @param start
	 *            numer pierwszej z pobieranych wiadomości licząc od jeden.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @return lista zawierająca listę wiadomości
	 * @throws CommonMailException
	 */
	public static ResultSet getMessages(final String server, final String username, final String password,
			final String mailBox, final boolean flgDeleteRead, final int limit, final int start,
			final boolean startTSLEnable, final String socketFactoryProvider, final String socketFactoryClass,
			final int connectiontimeout) throws CommonMailException {
		return getBox(server, username, password, mailBox, startTSLEnable, socketFactoryProvider, socketFactoryClass,
				connectiontimeout).loadAll(true, flgDeleteRead, limit, start);
	}

	/**
	 * Pobieranie wiadomości na podstawie numeru wiadomości w folderze (obsługa
	 * folderu).
	 * 
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param flgDeleteRead
	 *            czy kasować wiadomość po przeczytaniu?
	 * @param messageNumber
	 *            numer wiadomości do pobrania
	 * @param rootLocalDir
	 *            lokalny katalog na serwerze (pełna ścieżka)
	 * @param rootHttpContext
	 *            kontekst HTTP (tak aby można było pobrać plik po WWW)
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param saveParts
	 *            czy poszczególne części wiadomości mają być zapisane w katalogu
	 *            zdefiniowanym jako parametr {@code rootLocalDir}
	 * @param saveEml
	 *            czy cała wiadomość ma być zapisana w postaci pliku EML w katalogu
	 *            zdefiniowanym jako parametr {@code rootLocalDir}
	 * @param filterScripts
	 *            czy mają być odfiltrowane elementy skryptowe zawarte z treści
	 * @return wszystkie części wiadomości
	 * @throws CommonMailException
	 */
	public static ResultSet getMessage(final String server, final String username, final String password,
			final String mailBox, final boolean flgDeleteRead, final String messageNumber, final String rootLocalDir,
			final String rootHttpContext, final boolean startTSLEnable, final String socketFactoryProvider,
			final String socketFactoryClass, final boolean saveParts, final boolean saveEml,
			final boolean filterScripts) throws CommonMailException {
		return getMessage(server, username, password, mailBox, flgDeleteRead, messageNumber, rootLocalDir,
				rootHttpContext, startTSLEnable, socketFactoryProvider, socketFactoryClass, saveParts, saveEml,
				filterScripts, defaultimapConnectiontimeout);
	}

	/**
	 * Pobieranie wiadomości na podstawie numeru wiadomości w folderze (obsługa
	 * folderu).
	 * 
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param flgDeleteRead
	 *            czy kasować wiadomość po przeczytaniu?
	 * @param messageNumber
	 *            numer wiadomości do pobrania
	 * @param rootLocalDir
	 *            lokalny katalog na serwerze (pełna ścieżka)
	 * @param rootHttpContext
	 *            kontekst HTTP (tak aby można było pobrać plik po WWW)
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param saveParts
	 *            czy poszczególne części wiadomości mają być zapisane w katalogu
	 *            zdefiniowanym jako parametr {@code rootLocalDir}
	 * @param saveEml
	 *            czy cała wiadomość ma być zapisana w postaci pliku EML w katalogu
	 *            zdefiniowanym jako parametr {@code rootLocalDir}
	 * @param filterScripts
	 *            czy mają być odfiltrowane elementy skryptowe zawarte z treści
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @return wszystkie części wiadomości
	 * @throws CommonMailException
	 */
	public static ResultSet getMessage(final String server, final String username, final String password,
			final String mailBox, final boolean flgDeleteRead, final String messageNumber, final String rootLocalDir,
			final String rootHttpContext, final boolean startTSLEnable, final String socketFactoryProvider,
			final String socketFactoryClass, final boolean saveParts, final boolean saveEml,
			final boolean filterScripts, final int connectiontimeout) throws CommonMailException {
		return getBox(server, username, password, mailBox, startTSLEnable, socketFactoryProvider, socketFactoryClass,
				connectiontimeout).loadByMessageNumber(Integer.parseInt(messageNumber), flgDeleteRead, rootLocalDir,
						rootHttpContext, saveParts, saveEml, filterScripts);
	}

	/**
	 * Pobranie ścieżki lokalnej do pliku wiadomości na podstawie identyfikatora
	 * wiadomości (obsługa pojedynczych wiadomości).
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param messageID
	 *            identyfikator wiadomości np. Message-ID: &lt;
	 *            41ef15b243ca4fef978354a680/2e07a0@DB4PR06MB141.eurprd06.prod.o u t
	 *            l o o k . c o m >
	 * @param rootLocalDir
	 *            lokalny katalog na serwerze (pełna ścieżka)
	 * @return ścieżka do pliku wiadomości
	 */
	public static String getMessagePathByID(String server, String username, String mailBox, String messageID,
			String rootLocalDir) {
		return MessageHelper.getPathMsgEml(rootLocalDir, messageID,
				ResultSetHelper.getDefaultFileName(server, username, mailBox, messageID));
	}

	/**
	 * Pobieranie (wyszukiwanie) wiadomości z folderu na podstawie identyfikatora
	 * wiadomości (obsługa pojedynczych wiadomości).
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param flgDeleteRead
	 *            czy kasować wiadomość po przeczytaniu?
	 * @param messageIDs
	 *            identyfikatory wiadomości np. Message-ID: &lt;
	 *            41ef15b243ca4fef978354a6802e07a0@DB4PR06MB141.eurprd06.prod.ou t l
	 *            o o k . c o m >
	 * @param rootLocalDir
	 *            lokalny katalog na serwerze (pełna ścieżka)
	 * @param rootHttpContext
	 *            kontekst HTTP (tak aby można było pobrać plik po WWW)
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param saveParts
	 *            czy zapisywać wiadomość jako pliki .txt i .html
	 * @param saveEml
	 *            czy zapisywać wiadomość jako plik .eml
	 * @param filterScripts
	 *            czy odfiltrować HTML ze skryptów
	 * @return lista zawierająca listę wszystkich części wiadomości
	 * @throws CommonMailException
	 */
	public static List<ResultSet> getMessagesByIDs(final String server, final String username, final String password,
			final String mailBox, final boolean flgDeleteRead, String[] messageIDs, final String rootLocalDir,
			final String rootHttpContext, final boolean startTSLEnable, final String socketFactoryProvider,
			final String socketFactoryClass, final boolean saveParts, final boolean saveEml,
			final boolean filterScripts) throws CommonMailException {
		return getMessagesByIDs(server, username, password, mailBox, flgDeleteRead, messageIDs, rootLocalDir,
				rootHttpContext, startTSLEnable, socketFactoryProvider, socketFactoryClass, saveParts, saveEml,
				filterScripts, defaultimapConnectiontimeout);
	}

	/**
	 * Pobieranie (wyszukiwanie) wiadomości z folderu na podstawie identyfikatorów
	 * wiadomości.
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param flgDeleteRead
	 *            czy kasować wiadomość po przeczytaniu?
	 * @param messageIDs
	 *            identyfikatory wiadomości np. Message-ID: &lt;
	 *            41ef15b243ca4fef978354a6802e07a0@DB4PR06MB141.eurprd06.prod.ou t l
	 *            o o k . c o m >
	 * @param rootLocalDir
	 *            lokalny katalog na serwerze (pełna ścieżka)
	 * @param rootHttpContext
	 *            kontekst HTTP (tak aby można było pobrać plik po WWW)
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @param saveParts
	 *            czy zapisywać wiadomość jako pliki .txt i .html
	 * @param saveEml
	 *            czy zapisywać wiadomość jako plik .eml
	 * @param filterScripts
	 *            czy odfiltrować HTML ze skryptów
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @return lista zawierająca listę wszystkich części wiadomości
	 * @throws CommonMailException
	 */
	public static List<ResultSet> getMessagesByIDs(final String server, final String username, final String password,
			final String mailBox, final boolean flgDeleteRead, final String[] messageIDs, final String rootLocalDir,
			final String rootHttpContext, final boolean startTSLEnable, final String socketFactoryProvider,
			final String socketFactoryClass, final boolean saveParts, final boolean saveEml,
			final boolean filterScripts, final int connectiontimeout) throws CommonMailException {
		final IMailBox box = getBox(server, username, password, mailBox, startTSLEnable, socketFactoryProvider,
				socketFactoryClass, connectiontimeout);
		return box.loadByMessageIDs(messageIDs, flgDeleteRead, rootLocalDir, rootHttpContext, saveParts, saveEml,
				filterScripts);
	}

	/**
	 * Pobieranie listy skrzynek pocztowych (folderów)
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            domyślna nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param limit
	 *            maksymalna liczba folderów jaka ma być zwrócona podczas
	 *            pobierania.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @return lista folderów jaki wynik SQL
	 * @throws CommonMailException
	 */
	public static ResultSet getMailBoxes(final String server, final String username, final String password,
			final String mailBox, final Integer limit, final boolean startTSLEnable, final String socketFactoryProvider,
			final String socketFactoryClass) throws CommonMailException {
		return getMailBoxes(server, username, password, mailBox, limit, startTSLEnable, socketFactoryProvider,
				socketFactoryClass, defaultimapConnectiontimeout);
	}

	/**
	 * Pobieranie listy skrzynek pocztowych (folderów)
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            domyślna nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param limit
	 *            maksymalna liczba folderów jaka ma być zwrócona podczas
	 *            pobierania.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @return lista folderów jaki wynik SQL
	 * @throws CommonMailException
	 */
	public static ResultSet getMailBoxes(final String server, final String username, final String password,
			final String mailBox, final Integer limit, final boolean startTSLEnable, final String socketFactoryProvider,
			final String socketFactoryClass, final int connectiontimeout) throws CommonMailException {
		return getBox(server, username, password, mailBox, startTSLEnable, socketFactoryProvider, socketFactoryClass,
				connectiontimeout).loadMailBoxes(limit);
	}

	/**
	 * Wysłanie wiadomości z obsługą strony kodowej.
	 * 
	 * @param smtpServer
	 *            nazwa serwera SMTP
	 * @param smtpUsername
	 *            nazwa użytkownika poczty
	 * @param smtpPassword
	 *            hasło użytkownika poczty (zakodowane)
	 * @param smtpPort
	 *            port serwera SMTP
	 * @param auth
	 *            czy wymagane jest uwierzytelnianie serwera SMTP
	 * @param ssl
	 *            czy wymagane jest kodowanie SSL serwera SMTP
	 * @param tls
	 *            czy wymagane jest kodowanie TSL serwera SMTP
	 * @param imapServer
	 *            nazwa serwera IMAP
	 * @param imapUsername
	 *            nazwa użytkownika IMAP
	 * @param imapPassword
	 *            hasło użytkownika IMAP
	 * @param mailBox
	 *            nazwa skrzynki do której wstawiona zostanie wysyłana wiadomość np.
	 *            "Sent"
	 * @param from
	 *            dane od kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param replyTo
	 *            dane o osobie do kogo ma być wysyłana odpowiedź na wiadomość:
	 *            adres email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param to
	 *            dane do kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param cc
	 *            dane "do wiadomości" kogo wysyłana jest wiadomość: adres email,
	 *            albo "imie_i_nazwisko &lt;adres@email>"
	 * @param bcc
	 *            dane "ukrytego do wiadomości" kogo wysyłana jest wiadomość: adres
	 *            email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param subject
	 *            temat wiadomości
	 * @param text
	 *            treść wiadomości
	 * @param isHTML
	 *            czy treść jest w HTML?
	 * @param localFiles2Attach
	 *            ścieżki do lokalnego pliku z załącznikami odseparowane znakiem
	 *            przecinka.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param codePage
	 *            strona kodowa na jaką wiadomość zostanie zamieniona przed
	 *            wysłaniem.
	 * @throws CommonMailException
	 */
	public static Message sendMail(String smtpServer, String smtpUsername, String smtpPassword, String smtpPort,
			boolean auth, boolean ssl, boolean tls, String imapServer, String imapUsername, String imapPassword,
			String mailBox, String from, String replyTo, String to, String cc, String bcc, String subject, String text,
			boolean isHTML, String localFiles2Attach, boolean startTSLEnable, String socketFactoryProvider,
			String socketFactoryClass, String codePage) throws CommonMailException {
		return sendMail(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, imapServer, imapUsername,
				imapPassword, mailBox, from, replyTo, to, cc, bcc, subject, text, isHTML, localFiles2Attach,
				startTSLEnable, socketFactoryProvider, socketFactoryClass, codePage, defaultimapConnectiontimeout);
	}

	/**
	 * Wysłanie wiadomości z obsługą strony kodowej.
	 * 
	 * @param smtpServer
	 *            nazwa serwera SMTP
	 * @param smtpUsername
	 *            nazwa użytkownika poczty
	 * @param smtpPassword
	 *            hasło użytkownika poczty (zakodowane)
	 * @param smtpPort
	 *            port serwera SMTP
	 * @param auth
	 *            czy wymagane jest uwierzytelnianie serwera SMTP
	 * @param ssl
	 *            czy wymagane jest kodowanie SSL serwera SMTP
	 * @param tls
	 *            czy wymagane jest kodowanie TSL serwera SMTP
	 * @param imapServer
	 *            nazwa serwera IMAP
	 * @param imapUsername
	 *            nazwa użytkownika IMAP
	 * @param imapPassword
	 *            hasło użytkownika IMAP
	 * @param mailBox
	 *            nazwa skrzynki do której wstawiona zostanie wysyłana wiadomość np.
	 *            "Sent"
	 * @param from
	 *            dane od kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param replyTo
	 *            dane o osobie do kogo ma być wysyłana odpowiedź na wiadomość:
	 *            adres email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param to
	 *            dane do kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param cc
	 *            dane "do wiadomości" kogo wysyłana jest wiadomość: adres email,
	 *            albo "imie_i_nazwisko &lt;adres@email>"
	 * @param bcc
	 *            dane "ukrytego do wiadomości" kogo wysyłana jest wiadomość: adres
	 *            email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param subject
	 *            temat wiadomości
	 * @param text
	 *            treść wiadomości
	 * @param isHTML
	 *            czy treść jest w HTML?
	 * @param localFiles2Attach
	 *            ścieżki do lokalnego pliku z załącznikami odseparowane znakiem
	 *            przecinka.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param codePage
	 *            strona kodowa na jaką wiadomość zostanie zamieniona przed
	 *            wysłaniem.
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @throws CommonMailException
	 */
	public static Message sendMail(String smtpServer, String smtpUsername, String smtpPassword, String smtpPort,
			boolean auth, boolean ssl, boolean tls, String imapServer, String imapUsername, String imapPassword,
			String mailBox, String from, String replyTo, String to, String cc, String bcc, String subject, String text,
			boolean isHTML, String localFiles2Attach, boolean startTSLEnable, String socketFactoryProvider,
			String socketFactoryClass, String codePage, final int connectiontimeout) throws CommonMailException {
		final Message msg = sendMessage(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, from, replyTo,
				to, cc, bcc, subject, text, isHTML, localFiles2Attach, startTSLEnable, socketFactoryProvider,
				socketFactoryClass, codePage, connectiontimeout);
		/* zapis wiadomości do folderu wysyłki */
		getBox(imapServer, imapUsername, imapPassword, mailBox, startTSLEnable, socketFactoryProvider,
				socketFactoryClass, connectiontimeout).appendMessage(msg);
		return msg;
	}

	/**
	 * Wysłanie wiadomości.
	 * 
	 * @param smtpServer
	 *            nazwa serwera SMTP
	 * @param smtpUsername
	 *            nazwa użytkownika poczty
	 * @param smtpPassword
	 *            hasło użytkownika poczty (zakodowane)
	 * @param smtpPort
	 *            port serwera SMTP
	 * @param auth
	 *            czy wymagane jest uwierzytelnianie serwera SMTP
	 * @param ssl
	 *            czy wymagane jest kodowanie SSL serwera SMTP
	 * @param tls
	 *            czy wymagane jest kodowanie TSL serwera SMTP
	 * @param imapServer
	 *            nazwa serwera IMAP
	 * @param imapUsername
	 *            nazwa użytkownika IMAP
	 * @param imapPassword
	 *            hasło użytkownika IMAP
	 * @param mailBox
	 *            nazwa skrzynki do której wstawiona zostanie wysyłana wiadomość np.
	 *            "Sent"
	 * @param from
	 *            dane od kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param replyTo
	 *            dane o osobie do kogo ma być wysyłana odpowiedź na wiadomość:
	 *            adres email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param to
	 *            dane do kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param cc
	 *            dane "do wiadomości" kogo wysyłana jest wiadomość: adres email,
	 *            albo "imie_i_nazwisko &lt;adres@email>"
	 * @param bcc
	 *            dane "ukrytego do wiadomości" kogo wysyłana jest wiadomość: adres
	 *            email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param subject
	 *            temat wiadomości
	 * @param text
	 *            treść wiadomości
	 * @param isHTML
	 *            czy treść jest w HTML?
	 * @param localFiles2Attach
	 *            ścieżki do lokalnego pliku z załącznikami odseparowane znakiem
	 *            przecinka.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param rootLocalDir
	 *            miejsce do zapisu na dysku (tam gdzie są składowane ewentualne
	 *            załączniki)
	 * @param rootHttpContext
	 *            ścieżka (kontekst) HTTP, z którego można sobie pobrać załącznik.
	 * @param saveParts
	 *            czy zapisać na dysk elementy wiadomości lub całą wiadomość? -
	 *            pytanie podchwytliwe, bo pod zachowaniem poszczególnych części
	 *            wiadomości kryje się również przekazanie kontentu wiadomości do
	 *            BPM'a (bez zapisu wiadomości nie jest możliwym ustawienie kontentu
	 *            - przynajmniej teraz tak to działa).
	 * @param saveAsEml
	 *            czy zapisać na dysk elementy wiadomość w postaci pliku EML.
	 * @throws CommonMailException
	 */
	public static ResultSet sendMail(String smtpServer, String smtpUsername, String smtpPassword, String smtpPort,
			boolean auth, boolean ssl, boolean tls, String imapServer, String imapUsername, String imapPassword,
			String mailBox, String from, String replyTo, String to, String cc, String bcc, String subject, String text,
			boolean isHTML, String localFiles2Attach, boolean startTSLEnable, String socketFactoryProvider,
			String socketFactoryClass, String rootLocalDir, String rootHttpContext, final boolean saveParts,
			final boolean saveAsEml) throws CommonMailException {
		return sendMail(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, imapServer, imapUsername,
				imapPassword, mailBox, from, replyTo, to, cc, bcc, subject, text, isHTML, localFiles2Attach,
				startTSLEnable, socketFactoryProvider, socketFactoryClass, rootLocalDir, rootHttpContext, null,
				saveParts, saveAsEml);
	}

	/**
	 * Wysłanie wiadomości z obsługą strony kodowej.
	 * 
	 * 
	 * @param smtpServer
	 *            nazwa serwera SMTP
	 * @param smtpUsername
	 *            nazwa użytkownika poczty
	 * @param smtpPassword
	 *            hasło użytkownika poczty (zakodowane)
	 * @param smtpPort
	 *            port serwera SMTP
	 * @param auth
	 *            czy wymagane jest uwierzytelnianie serwera SMTP
	 * @param ssl
	 *            czy wymagane jest kodowanie SSL serwera SMTP
	 * @param tls
	 *            czy wymagane jest kodowanie TSL serwera SMTP
	 * @param imapServer
	 *            nazwa serwera IMAP
	 * @param imapUsername
	 *            nazwa użytkownika IMAP
	 * @param imapPassword
	 *            hasło użytkownika IMAP
	 * @param mailBox
	 *            nazwa skrzynki do której wstawiona zostanie wysyłana wiadomość np.
	 *            "Sent"
	 * @param from
	 *            dane od kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param replyTo
	 *            dane o osobie do kogo ma być wysyłana odpowiedź na wiadomość:
	 *            adres email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param to
	 *            dane do kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param cc
	 *            dane "do wiadomości" kogo wysyłana jest wiadomość: adres email,
	 *            albo "imie_i_nazwisko &lt;adres@email>"
	 * @param bcc
	 *            dane "ukrytego do wiadomości" kogo wysyłana jest wiadomość: adres
	 *            email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param subject
	 *            temat wiadomości
	 * @param text
	 *            treść wiadomości
	 * @param isHTML
	 *            czy treść jest w HTML?
	 * @param localFiles2Attach
	 *            ścieżki do lokalnego pliku z załącznikami odseparowane znakiem
	 *            przecinka.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param rootLocalDir
	 *            miejsce do zapisu na dysku (tam gdzie są składowane ewentualne
	 *            załączniki)
	 * @param rootHttpContext
	 *            ścieżka (kontekst) HTTP, z którego można sobie pobrać załącznik.
	 * @param codePage
	 *            strona kodowa na jaką wiadomość zostanie zamieniona przed
	 *            wysłaniem.
	 * @param saveParts
	 *            czy zapisać na dysk elementy wiadomości lub całą wiadomość? -
	 *            pytanie podchwytliwe, bo pod zachowaniem poszczególnych części
	 *            wiadomości kryje się również przekazanie kontentu wiadomości do
	 *            BPM'a (bez zapisu wiadomości nie jest możliwym ustawienie kontentu
	 *            - przynajmniej teraz tak to działa).
	 * @param saveAsEml
	 *            czy zapisać na dysk elementy wiadomość w postaci pliku EML.
	 * @throws Exception
	 */
	public static ResultSet sendMail(final String smtpServer, final String smtpUsername, final String smtpPassword,
			final String smtpPort, final boolean auth, final boolean ssl, final boolean tls, final String imapServer,
			final String imapUsername, final String imapPassword, final String mailBox, final String from,
			final String replyTo, final String to, final String cc, final String bcc, final String subject,
			final String text, final boolean isHTML, final String localFiles2Attach, final boolean startTSLEnable,
			final String socketFactoryProvider, final String socketFactoryClass, final String rootLocalDir,
			final String rootHttpContext, final String codePage, final boolean saveParts, final boolean saveAsEml)
			throws CommonMailException {
		return sendMail(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, imapServer, imapUsername,
				imapPassword, mailBox, from, replyTo, to, cc, bcc, subject, text, isHTML, localFiles2Attach,
				startTSLEnable, socketFactoryProvider, socketFactoryClass, rootLocalDir, rootHttpContext, codePage,
				defaultimapConnectiontimeout, saveParts, saveAsEml);
	}

	/**
	 * Wysłanie wiadomości z obsługą strony kodowej.
	 * 
	 * 
	 * @param smtpServer
	 *            nazwa serwera SMTP
	 * @param smtpUsername
	 *            nazwa użytkownika poczty
	 * @param smtpPassword
	 *            hasło użytkownika poczty (zakodowane)
	 * @param smtpPort
	 *            port serwera SMTP
	 * @param auth
	 *            czy wymagane jest uwierzytelnianie serwera SMTP
	 * @param ssl
	 *            czy wymagane jest kodowanie SSL serwera SMTP
	 * @param tls
	 *            czy wymagane jest kodowanie TSL serwera SMTP
	 * @param imapServer
	 *            nazwa serwera IMAP
	 * @param imapUsername
	 *            nazwa użytkownika IMAP
	 * @param imapPassword
	 *            hasło użytkownika IMAP
	 * @param mailBox
	 *            nazwa skrzynki do której wstawiona zostanie wysyłana wiadomość np.
	 *            "Sent"
	 * @param from
	 *            dane od kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param replyTo
	 *            dane o osobie do kogo ma być wysyłana odpowiedź na wiadomość:
	 *            adres email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param to
	 *            dane do kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param cc
	 *            dane "do wiadomości" kogo wysyłana jest wiadomość: adres email,
	 *            albo "imie_i_nazwisko &lt;adres@email>"
	 * @param bcc
	 *            dane "ukrytego do wiadomości" kogo wysyłana jest wiadomość: adres
	 *            email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param subject
	 *            temat wiadomości
	 * @param text
	 *            treść wiadomości
	 * @param isHTML
	 *            czy treść jest w HTML?
	 * @param localFiles2Attach
	 *            ścieżki do lokalnego pliku z załącznikami odseparowane znakiem
	 *            przecinka.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param rootLocalDir
	 *            miejsce do zapisu na dysku (tam gdzie są składowane ewentualne
	 *            załączniki)
	 * @param rootHttpContext
	 *            ścieżka (kontekst) HTTP, z którego można sobie pobrać załącznik.
	 * @param codePage
	 *            strona kodowa na jaką wiadomość zostanie zamieniona przed
	 *            wysłaniem.
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @param saveParts
	 *            czy zapisać na dysk elementy wiadomości lub całą wiadomość? -
	 *            pytanie podchwytliwe, bo pod zachowaniem poszczególnych części
	 *            wiadomości kryje się również przekazanie kontentu wiadomości do
	 *            BPM'a (bez zapisu wiadomości nie jest możliwym ustawienie kontentu
	 *            - przynajmniej teraz tak to działa).
	 * @param saveAsEml
	 *            czy zapisać na dysk elementy wiadomość w postaci pliku EML.
	 * @throws CommonMailException
	 */
	public static ResultSet sendMail(final String smtpServer, final String smtpUsername, final String smtpPassword,
			final String smtpPort, final boolean auth, final boolean ssl, final boolean tls, final String imapServer,
			final String imapUsername, final String imapPassword, final String mailBox, final String from,
			final String replyTo, final String to, final String cc, final String bcc, final String subject,
			final String text, final boolean isHTML, final String localFiles2Attach, final boolean startTSLEnable,
			final String socketFactoryProvider, final String socketFactoryClass, final String rootLocalDir,
			final String rootHttpContext, final String codePage, final int connectiontimeout, final boolean saveParts,
			final boolean saveAsEml) throws CommonMailException {

		final Message msg = sendMessage(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, from, replyTo,
				to, cc, bcc, subject, text, isHTML, localFiles2Attach, startTSLEnable, socketFactoryProvider,
				socketFactoryClass, codePage, connectiontimeout);

		final IMailBox box = getBox(imapServer, imapUsername, imapPassword, mailBox, startTSLEnable,
				socketFactoryProvider, socketFactoryClass, connectiontimeout);
		box.appendMessage(msg);
		String msgIdTmp;
		try {
			msgIdTmp = MimeUtility.decodeText(msg.getHeader(MessageHelper.MAIL_MESSAGE_ID_COLUMN)[0]);
			final String msgId = msgIdTmp.substring(1, msgIdTmp.length() - 1);
			return ResultSetHelper.transformMsg(msg, new PartMetaData(box), rootLocalDir, msgId, rootHttpContext,
					(new StringBuilder()).append(smtpServer).append('-').append(smtpUsername).append('-')
							.append(msg.getMessageNumber()).toString(),
					saveParts, saveAsEml, false);
		} catch (MessagingException | IOException | SQLException e) {
			throw new CommonMailException(e);
		}
	}

	/**
	 * Usuwanie wiadomości ze skrzynki pocztowej.
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param messageIDs
	 *            identyfikatory wiadomości w skrzynce
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @throws CommonMailException
	 */
	public static void deleteByMessageIDs(String server, String username, String password, String mailBox,
			String[] messageIDs, boolean startTSLEnable, String socketFactoryProvider, String socketFactoryClass)
			throws CommonMailException {
		deleteByMessageIDs(server, username, password, mailBox, messageIDs, startTSLEnable, socketFactoryProvider,
				socketFactoryClass, defaultimapConnectiontimeout);
	}

	/**
	 * Usuwanie wiadomości ze skrzynki pocztowej.
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param messageIDs
	 *            identyfikatory wiadomości w skrzynce
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @throws CommonMailException
	 */
	public static void deleteByMessageIDs(String server, String username, String password, String mailBox,
			String[] messageIDs, boolean startTSLEnable, String socketFactoryProvider, String socketFactoryClass,
			final int connectiontimeout) throws CommonMailException {
		getBox(server, username, password, mailBox, startTSLEnable, socketFactoryProvider, socketFactoryClass,
				connectiontimeout).deleteByMessageIDs(messageIDs);
	}

	/**
	 * Ustawianie stanu wiadomości jako wiadomości przeczytanej/nie przeczytanej w
	 * zależności od wartości argumentu {@code flgRead}.
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param messageNumber
	 *            numer wiadomości w skrzynce
	 * @param flgRead
	 *            wartość flagi oznaczającej stan wiadomości przeczytanej/nie
	 *            przeczytanej
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @throws CommonMailException
	 */
	public static void setAsRead(String server, String username, String password, String mailBox, String messageNumber,
			boolean flgRead, boolean startTSLEnable, String socketFactoryProvider, String socketFactoryClass)
			throws CommonMailException {
		setAsRead(server, username, password, mailBox, messageNumber, flgRead, startTSLEnable, socketFactoryProvider,
				socketFactoryClass, defaultimapConnectiontimeout);
	}

	/**
	 * Ustawianie stanu wiadomości jako wiadomości przeczytanej/nie przeczytanej w
	 * zależności od wartości argumentu {@code flgRead}.
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param messageNumber
	 *            numer wiadomości w skrzynce
	 * @param flgRead
	 *            wartość flagi oznaczającej stan wiadomości przeczytanej/nie
	 *            przeczytanej
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @throws Exception
	 */
	public static void setAsRead(String server, String username, String password, String mailBox, String messageNumber,
			boolean flgRead, boolean startTSLEnable, String socketFactoryProvider, String socketFactoryClass,
			final int connectiontimeout) throws CommonMailException {
		getBox(server, username, password, mailBox, startTSLEnable, socketFactoryProvider, socketFactoryClass,
				connectiontimeout).setAsRead(Integer.parseInt(messageNumber), flgRead);
	}

	/**
	 * Przenoszenie wiadomości pomiędzy folderami (skrzynkami pocztowymi).
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param sourceMailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox', z której wiadomość
	 *            ma zostać przeniesiona.
	 * @param messageIDs
	 *            identyfikatory wiadomości w skrzynce
	 * @param targetMailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Trash', do której wiadomość
	 *            ma zostać przeniesiona.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @throws CommonMailException
	 */
	public static void moveByMessageIDs(String server, String username, String password, String sourceMailBox,
			String[] messageIDs, String targetMailBox, boolean startTSLEnable, String socketFactoryProvider,
			String socketFactoryClass) throws CommonMailException {
		moveByMessageIDs(server, username, password, sourceMailBox, messageIDs, targetMailBox, startTSLEnable,
				socketFactoryProvider, socketFactoryClass, defaultimapConnectiontimeout);
	}

	/**
	 * Przenoszenie wiadomości pomiędzy folderami (skrzynkami pocztowymi).
	 * 
	 * @param server
	 *            nazwa serwera pocztowego
	 * @param username
	 *            nazwa użytkownika
	 * @param password
	 *            hasło użytkownika
	 * @param sourceMailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox', z której wiadomość
	 *            ma zostać przeniesiona.
	 * @param messageIDs
	 *            identyfikatory wiadomości w skrzynce
	 * @param targetMailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Trash', do której wiadomość
	 *            ma zostać przeniesiona.
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @throws CommonMailException
	 */
	public static void moveByMessageIDs(String server, String username, String password, String sourceMailBox,
			String[] messageIDs, String targetMailBox, boolean startTSLEnable, String socketFactoryProvider,
			String socketFactoryClass, final int connectiontimeout) throws CommonMailException {
		getBox(server, username, password, sourceMailBox, startTSLEnable, socketFactoryProvider, socketFactoryClass,
				connectiontimeout).moveByMessageIDs(messageIDs, targetMailBox);
	}

	/**
	 * Pomocnicza metoda kasowania pliku.
	 * 
	 * @param tmpFileName
	 *            nazwa pliku.
	 * @return wynik: Czy się udało skasować?
	 */
	public static boolean deleteTemporaryFile(String tmpFileName) {
		File f = new File(tmpFileName);
		return f.delete();
	}

	/**
	 * Wspólna funkcja ustawiająca wysyłacza poczty
	 */
	protected static IMailSender getSender(final String smtpServer, final String smtpUsername,
			final String smtpPassword, final String smtpPort, final boolean auth, final boolean ssl, final boolean tls,
			final int connectiontimeout) {
		IMailSender sender = new MailSender(smtpServer, smtpUsername, getPasswordEncoderClass());
		IMailSenderProvider provider = sender.getMailSenderProvider();
		if (provider instanceof IMailSenderProviderStandAlone) {
			IMailSenderProviderStandAlone standAlone = (IMailSenderProviderStandAlone) provider;
			standAlone.setPassword(smtpPassword);
			standAlone.setPort(smtpPort);
			standAlone.setAuth(auth);
			standAlone.setSsl(ssl);
			standAlone.setTls(tls);
			standAlone.setConnectiontimeout(connectiontimeout);
		}
		return sender;
	}

	/**
	 * Wspólna funkcja wysyłająca wiadomość. Wynik nie może być nullowy!
	 */
	protected static Message sendMessage(String smtpServer, String smtpUsername, String smtpPassword, String smtpPort,
			boolean auth, boolean ssl, boolean tls, String from, String replyTo, String to, String cc, String bcc,
			String subject, String text, boolean isHTML, String localFiles2Attach, boolean startTSLEnable,
			String socketFactoryProvider, String socketFactoryClass, String codePage, int connectiontimeout)
			throws CommonMailException {
		String[] localFiles2AttachArray = null;
		if (StringUtils.isNotBlank(localFiles2Attach)) {
			localFiles2AttachArray = localFiles2Attach.split("\\,");
		}
		Message msg = getSender(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, connectiontimeout)
				.send(from, replyTo, to, cc, bcc, subject, text, isHTML, localFiles2AttachArray, socketFactoryProvider,
						socketFactoryClass, codePage);
		if (msg != null) {
			return msg;
		}
		throw new CommonMailException("Nie udało się wysłać wiadomości.");
	}

	/**
	 * Wysłanie wiadomości z obsługą strony kodowej.
	 * 
	 * @param smtpServer
	 *            nazwa serwera SMTP
	 * @param smtpUsername
	 *            nazwa użytkownika poczty
	 * @param smtpPassword
	 *            hasło użytkownika poczty (zakodowane)
	 * @param smtpPort
	 *            port serwera SMTP
	 * @param auth
	 *            czy wymagane jest uwierzytelnianie serwera SMTP
	 * @param ssl
	 *            czy wymagane jest kodowanie SSL serwera SMTP
	 * @param tls
	 *            czy wymagane jest kodowanie TSL serwera SMTP
	 * @param imapServer
	 *            nazwa serwera IMAP
	 * @param imapUsername
	 *            nazwa użytkownika IMAP
	 * @param imapPassword
	 *            hasło użytkownika IMAP
	 * @param mailBox
	 *            nazwa skrzynki wiadomości wysłanych, z której pobrana zostanie
	 *            przesyłana wiadomość.
	 * @param messageIDs
	 *            lista identyfikatorów wiadomości do przesłania
	 * @param from
	 *            dane od kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param to
	 *            dane do kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param replyTo
	 *            dane do kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param cc
	 *            dane "do wiadomości" kogo wysyłana jest wiadomość: adres email,
	 *            albo "imie_i_nazwisko &lt;adres@email>"
	 * @param bcc
	 *            dane "ukrytego do wiadomości" kogo wysyłana jest wiadomość: adres
	 *            email, albo "imie_i_nazwisko &lt;adres@email>"
	 * @param outBox
	 *            nazwa skrzynki do której wstawiona zostanie wysyłana wiadomość np.
	 *            "Sent"
	 * @param codePage
	 *            kod strony kodowej adresów
	 * @param startTSLEnable
	 *            wartość parametru {@code mail.imap.starttls.enable}
	 * @param socketFactoryProvider
	 *            wartość parametru {@code ssl.SocketFactory.provider}
	 * @param socketFactoryClass
	 *            wartość parametru {@code mail.imap.socketFactory.class}
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @param additionalSubject
	 *            dodatkowy/zmieniony temat dla forwardowanej wiadomości
	 * @param additionalContent
	 *            dodatkowa treść dołączona do wiadomości
	 * @param additionalContentIsHTML
	 *            flaga czy dodatkowa treść jest kodem HTML
	 * @param addAsAttachement
	 *            flaga czy wiadomość ma zostać przesłana jako załącznik. Wartość
	 *            {@code true}, oznacza, że przesyłana wiadomość zostanie dodana
	 *            jako załącznik z 'Content-Type' ustawionym na
	 *            {@link IMailOptions#FORWARD_CONTENT_TYPE}. W przeciwnym wypadku
	 *            treść wiadomości zostanie wstawiona do wiadomości podstawowej z
	 *            odpowiednim dodatkowym nagłówkiem.
	 * @throws CommonMailException
	 */
	public static void forwardByMessageIDs(final String smtpServer, final String smtpUsername,
			final String smtpPassword, final String smtpPort, final boolean auth, final boolean ssl, final boolean tls,
			final String imapServer, final String imapUsername, final String imapPassword, final String mailBox,
			final String[] messageIDs, final String from, final String to, final String replyTo, final String cc,
			final String bcc, final String outBox, final String codePage, final boolean startTSLEnable,
			final String socketFactoryProvider, final String socketFactoryClass, int connectiontimeout,
			String additionalSubject, String additionalContent, boolean additionalContentIsHTML,
			boolean addAsAttachement) throws CommonMailException {
		if (connectiontimeout < 1) {
			connectiontimeout = defaultimapConnectiontimeout;
		}
		final IMailBox box = getBox(imapServer, imapUsername, imapPassword, mailBox, startTSLEnable,
				socketFactoryProvider, socketFactoryClass, connectiontimeout);
		getSender(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, connectiontimeout)
				.forwardByMessageIDs(box, messageIDs, from, to, replyTo, cc, bcc, outBox, codePage,
						socketFactoryProvider, socketFactoryClass, additionalSubject, additionalContent,
						additionalContentIsHTML, addAsAttachement);
	}

	/**
	 * Wspólna funkcja zwracająca skrzynkę pocztową
	 * 
	 * @param imapServer
	 * @param imapUsername
	 * @param imapPassword
	 * @param mailBox
	 * @param startTSLEnable
	 * @param socketFactoryProvider
	 * @param socketFactoryClass
	 * @param connectiontimeout
	 * @return
	 */
	private static IMailBox getBox(String imapServer, String imapUsername, String imapPassword, String mailBox,
			boolean startTSLEnable, String socketFactoryProvider, String socketFactoryClass, int connectiontimeout) {

		if (logger.isDebugEnabled()) {
			logger.debug("-->getBox: imapServer: {}, imapUsername: {}, mailBox:{}", imapServer, imapUsername, mailBox);
		}
		IMailBox box = new MailBox(MailProtocol.imap, imapServer, imapUsername, mailBox, getPasswordEncoderClass());
		IMailBoxProvider provider = box.getMailBoxProvider();
		if (provider instanceof IMailBoxProviderStandAlone) {
			IMailBoxProviderStandAlone standAlone = (IMailBoxProviderStandAlone) provider;
			standAlone.setPassword(imapPassword);
			/* dodatkowe parametry */
			standAlone.setStartTSLEnable(startTSLEnable);
			standAlone.setSocketFactoryProvider(socketFactoryProvider);
			standAlone.setSocketFactoryClass(socketFactoryClass);
			standAlone.setConnectiontimeout(connectiontimeout);
		} else {
			throw new IllegalAccessError("Nieprawidłowa implementacja dostawcy skrzynki pocztowej.");
		}
		return box;
	}

	/**
	 * Zwraca format daty przekazywanej w parametrach jako ciąg znaków
	 * 
	 * @return format daty
	 */
	public static String getDateFormat() {
		return MessageHelper.DATE_FORMAT;
	}

	/**
	 * Tworzy wiadomość i zapisuje ją w pliku
	 * 
	 * @param to
	 *            dane do kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param replyTo
	 *            dane do kogo miała być wysłana ewentualna odpowiedź na wiadomość
	 * @param cc
	 *            dane "do wiadomości" kogo wysyłana jest wiadomość: adres email,
	 *            albo "imie_i_nazwisko &lt;adres@email>"
	 * @param bcc
	 *            dane "ukryte do wiadomości" kogo wysyłana jest wiadomość: adres
	 *            email, albo "imie_i_nazwisko &lt;adres@email>" *
	 * @param from
	 *            dane od kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param subject
	 *            temat wiadomości
	 * @param body
	 *            treść wiadomości
	 * @param isHTML
	 *            czy treść jest w HTML?
	 * @param codePage
	 *            strona kodowa na jaką wiadomość zostanie zamieniona przed zapisem.
	 * @param attachments
	 *            załączniki do wiadomości
	 * @param targetFilePath
	 *            ścieżka do lokalizacji pliku z zachowaną wiadomością
	 * @param receiveDate
	 *            data przesłania wiadomości przez SMTP
	 * @param sentDate
	 *            data wysłania wiadomości przez użytkownika
	 * @return Ścieżka do pliku wiadomości
	 * @throws MessagingException
	 * @throws CommonMailException
	 * @throws IOException
	 */
	public static String createMessageToFile(final String to, final String replyTo, final String cc, final String bcc,
			final String from, final String subject, final String body, final boolean isHTML, final String codePage,
			final String[] attachments, final String targetFilePath, final String receiveDate, final String sentDate)
			throws MessagingException, CommonMailException, IOException {

		if (StringUtils.isBlank(targetFilePath)) {
			throw new CommonMailException("Parametr 'targetFilePath' nie może być pusty.");
		}

		/* setup message */
		Message msg = MessageHelper.prepareMsgForSave(from, replyTo, to, cc, bcc, subject, body, isHTML, attachments,
				codePage, receiveDate, sentDate);

		if (msg == null) {
			throw new CommonMailException("Nie udało się utworzyć wiadomości.");
		}

		/* store message to file */
		String outputPathFileName = targetFilePath;
		File f = new File(targetFilePath);
		if (f.isDirectory()) {
			/*
			 * podana ścieżka z informacją gdzie ma być zapisany plik z wiadomością to tylko
			 * katalog - trzeba spreparować nazwę wiadomości...
			 */
			String localMessageFileName = UUID.randomUUID().toString() + "@" + MailBoxVersion.MailBoxArtefactId + "-"
					+ MailBoxVersion.MailVersion;
			outputPathFileName = MessageHelper.getPathMsg(targetFilePath, /* dir */
					null, localMessageFileName, /* prefix */null, /* postfix */
					".eml", /* encodePrefix */true, MessageHelper.DEFAULT_CHARSET);
			f = new File(outputPathFileName);
		}
		try (OutputStream os = new FileOutputStream(f);) {
			msg.writeTo(os);
		}
		return outputPathFileName;
	}
}
