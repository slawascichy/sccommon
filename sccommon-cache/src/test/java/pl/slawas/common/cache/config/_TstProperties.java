package pl.slawas.common.cache.config;

import java.io.PrintWriter;
import java.util.Hashtable;

import org.apache.log4j.config.PropertyPrinter;

import pl.slawas.common.cache.config.CacheConfig;
import pl.slawas.helpers.Configurations;

public class _TstProperties {

	private static boolean log4jPropsPrint = true;

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
		Hashtable<String, String> _Properties = null;
		_Properties = Configurations.loadHashtable(_TstProperties.class,
				propkFileName);
		props.putAll(_Properties);

		/** załadowanie ustawień */
		final String mockFileName = _Properties.get("test.file.ext");
		System.out.println("Loading test properties from " + mockFileName
				+ "....");
		_Properties = Configurations.loadHashtable(_TstProperties.class,
				mockFileName);
		props.putAll(_Properties);
		CacheConfig._Instance = null;
		CacheConfig.getInstance(mockFileName);
		return props;
	}

}
