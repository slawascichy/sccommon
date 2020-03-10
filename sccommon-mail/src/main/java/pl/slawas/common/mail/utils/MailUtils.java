package pl.slawas.common.mail.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentDisposition;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeUtility;
import javax.mail.internet.ParameterList;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailUtils {

	private static Logger logger = LoggerFactory.getLogger(MailUtils.class);

	private static int id = 0;

	/** nazwa nagłówka typu kontentu */
	public static final String CONTENT_TYPE_HEADER_NAME = "Content-Type";
	public static final String CONTENT_TYPE_FILE_NAME = "name";
	public static final String CONTENT_TYPE_FILE_NAME_ENCODED = "nameEncoded";
	public static final String CONTENT_TYPE_FILE_NAME_ENCODING = "nameEncoding";
	/** Content-Type: typ/podtyp */
	public static final String CONTENT_TYPE_BASE_TYPE = "CONTENT_TYPE_BASE_TYPE";
	/** Content-Type: tylko typ */
	public static final String CONTENT_TYPE_PRIMARY_TYPE = "CONTENT_TYPE_PRIMARY_TYPE";
	/** Content-Type: tylko podtyp */
	public static final String CONTENT_TYPE_SUBTYPE = "CONTENT_TYPE_SUBTYPE";
	/** Content-Type: charset */
	public static final String CONTENT_TYPE_PARAM_NAME_CHARSET = "charset";
	/** Content-Type: format */
	public static final String CONTENT_TYPE_FORMAT_NAME_CHARSET = "format";

	/** nazwa nagłówka dyspozycji */
	public static final String CONTENT_DISPOSITION_HEADER_NAME = "Content-Disposition";
	public static final String CONTENT_DISPOSITION_FILE_NAME = "filename";
	public static final String CONTENT_DISPOSITION_FILE_NAME_ENCODED = "filenameEncoded";
	public static final String CONTENT_DISPOSITION_FILE_NAME_ENCODING = "filenameEncoding";
	public static final String CONTENT_DISPOSITION_PARAM_NAME = "Content-Disposition";

	@SuppressWarnings("unchecked")
	public static Map<String, String> parseContentTypeFields(Part bodyPart) throws MessagingException {
		Map<String, String> result = new HashMap<>();
		if (bodyPart.getHeader(CONTENT_TYPE_HEADER_NAME) != null
				&& bodyPart.getHeader(CONTENT_TYPE_HEADER_NAME).length > 0) {
			String cType = bodyPart.getHeader(CONTENT_TYPE_HEADER_NAME)[0];
			ContentType ct;
			if (bodyPart.isMimeType(cType)) {
				ct = new ContentType(cType);
			} else {
				String primaryType = cType;
				String subType = StringUtils.EMPTY;
				ParameterList pList = null;
				ct = new ContentType(primaryType, subType, pList);
			}
			logger.debug("Type: \nBaseType:{}\nPrimaryType:{}\nSubType:{}\n parameters: {}", ct.getBaseType(),
					ct.getPrimaryType(), ct.getSubType(), ct.getParameterList());
			/* CONTENT_TYPE_BASE_TYPE */
			if (StringUtils.isNotBlank(ct.getBaseType())) {
				result.put(CONTENT_TYPE_BASE_TYPE, ct.getBaseType());
			}
			/* CONTENT_TYPE_PRIMARY_TYPE */
			if (StringUtils.isNotBlank(ct.getPrimaryType())) {
				result.put(CONTENT_TYPE_PRIMARY_TYPE, ct.getPrimaryType());
			}
			/* CONTENT_TYPE_SUBTYPE */
			if (StringUtils.isNotBlank(ct.getSubType())) {
				result.put(CONTENT_TYPE_SUBTYPE, ct.getSubType());
			}

			/**
			 * inne parametry, ze szczególnym uwzględnieniem nazwy pliku - START
			 */
			StringBuilder fileName = new StringBuilder();
			if (ct.getParameterList() != null) {
				Enumeration<String> names = ct.getParameterList().getNames();
				while (names.hasMoreElements()) {
					String paramName = names.nextElement();
					logger.debug("paramName = {}", paramName);
					if (paramName.startsWith(CONTENT_TYPE_FILE_NAME) && !paramName.equals(CONTENT_TYPE_FILE_NAME)) {
						fileName.append(ct.getParameterList().get(paramName));
					} else {
						result.put(paramName, ct.getParameterList().get(paramName));
					}

				}
			}
			/**
			 * inne parametry, ze szczególnym uwzględnieniem nazwy pliku - END
			 */
			String fileNameStr = (fileName.length() != 0 ? fileName.toString() : null);
			String fileStr = StringUtils.isNotBlank(result.get(CONTENT_TYPE_FILE_NAME))
					? result.get(CONTENT_TYPE_FILE_NAME)
					: fileNameStr;

			if (fileStr != null) {
				logger.debug("before parse fileStr='{}'", fileStr);
				if (fileStr.startsWith("=?")) {
					/*
					 * przypadek kodowania nazwy pliku: =?UTF-8?Q?Afryka=C5=84ski
					 * -s=C5=82o=C5=84-wielko=C5=9B=C4=87.jpg?=
					 */
					try {
						fileStr = MimeUtility.decodeText(fileStr);
					} catch (UnsupportedEncodingException e) {
						logger.warn("Nie udało się zdekodowac nazwy pliku " + fileStr, e);
					}

				} else if (fileStr.indexOf("''") > 0) {
					/*
					 * przypadek kodowania nazwy pliku: UTF-8
					 * ''pe%C5%82nomocnictwo%20nr%20zg%C5%82oszenia%200003753215.pdf
					 */
					String[] fileNameElements = fileStr.split("'");
					if (fileNameElements.length > 2) {
						try {
							String encodedFileName = fileNameElements[2];
							String encoding = fileNameElements[0];
							fileStr = URLDecoder.decode(encodedFileName, encoding);
							result.put(CONTENT_TYPE_FILE_NAME_ENCODED, encodedFileName);
							result.put(CONTENT_TYPE_FILE_NAME_ENCODING, encoding);
						} catch (UnsupportedEncodingException e) {
							logger.warn("Nie udało się zdekodowac nazwy pliku " + fileStr, e);
						}
					}
				}
				result.put(CONTENT_TYPE_FILE_NAME, fileStr);
			}

		}
		logger.debug("parse result={}", result);
		return result;
	}

	public static String getUniqueId(String suffix) {
		StringBuilder s = new StringBuilder();
		s.append(s.hashCode()).append('.').append(getUniqueId()).append('.').append(System.currentTimeMillis())
				.append('.').append("sccommon-mail.").append(suffix);
		return s.toString();
	}

	private static synchronized int getUniqueId() {
		return (id++);
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> parseContentDispositionFields(Part bodyPart) throws MessagingException {
		Map<String, String> result = new HashMap<>();
		if (bodyPart.getHeader(CONTENT_DISPOSITION_HEADER_NAME) != null
				&& bodyPart.getHeader(CONTENT_DISPOSITION_HEADER_NAME).length > 0) {

			String cDisp = bodyPart.getHeader(CONTENT_DISPOSITION_HEADER_NAME)[0];

			ContentDisposition cd = new ContentDisposition(cDisp);
			if (logger.isDebugEnabled()) {
				logger.debug("Disposition: {} \n with parameters: {}", cd.getDisposition(), cd.getParameterList());
			}

			result.put(CONTENT_DISPOSITION_PARAM_NAME, cd.getDisposition());

			/**
			 * inne parametry, ze szczególnym uwzględnieniem nazwy pliku - START
			 */
			StringBuilder fileName = new StringBuilder();
			if (cd.getParameterList() != null) {
				Enumeration<String> names = cd.getParameterList().getNames();
				while (names.hasMoreElements()) {
					String paramName = names.nextElement();
					logger.debug("paramName = {}", paramName);
					if (paramName.startsWith(CONTENT_DISPOSITION_FILE_NAME)
							&& !paramName.equals(CONTENT_DISPOSITION_FILE_NAME)) {
						fileName.append(cd.getParameterList().get(paramName));
					} else {
						result.put(paramName, cd.getParameterList().get(paramName));
					}

				}
			}

			/**
			 * inne parametry, ze szczególnym uwzględnieniem nazwy pliku - KONIEC
			 */
			String fileNameStr = (fileName.length() != 0 ? fileName.toString() : null);
			String fileStr = StringUtils.isNotBlank(result.get(CONTENT_DISPOSITION_FILE_NAME))
					? result.get(CONTENT_DISPOSITION_FILE_NAME)
					: fileNameStr;

			if (fileStr != null) {
				logger.debug("before parse fileStr='{}'", fileStr);
				if (fileStr.startsWith("=?")) {
					/*
					 * przypadek kodowania nazwy pliku: =?UTF-8?Q?Afryka=C5=84ski
					 * -s=C5=82o=C5=84-wielko=C5=9B=C4=87.jpg?=
					 */
					try {
						fileStr = MimeUtility.decodeText(fileStr);
					} catch (UnsupportedEncodingException e) {
						logger.warn("Nie udało się zdekodowac nazwy pliku " + fileStr, e);
					}

				} else if (fileStr.indexOf("''") > 0) {
					/*
					 * przypadek kodowania nazwy pliku: UTF-8
					 * ''pe%C5%82nomocnictwo%20nr%20zg%C5%82oszenia%200003753215.pdf
					 */
					String[] fileNameElements = fileStr.split("'");
					if (fileNameElements.length > 2) {
						try {
							String encodedFileName = fileNameElements[2];
							String encoding = fileNameElements[0];
							fileStr = URLDecoder.decode(encodedFileName, encoding);
							result.put(CONTENT_DISPOSITION_FILE_NAME_ENCODED, encodedFileName);
							result.put(CONTENT_DISPOSITION_FILE_NAME_ENCODING, encoding);
						} catch (UnsupportedEncodingException e) {
							logger.warn("Nie udało się zdekodowac nazwy pliku " + fileStr, e);
						}
					}
				}
				result.put(CONTENT_DISPOSITION_FILE_NAME, fileStr);
			}

		}
		logger.debug("parse result={}", result);
		return result;
	}
}
