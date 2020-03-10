package pl.slawas.common.mail;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.ws.security.util.WSEncoderDecoder;

import junit.framework.TestCase;
import pl.slawas.common.mail.helpers.MessageHelper;
import pl.slawas.common.mail.helpers.MessageHelper.FileNameStringValidator;
import pl.slawas.common.mail.smtp.api.MailConstants;
import pl.slawas.common.mail.sql.ResultSetData;
import pl.slawas.common.mail.test.config.TstProperties;
import pl.slawas.security.PasswordEncoder;

/**
 * Projekt kopiowany z integration.jar zmodyfikowany do współpracy z Exchang'em
 * 
 * @author Sławomir Cichy &lt;scichy@ibpm.pro&gt;
 * @author Paweł Jaliński &lt;kelvan@scisoftware.pl&gt;
 * 
 * @version $Revision: 1.23 $
 */
public class ExchangeTest extends TestCase {

	final private static Logger logger = LoggerFactory.getLogger(ExchangeTest.class.getName());

	final private String className = ExchangeTest.class.getName();

	/**
	 * Parametry testów
	 */
	private static Properties props = new Properties();

	/**
	 * Dane testowe
	 */
	private static Properties testData = new Properties();

	static {
		Hashtable<String, String> _Properties = TstProperties.loadProperties();
		props.putAll(_Properties);
		Hashtable<String, String> _DataProperties = TstProperties.loadDataProperties();
		testData.putAll(_DataProperties);
		Exchange.setPasswordEncoderClass(PasswordEncoder.WEBSPHERE_ENCODER_CLASS);
	}

	@Test
	public void testGetMessagesByID() throws Exception {
		final String methodName = "testGetMessagesByID";
		String result = "OK";

		if (Boolean.parseBoolean(props.getProperty(className + '.' + methodName + ".skip"))) {
			return;
		}

		try {

			logger.info("\n\n****************************************\n" + "*** START " + methodName + "        ***\n"
					+ "****************************************\n");

			String imapPassword = props.getProperty("mail.imap.password");
			logger.info("encodepassword.mail.imap.password = {}", (new WSEncoderDecoder()).encode(imapPassword));

			String imapServer = props.getProperty("mail.imap.server");
			String imapUsername = props.getProperty("mail.imap.username");
			String mailBox = props.getProperty("mail.imap.mailbox.in");

			String rootLocalDir = props.getProperty("targetPath");
			String rootHttpContext = "file://" + rootLocalDir;
			boolean flgDeleteAftRead = false;

			@SuppressWarnings("serial")
			final Collection<String> messageIds = new ArrayList<String>() {
				{
					add(props.getProperty("mail.imap.forward.id"));
				}
			};
			logger.debug("imapServer :{}, imapUsername:{}, password:{}, mailBox:{}",
					new Object[] { imapServer, imapUsername, imapPassword, mailBox });

			Exchange.getMessagesByIDs(imapServer, imapUsername, imapPassword, mailBox, flgDeleteAftRead,
					messageIds.toArray(new String[] {}), rootLocalDir, rootHttpContext,
					/* saveParts */
					true,
					/* saveEml */
					true,
					/* filterScripts */
					false);

		} catch (Exception exc) {
			exc.printStackTrace();
			result = "ERROR";
		}
		assertEquals(result, "OK");

	}

