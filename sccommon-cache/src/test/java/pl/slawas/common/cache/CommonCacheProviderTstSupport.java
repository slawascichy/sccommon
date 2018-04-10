package pl.slawas.common.cache;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import junit.framework.TestCase;
import pl.slawas.common.cache.beans.CachedElement;
import pl.slawas.common.cache.beans.CachedObjectFactory;
import pl.slawas.common.cache.beans.CachedObjectResult;
import pl.slawas.common.cache.ehcache.EhCacheConfig;
import pl.slawas.common.cache.ehcache.EhCacheConstants;
import pl.slawas.common.cache.ehcache._TstProperties;
import pl.slawas.common.cache.exceptions.CacheErrorException;
import pl.slawas.common.cache.threads.SampleMultiClient;
import pl.slawas.common.cache.threads.UserSynchronizationMultiClient;
import pl.slawas.helpers.Strings;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

public class CommonCacheProviderTstSupport extends TestCase {

	private static final Logger logger = LoggerFactory.getLogger(CommonCacheProviderTstSupport.class);

	protected static Properties props = new Properties();

	protected final Class<?> provider;

	static {
		Hashtable<String, String> _Properties = _TstProperties.loadProperties();
		props.putAll(_Properties);
	}

	protected CommonCacheProviderTstSupport(Class<?> provider) {
		super();
		this.provider = provider;
	}

	public void testMultiGet() throws ExecutionException {
		CacheProviderFactory.close();

		Properties props = EhCacheConfig.getInstance().getPropertyList();
		props.put(EhCacheConstants.PROP_PROVIDER_IMPL, this.provider.getName());
		ObjectCacheStatisticsList stats;
		List<CachedElement> list;
		_IObjectCacheProvider<?> lProvider = null;
		try {
			lProvider = CacheProviderFactory.getInstance(props);
			SampleMultiClient sampleClient = new SampleMultiClient();
			UserSynchronizationMultiClient userClinet = new UserSynchronizationMultiClient();

			stats = lProvider.getAllStatistics(0, 100);
			printResult2Log(stats.getList());

			list = readTestData("/test-sample-data.csv");
			sampleClient.check(list, props);
			list = readTestData("/test-user-data.csv");
			userClinet.check(list, props);

			stats = lProvider.getAllStatistics(0, 100);
			assertNotNull("Wynik nie może być null", stats);
			printResult2Log(stats.getList());
		} finally {
			if (lProvider != null) {
				lProvider.close();
			}

		}
	}

