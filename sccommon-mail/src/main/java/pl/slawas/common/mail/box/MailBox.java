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

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.BodyPart;
import javax.mail.FetchProfile;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.search.MessageIDTerm;
import javax.mail.search.OrTerm;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.mail.box.api.IMailBox;
import pl.slawas.common.mail.box.api.IMailBoxProvider;
import pl.slawas.common.mail.exceptions.CommonMailException;
import pl.slawas.common.mail.exceptions.CommonMailFolderException;
import pl.slawas.common.mail.helpers.MailProtocol;
import pl.slawas.common.mail.helpers.MessageHelper;
import pl.slawas.common.mail.smtp.api.MailConstants;
import pl.slawas.common.mail.sql.FolderMetaData;
import pl.slawas.common.mail.sql.ListMetaData;
import pl.slawas.common.mail.sql.PartMetaData;
import pl.slawas.common.mail.sql.ResultSetHelper;

/**
 * MailBox - skrzynka (katalog) poczty elektronicznej.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class MailBox extends MailConstants implements IMailBox, Serializable {

	private static final long serialVersionUID = 7203553866919752293L;

	private static final Logger logger = LoggerFactory.getLogger(MailBox.class.getName());

	/** nazwa skrzynki (katalogu) */
	private final String name;
	/** zakodowane hasło użytkownika/właściciela skrzynki */
	private final Object lock;
	/** Fabryka sesji */
	private final IMailBoxProvider mailBoxProvider;

	public MailBox(MailProtocol protocol, String server, String username, String name, String passwordEncoderClass) {
		super();
		this.mailBoxProvider = new MailBoxProviderStandAlone(protocol, server, username, passwordEncoderClass);
		this.name = name;
		this.lock = (new StringBuilder()).append(protocol).append(':').append(server).append(':').append(username)
				.append(':').append(name).toString().intern();
	}

	public MailBox(String resourceJndiName, String name) {
		super();
		this.mailBoxProvider = new MailBoxProviderEmbedded(resourceJndiName);
		this.name = name;
		this.lock = (new StringBuilder()).append(resourceJndiName).append(':').append(name).toString().intern();
	}

	/**
	 * @return the {@link #name}
	 */
	public String getName() {
		return name;
	}

	@Override
	public String getServer() {
		return this.mailBoxProvider.getServer();
	}

	@Override
	public String getUsername() {
		return this.mailBoxProvider.getUsername();
	}

	/**
	 * @return the {@link #mailBoxProvider}
	 */
	public IMailBoxProvider getMailBoxProvider() {
		return mailBoxProvider;
	}

	@Override
	public ResultSet loadAll(final boolean partialfetch, final boolean flgDeleteRead, final int limit, final int start)
			throws CommonMailException {
		if (start < 1) {
			throw new CommonMailException("Wartość parametru start nie może być mniejsza od jeden");
		}
		ResultSet result = null;
		synchronized (this.lock) {
			try {
				result = loadAllInConnection(partialfetch, flgDeleteRead, limit, start);
			} catch (UnsupportedEncodingException | MessagingException | SQLException e) {
				throw new CommonMailException(e);
			}
		}
		return result;
	}

	protected ResultSet loadAllInConnection(final boolean partialfetch, final boolean flgDeleteRead, final int limit,
			final int start)
			throws CommonMailException, MessagingException, UnsupportedEncodingException, SQLException {
		ResultSet result = null;
		Folder folder = null;
		Store store = null;
		Message[] msgs;
		try {
			store = mailBoxProvider.getStore(partialfetch);
			store.connect();
			folder = store.getFolder(this.name);
			folder.open(Folder.READ_WRITE);

			long startTime = 0;
			long endTime = 0;

			/* pobieranie wiadomości - START */
			if (logger.isDebugEnabled()) {
				startTime = System.currentTimeMillis();
			}
			if (limit < 1) {
				msgs = folder.getMessages();
			} else {
				msgs = new Message[limit];
				int i = 0;
				try {
					do {
						msgs[i] = folder.getMessage(start + i);
					} while (++i < limit);
				} catch (IndexOutOfBoundsException ex) {
					if ((i == 0) && (start != 1)) {
						throw new CommonMailException(
								"Wartość parametru start nie może być większa od liczby wiadomości w skrzynce");
					}
					msgs = Arrays.copyOf(msgs, i);
				}
			}
			FetchProfile fetchProfile = new FetchProfile();
			fetchProfile.add(FetchProfile.Item.ENVELOPE);
			fetchProfile.add(FetchProfile.Item.CONTENT_INFO);
			folder.fetch(msgs, fetchProfile);
			if (logger.isDebugEnabled()) {
				endTime = System.currentTimeMillis();
				logger.debug("Czas pobrania {} wiadomości {} ms (limit {}, start {})", msgs.length, endTime - startTime,
						limit, start);
				startTime = System.currentTimeMillis();
			}
			/* pobieranie wiadomości - KONIEC */

			/* przekształcanie wiadomości - START */
			result = ResultSetHelper.transformMailList(msgs, new ListMetaData(this), limit);
			if (logger.isDebugEnabled()) {
				endTime = System.currentTimeMillis();
				logger.debug("Czas przekształcenia do wiersza {} ms", endTime - startTime);
			}
			/* przekształcanie wiadomości - KONIEC */
		} finally {
			if (folder != null) {
				folder.close(flgDeleteRead);
			}
			if (store != null) {
				store.close();
			}
		}
		return result;
	}

	@Override
	public ResultSet loadByMessageNumber(int messageNumber, boolean flgDeleteRead, String rootLocalDir,
			String rootHttpContext, boolean saveParts, boolean saveEml, boolean filterScripts)
			throws CommonMailException {
		ResultSet result = null;
		synchronized (this.lock) {
			try {
				result = loadByMessageNumberInConnection(messageNumber, flgDeleteRead, rootLocalDir, rootHttpContext,
						saveParts, saveEml, filterScripts);
			} catch (MessagingException | IOException | SQLException e) {
				throw new CommonMailException(e);
			}
		}
		return result;
	}

	protected ResultSet loadByMessageNumberInConnection(int messageNumber, boolean flgDeleteRead, String rootLocalDir,
			String rootHttpContext, boolean saveParts, boolean saveEml, boolean filterScripts)
			throws MessagingException, IOException, SQLException, CommonMailException {

		Store store = null;
		Folder folder = null;
		Message msg = null;
		ResultSet result = null;
		try {
			store = mailBoxProvider.getStore(false);
			store.connect();
			folder = store.getFolder(this.name);
			folder.open(Folder.READ_ONLY);
			msg = folder.getMessage(messageNumber);
			if (msg != null) {
				final String messageID = MimeUtility.decodeText(msg.getHeader(MessageHelper.MAIL_MESSAGE_ID_COLUMN)[0]);
				result = ResultSetHelper.transformMsg(msg, new PartMetaData(this), rootLocalDir,
						messageID.substring(1, messageID.length() - 1), rootHttpContext, ResultSetHelper
								.getDefaultFileName(getServer(), getUsername(), name, String.valueOf(messageNumber)),
						saveParts, saveEml, filterScripts);
				if (flgDeleteRead) {
					msg.setFlag(Flags.Flag.DELETED, true);
				}
			} else {
				throw new CommonMailException(String.format("Nie znalazłem wiadomości o numerze %s", messageNumber));
			}
		} finally {
			if (folder != null) {
				folder.close(flgDeleteRead);
			}
			if (store != null) {
				store.close();
			}
		}
		return result;
	}

	@Override
	public List<ResultSet> loadByMessageIDs(final String[] messageIDs, final boolean flgDeleteRead,
			final String rootLocalDir, final String rootHttpContext, final boolean saveParts, final boolean saveEml,
			final boolean filterScripts) throws CommonMailException {
		if ((messageIDs == null) || (messageIDs.length < 1)) {
			throw new CommonMailException("Pole 'messageIDs' powinno zawierać przynajmniej jedną wartość");
		}
		List<ResultSet> result = null;
		synchronized (this.lock) {
			try {
				result = loadByMessageIDsInConnection(messageIDs, flgDeleteRead, rootLocalDir, rootHttpContext,
						saveParts, saveEml, filterScripts);
			} catch (IOException | MessagingException | SQLException e) {
				throw new CommonMailException(e);
			}
		}
		return result;
	}

	protected List<ResultSet> loadByMessageIDsInConnection(final String[] messageIDs, final boolean flgDeleteRead,
			final String rootLocalDir, final String rootHttpContext, final boolean saveParts, final boolean saveEml,
			final boolean filterScripts) throws CommonMailException, IOException, MessagingException, SQLException {

		if ((messageIDs == null) || (messageIDs.length < 1)) {
			throw new CommonMailException("Pole 'messageIDs' powinno zawierać przynajmniej jedną wartość");
		}

		Store store = null;
		Folder folder = null;
		List<ResultSet> result = new ArrayList<>();
		try {
			store = mailBoxProvider.getStore(false);
			store.connect();
			folder = store.getFolder(this.name);
			if (folder.isOpen()) {
				throw new CommonMailFolderException("Błąd : folder jest już otwarty.");
			}
			folder.open(Folder.READ_WRITE);
			if (!folder.isOpen()) {
				throw new CommonMailFolderException("Błąd : folder nadal jest zamknięty.");
			}

			long startTime = 0;
			long endTime = 0;
			if (logger.isDebugEnabled()) {
				startTime = System.currentTimeMillis();
			}

			/* pobieranie wiadomości - START */
			Message[] msgs = findByMessageIDs(folder, messageIDs);
			FetchProfile fetchProfile = new FetchProfile();
			fetchProfile.add(FetchProfile.Item.ENVELOPE);
			fetchProfile.add(FetchProfile.Item.CONTENT_INFO);
			folder.fetch(msgs, fetchProfile);
			if (logger.isDebugEnabled()) {
				endTime = System.currentTimeMillis();
				logger.debug("loadByMessageIDs czas wyszukania wiadomości: {} ms.", endTime - startTime);
			}
			/* pobieranie wiadomości - KONIEC */

			final PartMetaData metaData = new PartMetaData(this);
			for (final Message msg : msgs) {
				/* przekształcanie pojedynczej wiadomości - START */
				if (logger.isDebugEnabled()) {
					startTime = System.currentTimeMillis();
				}
				final String messageIDtmp = MimeUtility
						.decodeText(msg.getHeader(MessageHelper.MAIL_MESSAGE_ID_COLUMN)[0]);
				final String messageID = messageIDtmp.substring(1, messageIDtmp.length() - 1);
				result.add(ResultSetHelper.transformMsg(msg, metaData, rootLocalDir, messageID, rootHttpContext,
						ResultSetHelper.getDefaultFileName(getServer(), getUsername(), name, messageID), saveParts,
						saveEml, filterScripts));

				if (flgDeleteRead) {
					msg.setFlag(Flags.Flag.DELETED, true);
				}
				if (logger.isDebugEnabled()) {
					endTime = System.currentTimeMillis();
					logger.debug("loadByMessageIDs czas przekształcenia wiadomości id={} do wiersza: {} ms.", messageID,
							endTime - startTime);
				}
				/* przekształcanie pojedynczej wiadomości - KONIEC */
			}
		} finally {
			if (folder != null) {
				folder.close(flgDeleteRead);
			}
			if (store != null) {
				store.close();
			}
		}
		return result;
	}

	@Override
	public void setAsRead(int messageNumber, boolean flgRead) throws CommonMailException {
		synchronized (this.lock) {
			try {
				setAsReadInConnection(messageNumber, flgRead);
			} catch (MessagingException e) {
				throw new CommonMailException(e);
			}
		}
	}

	protected void setAsReadInConnection(int messageNumber, boolean flgRead) throws MessagingException {
		Store store = null;
		Folder folder = null;
		Message msg = null;
		try {
			store = mailBoxProvider.getStore(false);
			store.connect();
			folder = store.getFolder(this.name);
			folder.open(Folder.READ_WRITE);
			msg = folder.getMessage(messageNumber);
			msg.setFlag(Flags.Flag.SEEN, flgRead);
		} finally {
			if (folder != null && folder.isOpen()) {
				folder.close(false);
			}
			if (store != null) {
				store.close();
			}
		}
	}

	@Override
	public void deleteByMessageIDs(final String[] messageIDs) throws CommonMailException {
		if ((messageIDs == null) || (messageIDs.length < 1)) {
			throw new CommonMailException("Pole 'messageIDs' powino zawierać przynajmniej jedną wartość");
		}
		synchronized (this.lock) {
			try {
				deleteByMessageIDsInConnection(messageIDs);
			} catch (MessagingException e) {
				throw new CommonMailException(e);
			}
		}
	}

	protected void deleteByMessageIDsInConnection(final String[] messageIDs) throws MessagingException {
		Store store = null;
		Folder folder = null;
		try {
			store = mailBoxProvider.getStore(false);
			store.connect();
			folder = store.getFolder(this.name);
			folder.open(Folder.READ_WRITE);
			for (final Message msg : findByMessageIDs(folder, messageIDs)) {
				msg.setFlag(Flags.Flag.DELETED, true);
			}
			folder.expunge();
		} finally {
			if (folder != null && folder.isOpen()) {
				folder.close(false);
			}
			if (store != null) {
				store.close();
			}
		}
	}

	@Override
	public void appendMessage(Message message) throws CommonMailException {
		synchronized (this.lock) {
			try {
				appendMessageInConnection(message);
			} catch (MessagingException e) {
				throw new CommonMailException(e);
			}
		}
	}

	protected void appendMessageInConnection(Message message) throws MessagingException {
		Store store = null;
		Folder folder = null;
		try {
			store = mailBoxProvider.getStore(false);
			store.connect();
			folder = store.getFolder(this.name);
			folder.open(Folder.READ_WRITE);
			if (!folder.exists()) {
				folder.create(Folder.HOLDS_MESSAGES);
			}
			folder.appendMessages(new Message[] { message });
		} finally {
			if (folder != null && folder.isOpen()) {
				folder.close(false);
			}
			if (store != null) {
				store.close();
			}
		}
	}

	@Override
	public ResultSet loadMailBoxes(Integer limit) throws CommonMailException {
		ResultSet result = null;
		synchronized (this.lock) {
			try {
				result = loadMailBoxesInConnection(limit);
			} catch (UnsupportedEncodingException | MessagingException | SQLException e) {
				throw new CommonMailException(e);
			}
		}
		return result;
	}

	protected ResultSet loadMailBoxesInConnection(Integer limit)
			throws MessagingException, UnsupportedEncodingException, SQLException {
		Store store = null;
		Folder[] folders = new Folder[0];
		ResultSet result = null;
		try {
			store = mailBoxProvider.getStore(false);
			store.connect();
			List<Folder> fs = loadSubFolders(store.getDefaultFolder());
			result = ResultSetHelper.transformFolderList(fs.toArray(folders), new FolderMetaData(this), limit);
		} finally {
			if (store != null) {
				store.close();
			}
		}
		return result;
	}

	/**
	 * Rekurencyjne listowanie katalogów.
	 * 
	 * @param parentFolder
	 * @return
	 * @throws MessagingException
	 */
	private List<Folder> loadSubFolders(Folder parentFolder) throws MessagingException {
		Folder[] subFoldersArray = parentFolder.list();
		List<Folder> subFolders = new ArrayList<>();
		if (subFoldersArray != null && subFoldersArray.length > 0) {
			for (Folder sub : subFoldersArray) {
				subFolders.add(sub);
				if (logger.isDebugEnabled())
					logger.debug("sub: {}", sub);
				List<Folder> subSub = loadSubFolders(sub);
				if (subSub != null && !subSub.isEmpty()) {
					subFolders.addAll(subSub);
				}
			}
		}
		return subFolders;
	}

	@Override
	public void moveByMessageIDs(final String[] messageIDs, final String targetMailBox) throws CommonMailException {
		if ((messageIDs == null) || (messageIDs.length < 1)) {
			throw new CommonMailException("Pole 'messageIDs' powinno zawierać przynajmniej jedną wartość");
		}
		if (StringUtils.isBlank(targetMailBox)) {
			throw new IllegalArgumentException("Pole 'targetMailBox' nie może być puste.");
		}
		synchronized (this.lock) {
			try {
				moveByMessageIDsInConnection(messageIDs, targetMailBox);
			} catch (MessagingException e) {
				throw new CommonMailException(e);
			}
		}

	}

	protected void moveByMessageIDsInConnection(final String[] messageIDs, final String targetMailBox)
			throws MessagingException {
		Store store = null;
		Folder sourceFolder = null;
		Folder targetFolder = null;
		Message[] msgs = null;
		try {
			store = mailBoxProvider.getStore(false);
			store.connect();
			sourceFolder = store.getFolder(this.name);
			sourceFolder.open(Folder.READ_WRITE);
			msgs = findByMessageIDs(sourceFolder, messageIDs);
			targetFolder = store.getFolder(targetMailBox);
			targetFolder.open(Folder.READ_WRITE);
			sourceFolder.copyMessages(msgs, targetFolder);
			sourceFolder.setFlags(msgs, new Flags(Flags.Flag.DELETED), true);
		} finally {
			if (targetFolder != null && targetFolder.isOpen()) {
				targetFolder.close(false);
			}
			if (sourceFolder != null && sourceFolder.isOpen()) {
				sourceFolder.close(true);
			}
			if (store != null) {
				store.close();
			}
		}
	}

	@Override
	public Collection<Message> forwardByMessageIDs(final Session smtpSession, final String[] messageIDs,
			final String from, final String to, final String replyTo, final String cc, final String bcc,
			final String outBox, String codePage, String additionalSubject, String additionalContent,
			boolean additionalContentIsHTML, boolean addAsAttachement) throws CommonMailException {

		if (StringUtils.isBlank(codePage)) {
			codePage = MessageHelper.DEFAULT_CHARSET;
		}
		if (!Charset.isSupported(codePage)) {
			final String msg = "Wartość pola 'codePage'='" + codePage + "' nie jest obsługiwaną stroną kodową.";
			throw new IllegalArgumentException(msg);
		}
		if ((messageIDs == null) || (messageIDs.length < 1)) {
			throw new CommonMailException("Pole 'messageIDs' powino zawierać przynajmniej jedną wartość");
		}
		if (StringUtils.isBlank(from)) {
			throw new IllegalArgumentException("Pole 'Od' nie może być puste.");
		}
		if (StringUtils.isBlank(to)) {
			throw new IllegalArgumentException("Pole 'Do' nie może być puste.");
		}
		if (StringUtils.isBlank(outBox)) {
			throw new IllegalArgumentException("Pole 'outBox' nie może być puste.");
		}
		if (logger.isDebugEnabled()) {
			logger.debug("forwardByMessageIDs: from={} to={} replyTo={} cc={} bcc={} outBox={} codePage={}", from, to,
					replyTo, cc, bcc, outBox, codePage);
		}
		Collection<Message> forwards = null;
		synchronized (this.lock) {
			try {
				forwards = forwardByMessageIDsInConnection(smtpSession, messageIDs, from, to, replyTo, cc, bcc, outBox,
						codePage, additionalSubject, additionalContent, additionalContentIsHTML, addAsAttachement);
			} catch (MessagingException | IOException e) {
				throw new CommonMailException(e);
			}
		}
		return forwards;
	}

	protected Collection<Message> forwardByMessageIDsInConnection(final Session smtpSession, final String[] messageIDs,
			final String from, final String to, final String replyTo, final String cc, final String bcc,
			final String outBox, String codePage, String additionalSubject, String additionalContent,
			boolean additionalContentIsHTML, boolean addAsAttachement)
			throws MessagingException, CommonMailFolderException, IOException {

		Store store = null;
		Folder folder = null;
		Folder outFolder = null;
		Collection<Message> forwards = null;
		try {
			store = mailBoxProvider.getStore(false);
			store.connect();
			folder = store.getFolder(this.name);
			if (folder.isOpen()) {
				throw new CommonMailFolderException("Błąd : folder jest już otwarty.");
			}
			folder.open(Folder.READ_WRITE);
			if (!folder.isOpen()) {
				throw new CommonMailFolderException("Błąd : folder nadal jest zamknięty.");
			}
			long startTime = 0;
			long endTime = 0;

			/* pobieranie wiadomości - START */
			if (logger.isDebugEnabled()) {
				startTime = System.currentTimeMillis();
			}
			final Message[] msgs = findByMessageIDs(folder, messageIDs);
			final FetchProfile fetchProfile = new FetchProfile();
			fetchProfile.add(FetchProfile.Item.ENVELOPE);
			fetchProfile.add(FetchProfile.Item.CONTENT_INFO);
			folder.fetch(msgs, fetchProfile);
			if (logger.isDebugEnabled()) {
				endTime = System.currentTimeMillis();
				logger.debug("forwardByMessageIDs: znalazłem: {} wiadomości, czas wyszukania : {} ms.", msgs.length,
						endTime - startTime);
			}
			/* pobieranie wiadomości - KONIEC */

			forwards = new ArrayList<>();
			for (final Message msg : msgs) {
				/* przekształcanie wiadomości do wiadomości FORVARD - START */
				if (logger.isDebugEnabled()) {
					startTime = System.currentTimeMillis();
				}

				/*
				 * Create new message - wiadomość, która zostanie utworzona jako kontener/nośnik
				 * (opakowanie) przesyłanej wiadomości oryginalnej.
				 */
				final MimeMessage forward = new MimeMessage(smtpSession);
				/*
				 * Dodanie odpowiednich parametrów nagłówka do wiadomości kontenera -
				 * przepisanie niektórych oryginalnych nagłówków w celach informacyjnych.
				 */
				String[] hOrig;
				/* informacja o oryginalnym podmiocie wysyłającym */
				hOrig = msg.getHeader(FORWARD_HEADER_SENDER);
				if (hOrig != null && hOrig.length > 0) {
					for (int i = 0; i < hOrig.length; i++) {
						if (i == 0) {
							forward.setHeader(FORWARD_HEADER_SENDER, MimeUtility.decodeText(hOrig[i]));
						} else {
							forward.addHeader(FORWARD_HEADER_SENDER, MimeUtility.decodeText(hOrig[i]));
						}
					}
				}
				/*
				 * informacja o oryginalnym identyfikatorze przesyłanej wiadomości dołączamy
				 * jako odpowiednie informacje w nagłówku wiadomości kontenera...
				 */
				hOrig = msg.getHeader(MessageHelper.MAIL_MESSAGE_ID_COLUMN);
				if (hOrig != null && hOrig.length > 0) {
					String messageId = MimeUtility.decodeText(hOrig[0]);
					/* referencja do identyfikatora oryginalnej wiadomości */
					forward.setHeader(FORWARD_HEADER_REFERENCES, messageId);
					/* In-Reply-To do identyfikatora oryginalnej wiadomości */
					forward.setHeader(FORWARD_HEADER_IN_REPLY_TO, messageId);
				}

				/*
				 * Ustawienie tematu wiadomości kontenera - domyślnie sklejenie prefiksu 'FW' z
				 * oryginalnym tematem przesyłanej wiadomości.
				 */
				String forwardSubject = FORWARD_SUBJECT_PREFIX + msg.getSubject();
				if (StringUtils.isNotBlank(additionalSubject)) {
					/*
					 * można sobie ustawić inny temat dla kontenera, co też zostało przekazanie i
					 * teraz, domyślnie skonstruowany temat zostanie nadpisany
					 */
					forwardSubject = additionalSubject;
				}
				forward.setSubject(forwardSubject, codePage);
				/* Ustawiamy w wiadomości głównej kto przesyła wiadomość */
				forward.setFrom(null);
				forward.addFrom(MessageHelper.prepareRecipientsAddressesArray(from, codePage));
				/*
				 * Ustawiamy w wiadomości głównej do kogo mają być wysyłane odpowiedzi na
				 * przesyłaną wiadomość.
				 */
				forward.setReplyTo(StringUtils.isBlank(replyTo) ? null
						: MessageHelper.prepareRecipientsAddressesArray(replyTo, codePage));
				/* do kogo przesyłamy wiadomość */
				forward.setRecipients(Message.RecipientType.TO,
						MessageHelper.prepareRecipientsAddressesArray(to, codePage));
				forward.setRecipients(Message.RecipientType.CC,
						StringUtils.isBlank(cc) ? null : MessageHelper.prepareRecipientsAddressesArray(cc, codePage));
				forward.setRecipients(Message.RecipientType.BCC,
						StringUtils.isBlank(bcc) ? null : MessageHelper.prepareRecipientsAddressesArray(bcc, codePage));
				/* data przesłania wiadomości */
				forward.setSentDate(new Date());

				/* Części wiadomości głównej/kontenera */
				Multipart forwardMainMultipart = new MimeMultipart(MessageHelper.MIXED_MULTIPART_SUBTYPE);
				/*
				 * Tworzymy część wiadomości do której dołączymy odpowiednie elementy
				 * oryginalnej wiadomości - na koniec ta część będzie dołączona do wiadomości
				 * głównej/kontenera
				 */
				BodyPart messageBodyPartWithOrigMessageContent = new MimeBodyPart();
				/*
				 * flaga pomocnicza, z informacją czy do części z oryginalną wiadomością został
				 * załączony jakiś kontent
				 */
				boolean messageBodyPartWithOrigMessageContentHasContent = false;

				/** Budujemy treść wiadomości głównej/kontenera - START */
				/*
				 * Ustalenie treści wiadomości - domyślnie jest to komunikat, że przesyłana
				 * wiadomość jest w całości załączona jako załącznik do wiadomości
				 * głównej/kontenera.
				 */
				String forwardContent = MessageHelper.DEFAULT_FORWARD_ATTACHED_MSG;
				if (!addAsAttachement) {
					/**
					 * Treść przesyłanej wiadomości ma zostać doklejona do wiadomości głównej
					 * kontenera - START
					 */
					/* Pobieram kontent wiadomości oryginalniej/przesyłanej */
					Object msgContent = msg.getContent();
					if (msgContent instanceof Multipart) {
						/*
						 * Kontent oryginalnej/przesyłanej wiadomości składa się z kilku części -
						 * prawdopodobnie jakieś info oraz załączniki w postaci plików. Ja muszę wydobyć
						 * to info by dokleić je do wiadomości głównej/kontenera, a pozostałe części
						 * dołączyć jako załączniki. Zależy mi na tym, by nie załączać info od nadawcy 2
						 * razy: jako treść wiadomości głównej/kontenera oraz jako osobny załącznik - bo
						 * to bez sensu
						 */
						boolean contentIsAsHtmlForwardContent = false;
						Multipart multipartOrig = (Multipart) msgContent;
						Multipart multipartFwd = new MimeMultipart(MessageHelper.MIXED_MULTIPART_SUBTYPE);
						for (int j = 0; j < multipartOrig.getCount(); j++) {
							/**
							 * pętla po elementach oryginalnej wiadomości - START
							 */
							BodyPart bodyPart = multipartOrig.getBodyPart(j);
							String disposition = bodyPart.getDisposition();
							String contentType = bodyPart.getContentType();
							boolean contentIsAsForwardContent = false;

							if ((StringUtils.isBlank(disposition)
									|| disposition.toLowerCase().startsWith(MimeBodyPart.INLINE))
									&& StringUtils.isNotBlank(contentType)) {
								/*
								 * znaleziony element jest częścią z dyspozycją prezentacji "inline" -
								 * prawdopodobnie mamy do czynienia z informacją przekazaną od nadawcy - to
								 * właśnie tego szukamy by załączyć to jako treść wiadomości głównej/kontenera
								 */
								if (contentType.toLowerCase().startsWith(CONTENT_TYPE_HTML_PREFIX)) {
									/*
									 * OK, mamy info od nadawcy w postaci HTML
									 */
									forwardContent = bodyPart.getContent().toString();
									contentIsAsForwardContent = true;
									/*
									 * Dla mnie ta część jest najważniejsza, jeżeli istnieje alternatywne info w
									 * postaci zwykłego tekstu, to chciałbym by było zignorowane - wiadomość główna
									 * przesyłana jest jako HTML - ustawiam flagę pomocniczą, że mam już info w
									 * HTML.
									 */
									contentIsAsHtmlForwardContent = true;

								}
								if (!contentIsAsHtmlForwardContent
										&& contentType.toLowerCase().startsWith(CONTENT_TYPE_TEXT_PREFIX)) {
									/*
									 * OK, mamy info od nadawcy w postaci Text, jednocześnie nie znalazłem jeszcze
									 * info w postaci HTML. Jeżeli takie zostanie znalezione w późniejszym cyklu
									 * pętli poniższa treść zostanie zastąpiona treścią HTML.
									 */
									forwardContent = "<pre>" + bodyPart.getContent().toString() + "</pre>";
									contentIsAsForwardContent = true;
								}
							}
							if (!contentIsAsForwardContent) {
								/*
								 * w tym cyklu nie znalazłem informacji od nadawcy, którą można by było dołączyć
								 * do wiadomości głównej - dla mnie to oznacza, że element który teraz analizuje
								 * jest zwykłym załącznikiem - zatem traktuję to jako zwykły załącznik i dodaję
								 * (załączam) do wiadomości głównej/kontenera
								 */
								multipartFwd.addBodyPart(bodyPart);
							}
							/**
							 * pętla po elementach oryginalnej wiadomości - KONIEC
							 */
						}
						if (multipartFwd.getCount() != 0) {
							/*
							 * w obiekcie multipartFwd mam załączniki - może ich nie być, jeżeli oryginalna,
							 * przesyłana wiadomość miała tylko info od nadawcy - załączam znalezione
							 * załączniki jako kontent z elementami wiadomości oryginalnej.
							 */
							messageBodyPartWithOrigMessageContent.setContent(multipartFwd);
							/*
							 * ustawiam flagę, że część z elementami wiadomości oryginalnej ma jakiś kontent
							 */
							messageBodyPartWithOrigMessageContentHasContent = true;
						}
					} else if (msgContent instanceof BodyPart) {
						/*
						 * Kontent oryginalnej wiadomości to tylko pojedyncze info od nadawcy - teraz
						 * tylko muszę zidentyfikować czy to info jest prezentowane w trybie "inline" i
						 * czy jest HTM, czy też zwykły tekst.
						 */
						BodyPart bodyPart = (BodyPart) msgContent;
						String disposition = bodyPart.getDisposition();
						String contentType = bodyPart.getContentType();
						boolean contentIsAsForwardContent = false;
						if ((StringUtils.isBlank(disposition)
								|| disposition.toLowerCase().startsWith(MimeBodyPart.INLINE))
								&& StringUtils.isNotBlank(contentType)) {
							/*
							 * Kontent oryginalnej wiadomości jest w trybie "inline"
							 */
							if (contentType.toLowerCase().startsWith(CONTENT_TYPE_HTML_PREFIX)) {
								/* info jest w postaci HTML */
								forwardContent = bodyPart.getContent().toString();
								contentIsAsForwardContent = true;
							} else if (contentType.toLowerCase().startsWith(CONTENT_TYPE_TEXT_PREFIX)) {
								/* info jest w postaci tekstu */
								forwardContent = "<pre>" + bodyPart.getContent().toString() + "</pre>";
								contentIsAsForwardContent = true;
							}
						}
						if (!contentIsAsForwardContent) {
							/*
							 * Nie ma żadnej czytelnej treści od nadawcy, ani w HTML, ani w postaci zwykłego
							 * tekstu. W sumie nie wiadomo o co chodzi, więc na wszelki wypadek ustawiam
							 * flagę wymuszającą załączenie całej oryginalnej wiadomości jako załącznik.
							 */
							addAsAttachement = true;
						}
					} else {
						/*
						 * Nie udało mi się zidentyfikować żadnej postaci przesyłanej wiadomości,
						 * wiadomość bez ciała? - nie wiem czy to możliwe. W sumie nie wiadomo o co
						 * chodzi, więc na wszelki wypadek ustawiam flagę wymuszającą załączenie całej
						 * oryginalnej wiadomości jako załącznik.
						 */
						addAsAttachement = true;
					}
					/**
					 * Treść przesyłanej wiadomości ma zostać doklejona do wiadomości głównej
					 * kontenera - KONIEC
					 */
				}
				/*
				 * Czy wiadomość ma być dołączona w całości jako załącznik do wiadomości
				 * głównej/kontenera?
				 */
				if (addAsAttachement) {
					/*
					 * TAK - dodajemy całą oryginalną, przesyłaną wiadomość jako załącznik
					 * wiadomości głównej/kontenera
					 */
					messageBodyPartWithOrigMessageContent.setDisposition(MimeBodyPart.ATTACHMENT);
					messageBodyPartWithOrigMessageContent.setContent(msg, FORWARD_CONTENT_TYPE);
					messageBodyPartWithOrigMessageContentHasContent = true;
				}

				/**
				 * Budowa części z informacją od adresata przesyłanej wiadomości
				 */
				BodyPart forwardMessageBodyPart;
				/* informacja jest w postaci HTML */
				forwardMessageBodyPart = new MimeBodyPart();
				forwardMessageBodyPart.setDisposition(MimeBodyPart.INLINE);
				forwardMessageBodyPart.setContent(
						buildForwardMessagePartInHtml(msg, additionalContent, additionalContentIsHTML, forwardContent),
						(String) (MessageHelper.CONTENT_HTML + MessageHelper.CONTENT_TYPE_CHARSET_PREFIX
								+ MessageHelper.DEFAULT_CHARSET));
				/*
				 * załączam utworzone info od adresata do wiadomości głównej/kontenera...
				 */
				forwardMainMultipart.addBodyPart(forwardMessageBodyPart);
				forwardMainMultipart.addBodyPart(MessageHelper.getAttachLogo());
				/** Budujemy treść wiadomości głównej/kontenera - KONIEC */

				/*
				 * Dodaję do wiadomości głównej część z oryginalnymi elementami wiadomości.
				 */
				if (messageBodyPartWithOrigMessageContentHasContent) {
					/*
					 * dodajemy tylko wtedy gdy do części z oryginalnymi elementami wiadomości
					 * został dodany jakiś kontent. Może nie być kontentu w przypadku gdy treść
					 * przesyłanej wiadomości została wklejona treści wiadomości głównej/kontenera
					 * (!addAsAttachement) - czy to jest zrozumiałe?
					 */
					forwardMainMultipart.addBodyPart(messageBodyPartWithOrigMessageContent);
				}

				/* Add multi-part to message */
				forward.setContent(forwardMainMultipart);
				/* Update headers and append new message to folder */
				forward.saveChanges();
				/* Send copy of message */
				Transport.send(forward);

				outFolder = store.getFolder(outBox);
				outFolder.open(Folder.READ_WRITE);
				outFolder.appendMessages(new Message[] { forward });

				forwards.add(forward);

				if (logger.isDebugEnabled()) {
					endTime = System.currentTimeMillis();
					logger.debug("forwardByMessageIDs czas przesłania wiadomości: {} ms.", endTime - startTime);
				}
				/* przekształcanie wiadomości do wiadomości FORVARD - START */
			}
		} finally {
			if (outFolder != null && outFolder.isOpen()) {
				outFolder.close(false);
			}
			if (folder != null) {
				folder.close(false);
			}
			if (store != null) {
				store.close();
			}
		}
		return forwards;
	}

	/**
	 * Budowanie treści wiadomości (info) od adresata przesyłanej wiadomości
	 * 
	 * @param msg
	 *            obiekt oryginalnej/przesyłanej wiadomości
	 * @param additionalContent
	 *            dodatkowa informacja od adresata
	 * @param additionalContentIsHTML
	 *            informacja czy dodatkowa informacja od adresata jest w postaci
	 *            HTML, czy też w postaci zwykłego tekstu.
	 * @param mainContent
	 *            doklejony kontent do wiadomości od adresata - może to być kontent
	 *            z info od nadawcy w przypadku gdy przesyłana wiadomość ma być
	 *            doklejona, lub jest to domyślne info o tym, że cała wiadomość
	 *            oryginalna/przesyłana jest dołączona jako załącznik.
	 * @return kontent/treść wiadomości od adresata
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	private String buildForwardMessagePartInHtml(Message msg, String additionalContent, boolean additionalContentIsHTML,
			String mainContent) throws MessagingException, UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(additionalContent)) {
			if (!additionalContentIsHTML) {
				sb.append("<pre>");
			}
			sb.append(additionalContent);
			if (!additionalContentIsHTML) {
				sb.append("</pre>");
			}
			sb.append("<br/><br/>");
		}
		sb.append("<br/><hr/>");
		sb.append("<pre>\nFrom: ");
		sb.append(StringEscapeUtils.escapeHtml(MimeUtility.decodeText(InternetAddress.toString(msg.getFrom()))));
		sb.append("\nSent: ");
		sb.append((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS z")).format(msg.getSentDate()));
		sb.append("\nTo: ");
		sb.append(StringEscapeUtils.escapeHtml(
				MimeUtility.decodeText(InternetAddress.toString(msg.getRecipients(Message.RecipientType.TO)))));
		sb.append("\nSubject: ");
		sb.append(
				MimeUtility.decodeText((StringUtils.isNotBlank(msg.getSubject()) ? msg.getSubject() : "not allowed")));
		sb.append("\n</pre><br/>");
		sb.append(mainContent);
		sb.append("<br/>");
		sb.append(MessageHelper.buildDefaultForwardFooterInHTMLMsg());
		return sb.toString();
	}

	private Message[] findByMessageIDs(final Folder folder, final String[] messageIDs) throws MessagingException {
		final Set<MessageIDTerm> searchTerms = new HashSet<>();
		for (final String messageID : messageIDs) {
			searchTerms.add(new MessageIDTerm(messageID));
		}
		return folder.search(new OrTerm(searchTerms.toArray(new MessageIDTerm[0])));
	}

}
