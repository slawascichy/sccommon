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
package pl.slawas.common.mail.sql;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeUtility;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.mail.helpers.MessageHelper;
import pl.slawas.common.mail.helpers.MessageHelper.FileNameStringValidator;
import pl.slawas.common.mail.utils.beans.MailHeaderHelper;

/**
 * ResultSetHelper klasa narzędziowa wspierające przekształcanie listy
 * wiadomości do "wynuku SQL"
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class ResultSetHelper {

	private static final Logger logger = LoggerFactory.getLogger(ResultSetHelper.class.getName());

	/**
	 * Transformacja listy wiadomości do obiektu {@link SQLResult}.
	 * 
	 * @param msgs
	 *            lista obiektów wiadomości
	 * @param metaData
	 *            metadane opisujące kolumny
	 * @param limit
	 *            limit ograniczający liczbę przesłanych wiadomości na liście.
	 * @return
	 * @throws SQLException
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public static ResultSet transformMailList(final Message[] msgs, final ListMetaData metaData, final int limit)
			throws SQLException, MessagingException, UnsupportedEncodingException {

		int numColumns = metaData.getColumnCount();
		if (logger.isDebugEnabled()) {
			try {
				logger.debug("Mam matadata z {} kolumnami.", metaData.getColumnCount());
			} catch (SQLException e) {
				logger.debug("Oooops...{}", e.getMessage());
			}
		}

		/* Ustawiam informacje o kolumnach na podstawie metadanych. */
		List<ResultSetColumn> columns = new ArrayList<>();
		for (int i = 0; i < numColumns; i++) {
			columns.add(new ResultSetColumn(metaData, i));
		}

		ResultSetRow row;
		List<ResultSetRow> rows = new ArrayList<>();
		int recordCount = 1;
		SimpleDateFormat longDateFormater = MessageHelper.getLongDateFormater();
		for (Message msg : msgs) {
			if ((limit != 0) && (recordCount > limit)) {
				break;
			}
			if (logger.isDebugEnabled()) {
				logger.debug("Czytam {} wiadomość: {}", recordCount, msg.getSubject());
			}
			final MailHeaderHelper mailHeaderHelper = new MailHeaderHelper(msg);
			String charset = mailHeaderHelper.getCharset();
			if (StringUtils.isBlank(charset)) {
				charset = MessageHelper.DEFAULT_CHARSET;
			}
			row = new ResultSetRow(charset);
			for (int i = 0; i < columns.size(); i++) {
				ResultSetColumn column = columns.get(i);
				String label = column.getName();
				String columnName = column.getColumnName();
				String[] values = null;
				if (MessageHelper.MAIL_SENT_DATE_COLUMN.equals(columnName)) {
					java.util.Date sDate = msg.getSentDate();
					if (sDate != null) {
						values = new String[] { longDateFormater.format(sDate) };
					}
				} else if (MessageHelper.MAIL_RECEIVED_DATE_COLUMN.equals(columnName)) {
					java.util.Date rDate = msg.getReceivedDate();
					if (rDate != null) {
						values = new String[] { longDateFormater.format(rDate) };
					}
				} else if (MessageHelper.MAIL_NUMBER_COLUMN.equals(columnName)) {
					values = new String[] { Integer.toString(msg.getMessageNumber()) };
				} else if (MessageHelper.MAIL_SIZE.equals(columnName)) {
					values = new String[] { Integer.toString(msg.getSize()) };
				} else if (MessageHelper.MAIL_IS_READED_COLUMN.equals(columnName)) {
					values = new String[] { Boolean.toString(msg.isSet(Flags.Flag.SEEN)) };
				} else {
					values = msg.getHeader(columnName);
				}
				String value = null;
				if (values != null && values.length > 0) {
					StringBuilder val = new StringBuilder();
					for (int ii = 0; ii < values.length; ii++) {
						if (ii != 0) {
							val.append(MessageHelper.MANY_VALUES_SEPARATOR);
						}
						val.append(MimeUtility.decodeText(values[ii]));
					}
					value = val.toString();
				}
				if (logger.isDebugEnabled() && (MessageHelper.MAIL_MESSAGE_ID_COLUMN.equals(columnName)
						|| MessageHelper.MAIL_SIZE.equals(columnName))) {
					logger.debug("{}={}", columnName, value);
				}
				row.put(label, value);
			}
			rows.add(row);
			recordCount++;

		}
		return new ResultSetData(metaData, columns, rows);

	}

	/**
	 * Transformacja wiadomości do obiektu {@link SQLResult}.
	 * 
	 * @param msg
	 *            wiadomość elektroniczna
	 * @param metaData
	 *            metadane opisujące kolumny
	 * @param rootLocalDir
	 *            pełna ścieżka do lokalnego katalogu (na serwerze)
	 * @param dir
	 *            unikalna nazwa katalogu (na serwerze) w którym mają zostać
	 *            utworzone pliki wiadomości lub null
	 * @param rootHttpContext
	 *            pełna ścieżka kontekstu HTTP, tak aby można było zrobić download
	 *            danej części wiadomości.
	 * @param defaultFileName
	 *            domyślna nazwa pliku (gdy część wiadomości nie ma nazwy, to nazwa
	 *            pliku jest generowana w oparciu o wartość tu zadeklarowaną).
	 * @param saveParts
	 *            czy mają być zachowanie poszczególne części wiadomości? - pytanie
	 *            podchwytliwe, bo pod zachowaniem poszczególnych części wiadomości
	 *            kryje się również przekazanie kontentu wiadomości do BPM'a (bez
	 *            zapisu wiadomości nie jest możliwym ustawienie kontentu -
	 *            przynajmniej teraz tak to działa).
	 * @param saveAsEml
	 *            czy cała wiadomość ma być zachowana w pliku EML?
	 * @param filterScripts
	 *            czy odfiltrować HTML ze skryptów
	 * @return
	 * @throws IOException
	 * @throws MessagingException
	 * @throws SQLException
	 */
	public static ResultSet transformMsg(final Message msg, final PartMetaData metaData, final String rootLocalDir,
			final String dir, final String rootHttpContext, final String defaultFileName, final boolean saveParts,
			final boolean saveAsEml, final boolean filterScripts) throws IOException, MessagingException, SQLException {

		if (logger.isDebugEnabled()) {
			logger.debug("Przetwarzam msg {}...", msg);
			try {
				logger.debug("Mam matadata z {} kolumnami.", metaData.getColumnCount());
			} catch (SQLException e) {
				logger.error("Oooops...", e);
			}
		}

		final Counter counter = new Counter();
		if ((saveAsEml || saveParts) && StringUtils.isNotBlank(dir)) {
			new File(rootLocalDir, FileNameStringValidator.invalidCharsReplaced(dir)).mkdir();
		}
		String localSourceEmlFilePath = StringUtils.EMPTY;
		String httpSourceEmlFilePath = StringUtils.EMPTY;
		if (saveAsEml) {
			/* Zapisuję wiadomość w formacie EML */
			localSourceEmlFilePath = MessageHelper.getPathMsgEml(rootLocalDir, dir, defaultFileName);
			/* dla plików EML nie ma prefiksów, zatem nie trzeba ich enkodować */
			httpSourceEmlFilePath = MessageHelper.getPathMsgEml(rootHttpContext, dir, defaultFileName);
			File f = new File(localSourceEmlFilePath);
			try (OutputStream os = new FileOutputStream(f);) {
				msg.writeTo(os);
			}
		}

		/* Ustawiam informacje o kolumnach na podstawie metadanych. */
		List<ResultSetColumn> columns = new ArrayList<>();
		int numColumns = metaData.getColumnCount();
		for (int i = 0; i < numColumns; i++) {
			columns.add(new ResultSetColumn(metaData, i));
		}

		List<PartContent> parts = new ArrayList<>();
		if (saveParts) {
			Object o = null;
			try {
				o = msg.getContent();
			} catch (IOException e) {
				logger.warn(
						"[IOException] Problem z pobraniem kontentu wiadomości (subject: '{}', from: '{}'). Pewnie jest pusta. Komunikat wyjątku: {}",
						msg.getSubject(), msg.getFrom(), e.getMessage());
			} catch (MessagingException e) {
				logger.warn(
						"[MessagingException] Problem z pobraniem kontentu wiadomości (subject: '{}', from: '{}'). Pewnie jest pusta. Komunikat wyjątku: {}",
						msg.getSubject(), msg.getFrom(), e.getMessage());
			}
			if (o instanceof Multipart) {
				logger.debug("Multipart...");
				parts.addAll(saveParts((Multipart) o, rootLocalDir, dir, rootHttpContext, defaultFileName,
						filterScripts, counter, localSourceEmlFilePath, httpSourceEmlFilePath));
			} else {
				logger.debug("Single...");
				readSingleContent(rootLocalDir, dir, rootHttpContext, defaultFileName, parts, msg, filterScripts,
						counter, localSourceEmlFilePath, httpSourceEmlFilePath);
			}
		} else if (saveAsEml) {
			final MailHeaderHelper mailHeaderHelper = new MailHeaderHelper(msg);
			String charset = mailHeaderHelper.getCharset();
			if (StringUtils.isBlank(charset)) {
				charset = MessageHelper.DEFAULT_CHARSET;
			}
			logger.debug("Eml(charset={})...", charset);
			PartContent pc = new PartContent(charset, rootLocalDir, dir,
					/* content */"Message was saved as EML file in location <a href=\"" + httpSourceEmlFilePath + "\">"
							+ defaultFileName + ".eml</a>",
					msg.getDisposition(), msg.getContentType(), getContentTransferEncoding(msg), localSourceEmlFilePath,
					httpSourceEmlFilePath);
			parts.add(pc);
		}

		ResultSetRow row;
		List<ResultSetRow> rows = new ArrayList<>();
		SimpleDateFormat longDateFormater = MessageHelper.getLongDateFormater();
		for (PartContent part : parts) {
			// Ładujemy poszczególne części jak kolejne wiersze.
			row = new ResultSetRow(part.charsetName);
			logger.debug("--> ResultSetRow(charset={})...", part.charsetName);
			for (int i = 0; i < columns.size(); i++) {

				ResultSetColumn column = columns.get(i);
				String label = column.getName();

				String columnName = column.getColumnName();
				String[] values = null;
				if (MessageHelper.MAIL_SENT_DATE_COLUMN.equals(columnName)) {
					java.util.Date sDate = msg.getSentDate();
					if (sDate != null) {
						values = new String[] { longDateFormater.format(sDate) };
					}
				} else if (MessageHelper.MAIL_RECEIVED_DATE_COLUMN.equals(columnName)) {
					java.util.Date rDate = msg.getReceivedDate();
					if (rDate != null) {
						values = new String[] { longDateFormater.format(rDate) };
					}
				} else if (MessageHelper.MAIL_NUMBER_COLUMN.equals(columnName)) {
					values = new String[] { Integer.toString(msg.getMessageNumber()) };
				} else if (MessageHelper.MAIL_SIZE.equals(columnName)) {
					values = new String[] { Integer.toString(msg.getSize()) };
				} else if (MessageHelper.MAIL_PART_CONTENT_COLUMN.equals(columnName)) {
					values = new String[] { part.content };
				} else if (MessageHelper.MAIL_PART_LOCAL_FILE_PATH.equals(columnName)) {
					values = new String[] { part.localFilePath };
				} else if (MessageHelper.MAIL_PART_HTTP_FILE_PATH.equals(columnName)) {
					values = new String[] { part.httpFilePath };
				} else if (MessageHelper.MAIL_SOURCE_EML_LOCAL_FILE_PATH.equals(columnName)) {
					values = new String[] { part.localSourceEmlFilePath };
				} else if (MessageHelper.MAIL_SOURCE_EML_HTTP_FILE_PATH.equals(columnName)) {
					values = new String[] { part.httpSourceEmlFilePath };
				} else if (MessageHelper.MAIL_PART_CONTENT_TYPE.equals(columnName)) {
					values = new String[] { part.contentType };
				} else if (MessageHelper.MAIL_PART_CONTENT_TENCODING_COLUMN.equals(columnName)) {
					values = new String[] { part.contentTransferEncoding };
				} else if (MessageHelper.MAIL_PART_CONTENT_DISPOSITION.equals(columnName)) {
					values = new String[] { part.contentDisposition };
				} else {
					values = msg.getHeader(columnName);
				}
				String value = null;
				if (values != null && values.length > 0) {
					StringBuilder val = new StringBuilder();
					int posCounter = 0;
					for (int ii = 0; ii < values.length; ii++) {
						if (posCounter != 0) {
							val.append(MessageHelper.MANY_VALUES_SEPARATOR);
						}
						if (StringUtils.isNotBlank(values[ii])) {
							val.append(MimeUtility.decodeText(values[ii]));
							posCounter++;
						}
					}
					value = val.toString();
				}
				if (logger.isDebugEnabled()) {
					logger.debug("[{}/{}]{} : {} = {}", i, numColumns, columnName, label, value);
				}
				row.put(label, value);
			}
			rows.add(row);
		}
		return new ResultSetData(metaData, columns, rows);
	}

	/**
	 * Zapisanie wielu części wiadomości.
	 * 
	 * @param content
	 *            kontent wiadomości poddany analizie
	 * @param rootLocalDir
	 * @param rootHttpContext
	 * @param defaultFilename
	 * @param filterScripts
	 *            czy odfiltrować HTML ze skryptów
	 * @param counter
	 *            licznik zapisanego pliku
	 * @param localSourceEmlFilePath
	 *            lokalna ścieżka kierująca do źródłowego pliku EML całej wiadomości
	 * @param httpSourceEmlFilePath
	 *            adres HTTP kierująca do źródłowego pliku EML całej wiadomości
	 * @return lista części wiadomości
	 * @throws IOException
	 * @throws MessagingException
	 */
	private static List<PartContent> saveParts(final Multipart content, final String rootLocalDir, final String dir,
			final String rootHttpContext, final String defaultFilename, final boolean filterScripts,
			final Counter counter, final String localSourceEmlFilePath, final String httpSourceEmlFilePath)
			throws MessagingException, IOException {
		List<PartContent> parts = new ArrayList<>();

		for (int i = 0; i < content.getCount(); i++) {
			BodyPart bodyPart = content.getBodyPart(i);

			Object lContent = null;
			if (bodyPart.getLineCount() != 0) {
				try {
					lContent = bodyPart.getContent();
				} catch (IOException e) {
					logger.warn("Problem z pobraniem załącznika. Pewnie jest pusty.");
				}
			}

			if (lContent instanceof Multipart) {
				final List<PartContent> subParts = saveParts((Multipart) bodyPart.getContent(), rootLocalDir, dir,
						rootHttpContext, defaultFilename, filterScripts, counter, localSourceEmlFilePath,
						httpSourceEmlFilePath);
				parts.addAll(subParts);
			} else {
				readSingleContent(rootLocalDir, dir, rootHttpContext, defaultFilename, parts, bodyPart, filterScripts,
						counter, localSourceEmlFilePath, httpSourceEmlFilePath);
			}
		}

		return parts;
	}

	/**
	 * 
	 * @param rootLocalDir
	 * @param dir
	 * @param rootHttpContext
	 * @param defaultFileName
	 * @param parts
	 * @param bodyPart
	 * @param filterScripts
	 *            czy odfiltrować HTML ze skryptów
	 * @param counter
	 *            licznik zapisanego pliku
	 * @param localSourceEmlFilePath
	 *            lokalna ścieżka kierująca do źródłowego pliku EML całej wiadomości
	 * @param httpSourceEmlFilePath
	 *            adres HTTP kierująca do źródłowego pliku EML całej wiadomości
	 * @throws MessagingException
	 * @throws IOException
	 */
	private static void readSingleContent(final String rootLocalDir, final String dir, final String rootHttpContext,
			final String defaultFileName, final List<PartContent> parts, final Part bodyPart,
			final boolean filterScripts, final Counter counter, final String localSourceEmlFilePath,
			final String httpSourceEmlFilePath) throws MessagingException, IOException {

		String textBody = null;
		String localFilePath;
		String httpFilePath;
		final String contentType = bodyPart.getContentType();
		final String contentDisposition = bodyPart.getDisposition();
		String charset = MessageHelper.DEFAULT_CHARSET;

		if (logger.isDebugEnabled()) {
			logger.debug("--> readSingleContent: contentType={} \n contentDisposition={}", contentType,
					contentDisposition);
		}

		final MailHeaderHelper mailHeaderHelper = new MailHeaderHelper(bodyPart);
		charset = mailHeaderHelper.getCharset();
		if (logger.isDebugEnabled()) {
			logger.debug("--> readSingleContent: charset={}", charset);
		}
		final String contentTransferEncoding = getContentTransferEncoding(bodyPart);
		InputStream is = null;
		final String suffix = counter.inc();
		String encoding = mailHeaderHelper.getFileNameEncoding();
		if (StringUtils.isNotBlank(mailHeaderHelper.getFileName())) {
			/* ścieżka lokalna na podstawie nazwy pliku */
			String bFileName = mailHeaderHelper.getFileName();
			localFilePath = MessageHelper.getPathMsg(rootLocalDir, dir, bFileName, suffix, null, false, null);
			/* ścieżka zewnętrzna na podstawie encodowanej nazwy pliku */
			if (StringUtils.isNotBlank(mailHeaderHelper.getFileNameEncoded())) {
				bFileName = mailHeaderHelper.getFileNameEncoded();
			} else {
				bFileName = URLEncoder.encode(bFileName, encoding);
			}
			httpFilePath = MessageHelper.getPathMsg(rootHttpContext, dir, bFileName, suffix, null, true, encoding);
		} else {
			String postfix = null;
			if (bodyPart.isMimeType(MessageHelper.CONTENT_HTML)) {
				postfix = ".html";
			} else if (bodyPart.isMimeType(MessageHelper.CONTENT_TEXT)) {
				postfix = ".txt";
			} else {
				String subType = mailHeaderHelper.getSubType();
				if (StringUtils.isNotBlank(subType) && MessageHelper.FORWARD_SUBTYPE.equalsIgnoreCase(subType)) {
					/*
					 * element jest przesyłaną wiadomością - zaryzykuję i dam mu rozszerzenie .EML
					 */
					postfix = ".eml";
				} else {
					postfix = "." + (StringUtils.isNotBlank(subType) ? subType : "data");
				}
			}
			localFilePath = MessageHelper.getPathMsg(rootLocalDir, dir, defaultFileName, suffix, postfix, false, null);
			httpFilePath = MessageHelper.getPathMsg(rootHttpContext, dir, defaultFileName, suffix, postfix, true,
					encoding);

			if (bodyPart.isMimeType("text/*") && bodyPart.getSize() != 0) {
				try {
					textBody = (String) bodyPart.getContent();
				} catch (Exception e) {
					logger.warn("Problem z pobraniem treści. Może jest pusta lub jest strumieniem.");
				}
			}

			if (StringUtils.isNotBlank(textBody)) {
				if (filterScripts) {
					textBody = removeScript(textBody);
				}
				is = IOUtils.toInputStream(textBody, charset);
			}
		}
		if (logger.isDebugEnabled()) {
			logger.debug("--> readSingleContent: localFilePath {}...", localFilePath);
		}
		if (is == null) {
			try {
				is = bodyPart.getInputStream();
			} catch (IOException e) {
				logger.warn("Problem z pobraniem załącznika:{} size={} disposition={} contentType={} message={}",
						bodyPart.getFileName(), bodyPart.getSize(), bodyPart.getDisposition(),
						bodyPart.getContentType(), e.getMessage());
			}
		}
		File f = new File(localFilePath);
		try (OutputStream os = new FileOutputStream(f);) {
			byte[] buf = new byte[4096];
			int bytesRead;
			if (is != null) {
				while ((bytesRead = is.read(buf)) != -1) {
					os.write(buf, 0, bytesRead);
				}
			}
		}
		PartContent pc = new PartContent(charset, localFilePath, httpFilePath, textBody, contentDisposition,
				contentType, contentTransferEncoding, localSourceEmlFilePath, httpSourceEmlFilePath);
		parts.add(pc);
	}

	/**
	 * 
	 * PartContent - klasa pomocnicza do przekazywania danych związanych
	 * poszczególnymi częściami wiadomości elektronicznej.`
	 * 
	 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
	 * @version $Revision: 1.1 $
	 * 
	 */
	static class PartContent implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4329595216678232588L;
		final String charsetName;
		/** lokalna ścieżka na serwerze */
		final String localFilePath;
		/** adres http do pliku - aby można było go ściągnąć */
		final String httpFilePath;
		/** opcjonalnie treść */
		final String content;
		final String contentDisposition;
		final String contentType;
		final String contentTransferEncoding;
		/** lokalna ścieżka do źródłowego pliku EML */
		final String localSourceEmlFilePath;

		/**
		 * adres http do pliku źródłowego pliku EML tak aby można było go ściągnąć
		 */
		final String httpSourceEmlFilePath;

		/**
		 * 
		 * @param charsetName
		 * @param localFilePath
		 * @param httpFilePath
		 * @param content
		 * @param contentDisposition
		 * @param contentType
		 * @param contentTransferEncoding
		 * @param localSourceEmlFilePath
		 *            {@link #localSourceEmlFilePath}
		 * @param httpSourceEmlFilePath
		 *            {@link #httpSourceEmlFilePath}
		 */
		public PartContent(String charsetName, String localFilePath, String httpFilePath, String content,
				String contentDisposition, String contentType, String contentTransferEncoding,
				String localSourceEmlFilePath, String httpSourceEmlFilePath) {
			super();
			this.charsetName = charsetName;
			this.localFilePath = localFilePath;
			this.httpFilePath = httpFilePath;
			this.content = content;
			this.contentDisposition = contentDisposition;
			this.contentType = contentType;
			this.contentTransferEncoding = contentTransferEncoding;
			this.localSourceEmlFilePath = localSourceEmlFilePath;
			this.httpSourceEmlFilePath = httpSourceEmlFilePath;
		}

	}

	/**
	 * 
	 * Counter - klasa pomocnicza do przekazywania suffixy kolejnych zapisywanych
	 * plików.
	 */
	private static class Counter {
		private int i = 0;

		String inc() {
			return String.valueOf(++i);
		}
	}

	/**
	 * Transformacja listy folderów do obiektu {@link SQLResult}.
	 * 
	 * @param folders
	 *            lista folderów (skrzynek pocztowych)
	 * @param metaData
	 *            definicja kolumn
	 * @param limit
	 *            ograniczenie dla liczby zwracanych wierszy
	 * @return
	 * @throws SQLException
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public static ResultSet transformFolderList(Folder[] folders, FolderMetaData metaData, Integer limit)
			throws SQLException, MessagingException, UnsupportedEncodingException {

		int numColumns = metaData.getColumnCount();
		if (logger.isDebugEnabled()) {
			try {
				logger.debug(
						"\n------------------------------------\nMam matadata z {} kolumnami.\nMam {} wierszy.\nLimit ustawiono na {} wierszy.",
						metaData.getColumnCount(), folders.length, limit);
			} catch (SQLException e) {
				logger.error("Oooops...", e);
			}
		}

		/* Ustawiam informacje o kolumnach na podstawie metadanych. */
		List<ResultSetColumn> columns = new ArrayList<>();
		for (int i = 0; i < numColumns; i++) {
			columns.add(new ResultSetColumn(metaData, i));
		}

		ResultSetRow row;
		List<ResultSetRow> rows = new ArrayList<>();
		int recordCount = 1;
		for (Folder folder : folders) {

			if (limit != null && limit.intValue() != 0 && recordCount > limit.intValue()) {
				break;
			}
			row = new ResultSetRow(MessageHelper.DEFAULT_CHARSET);
			/* Otwieram */
			folder.open(Folder.READ_ONLY);
			String fullName = folder.getFullName();
			String name = folder.getName();
			String parentName = folder.getParent().getName();
			int type = folder.getType();
			int mode = folder.getMode();
			int newMessageCount = folder.getNewMessageCount();
			int unreadMessageCount = folder.getUnreadMessageCount();
			int allMessageCount = folder.getMessageCount();
			/* Zamykam */
			folder.close(false);

			if (logger.isDebugEnabled()) {
				logger.debug("Folder[{}]: {}", recordCount, fullName);
			}
			for (int i = 0; i < columns.size(); i++) {

				ResultSetColumn column = columns.get(i);
				String label = column.getName();
				String columnName = column.getColumnName();
				String[] values = null;

				MessageHelper.FolderFields field = null;
				try {
					field = MessageHelper.FolderFields.valueOf(columnName);
				} catch (Exception e) {
					logger.warn("Foleder nie ma właściwości odpowiadajacej nazwie '" + columnName + "'", e);
				}
				if (field != null) {
					switch (field) {
					case fullName:
						values = new String[1];
						values[0] = fullName;
						break;
					case name:
						values = new String[1];
						values[0] = name;
						break;
					case parentName:
						values = new String[1];
						values[0] = parentName;
						break;
					case type:
						values = new String[1];
						switch (type) {
						case Folder.HOLDS_FOLDERS:
							values[0] = MessageHelper.FOLDER_TYPE_HOLD_FOLDERS;
							break;
						case Folder.HOLDS_MESSAGES:
							values[0] = MessageHelper.FOLDER_TYPE_HOLD_MESSAGES;
							break;
						default:
							values[0] = MessageHelper.FOLDER_TYPE_HOLD_FOLDERS_AND_MESSAGES;
						}
						break;
					case mode:
						values = new String[1];
						if (mode == Folder.READ_ONLY) {
							values[0] = MessageHelper.FOLDER_MODE_RO;
						} else {
							values[0] = MessageHelper.FOLDER_MODE_RW;
						}
						break;
					case newMessageCount:
						values = new String[1];
						values[0] = Integer.toString(newMessageCount);
						break;
					case unreadMessageCount:
						values = new String[1];
						values[0] = Integer.toString(unreadMessageCount);
						break;
					case allMessageCount:
						values = new String[1];
						values[0] = Integer.toString(allMessageCount);
						break;
					default:
						break;
					}
				}

				String value = null;
				if (values != null && values.length > 0) {
					StringBuilder val = new StringBuilder();
					for (int ii = 0; ii < values.length; ii++) {
						if (ii != 0) {
							val.append(MessageHelper.MANY_VALUES_SEPARATOR);
						}
						val.append(MimeUtility.decodeText(values[ii]));
					}
					value = val.toString();
				}
				row.put(label, value);
			}
			rows.add(row);
			recordCount++;

		}
		logger.debug("\n------------------------------------");
		return new ResultSetData(metaData, columns, rows);

	}

	/**
	 * Prosty filtr usuwający tagi <script> i podobne z HTMLa
	 * 
	 * @param html
	 *            filtrowany HTML
	 * @return
	 */
	private static String removeScript(String html) {
		String[] filtersSetSL = { "<script>(.*?)</script>", "</script>", "<img(.*?)>", "javascript:", "vbscript:" };
		String[] filtersSetML = { "src[\r\n]*=[\r\n]*\\\'(.*?)\\\'", "src[\r\n]*=[\r\n]*\\\"(.*?)\\\"", "<script(.*?)>",
				"eval\\((.*?)\\)", "expression\\((.*?)\\)", "onload(.*?)=" };
		if (html != null) {
			/* usuń znaki 0x00 */
			html = html.replaceAll("", "");
			Pattern pattern;
			for (String filter : filtersSetSL) {
				pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE);
				html = pattern.matcher(html).replaceAll("");
			}
			for (String filter : filtersSetML) {
				pattern = Pattern.compile(filter, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
				html = pattern.matcher(html).replaceAll("");
			}
		}
		return html;
	}

	/**
	 * Pobranie nazwy pliku wiadomości na podstawie identyfikatora wiadomości
	 * (obsługa pojedynczych wiadomości).
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
	 * @return nazwa pliku wiadomości
	 */
	public static String getDefaultFileName(String server, String username, String mailBox, String messageID) {
		return new StringBuilder().append(server).append('-').append(username).append('-').append(mailBox).append('-')
				.append(messageID).toString();
	}

	private static String getContentTransferEncoding(final Part part) throws MessagingException {
		String[] contentTransferEncodings = part.getHeader(MessageHelper.MAIL_PART_CONTENT_TENCODING_COLUMN);
		if (contentTransferEncodings == null || contentTransferEncodings.length <= 0) {
			return null;
		}
		return contentTransferEncodings[0];
	}
}
