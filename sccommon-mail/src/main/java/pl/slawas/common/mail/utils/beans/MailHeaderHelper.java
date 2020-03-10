package pl.slawas.common.mail.utils.beans;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Part;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.mail.helpers.MessageHelper;
import pl.slawas.common.mail.utils.MailUtils;

public class MailHeaderHelper {

	private final Map<String, String> contentTypeFields;

	private final Map<String, String> contentDispositionFields;

	public MailHeaderHelper(Part bodyPart) throws MessagingException {
		contentTypeFields = MailUtils.parseContentTypeFields(bodyPart);
		contentDispositionFields = MailUtils.parseContentDispositionFields(bodyPart);
	}

	/**
	 * @return the {@link #contentTypeFields}
	 */
	public Map<String, String> getContentTypeFields() {
		return contentTypeFields;
	}

	/**
	 * @return the {@link #contentDispositionFields}
	 */
	public Map<String, String> getContentDispositionFields() {
		return contentDispositionFields;
	}

	public String getBaseType() {
		return contentTypeFields.get(MailUtils.CONTENT_TYPE_BASE_TYPE);
	}

	public String getPrimaryType() {
		return contentTypeFields.get(MailUtils.CONTENT_TYPE_PRIMARY_TYPE);
	}

	public String getSubType() {
		return contentTypeFields.get(MailUtils.CONTENT_TYPE_SUBTYPE);
	}

	/**
	 * Zwraca stronę kodową wiadomości.
	 * 
	 * @return
	 */
	public String getCharset() {
		String charset = contentTypeFields.get(MailUtils.CONTENT_TYPE_PARAM_NAME_CHARSET);
		if (StringUtils.isNotBlank(charset)) {
			return charset;
		}
		return contentDispositionFields.get(MailUtils.CONTENT_DISPOSITION_FILE_NAME_ENCODING);
	}

	/**
	 * Zwraca format wiadomości
	 * 
	 * @return
	 */
	public String getFormat() {
		return contentTypeFields.get(MailUtils.CONTENT_TYPE_FORMAT_NAME_CHARSET);
	}

	/**
	 * pobieranie zakodowanej nazwy pliku ( {@link URLEncoder} ) - do użytku do
	 * podawania w URL'u (kontekst HTTP(S)).
	 * 
	 * @return zakodowana nazwa pliku, jeżeli istnieje
	 */
	public String getFileNameEncoded() {
		String fileName = contentDispositionFields.get(MailUtils.CONTENT_DISPOSITION_FILE_NAME_ENCODED);
		if (StringUtils.isBlank(fileName)) {
			fileName = contentTypeFields.get(MailUtils.CONTENT_TYPE_FILE_NAME_ENCODED);
		}

		return fileName;
	}

	/**
	 * Rozkodowana nazwa pliku ({@link URLDecoder} - do użytku dla ścieżek
	 * lokalnych.
	 * 
	 * @return nazwa pliku
	 */
	public String getFileName() {
		String fileName = contentDispositionFields.get(MailUtils.CONTENT_DISPOSITION_FILE_NAME);
		if (StringUtils.isBlank(fileName)) {
			fileName = contentTypeFields.get(MailUtils.CONTENT_TYPE_FILE_NAME);
		}
		return fileName;
	}

	public String getFileNameEncoding() {
		String fileNameEncoding = contentDispositionFields.get(MailUtils.CONTENT_DISPOSITION_FILE_NAME_ENCODING);
		if (StringUtils.isBlank(fileNameEncoding)) {
			fileNameEncoding = contentTypeFields.get(MailUtils.CONTENT_TYPE_FILE_NAME_ENCODING);
		}
		return StringUtils.isBlank(fileNameEncoding) ? MessageHelper.DEFAULT_CHARSET : fileNameEncoding;
	}

}
