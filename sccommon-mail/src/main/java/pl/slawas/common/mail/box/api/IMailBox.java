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
package pl.slawas.common.mail.box.api;

import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;

import javax.mail.Message;
import javax.mail.Session;

import pl.slawas.common.mail.exceptions.CommonMailException;

/**
 * MailBox - interfejs skrzynki/folderu poczty elektronicznej
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface IMailBox {

	/**
	 * @return nazwa serwera
	 */
	String getServer();

	/**
	 * @return nazwa użytkownika
	 */
	String getUsername();

	IMailBoxProvider getMailBoxProvider();

	/**
	 * Lista skrzynek/folderów
	 * 
	 * @param limit
	 *            limit przeczytanych pozycji
	 * @return
	 * @throws CommonMailException
	 */
	ResultSet loadMailBoxes(Integer limit) throws CommonMailException;

	/**
	 * Ładowanie listy wiadomości znajdujących się w danym folderze/skrzynce
	 * pocztowej.
	 * 
	 * @param partialfetch
	 *            flaga, czy czytamy wiadomości w całości
	 * @param flgDeleteRead
	 *            flaga, czy po przeczytaniu wiadomości wiadomość ma zostać
	 *            skasowana z folderu/skrzynki
	 * @param limit
	 *            maksymalna liczba wiadomości jaka ma być zwrócona podczas
	 *            pobierania (wartość zero oznacza wszystkie).
	 * @param start
	 *            numer pierwszej z pobieranych wiadomości licząc od jeden.
	 * @return
	 * @throws CommonMailException
	 */
	ResultSet loadAll(boolean partialfetch, boolean flgDeleteRead, int limit, int start) throws CommonMailException;

	/**
	 * Pobranie wiadomości na podstawie numeru wiadomości w folderze.
	 * 
	 * @param messageNumber
	 *            numer wiadomości w folderze
	 * @param flgDeleteRead
	 *            czy wiadomość, gdy zostanie przeczytana ma automatycznie zostać
	 *            skasowana?
	 * @param rootLocalDir
	 *            lokalny katalog, w którym zapisane zostaną poszczególne części
	 *            wiadomości
	 * @param rootHttpContext
	 *            kontekst HTTP, pod którym będą dostępne zapisane części
	 *            wiadomości, tak aby można je sobie było ściągnąć po WWW
	 * @param saveParts
	 *            czy mają być zapisane poszczególne (wszystkie) części wiadomości
	 * @param saveEml
	 *            czy wiadomość ma być zapisana jako plik EML
	 * @param filterScripts
	 *            czy odfiltrować HTML ze skryptów
	 * @return
	 * @throws CommonMailException
	 */
	ResultSet loadByMessageNumber(int messageNumber, boolean flgDeleteRead, String rootLocalDir, String rootHttpContext,
			boolean saveParts, boolean saveEml, boolean filterScripts) throws CommonMailException;

	/**
	 * Pobranie wiadomości na podstawie identyfikatorów.
	 * 
	 * @param messageIDs
	 *            identyfikatory wiadomości (wartości pola Message-ID)
	 * @param flgDeleteRead
	 *            czy wiadomość, gdy zostanie przeczytana ma automatycznie zostać
	 *            skasowana?
	 * @param rootLocalDir
	 *            lokalny katalog, w którym zapisane zostaną poszczególne części
	 *            wiadomości
	 * @param rootHttpContext
	 *            kontekst HTTP, pod którym będą dostępne zapisane części
	 *            wiadomości, tak aby można je sobie było ściągnąć po WWW
	 * @param saveParts
	 *            czy mają być zapisane poszczególne (wszystkie) części wiadomości
	 * @param saveEml
	 *            czy wiadomość ma być zapisana jako plik EML
	 * @param filterScripts
	 *            czy odfiltrować HTML ze skryptów
	 * @return
	 * @throws CommonMailException
	 */
	List<ResultSet> loadByMessageIDs(String[] messageIDs, boolean flgDeleteRead, String rootLocalDir,
			String rootHttpContext, boolean saveParts, boolean saveEml, boolean filterScripts)
			throws CommonMailException;

	/**
	 * Oznaczenie wiadomości jako przeczytanej/nieprzeczytanej
	 * 
	 * @param messageNumber
	 *            identyfikator wiadomości
	 * @param flgRead
	 *            flaga przeczytania wiadomości: {@code true} - wiadomość
	 *            przeczytana, {@code false} - wiadomość nie przeczytana
	 * @throws CommonMailException
	 */
	void setAsRead(int messageNumber, boolean flgRead) throws CommonMailException;

	/**
	 * Usunięcie wiadomości
	 * 
	 * @param messageIDs
	 *            identyfikatory wiadomości
	 * @throws CommonMailException
	 */
	void deleteByMessageIDs(String[] messageIDs) throws CommonMailException;

	/**
	 * Dodanie wiadomości
	 * 
	 * @param msg
	 *            obiekt wiadomości
	 * @throws CommonMailException
	 */
	void appendMessage(Message msg) throws CommonMailException;

	/**
	 * Przenoszenie wiadomości do innego folderu.
	 * 
	 * @param messageIDs
	 *            identyfikatory wiadomości
	 * @param targetMailBox
	 *            nazwa folderu/skrzynki, do którego ma zostać przeniesiona
	 *            wiadomość.
	 * @throws CommonMailException
	 */
	void moveByMessageIDs(String[] messageIDs, String targetMailBox) throws CommonMailException;

	/**
	 * @return the {@link #name}
	 */
	String getName();

	/**
	 * Przesyła oryginalne wiadomości wg listy messageID i przenosi do folderu o
	 * nazwie outBox
	 * 
	 * @param smtpSession
	 *            sesja smtp
	 * @param messageIDs
	 *            identyfikatory wiadomości do przesłania
	 * @param from
	 *            autorzy
	 * @param to
	 *            adresaci
	 * @param replyTo
	 *            adresaci
	 * @param cc
	 *            dodatkowi adresaci
	 * @param bcc
	 *            ukryci adresaci
	 * @param outBox
	 *            nazwa folderu wysłanych wiadomości
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
	 * @return treści przesłanych wiadomości
	 * @throws CommonMailException
	 */
	Collection<Message> forwardByMessageIDs(Session smtpSession, String[] messageIDs, String from, String to,
			String replyTo, String cc, String bcc, String outBox, String codePage, String additionalSubject,
			String additionalContent, boolean additionalContentIsHTML, boolean addAsAttachement)
			throws CommonMailException;

}
