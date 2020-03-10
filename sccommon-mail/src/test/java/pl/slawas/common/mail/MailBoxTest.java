package pl.slawas.common.mail;

import java.util.Hashtable;
import java.util.Properties;

import com.ibm.ws.security.util.WSEncoderDecoder;

import junit.framework.TestCase;
import pl.slawas.common.mail.test.config.TstProperties;

public class MailBoxTest extends TestCase {

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

	public void test() {
		String pas = "sf?";
		String imapPassword = (new WSEncoderDecoder()).decode(pas);
		String enCodeImapPassword = (new WSEncoderDecoder()).encode(pas);
		System.out.println("imapPassword=" + imapPassword);
		System.out.println("enCodeImapPassword=" + enCodeImapPassword);
		assertTrue(true);
	}
}
