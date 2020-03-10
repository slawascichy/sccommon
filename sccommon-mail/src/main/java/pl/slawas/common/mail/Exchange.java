package pl.slawas.common.mail;

import java.sql.ResultSet;
import java.util.List;

import pl.slawas.common.mail.exceptions.CommonMailException;
import pl.slawas.common.mail.smtp.api.MailConstants;

/**
 * Exchange - Integracja MS Exchange.
 * 
 * @author Sławomir Cichy &lt;scichy@ibpm.pro&gt;
 * 
 * @version $Revision: 1.23 $
 */
public class Exchange extends MailConstants {

	public static String getPasswordEncoderClass() {
		return Imap.getPasswordEncoderClass();
	}

	public static void setPasswordEncoderClass(String passwordEncoderClass) {
		Imap.setPasswordEncoderClass(passwordEncoderClass);
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
	 *            pobierania (wartość zero oznacza wszystkie).
	 * @param start
	 *            numer pierwszej z pobieranych wiadomości licząc od jeden.
	 * @return lista zawierająca listę wiadomości
	 * @throws CommonMailException
	 */
	public static ResultSet getMessages(String server, String username, String password, String mailBox,
			boolean flgDeleteRead, int limit, int start) throws CommonMailException {
		return Imap.getMessages(server, username, password, mailBox, flgDeleteRead, limit, start, true,
				EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER);
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
	 *            pobierania (wartość zero oznacza wszystkie).
	 * @param start
	 *            numer pierwszej z pobieranych wiadomości licząc od jeden.
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @return lista zawierająca listę wiadomości
	 * @throws CommonMailException
	 */
	public static ResultSet getMessages(String server, String username, String password, String mailBox,
			boolean flgDeleteRead, int limit, int start, final int connectiontimeout) throws CommonMailException {
		return Imap.getMessages(server, username, password, mailBox, flgDeleteRead, limit, start, true,
				EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER, connectiontimeout);
	}

	/**
	 * Pobieranie wiadomości na podstawie numeru wiadomości w folderze (obsługa
	 * folderu).
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
	 * @return wszystkie części wiadomości
	 * @throws CommonMailException
	 */
	public static ResultSet getMessage(String server, String username, String password, String mailBox,
			boolean flgDeleteRead, String messageNumber, String rootLocalDir, String rootHttpContext)
			throws CommonMailException {
		return Imap.getMessage(server, username, password, mailBox, flgDeleteRead, messageNumber, rootLocalDir,
				rootHttpContext, true, EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER, true, false,
				false);
	}

	/**
	 * Pobieranie wiadomości na podstawie numeru wiadomości w folderze (obsługa
	 * folderu).
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
	 * @param saveParts
	 *            czy zapisywać wiadomość jako pliki .txt i .html
	 * @param saveEml
	 *            czy zapisywać wiadomość jako plik .eml
	 * @param filterScripts
	 *            czy odfiltrować HTML ze skryptów
	 * @return wszystkie części wiadomości
	 * @throws CommonMailException
	 */
	public static ResultSet getMessage(String server, String username, String password, String mailBox,
			boolean flgDeleteRead, String messageNumber, String rootLocalDir, String rootHttpContext, boolean saveParts,
			boolean saveEml, boolean filterScripts) throws CommonMailException {
		return Imap.getMessage(server, username, password, mailBox, flgDeleteRead, messageNumber, rootLocalDir,
				rootHttpContext, true, EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER, saveParts,
				saveEml, filterScripts);
	}

	/**
	 * Pobieranie (wyszukiwanie) wiadomości z folderu na podstawie identyfikatorów
	 * wiadomości
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
	 * @param messageID
	 *            identyfikator wiadomości np. Message-ID: &lt;
	 *            41ef15b243ca4fef978354a6802e07a0@DB4PR06MB141.eurprd06.prod.ou t l
	 *            o o k . c o m >
	 * @param rootLocalDir
	 *            lokalny katalog na serwerze (pełna ścieżka)
	 * @param rootHttpContext
	 *            kontekst HTTP (tak aby można było pobrać plik po WWW)
	 * @return lista zawierająca listę wszystkich części wiadomości
	 * @throws CommonMailException
	 */
	public static List<ResultSet> getMessagesByIDs(String server, String username, String password, String mailBox,
			boolean flgDeleteRead, String[] messageIDs, String rootLocalDir, String rootHttpContext)
			throws CommonMailException {
		return Imap.getMessagesByIDs(server, username, password, mailBox, flgDeleteRead, messageIDs, rootLocalDir,
				rootHttpContext, true, EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER, true, false,
				false);
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
	 * @param messageID
	 *            identyfikator wiadomości np. Message-ID: &lt;
	 *            41ef15b243ca4fef978354a680/2e07a0@DB4PR06MB141.eurprd06.prod.o u t
	 *            l o o k . c o m >
	 * @param rootLocalDir
	 *            lokalny katalog na serwerze (pełna ścieżka)
	 * @param rootHttpContext
	 *            kontekst HTTP (tak aby można było pobrać plik po WWW)
	 * @param saveParts
	 *            czy zapisywać wiadomość jako pliki .txt i .html
	 * @param saveEml
	 *            czy zapisywać wiadomość jako plik .eml
	 * @param filterScripts
	 *            czy odfiltrować HTML ze skryptów
	 * @return lista zawierająca listę wszystkich części wiadomości
	 * @throws Exception
	 */
	public static List<ResultSet> getMessagesByIDs(String server, String username, String password, String mailBox,
			boolean flgDeleteRead, String[] messageIDs, String rootLocalDir, String rootHttpContext, boolean saveParts,
			boolean saveEml, boolean filterScripts) throws CommonMailException {
		return Imap.getMessagesByIDs(server, username, password, mailBox, flgDeleteRead, messageIDs, rootLocalDir,
				rootHttpContext, true, EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER, saveParts,
				saveEml, filterScripts);
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
		return Imap.getMessagePathByID(server, username, mailBox, messageID, rootLocalDir);
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
	 * @return lista folderów/skrzynek
	 * @throws CommonMailException
	 */
	public static ResultSet getMailBoxes(String server, String username, String password, String mailBox, Integer limit)
			throws CommonMailException {
		return Imap.getMailBoxes(server, username, password, mailBox, limit, true, EXHANGE_SOCKET_FACTORY_PROVIDER,
				EXHANGE_SOCKET_FACTORY_PROVIDER);

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
	 * @throws Exception
	 */
	public static void sendMail(String smtpServer, String smtpUsername, String smtpPassword, String smtpPort,
			boolean auth, boolean ssl, boolean tls, String imapServer, String imapUsername, String imapPassword,
			String mailBox, String from, String replyTo, String to, String cc, String bcc, String subject, String text,
			boolean isHTML, String localFiles2Attach) throws CommonMailException {
		sendMail(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, imapServer, imapUsername,
				imapPassword, mailBox, from, replyTo, to, cc, bcc, subject, text, isHTML, localFiles2Attach, null);
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
	 * @param codePage
	 *            strona kodowa na jaką wiadomość zostanie zamieniona przed
	 *            wysłaniem.
	 * @throws Exception
	 */
	public static void sendMail(String smtpServer, String smtpUsername, String smtpPassword, String smtpPort,
			boolean auth, boolean ssl, boolean tls, String imapServer, String imapUsername, String imapPassword,
			String mailBox, String from, String replyTo, String to, String cc, String bcc, String subject, String text,
			boolean isHTML, String localFiles2Attach, String codePage) throws CommonMailException {
		Imap.sendMail(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, imapServer, imapUsername,
				imapPassword, mailBox, from, replyTo, to, cc, bcc, subject, text, isHTML, localFiles2Attach, true,
				EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER, codePage);
	}

	/**
	 * Przesłanie oryginalnych wiadomości wg messageID i skopiowanie do folderu o
	 * nazwie outBox
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
	 *            nazwa skrzynki zawierającej przesyłaną wiadomość np. "Inbox"
	 * @param messageIDs
	 *            identyfikatory wiadomości np. Message-ID: &lt;
	 *            41ef15b243ca4fef978354a680/2e07a0@DB4PR06MB141.eurprd06.prod.o u t
	 *            l o o k . c o m >
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
	 *            nazwa skrzynki do której wstawiona zostanie przesłana wiadomość
	 *            np. "Sent"
	 * @param codePage
	 *            kod strony kodowej adresów
	 * @param connectiontimeout
	 *            wartość parametru {@code mail.imap.connectiontimeout}
	 * @throws CommonMailException
	 */
	public static void forwardByMessageIDs(final String smtpServer, final String smtpUsername,
			final String smtpPassword, final String smtpPort, final boolean auth, final boolean ssl, final boolean tls,
			final String imapServer, final String imapUsername, final String imapPassword, final String mailBox,
			final String[] messageIDs, final String from, final String to, final String replyTo, final String cc,
			final String bcc, final String outBox, final String codePage, final int connectiontimeout)
			throws CommonMailException {
		Imap.forwardByMessageIDs(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, imapServer,
				imapUsername, imapPassword, mailBox, messageIDs, from, to, replyTo, cc, bcc, outBox, codePage, true,
				EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER, connectiontimeout, null, null, false,
				false);
	}

	/**
	 * Przesłanie oryginalnych wiadomości wg messageID i skopiowanie do folderu o
	 * nazwie outBox
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
	 *            nazwa skrzynki zawierającej przesyłaną wiadomość np. "Inbox"
	 * @param messageIDs
	 *            identyfikatory wiadomości np. Message-ID: &lt;
	 *            41ef15b243ca4fef978354a680/2e07a0@DB4PR06MB141.eurprd06.prod.o u t
	 *            l o o k . c o m >
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
	 *            nazwa skrzynki do której wstawiona zostanie przesłana wiadomość
	 *            np. "Sent"
	 * @param codePage
	 *            kod strony kodowej adresów
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
			final String bcc, final String outBox, final String codePage, final int connectiontimeout,
			String additionalSubject, String additionalContent, boolean additionalContentIsHTML,
			boolean addAsAttachement) throws CommonMailException {
		Imap.forwardByMessageIDs(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, imapServer,
				imapUsername, imapPassword, mailBox, messageIDs, from, to, replyTo, cc, bcc, outBox, codePage, true,
				EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER, connectiontimeout, additionalSubject,
				additionalContent, additionalContentIsHTML, addAsAttachement);
	}

	/**
	 * Wysłanie wiadomości bez parametru ze stroną kodową (jest ustawiana domyślna
	 * {@code UTF-8}).
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
			boolean isHTML, String localFiles2Attach, String rootLocalDir, String rootHttpContext, boolean saveParts,
			final boolean saveAsEml) throws CommonMailException {
		return sendMail(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, imapServer, imapUsername,
				imapPassword, mailBox, from, replyTo, to, cc, bcc, subject, text, isHTML, localFiles2Attach,
				rootLocalDir, rootHttpContext, /* codePage */null, saveParts, saveAsEml);
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
	 * @throws CommonMailException
	 */
	public static ResultSet sendMail(String smtpServer, String smtpUsername, String smtpPassword, String smtpPort,
			boolean auth, boolean ssl, boolean tls, String imapServer, String imapUsername, String imapPassword,
			String mailBox, String from, String replyTo, String to, String cc, String bcc, String subject, String text,
			boolean isHTML, String localFiles2Attach, String rootLocalDir, String rootHttpContext, String codePage,
			boolean saveParts, final boolean saveAsEml) throws CommonMailException {
		return Imap.sendMail(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls, imapServer, imapUsername,
				imapPassword, mailBox, from, replyTo, to, cc, bcc, subject, text, isHTML, localFiles2Attach, true,
				EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER, rootLocalDir, rootHttpContext,
				codePage, saveParts, saveAsEml);
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
	 * @throws CommonMailException
	 */
	public static void deleteByMessageIDs(String server, String username, String password, String mailBox,
			String[] messageIDs) throws CommonMailException {
		Imap.deleteByMessageIDs(server, username, password, mailBox, messageIDs, true, EXHANGE_SOCKET_FACTORY_PROVIDER,
				EXHANGE_SOCKET_FACTORY_PROVIDER);
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
	 * @throws CommonMailException
	 */
	public static void setAsRead(String server, String username, String password, String mailBox, String messageNumber,
			boolean flgRead) throws CommonMailException {
		Imap.setAsRead(server, username, password, mailBox, messageNumber, flgRead, true,
				EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER);
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
	 *            identyfikatory wiadomości
	 * @param targetMailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Trash', do której wiadomość
	 *            ma zostać przeniesiona.
	 * @throws CommonMailException
	 */
	public static void moveByMessageIDs(String server, String username, String password, String sourceMailBox,
			String[] messageIDs, String targetMailBox) throws CommonMailException {
		Imap.moveByMessageIDs(server, username, password, sourceMailBox, messageIDs, targetMailBox, true,
				EXHANGE_SOCKET_FACTORY_PROVIDER, EXHANGE_SOCKET_FACTORY_PROVIDER);
	}

	/**
	 * Pomocnicza metoda kasowania pliku.
	 * 
	 * @param tmpFileName
	 *            nazwa pliku.
	 * @return wynik: Czy się udało skasować?
	 */
	public static boolean deleteTemporaryFile(String tmpFileName) {
		return Imap.deleteTemporaryFile(tmpFileName);

	}

	/**
	 * Tworzy obiekt wiadomości i zapisuje ją w pliku
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
	 *            email, albo "imie_i_nazwisko &lt;adres@email>"
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
	 *            ścieżka do pliku z wiadomością
	 * @param receiveDate
	 *            data przesłania wiadomości przez SMTP
	 * @param sentDate
	 *            data wysłania wiadomości przez użytkownika
	 * @return Ścieżka do pliku wiadomości
	 * @throws Exception
	 */
	public static String createMessageToFile(final String to, final String replyTo, final String cc, final String bcc,
			final String from, final String subject, final String body, final boolean isHTML, final String codePage,
			final String[] attachments, final String targetFilePath, final String receiveDate, final String sentDate)
			throws Exception {
		return Imap.createMessageToFile(to, replyTo, cc, bcc, from, subject, body, isHTML, codePage, attachments,
				targetFilePath, receiveDate, sentDate);
	}

	/**
	 * Zwraca format daty przekazywanej w parametrach jako ciąg znaków
	 * 
	 * @return format daty
	 */
	public static String getDateFormat() {
		return Imap.getDateFormat();
	}

}
