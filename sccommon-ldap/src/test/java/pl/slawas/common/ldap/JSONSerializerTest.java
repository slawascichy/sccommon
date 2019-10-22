package pl.slawas.common.ldap;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import pl.slawas.common.ldap.test.beans.SampleBean4Json;
import pl.slawas.common.ldap.utils.JSONSerializer;

/**
 * 
 * JSONSerializerTest
 *
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl&gt;
 * @version $Revision: 1.1 $
 *
 */
public class JSONSerializerTest extends TestCase {

	private final static Logger logger = LoggerFactory.getLogger(JSONSerializerTest.class);

	private static final String[] test = new String[] { "Test\"", "Słowo skomplikowane", "@!#!@$!@\n" };
	private static final double[] testDouble = new double[] { 123.45, 323.45, 1234 };

	public void testStringArray() throws IOException {

		Writer paramWriter = new StringWriter();
		JSONSerializer ser = new JSONSerializer(paramWriter);
		ser.write(test);

		logger.info("--> testStringArray: JSONSerializer = {}", paramWriter.toString());
		JSONArray json = (JSONArray) net.sf.json.JSONSerializer.toJSON(paramWriter.toString());
		Object[] after = json.toArray();
		for (int i = 0; i < after.length; i++) {
			logger.info("--> testStringArray: after[{}] = {}", new Object[] { i, after[i] });
			assertEquals(test[i], after[i]);
		}

	}

	public void testDoubleArray() throws IOException {

		Writer paramWriter = new StringWriter();
		JSONSerializer ser = new JSONSerializer(paramWriter);
		ser.write(testDouble);

		logger.info("--> testDoubleArray: JSONSerializer = {}", paramWriter.toString());
		JSONArray json = (JSONArray) net.sf.json.JSONSerializer.toJSON(paramWriter.toString());
		Object[] after = json.toArray();
		for (int i = 0; i < after.length; i++) {
			logger.info("--> testDoubleArray: after[{}] = {}", new Object[] { i, after[i] });
			assertEquals(testDouble[i], after[i]);
		}

	}

	public void testStringSet() throws IOException {

		Set<String> testSet = new HashSet<String>();
		for (String str : test) {
			testSet.add(str);
		}
		Writer paramWriter = new StringWriter();
		JSONSerializer ser = new JSONSerializer(paramWriter);
		ser.write(testSet);

		logger.info("--> testStringSet: JSONSerializer = {}", paramWriter.toString());
		JSONArray json = (JSONArray) net.sf.json.JSONSerializer.toJSON(paramWriter.toString());
		Object[] after = json.toArray();
		for (int i = 0; i < after.length; i++) {
			logger.info("--> testStringSet: after[{}] = {}", new Object[] { i, after[i] });
			assertTrue(testSet.contains(after[i]));
		}

	}

	public void testStringList() throws IOException {

		List<String> testList = new ArrayList<String>();
		Set<String> expectedSet = new HashSet<String>();
		for (String str : test) {
			testList.add(str);
			expectedSet.add(str);
		}
		Writer paramWriter = new StringWriter();
		JSONSerializer ser = new JSONSerializer(paramWriter);
		ser.write(testList);

		logger.info("--> testStringList: JSONSerializer = {}", paramWriter.toString());
		JSONArray json = (JSONArray) net.sf.json.JSONSerializer.toJSON(paramWriter.toString());
		Object[] after = json.toArray();
		for (int i = 0; i < after.length; i++) {
			logger.info("--> testStringList: after[{}] = {}", new Object[] { i, after[i] });
			assertTrue(expectedSet.contains(after[i]));
		}

	}

	public void testStringMap() throws IOException {

		Map<String, String> testMap = new HashMap<String, String>();
		testMap.put("name", "Sławomir \"slawas\"");
		testMap.put("sn", "Cichy");
		testMap.put("title", "Architekt");
		Writer paramWriter = new StringWriter();
		JSONSerializer ser = new JSONSerializer(paramWriter);
		ser.write(testMap);

		logger.info("--> testStringMap: JSONSerializer = {}", paramWriter.toString());
		JSONObject json = (JSONObject) net.sf.json.JSONSerializer.toJSON(paramWriter.toString());
		Set<String> keys = testMap.keySet();
		for (String key : keys) {
			String value = json.getString(key);
			logger.info("--> testStringMap: after[{}] = {}", new Object[] { key, value });
			assertTrue(value.equals(testMap.get(key)));
		}

	}

	public void testObject() throws IOException {

		SampleBean4Json testObj = new SampleBean4Json("Sławomir \"slawas\"", "Cichy", 41);
		Writer paramWriter = new StringWriter();
		JSONSerializer ser = new JSONSerializer(paramWriter);
		ser.write(testObj);

		logger.info("--> testStringMap: JSONSerializer = {}", paramWriter.toString());
		JSONObject json = (JSONObject) net.sf.json.JSONSerializer.toJSON(paramWriter.toString());
		for (String key : SampleBean4Json.keys) {
			String value = json.getString(key);
			logger.info("--> testStringMap: after[{}] = {}", new Object[] { key, value });
			if ("name".equals(key)) {
				assertTrue(value.equals(testObj.getName()));
			}
			if ("sn".equals(key)) {
				assertTrue(value.equals(testObj.getSn()));
			}
			if ("are".equals(key)) {
				Integer intValue = Integer.parseInt(value);
				assertTrue(intValue.equals(testObj.getAge()));
			}
		}

	}
}
