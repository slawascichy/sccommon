package pl.slawas.common.mail;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.mail.box.MailBox;
import pl.slawas.common.mail.box.api.IMailBox;
import pl.slawas.common.mail.exceptions.CommonMailException;
import pl.slawas.common.mail.helpers.MessageHelper;
import pl.slawas.common.mail.smtp.MailSender;
import pl.slawas.common.mail.smtp.api.IMailSender;
import pl.slawas.common.mail.sql.PartMetaData;
import pl.slawas.common.mail.sql.ResultSetHelper;

public class EmbededImap {

	/**
	 * Metoda pobierania listy wiadomości ze skrzynki odbiorczej o podanej nazwie.
	 * 
	 * @param resourceName
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param flgDeleteRead
	 *            czy kasować wiadomość po przeczytaniu?
	 * @param limit
	 *            maksymalna liczba wiadomości jaka ma być zwrócona podczas
	 *            pobierania.
	 * @param start
	 *            numer pierwszej z pobieranych wiadomości licząc od jeden.
	 * @return
	 * @throws CommonMailException
	 */
	public static ResultSet getMessages(final String resourceName, final String mailBox, final boolean flgDeleteRead,
			final int limit, final int start) throws CommonMailException {
		return getBox(resourceName, mailBox).loadAll(true, flgDeleteRead, limit, start);
	}

	/**
	 * Pobieranie wiadomości na podstawie numeru wiadomości w folderze (obsługa
	 * folderu).
	 * 
	 * @param resourceName
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
	public static ResultSet getMessage(final String resourceName, final String mailBox, final boolean flgDeleteRead,
			final String messageNumber, final String rootLocalDir, final String rootHttpContext,
			final boolean saveParts, final boolean saveEml, final boolean filterScripts) throws CommonMailException {
		return getBox(resourceName, mailBox).loadByMessageNumber(Integer.parseInt(messageNumber), flgDeleteRead,
				rootLocalDir, rootHttpContext, saveParts, saveEml, filterScripts);

	}

	/**
	 * Pobieranie (wyszukiwanie) wiadomości z folderu na podstawie identyfikatora
	 * wiadomości (obsługa pojedynczych wiadomości).
	 * 
	 * @param resourceName
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
	 * @param saveParts
	 *            czy zapisywać wiadomość jako pliki .txt i .html
	 * @param saveEml
	 *            czy zapisywać wiadomość jako plik .eml
	 * @param filterScripts
	 *            czy odfiltrować HTML ze skryptów
	 * @return lista zawierająca listę wszystkich części wiadomości
	 * @throws CommonMailException
	 */
	public static List<ResultSet> getMessagesByIDs(final String resourceName, String mailBox, boolean flgDeleteRead,
			String[] messageIDs, String rootLocalDir, String rootHttpContext, final boolean saveParts,
			final boolean saveEml, final boolean filterScripts) throws CommonMailException {
		final IMailBox box = getBox(resourceName, mailBox);
		return box.loadByMessageIDs(messageIDs, flgDeleteRead, rootLocalDir, rootHttpContext, saveParts, saveEml,
				filterScripts);

	}

	/**
	 * Pobranie ścieżki lokalnej do pliku wiadomości na podstawie identyfikatora
	 * wiadomości (obsługa pojedynczych wiadomości).
	 * 
	 * @param resourceName
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param messageID
	 *            identyfikator wiadomości np. Message-ID: &lt;
	 *            41ef15b243ca4fef978354a680/2e07a0@DB4PR06MB141.eurprd06.prod.o u t
	 *            l o o k . c o m >
	 * @param rootLocalDir
	 *            lokalny katalog na serwerze (pełna ścieżka)
	 * @return ścieżka do pliku wiadomości
	 * @throws CommonMailException
	 */
	public static String getMessagePathByID(final String resourceName, String mailBox, String messageID,
			String rootLocalDir) throws CommonMailException {
		final IMailBox box = getBox(resourceName, mailBox);
		String server = null;
		String username = null;
		try {
			box.getMailBoxProvider().getStore(true);
			server = box.getServer();
			username = box.getUsername();
		} catch (NoSuchProviderException e) {
			throw new CommonMailException(e);
		}
		return Imap.getMessagePathByID(server, username, mailBox, messageID, rootLocalDir);
	}

