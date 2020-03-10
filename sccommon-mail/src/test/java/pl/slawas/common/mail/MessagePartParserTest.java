package pl.slawas.common.mail;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.mail.handlers.message_rfc822;

import junit.framework.TestCase;
import pl.slawas.common.mail.box.MailBox;
import pl.slawas.common.mail.facke.FackeMailDatasource;
import pl.slawas.common.mail.helpers.MailProtocol;
import pl.slawas.common.mail.helpers.MessageHelper;
import pl.slawas.common.mail.sql.PartMetaData;
import pl.slawas.common.mail.sql.ResultSetHelper;
import pl.slawas.common.mail.test.config.TstProperties;
import pl.slawas.common.mail.utils.beans.MailHeaderHelper;
import pl.slawas.security.PasswordEncoder;

public class MessagePartParserTest extends TestCase {

	final private static Logger logger = LoggerFactory.getLogger(MessagePartParserTest.class.getName());
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
	}

	public static void testMessageParser() throws Exception {

		ResultSet result;
		Msg msgFactory;
		MimeMessage msg;
		String messageID;
		Object o;

		String testResources = props.getProperty("testResourcesPath");
		String rootLocalDir = props.getProperty("targetPath");
		String rootHttpContext = "file://" + rootLocalDir + "/target";
		String server = "localhost";
		String username = "slawas";
		String folderName = "TEST";
		boolean saveParts = true;
		boolean saveEml = true;
		boolean filterScripts = true;
		MailBox mbox = new MailBox(MailProtocol.imap, server, username, folderName,
				PasswordEncoder.DEFAULT_ENCODER_CLASS);
		PartMetaData metaData = new PartMetaData(mbox);

		msgFactory = new Msg(
				testResources + "/mx13.ptu.pl-telecentrum_test-INBOX-20150908190457.73BF04016D10D@mgate.sample.pl.eml");
		msg = msgFactory.msg;
		messageID = msgFactory.messageID;

		o = msg.getContent();
		if (o instanceof Multipart) {
			Multipart multipart = (Multipart) o;
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				MailHeaderHelper headerHelper = new MailHeaderHelper(bodyPart);
				assertEquals("UTF-8", headerHelper.getCharset().toUpperCase());
				assertEquals("flowed", headerHelper.getFormat());

			}
		}

		result = ResultSetHelper.transformMsg(msg, metaData, rootLocalDir, messageID, rootHttpContext,
				ResultSetHelper.getDefaultFileName(server, username, folderName, messageID), saveParts, saveEml,
				filterScripts);

		while (result.next()) {
			String emlPath = result.getString(MessageHelper.MAIL_SOURCE_EML_HTTP_FILE_PATH);
			logger.debug(MessageHelper.MAIL_SOURCE_EML_HTTP_FILE_PATH + "={}", emlPath);
			String localEML = result.getString(MessageHelper.MAIL_SOURCE_EML_LOCAL_FILE_PATH);
			logger.debug(MessageHelper.MAIL_SOURCE_EML_LOCAL_FILE_PATH + "={}", localEML);
			File testFile = new File(localEML);
			assertTrue("Plik powinien istnieć", testFile.exists());
			String localPartFile = result.getString(MessageHelper.MAIL_PART_LOCAL_FILE_PATH);
			File testLocalAttachementFile = new File(localPartFile);
			assertTrue("Plik części maila powinien istnieć", testLocalAttachementFile.exists());
		}

		msgFactory = new Msg(testResources + "/mx13.ptu.pl-kontakt-MM-114252b94fa95ef850ec48d4e6c6df5f@darekn.pl.eml");
		msg = msgFactory.msg;
		messageID = msgFactory.messageID;

		o = msg.getContent();
		if (o instanceof Multipart) {
			Multipart multipart = (Multipart) o;
			for (int i = 0; i < multipart.getCount(); i++) {
				BodyPart bodyPart = multipart.getBodyPart(i);
				MailHeaderHelper headerHelper = new MailHeaderHelper(bodyPart);
				assertEquals("application", headerHelper.getPrimaryType());
				assertEquals("pdf", headerHelper.getSubType());
			}
		}

		result = ResultSetHelper.transformMsg(msg, metaData, rootLocalDir, messageID, rootHttpContext,
				ResultSetHelper.getDefaultFileName(server, username, folderName, messageID), saveParts, saveEml,
				filterScripts);

		while (result.next()) {
			String emlPath = result.getString(MessageHelper.MAIL_SOURCE_EML_HTTP_FILE_PATH);
			logger.debug(MessageHelper.MAIL_SOURCE_EML_HTTP_FILE_PATH + "={}", emlPath);
			String localEML = result.getString(MessageHelper.MAIL_SOURCE_EML_LOCAL_FILE_PATH);
			logger.debug(MessageHelper.MAIL_SOURCE_EML_LOCAL_FILE_PATH + "={}", localEML);
			File testFileEML = new File(localEML);
			assertTrue("Plik EML powinien istnieć", testFileEML.exists());
			String localPartFile = result.getString(MessageHelper.MAIL_PART_LOCAL_FILE_PATH);
			File testLocalAttachementFile = new File(localPartFile);
			assertTrue("Plik części maila powinien istnieć", testLocalAttachementFile.exists());
		}

	}

	private static class Msg {

		final MimeMessage msg;
		final String messageID;

		Msg(String emlFileName) throws IOException, MessagingException {

			message_rfc822 x = new message_rfc822();
			FackeMailDatasource ds = new FackeMailDatasource(emlFileName);
			msg = (MimeMessage) x.getContent(ds);
			String messageIDtmp = MimeUtility.decodeText(msg.getHeader(MessageHelper.MAIL_MESSAGE_ID_COLUMN)[0]);
			messageID = messageIDtmp.substring(1, messageIDtmp.length() - 1);

		}

	}
}