	@Test
	public void testGetMessagesByIDs() throws Exception {
		final String methodName = "testGetMessagesByIDs";
		String result = "ERROR";
		if (Boolean.parseBoolean(props.getProperty(className + '.' + methodName + ".skip"))) {
			return;
		}
		try {
			logger.info("\n\n****************************************\n" + "*** START " + methodName + "        ***\n"
					+ "****************************************\n");
			String imapPassword = props.getProperty("mail.imap.password");
			logger.info("encodepassword.mail.imap.password = {}", (new WSEncoderDecoder()).encode(imapPassword));

			String imapServer = props.getProperty("mail.imap.server");
			String imapUsername = props.getProperty("mail.imap.username");
			String mailBox = props.getProperty("mail.imap.mailbox.in");

			String rootLocalDir = props.getProperty("targetPath");
			String rootHttpContext = "file://" + rootLocalDir;
			boolean flgDeleteAftRead = false;

			/*
			 * aby wyciągnąć email by messageID należy pominąć brzegowe znaki messageID =
			 * "B981D1921B4C5C4DA20FC0CD77A844988F08DCBD@MX10.ptu.pl";
			 */
			@SuppressWarnings("serial")
			final Collection<String> messageIds = new ArrayList<String>() {
				{
					add(props.getProperty("mail.imap.forward.id.1"));
					add(props.getProperty("mail.imap.forward.id.2"));
				}
			};

			logger.debug("imapServer :{}, imapUsername:{}, password:{}, mailBox:{}",
					new Object[] { imapServer, imapUsername, imapPassword, mailBox });

			List<ResultSet> twListOfBag = Exchange.getMessagesByIDs(imapServer, imapUsername, imapPassword, mailBox,
					flgDeleteAftRead, messageIds.toArray(new String[] {}), rootLocalDir, rootHttpContext,
					/* saveParts */
					true,
					/* saveEml */
					true,
					/* filterScripts */
					false);

			final int size = twListOfBag.size();
			logger.debug("twListOfBag: size={}", size);

			final Collection<String> foundedMessageIds = new ArrayList<String>();
			for (int i = 0; i < size; i++) {
				final ResultSet mailWithParts = (ResultSet) twListOfBag.get(i);
				final String columnName = MessageHelper.mailProperty2ColumnName(MessageHelper.MAIL_MESSAGE_ID_COLUMN);
				while (mailWithParts.next()) {
					final String value = mailWithParts.getString(columnName);
					if (value != null) {
						final String messageId = value.substring(1, value.length() - 1);
						foundedMessageIds.add(messageId);
						break;
					}
				}
			}
			assertTrue((messageIds.size() == foundedMessageIds.size()) && messageIds.containsAll(foundedMessageIds));
			result = "OK";
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(result, "OK");
	}

	@Test
	public void testForwardMessagesByIDs() throws Exception {
		final String methodName = "testForwardMessagesByIDs";
		String result = "ERROR";

		if (Boolean.parseBoolean(props.getProperty(className + '.' + methodName + ".skip"))) {
			return;
		}

		try {
			logger.info("\n\n****************************************\n" + "*** START " + methodName + "        ***\n"
					+ "****************************************\n");
			String imapPassword = props.getProperty("mail.imap.password");
			logger.info("encodepassword.mail.imap.password = {}", (new WSEncoderDecoder()).encode(imapPassword));

			String imapServer = props.getProperty("mail.imap.server");
			String imapUsername = props.getProperty("mail.imap.username");
			String mailBox = props.getProperty("mail.imap.mailbox.in");
			String outBox = props.getProperty("mail.imap.mailbox.out");
			String to = props.getProperty("mail.imap.forward.to");
			String from = props.getProperty("mail.imap.forward.from");
			String replyTo = props.getProperty("mail.imap.forward.replyTo");
			String cc = props.getProperty("mail.imap.forward.cc");
			String bcc = props.getProperty("mail.imap.forward.bcc");
			final String messageID = props.getProperty("mail.imap.forward.id");
			String smtpServer = props.getProperty(MailConstants.smtpServer);
			String smtpUsername = props.getProperty(MailConstants.smtpUsername);
			String smtpPassword = (new WSEncoderDecoder()).encode(props.getProperty(MailConstants.smtpPassword));
			String smtpPort = props.getProperty(MailConstants.smtpPort);

			logger.debug(
					"smptServer :{}, smptUsername:{}, smptPassword:{}, "
							+ "imapServer :{}, imapUsername:{}, imapPassword:{}, mailBox:{}, " + "outBox:{}",
					new Object[] { smtpServer, smtpUsername, smtpPassword, imapServer, imapUsername, imapPassword,
							mailBox, outBox });

			List<String> twList = new ArrayList<>();
			twList.add(messageID);
			Exchange.forwardByMessageIDs(smtpServer, smtpUsername, smtpPassword, smtpPort, true, true, true, imapServer,
					imapUsername, imapPassword, mailBox, twList.toArray(new String[] {}), from, to, replyTo, cc, bcc,
					outBox, null, 0);
			result = "OK";
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(result, "OK");
	}

	@Test
	public void testGetMessagePathByID() throws Exception {
		final String methodName = "testGetMessagePathByID";
		String result = "ERROR";
		if (Boolean.parseBoolean(props.getProperty(className + '.' + methodName + ".skip"))) {
			return;
		}
		try {
			logger.info("\n\n****************************************\n" + "*** START " + methodName + "        ***\n"
					+ "****************************************\n");
			String password = props.getProperty("mail.imap.password");
			String imapPassword = (new WSEncoderDecoder()).encode(password);
			logger.info("encodepassword.mail.imap.password = {}", imapPassword);

			String imapServer = props.getProperty("mail.imap.server");
			String imapUsername = props.getProperty("mail.imap.username");
			String mailBox = props.getProperty("mail.imap.mailbox.in");

			String rootLocalDir = props.getProperty("targetPath");
			String messageID = "1/2\\3";
			assert FileNameStringValidator
					.invalidCharOff(messageID)[0] > -1 : "MessageId wiadomości nie zawiera niedozwolonych znaków";

			String messagePath = Exchange.getMessagePathByID(imapServer, imapUsername, mailBox, messageID,
					rootLocalDir);

			logger.debug(
					"imapServer :{}, imapUsername:{},  mailBox:{}, rootLocalDir:{}" + ", messageID:{}, messagePath:{}",
					new Object[] { imapServer, imapUsername, mailBox, rootLocalDir, messageID, messagePath });
			assert !messagePath.contains(messageID) : "Ścieżka do pliku wiadomości zawiera niedozwolone znaki";
			assert messagePath.contains(FileNameStringValidator
					.invalidCharsReplaced(messageID)) : "Ścieżka do pliku wiadomości nie została zmieniona";
			result = "OK";
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(result, "OK");
	}

	@Test
	public void testGetMessages() throws Exception {
		final String methodName = "testGetMessages";
		String result = "ERROR";
		if (Boolean.parseBoolean(props.getProperty(className + '.' + methodName + ".skip"))) {
			return;
		}
		try {
			logger.info("\n\n****************************************\n" + "*** START " + methodName + "       ***\n"
					+ "****************************************\n");
			final String password = props.getProperty("mail.imap.password");
			final String imapPassword = password;
			final boolean flgDeleteAftRead = false;
			logger.info("encodepassword.mail.imap.password = {}", imapPassword);

			final String imapServer = props.getProperty("mail.imap.server");
			final String imapUsername = props.getProperty("mail.imap.username");
			final String mailBox = props.getProperty("mail.imap.mailbox.in");
			final int limit = Integer.valueOf(props.getProperty("mail.imap.mailbox.limit"));
			final int start = Integer.valueOf(props.getProperty("mail.imap.mailbox.start"));
			final int timeout = Integer.valueOf(props.getProperty("mail.imap.mailbox.timeout"));

			logger.debug("imapServer :{}, imapUsername:{}, password:{}, mailBox:{}",
					new Object[] { imapServer, imapUsername, password, mailBox });

			ResultSet twList = Exchange.getMessages(imapServer, imapUsername, imapPassword, mailBox, flgDeleteAftRead,
					limit, start, timeout);
			final int size = ((ResultSetData) twList).getRows().size();
			assertTrue((size > 0) && (size <= limit));

		} catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(result, "OK");
	}

	@Test
	public void testSendMail() throws Exception {
		final String methodName = "testSendMail";
		String result = "ERROR";

		if (Boolean.parseBoolean(props.getProperty(className + '.' + methodName + ".skip"))) {
			return;
		}
		try {
			logger.info("\n\n****************************************\n" + "*** START " + methodName + "       ***\n"
					+ "****************************************\n");
			String imapPassword = props.getProperty("mail.imap.password");
			String imapServer = props.getProperty("mail.imap.server");
			String imapUsername = props.getProperty("mail.imap.username");
			String mailBox = props.getProperty("mail.imap.mailbox.out");
			logger.debug("imapServer :{}, imapUsername:{}, imapPassword:{}, mailBox:{}",
					new Object[] { imapServer, imapUsername, imapPassword, mailBox });
			Object sndObj = sendMsg(imapPassword, imapServer, imapUsername, mailBox, "test1", false);
			logger.debug("sndObj=" + sndObj);
			result = "OK";
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(result, "OK");
	}

	@Test
	public void testSendMailWithEmptyDocx() throws Exception {
		final String methodName = "testSendMailWithEmptyDocx";
		String result = "ERROR";

		if (Boolean.parseBoolean(props.getProperty(className + '.' + methodName + ".skip"))) {
			return;
		}
		try {
			logger.info("\n\n****************************************\n" + "*** START " + methodName + "       ***\n"
					+ "****************************************\n");
			String imapPassword = props.getProperty("mail.imap.password");
			String imapServer = props.getProperty("mail.imap.server");
			String imapUsername = props.getProperty("mail.imap.username");
			String mailBox = props.getProperty("mail.imap.mailbox.out");
			logger.debug("imapServer :{}, imapUsername:{}, imapPassword:{}, mailBox:{}",
					new Object[] { imapServer, imapUsername, imapPassword, mailBox });
			Object sndObj = sendMsg(imapPassword, imapServer, imapUsername, mailBox, "test2", false);
			logger.debug("sndObj=" + sndObj);
			result = "OK";
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(result, "OK");
	}

	@Test
	public void testSendEmptyMail() throws Exception {
		final String methodName = "testSendEmptyMail";
		String result = "ERROR";
		if (Boolean.parseBoolean(props.getProperty(className + '.' + methodName + ".skip"))) {
			return;
		}
		try {
			logger.info("\n\n****************************************\n" + "*** START " + methodName + "       ***\n"
					+ "****************************************\n");
			String imapPassword = props.getProperty("mail.imap.password");
			String imapServer = props.getProperty("mail.imap.server");
			String imapUsername = props.getProperty("mail.imap.username");
			String mailBox = props.getProperty("mail.imap.mailbox.out");
			logger.debug("imapServer :{}, imapUsername:{}, imapPassword:{}, mailBox:{}",
					new Object[] { imapServer, imapUsername, imapPassword, mailBox });
			Object sndObj = sendMsg(imapPassword, imapServer, imapUsername, mailBox, "test3", false);
			logger.debug("sndObj=" + sndObj);
			result = "OK";
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(result, "OK");
	}

	void getMessages(String imapPassword, String imapServer, String imapUsername, String mailBox) {
		try {
			Exchange.getMessages(imapServer, imapUsername, imapPassword, mailBox, false, 10, 1);
		} catch (Exception e) {
			logger.info("[getMessages] Ignoruję błąd Exception", e);
		} catch (NoClassDefFoundError e) {
			logger.info("[getMessages] Ignoruję błąd NoClassDefFoundError");
		}
	}

	void getMessage(String imapPassword, String imapServer, String imapUsername, String mailBox, boolean saveParts,
			boolean saveEml, boolean filterScripts) {
		try {
			// 10
			// 2919
			// 2922
			String rootLocalDir = props.getProperty("targetPath");
			String rootHttpContext = "file://" + rootLocalDir;
			Exchange.getMessage(imapServer, imapUsername, imapPassword, mailBox, false, "10", rootLocalDir,
					rootHttpContext, saveParts, saveEml, filterScripts);
		} catch (Exception e) {
			logger.info("[getMessage] Ignoruję błąd Exception", e);
		} catch (NoClassDefFoundError e) {
			logger.info("[getMessage] Ignoruję błąd NoClassDefFoundError");
		}
	}

	void getMailBoxes(String imapPassword, String imapServer, String imapUsername) {
		try {
			Exchange.getMailBoxes(imapServer, imapUsername, imapPassword, "Inbox", 10);
		} catch (Exception e) {
			logger.info("[getMailBoxes] Ignoruję błąd Exception", e);
		} catch (NoClassDefFoundError e) {
			logger.info("[getMailBoxes] Ignoruję błąd NoClassDefFoundError");
		}
	}

	Object sendMsg(String imapPassword, String imapServer, String imapUsername, String mailBox, final String testItem,
			final boolean toFile) {
		Object result = null;
		try {
			String smtpServer = props.getProperty(MailConstants.smtpServer);
			String smtpUsername = props.getProperty(MailConstants.smtpUsername);
			String smtpPassword = (new WSEncoderDecoder()).encode(props.getProperty(MailConstants.smtpPassword));
			String smtpPort = props.getProperty(MailConstants.smtpPort);

			boolean auth = true;
			boolean ssl = false;
			boolean tls = true;

			String from = testData.getProperty("mail.data." + testItem + ".from");
			String replyTo = testData.getProperty("mail.data." + testItem + ".replyTo");
			String to = testData.getProperty("mail.data." + testItem + ".to");
			String cc = testData.getProperty("mail.data." + testItem + ".cc");
			String bcc = testData.getProperty("mail.data." + testItem + ".bcc");
			String subject = testData.getProperty("mail.data." + testItem + ".subject");
			String txt = testData.getProperty("mail.data." + testItem + ".txt");
			String attachments = testData.getProperty("mail.data." + testItem + ".attachments");
			String codePage = testData.getProperty("mail.data." + testItem + ".codePage");
			final boolean saveOnlyParts = new Boolean(testData.getProperty("mail.data." + testItem + ".saveOnlyParts"))
					.booleanValue();

			String rootLocalDir = props.getProperty("targetPath");
			String rootHttpContext = "file://" + rootLocalDir;

			if (toFile) {
				final List<String> twList = new ArrayList<>();
				twList.add(attachments);
				final String emailFilePath = rootLocalDir.concat("/email.file");
				result = Exchange.createMessageToFile(to, /* replyTo */null, cc, /* bcc */null, from, subject, txt,
						false, codePage, twList.toArray(new String[] {}), emailFilePath,
						new SimpleDateFormat(Exchange.getDateFormat()).format(new Date()),
						new SimpleDateFormat(Exchange.getDateFormat()).format(new Date()));
				assertTrue(emailFilePath.equals((String) result));
			} else {
				if (codePage == null) {
					result = Exchange.sendMail(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls,
							imapServer, imapUsername, imapPassword, mailBox, from, replyTo, to, cc, bcc, subject, txt,
							true, attachments, rootLocalDir, rootHttpContext, saveOnlyParts, !saveOnlyParts);
				} else {
					result = Exchange.sendMail(smtpServer, smtpUsername, smtpPassword, smtpPort, auth, ssl, tls,
							imapServer, imapUsername, imapPassword, mailBox, from, replyTo, to, cc, bcc, subject, txt,
							false, attachments, rootLocalDir, rootHttpContext, codePage, saveOnlyParts, !saveOnlyParts);
				}
			}

		} catch (Exception e) {
			logger.info("[sendMsg] Ignoruję błąd Exception", e);
		} catch (NoClassDefFoundError e) {
			logger.info("[sendMsg] Ignoruję błąd NoClassDefFoundError", e);
		}

		return result;
	}

	@Test
	public void testCreateMessageToFile() throws Exception {
		final String methodName = "testCreateMessageToFile";
		String result = "ERROR";
		if (Boolean.parseBoolean(props.getProperty(className + '.' + methodName + ".skip"))) {
			return;
		}
		try {
			logger.info("\n\n****************************************\n" + "*** START " + methodName + "       ***\n"
					+ "****************************************\n");
			String imapPassword = props.getProperty("mail.imap.password");
			String imapServer = props.getProperty("mail.imap.server");
			String imapUsername = props.getProperty("mail.imap.username");
			String mailBox = props.getProperty("mail.imap.mailbox.out");
			logger.debug("imapServer :{}, imapUsername:{}, imapPassword:{}, mailBox:{}",
					new Object[] { imapServer, imapUsername, imapPassword, mailBox });
			Object sndObj = sendMsg(imapPassword, imapServer, imapUsername, mailBox, "test1", true);
			logger.debug("sndObj=" + sndObj);
			result = "OK";
		} catch (Exception exc) {
			exc.printStackTrace();
		}
		assertEquals(result, "OK");
	}

}
