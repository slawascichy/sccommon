package pl.slawas.common.cache.ehcache;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Map;

import org.apache.log4j.config.PropertyPrinter;

import pl.slawas.common.cache.ehcache.EhCacheConfig;
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
		Map<String, String> _Properties = null;
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
		EhCacheConfig._Instance = null;
		EhCacheConfig.getInstance(mockFileName);
		return props;
	}

}