	private List<CachedElement> readTestData(String csvFile) {
		List<CachedElement> list = new ArrayList<CachedElement>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ";";

		try {

			br = getBufferedReader(getClass(), csvFile);
			while ((line = br.readLine()) != null) {
				String[] elements = line.split(cvsSplitBy);
				list.add(new CachedElement(elements[0], elements[1]));

			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return list;
	}

	private static BufferedReader getBufferedReader(Class<?> clazz, String fileName) throws FileNotFoundException {
		BufferedReader d = null;
		URL resource = clazz.getResource(fileName);
		if (resource == null) {
			FileInputStream fis = new FileInputStream(fileName);
			try {
				d = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
		} else {
			try {
				d = new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(fileName), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
		}
		return d;
	}

	public void testApp() throws CacheErrorException {

		CacheProviderFactory.close();
		CachedObjectResult result;
		Properties props = EhCacheConfig.getInstance().getPropertyList();
		props.put(EhCacheConstants.PROP_PROVIDER_IMPL, this.provider.getName());

		_IObjectCacheProvider<?> lProvider = null;
		try {
			CachedElement testElement1vA = new CachedElement("e1", "vA");
			CachedElement testElement1vA_cached = testElement1vA;
			CachedElement testElement1vB = new CachedElement("e1", "vB");
			CachedElement testElement2vA = new CachedElement("e2", "vA");
			CachedElement testElement3vA = new CachedElement("e3", "vA");
			CachedElement testElement4vA = new CachedElement("e4", "vA");

			/* wstawianie do pamięci podręcznej */
			result = CachedObjectFactory.get(testElement1vA, CacheUsage.TO_USE, props);
			assertNotNull("Wynik nie może być null", result);
			assertEquals("Wynik powinien być nie z pamięci podręcznej (dodany).", Boolean.FALSE, result.getFromCache());
			assertEquals("Powinien być to ten sam obiekt.", testElement1vA.hashCode(), result.getObj().hashCode());

			testElement1vA = new CachedElement("e1", "vA");
			result = CachedObjectFactory.get(testElement1vA, CacheUsage.TO_USE, props);
			assertNotNull("Wynik nie może być null", result);
			assertEquals("Wynik powinien być z pamięci podręcznej (dodany).", Boolean.TRUE, result.getFromCache());
			assertEquals("Powinien być to ten sam obiekt co do wartości.", testElement1vA.hashCode(),
					result.getObj().hashCode());
			assertNotSame("Powinien być to różne obiekty co do instancji.",
					System.identityHashCode(testElement1vA_cached), System.identityHashCode(testElement1vA));

			result = CachedObjectFactory.get(testElement1vB, CacheUsage.TO_USE, props);
			assertNotNull("Wynik nie może być null", result);
			assertEquals("Wynik powinien być z pamięci podręcznej (dodany).", Boolean.TRUE, result.getFromCache());
			assertEquals("Powinien być to stary obiekt.", testElement1vA.hashCode(), result.getObj().hashCode());
			assertNotSame("Nie powinien być to być nowy obiekt, ponieważ pamięć podręczna nie została odświeżona.",
					testElement1vB.hashCode(), result.getObj().hashCode());

			result = CachedObjectFactory.get(testElement1vB, CacheUsage.REFRESH, props);
			assertNotNull("Wynik nie może być null", result);
			assertEquals("Wynik powinien być z pamięci podręcznej (zaktualizowany).", Boolean.FALSE,
					result.getFromCache());
			assertEquals("Powinien być to ten sam obiekt.", testElement1vB.hashCode(), result.getObj().hashCode());

			/* Badanie statystyk */
			for (int i = 0; i < 20; i++) {
				result = CachedObjectFactory.get(testElement1vB, CacheUsage.TO_USE, props);
				result = CachedObjectFactory.get(testElement2vA, CacheUsage.TO_USE, props);
				result = CachedObjectFactory.get(testElement3vA, CacheUsage.TO_USE, props);
				result = CachedObjectFactory.get(testElement4vA, CacheUsage.TO_USE, props);
			}

			ObjectCacheStatisticsList stats;
			lProvider = CacheProviderFactory.getInstance(props);
			stats = lProvider.getAllStatistics(0, 100);
			assertNotNull("Wynik nie może być null", stats);
			printResult2Log(stats.getList());
			assertEquals("Nieprawidłowa liczba wierszy", 2, stats.getSize());
			_IObjectCacheStatistics row = stats.getList().get(0);
			assertEquals("Nieprawidłowa cacheHits", 80L, row.getCacheHits());
			assertEquals("Nieprawidłowa cacheMisses", 4L, row.getCacheMisses());
			assertEquals("Nieprawidłowa count", 4L, row.getObjectCount());

			String[] cs = lProvider.getCacheNames();
			assertNotNull("[getCacheNames] Wynik nie może być null", cs);
			for (String name : cs) {
				_IObjectCache cache = lProvider.getCache(name);
				assertNotNull("[getCache('" + name + "')] Wynik nie może być null", cache);
				List<String> keys = lProvider.getKeysList(name);
				assertNotNull("[getKeysList('" + name + "')] Wynik nie może być null", keys);
			}

			lProvider.clearCache(CachedObjectFactory.cachedObjectRegionName);
			for (int i = 0; i < 20; i++) {
				result = CachedObjectFactory.get(testElement1vB, CacheUsage.TO_USE, props);
				result = CachedObjectFactory.get(testElement2vA, CacheUsage.TO_USE, props);
				result = CachedObjectFactory.get(testElement3vA, CacheUsage.TO_USE, props);
				result = CachedObjectFactory.get(testElement4vA, CacheUsage.TO_USE, props);
			}
			/** statystyki po wyczyszczeniu cache */
			stats = lProvider.getAllStatistics(0, 100);
			assertNotNull("Wynik nie może być null", stats);
			printResult2Log(stats.getList());
			assertEquals("Nieprawidłowa liczba wierszy", 2, stats.getSize());
			row = stats.getList().get(0);
			assertEquals("Nieprawidłowa cacheHits", 156L, row.getCacheHits());
			assertEquals("Nieprawidłowa cacheMisses", 8L, row.getCacheMisses());
			assertEquals("Nieprawidłowa count", 4L, row.getObjectCount());

		} finally {
			if (lProvider != null) {
				lProvider.close();
			}
		}

	}

	private static final int row1size = CachedObjectFactory.cachedObjectRegionName.length() + 8;
	private static final int row2size = 11;
	private static final int row3size = 11;
	private static final int row4size = 11;
	private static final int row5size = 11;

	private void printResult2Log(List<_IObjectCacheStatistics> result) {
		StringBuffer out = new StringBuffer("\n Znalazlem " + result.size() + " wierszy");
		out.append("\n*-" + Strings.lpad("-", "-", row1size));
		out.append("-+-" + Strings.rpad("-", "-", row2size));
		out.append("-+-" + Strings.lpad("-", "-", row3size));
		out.append("-+-" + Strings.lpad("-", "-", row4size));
		out.append("-+-" + Strings.rpad("-", "-", row5size));
		out.append("-+");
		out.append(printRow("name", "cacheHits", "cacheMisses", "hitRatio", "count"));
		out.append("\n*-" + Strings.lpad("-", "-", row1size));
		out.append("-+-" + Strings.rpad("-", "-", row2size));
		out.append("-+-" + Strings.lpad("-", "-", row3size));
		out.append("-+-" + Strings.lpad("-", "-", row4size));
		out.append("-+-" + Strings.rpad("-", "-", row5size));
		out.append("-+");
		for (_IObjectCacheStatistics row : result) {
			out.append(printRow(row.getAssociatedCacheName(), Long.toString(row.getCacheHits()),
					Long.toString(row.getCacheMisses()), Double.toString(row.getHitsRatio()),
					Long.toString(row.getObjectCount())));
		}
		out.append("\n*-" + Strings.lpad("-", "-", row1size));
		out.append("-+-" + Strings.rpad("-", "-", row2size));
		out.append("-+-" + Strings.lpad("-", "-", row3size));
		out.append("-+-" + Strings.lpad("-", "-", row4size));
		out.append("-+-" + Strings.rpad("-", "-", row5size));
		out.append("-+");

		logger.info("{}", out.toString());
	}

	private String printRow(String name, String cacheHits, String cacheMisses, String hitRatio, String count) {
		StringBuffer out = new StringBuffer();
		// out.append("\n*-------------------+");
		out.append("\n| " + Strings.rpad(name, " ", row1size));
		out.append(" | " + Strings.lpad(cacheHits, " ", row2size));
		out.append(" | " + Strings.lpad(cacheMisses, " ", row3size));
		out.append(" | " + Strings.lpad(hitRatio, " ", row4size));
		out.append(" | " + Strings.lpad(count, " ", row5size));
		out.append(" |");

		return out.toString();
	}

}
