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

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.mail.box.api.IMailBox;
import pl.slawas.common.mail.exceptions.CommonMailException;
import pl.slawas.common.mail.helpers.MessageHelper;
import pl.slawas.common.mail.smtp.api.IMailSender;
import pl.slawas.common.mail.smtp.api.IMailSenderProvider;
import pl.slawas.common.mail.smtp.api.IMailSenderProviderStandAlone;
import pl.slawas.common.mail.smtp.api.MailConstants;

/**
 * MailSender
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class MailSender extends MailConstants implements IMailSender {

	private final Logger logger = LoggerFactory.getLogger(MailSender.class.getName());

	private final IMailSenderProvider mailSenderProvider;

	public MailSender(String host, String username, String passwordEncoderClass) {
		super();
		this.mailSenderProvider = new MailSenderProviderStandAlone(host, username);
		this.mailSenderProvider.setPasswordEncoderClass(passwordEncoderClass);
	}

	public MailSender(String resourceJndiName) {
		super();
		this.mailSenderProvider = new MailSenderProviderEmbedded(resourceJndiName);
	}

	@Override
	public Message send(String from, String replyTo, String to, String cc, String bcc, String subject, String text,
			boolean isHTML, String[] localFiles2Attach, String socketFactoryProvider, String socketFactoryClass,
			String codePage) throws CommonMailException {

		if (mailSenderProvider instanceof IMailSenderProviderStandAlone) {
			((IMailSenderProviderStandAlone) mailSenderProvider).setSocketFactoryClass(socketFactoryClass);
		}
		Session session = mailSenderProvider.getSession();
		/* przygotowanie obiektu wiadomości */
		Message msg;
		try {
			msg = MessageHelper.prepareMsgForSend(session, from, replyTo, to, cc, bcc, subject, text, isHTML,
					localFiles2Attach, codePage);
			if (msg == null) {
				throw new CommonMailException("Nie udało się wysłać wiadomości.");
			}
			/* Sent message date */
			msg.setSentDate(new Date());
			Transport.send(msg);
			logger.debug("Send message successfully....");
			return msg;
		} catch (UnsupportedEncodingException | MessagingException e) {
			throw new CommonMailException(e);
		}

	}

	@Override
	public Collection<Message> forwardByMessageIDs(final IMailBox box, final String[] messageIDs, final String from,
			final String to, final String replyTo, final String cc, final String bcc, final String outBox,
			final String codePage, final String socketFactoryProvider, final String socketFactoryClass,
			String additionalSubject, String additionalContent, boolean additionalContentIsHTML,
			boolean addAsAttachement) throws CommonMailException {

		/* setup sender */
		if (mailSenderProvider instanceof IMailSenderProviderStandAlone) {
			((IMailSenderProviderStandAlone) mailSenderProvider).setSocketFactoryClass(socketFactoryClass);
		}
		Session session = mailSenderProvider.getSession();
		/* search and forward message */
		Collection<Message> msgs = box.forwardByMessageIDs(session, messageIDs, from, to, replyTo, cc, bcc, outBox,
				codePage, additionalSubject, additionalContent, additionalContentIsHTML, addAsAttachement);
		if (msgs == null) {
			throw new CommonMailException("Nie udało się przekazać wiadomości.");
		}
		logger.debug("Forward messages successfully....");
		return msgs;
	}

	/**
	 * @return the {@link #mailSenderProvider}
	 */
	public IMailSenderProvider getMailSenderProvider() {
		return mailSenderProvider;
	}

}