	/**
	 * 
	 * @param resourceName
	 * @param mailBox
	 *            domyślna nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param limit
	 *            maksymalna liczba folderów jaka ma być zwrócona podczas
	 *            pobierania.
	 * @return wszystkie części wiadomości jako wynik SQL
	 * @throws CommonMailException
	 */
	public static ResultSet getMailBoxes(final String resourceName, final String mailBox, final Integer limit)
			throws CommonMailException {
		return getBox(resourceName, mailBox).loadMailBoxes(limit);
	}

	/**
	 * Wysłanie wiadomości z obsługą strony kodowej.
	 * 
	 * 
	 * @param resourceName
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
	 * @return wszystkie części wiadomości jako wynik SQL
	 * @throws CommonMailException
	 */
	public static ResultSet sendMail(final String resourceName, final String mailBox, final String from,
			final String replyTo, final String to, final String cc, final String bcc, final String subject,
			final String text, final boolean isHTML, final String localFiles2Attach, final String rootLocalDir,
			final String rootHttpContext, final String codePage, final boolean saveParts, final boolean saveAsEml)
			throws CommonMailException {
		final Message msg = sendCurrentMessage(resourceName, from, replyTo, to, cc, bcc, subject, text, isHTML,
				localFiles2Attach, codePage);
		final IMailBox box = getBox(resourceName, mailBox);
		box.appendMessage(msg);
		String msgIdTmp;
		try {
			msgIdTmp = MimeUtility.decodeText(msg.getHeader(MessageHelper.MAIL_MESSAGE_ID_COLUMN)[0]);
			final String msgId = msgIdTmp.substring(1, msgIdTmp.length() - 1);
			return ResultSetHelper
					.transformMsg(msg, new PartMetaData(box), rootLocalDir, msgId, rootHttpContext,
							(new StringBuilder()).append(box.getServer()).append('-').append(box.getUsername())
									.append('-').append(msg.getMessageNumber()).toString(),
							saveParts, saveAsEml, false);
		} catch (MessagingException | IOException | SQLException e) {
			throw new CommonMailException(e);
		}
	}

