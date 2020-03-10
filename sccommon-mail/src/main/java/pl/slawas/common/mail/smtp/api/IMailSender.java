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
package pl.slawas.common.mail.smtp.api;

import java.util.Collection;

import javax.mail.Message;

import pl.slawas.common.mail.box.api.IMailBox;
import pl.slawas.common.mail.exceptions.CommonMailException;

/**
 * IMailSender
 * 
 * <a href=
 * "https://javamail.java.net/nonav/docs/api/com/sun/mail/smtp/package-summary.html"
 * >Package com.sun.mail.smtp</a>
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface IMailSender {

	/**
	 * @return the {@link #mailSenderProvider}
	 */
	IMailSenderProvider getMailSenderProvider();

	Message send(String from, String replyTo, String to, String cc, String bcc, String subject, String text,
			boolean isHTML, String[] localFiles2Attach, String socketFactoryProvider, String socketFactoryClass,
			String codePage) throws CommonMailException;

	/**
	 * Przesłanie wiadomości wg listy identyfikatorów przesyłanych wiadomości.
	 * 
	 * @param box
	 *            nazwa skrzynki wiadomości przychodzących
	 * @param messageIDs
	 *            identyfikatory wiadomości w skrzynce pocztowej
	 * @param from
	 *            lista autorów
	 * @param to
	 *            lista adresatów
	 * @param replyTo
	 *            lista adresatów
	 * @param cc
	 *            lista dodatkowych adresatów
	 * @param bcc
	 *            lista ukrytych adresatów
	 * @param outBox
	 *            nazwa skrzynki wiadomości wysłanych
	 * @param codePage
	 *            kod strony kodowej adresów
	 * @param socketFactoryProvider
	 * @param socketFactoryClass
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
	 *            {@link MailConstants#FORWARD_CONTENT_TYPE}. W przeciwnym wypadku
	 *            treść wiadomości zostanie wstawiona do wiadomości podstawowej z
	 *            odpowiednim dodatkowym nagłówkiem.
	 * @return
	 * @throws CommonMailException
	 */
	Collection<Message> forwardByMessageIDs(IMailBox box, String[] messageIDs, String from, String to, String replyTo,
			String cc, String bcc, String outBox, String codePage, String socketFactoryProvider,
			String socketFactoryClass, String additionalSubject, String additionalContent,
			boolean additionalContentIsHTML, boolean addAsAttachement) throws CommonMailException;

}
