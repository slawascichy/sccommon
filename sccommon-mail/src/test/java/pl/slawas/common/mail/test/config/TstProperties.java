package pl.slawas.common.mail.test.config;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.config.PropertyPrinter;

import pl.slawas.common.mail.ExchangeTest;
import pl.slawas.helpers.Configurations;

public class TstProperties {

	private static boolean log4jPropsPrint = false;

	/**
	 * Ładowanie parametrów testów.
	 * 
	 * @return
	 */
	public static Hashtable<String, String> loadProperties() {

		if (log4jPropsPrint) {
			System.out.println("#log4j Config");
			PrintWriter pw = new PrintWriter(System.out);
			PropertyPrinter pp = new PropertyPrinter(pw);
			pp.print(pw);
			System.out.println("#End of Config");
			log4jPropsPrint = false;
		}

		Hashtable<String, String> props = new Hashtable<String, String>();

		final String propkFileName = "/test.properties";
		Map<String, String> lodedProperties = null;
		lodedProperties = Configurations.loadHashtable(TstProperties.class, propkFileName);
		props.putAll(lodedProperties);

		/** załadowanie ustawień */
		final String mockFileName = lodedProperties.get("test.file.ext");
		System.out.println("Loading test properties from " + mockFileName + "....");
		lodedProperties = Configurations.loadHashtable(TstProperties.class, mockFileName);
		props.putAll(lodedProperties);

		return props;
	}

	/**
	 * Ładowanie parametrów testów.
	 * 
	 * @return
	 */
	public static Hashtable<String, String> loadDataProperties() {

		final String propkFileName = "/test.properties";
		Map<String, String> loadedProperties = null;
		loadedProperties = Configurations.loadHashtable(TstProperties.class, propkFileName);

		Hashtable<String, String> props = new Hashtable<String, String>();
		final String testDataPropertiesFile = loadedProperties.get("test.file.mail.test.data.properties");
		Map<String, String> _PropertiesTestData = Configurations.loadHashtable(ExchangeTest.class,
				testDataPropertiesFile);
		props.putAll(_PropertiesTestData);

		return props;
	}

}
