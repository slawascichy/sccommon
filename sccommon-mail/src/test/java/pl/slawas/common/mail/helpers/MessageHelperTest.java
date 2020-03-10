package pl.slawas.common.mail.helpers;

import java.util.Hashtable;
import java.util.Properties;

import javax.mail.Address;

import junit.framework.TestCase;
import pl.slawas.common.mail.test.config.TstProperties;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Projekt kopiowany z integration.jar zmodyfikowany do współpracy z Exchang'em
 * 
 * @author Sławomir Cichy &lt;scichy@ibpm.pro&gt;
 * @author Paweł Jaliński &lt;kelvan@scisoftware.pl&gt;
 * 
 * @version $Revision: 1.23 $
 */
public class MessageHelperTest extends TestCase {

	final private static Logger logger = LoggerFactory
			.getLogger(MessageHelperTest.class.getName());

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
		Hashtable<String, String> _DataProperties = TstProperties
				.loadDataProperties();
		testData.putAll(_DataProperties);
	}

	/** Dodatkowe testy analizy listy adresów email */
	@Test
	public void testPrepareRecipientsAddressesArray() throws Exception {
		String testParseContentType = MessageHelperTest.class.getName()
				+ ".testPrepareRecipientsAddressesArray";

		Boolean skip = Boolean.parseBoolean(props
				.getProperty(testParseContentType));
		if (skip) {
			return;
		}

		String testedRec = "Adresat z polskimi znakami ść  <marcin.szweda@ibpm.pro>, Sławomir Cichy <slawas@ibpm.pro>";
		Address[] result = MessageHelper.prepareRecipientsAddressesArray(
				testedRec, MessageHelper.DEFAULT_CHARSET);
		int i = 0;
		for (Address r : result) {
			logger.info("address = {}", new Object[] { r.toString() });

			if (i == 0) {
				assertEquals(
						"=?UTF-8?Q?Adresat_z_polskimi_znakami_=C5=9B=C4=87?= <marcin.szweda@ibpm.pro>",
						r.toString());
			} else {
				assertEquals(
						"=?UTF-8?Q?S=C5=82awomir_Cichy?= <slawas@ibpm.pro>",
						r.toString());
			}
			i++;
		}
	}

	/** Dodatkowe testy analizy pojedynczego adresu email */
	@Test
	public void testPrepareRecipientsAddresses() throws Exception {

		String testParseContentType = MessageHelperTest.class.getName()
				+ ".testPrepareRecipientsAddresses";

		Boolean skip = Boolean.parseBoolean(props
				.getProperty(testParseContentType));
		if (skip) {
			return;
		}

		String testedRec = "Sławomir Cichy <slawas@ibpm.pro>";
		Address result = MessageHelper.prepareRecipientsAddresses(testedRec,
				MessageHelper.DEFAULT_CHARSET);
		logger.info("address = {}", new Object[] { result.toString() });
		assertEquals("=?UTF-8?Q?S=C5=82awomir_Cichy?= <slawas@ibpm.pro>",
				result.toString());
	}
}