	/**
	 * Wysłanie wiadomości z obsługą strony kodowej.
	 * 
	 * 
	 * @param resourceName
	 * @param mailBox
	 *            nazwa skrzynki, z której zostanie pobrana przesyłana wiadomość np.
	 *            "Inbox"
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
	 *            nazwa skrzynki wiadomości wysłanych (do której zostanie zapisana
	 *            wiadomość po wysłaniu np. "Sent")
	 * @param codePage
	 *            kod strony kodowej adresów
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
	public static void forwardByMessageIDs(final String resourceName, final String mailBox, final String[] messageIDs,
			final String from, final String to, final String replyTo, final String cc, final String bcc,
			final String outBox, final String codePage, String additionalSubject, String additionalContent,
			boolean additionalContentIsHTML, boolean addAsAttachement) throws CommonMailException {
		final IMailBox box = getBox(resourceName, mailBox);
		getSender(resourceName).forwardByMessageIDs(box, messageIDs, from, to, replyTo, cc, bcc, outBox, codePage,
				/* socketFactoryProvider */
				null,
				/* socketFactoryClass */
				null, additionalSubject, additionalContent, additionalContentIsHTML, addAsAttachement);
	}

	/**
	 * Usuwanie wiadomości ze skrzynki pocztowej.
	 * 
	 * @param resourceName
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param messageIDs
	 *            identyfikatory wiadomości w skrzynce
	 * 
	 * @throws CommonMailException
	 */
	public static void deleteByMessageIDs(final String resourceName, String mailBox, String[] messageIDs)
			throws CommonMailException {
		getBox(resourceName, mailBox).deleteByMessageIDs(messageIDs);
	}

	/**
	 * Ustawianie stanu wiadomości jako wiadomości przeczytanej/nie przeczytanej w
	 * zależności od wartości argumentu {@code flgRead}.
	 * 
	 * @param resourceName
	 * @param mailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox'
	 * @param messageNumber
	 *            numer wiadomości w skrzynce
	 * @param flgRead
	 *            wartość flagi oznaczającej stan wiadomości przeczytanej/nie
	 *            przeczytanej
	 * @throws CommonMailException
	 */
	public static void setAsRead(final String resourceName, String mailBox, String messageNumber, boolean flgRead)
			throws CommonMailException {
		getBox(resourceName, mailBox).setAsRead(Integer.parseInt(messageNumber), flgRead);
	}

	/**
	 * Przenoszenie wiadomości pomiędzy folderami (skrzynkami pocztowymi).
	 * 
	 * @param resourceName
	 * @param sourceMailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Inbox', z której wiadomość
	 *            ma zostać przeniesiona.
	 * @param messageIDs
	 *            identyfikatory wiadomości w skrzynce
	 * @param targetMailBox
	 *            nazwa skrzynki pocztowej/katalog np. 'Trash', do której wiadomość
	 *            ma zostać przeniesiona.
	 * @throws CommonMailException
	 */
	public static void moveByMessageIDs(final String resourceName, String sourceMailBox, String[] messageIDs,
			String targetMailBox) throws CommonMailException {
		getBox(resourceName, sourceMailBox).moveByMessageIDs(messageIDs, targetMailBox);
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

	/**
	 * Wspólna funkcja zwracająca skrzynkę pocztową
	 * 
	 * @param resourceName
	 * @param mailBox
	 * @return
	 */
	private static IMailBox getBox(String resourceName, String mailBox) {
		return new MailBox(resourceName, mailBox);
	}

	/**
	 * Wspólna funkcja wysyłająca wiadomość. Wynik nie może być nullowy!
	 * 
	 * @throws CommonMailException
	 */
	protected static Message sendCurrentMessage(final String resourceName, String from, String replyTo, String to,
			String cc, String bcc, String subject, String text, boolean isHTML, String localFiles2Attach,
			String codePage) throws CommonMailException {
		String[] localFiles2AttachArray = null;
		if (StringUtils.isNotBlank(localFiles2Attach)) {
			localFiles2AttachArray = localFiles2Attach.split("\\,");
		}
		Message msg = getSender(resourceName).send(from, replyTo, to, cc, bcc, subject, text, isHTML,
				localFiles2AttachArray,
				/* socketFactoryProvider */
				null,
				/* socketFactoryClass */
				null, codePage);

		if (msg != null) {
			return msg;
		}
		throw new CommonMailException("Nie udało się wysłać wiadomości.");
	}

	/**
	 * Wysyłanie wiadomości bez umieszczania jej w skrzynce "Sent"
	 * 
	 * @param resourceName
	 * @param from
	 *            dane od kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
	 * @param replyTo
	 *            dane do kogo wysyłana jest wiadomość: adres email, albo
	 *            "imie_i_nazwisko &lt;adres@email>"
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
	 *            kod strony kodowej adresów
	 * @throws CommonMailException
	 */
	public static void sendMessage(final String resourceName, String from, String replyTo, String to, String cc,
			String bcc, String subject, String text, boolean isHTML, String localFiles2Attach, String codePage)
			throws CommonMailException {
		sendCurrentMessage(resourceName, from, replyTo, to, cc, bcc, subject, text, isHTML, localFiles2Attach,
				codePage);
	}

	/**
	 * Wspólna funkcja ustawiająca wysyłacza poczty
	 */
	protected static IMailSender getSender(final String resourceName) {
		return new MailSender(resourceName);
	}

}
