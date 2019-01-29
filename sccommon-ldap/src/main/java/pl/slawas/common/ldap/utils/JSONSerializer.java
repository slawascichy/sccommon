package pl.slawas.common.ldap.utils;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import pl.slawas.helpers.PrimitiveType;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * 
 * JSONSerializer - serializator jak jedne z wielu dostępnych na rynku w
 * openSource. Niestety nie wiem co może być dostępne w classpath BPM'a więc
 * musiał powstać tutaj.
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public class JSONSerializer {

	private static final Logger log = LoggerFactory.getLogger(JSONSerializer.class);

	private Writer writer;

	/**
	 * flaga informująca serializator czy wynik ma być formatowany (ładnie wyglądać)
	 */
	private boolean format = false;

	public JSONSerializer(Writer paramWriter) {
		this.writer = paramWriter;
	}

	public void flush() throws IOException {
		this.writer.flush();
	}

	public void close() throws IOException {
		this.writer.close();
	}

	public JSONSerializer writeRawString(String paramString) throws IOException {
		this.writer.write(paramString);

		return this;
	}

	public JSONSerializer writeNull() throws IOException {
		writeRawString("null");

		return this;
	}

	public JSONSerializer writeNumber(Number paramNumber) throws IOException {
		if (null == paramNumber)
			return writeNull();

		if (paramNumber instanceof Float && ((Float) paramNumber).isNaN()) {
			return writeNull();
		}

		if (paramNumber instanceof Double && ((Double) paramNumber).isNaN()) {
			return writeNull();
		}

		writeRawString(paramNumber.toString());

		return this;
	}

	public JSONSerializer writeBoolean(Boolean paramBoolean) throws IOException {
		if (null == paramBoolean)
			return writeNull();

		writeRawString(paramBoolean.toString());

		return this;
	}

	private String rightAlignedZero(String paramString, int paramInt) {
		if (paramInt == paramString.length()) {
			return paramString;
		}
		StringBuilder localStringBuilder = new StringBuilder(paramString);
		while (localStringBuilder.length() < paramInt) {
			localStringBuilder.insert(0, '0');
		}
		return localStringBuilder.toString();
	}

	public JSONSerializer writeString(String paramString) throws IOException {
		if (null == paramString)
			return writeNull();

		this.writer.write(34);

		char[] arrayOfChar = paramString.toCharArray();

		for (int i = 0; i < arrayOfChar.length; ++i) {
			int j = arrayOfChar[i];
			switch (j) {
			case 34:
				this.writer.write("\\\"");
				break;
			case 92:
				this.writer.write("\\\\");
				break;
			case 0:
				this.writer.write("\\0");
				break;
			case 8:
				this.writer.write("\\b");
				break;
			case 9:
				this.writer.write("\\t");
				break;
			case 10:
				this.writer.write("\\n");
				break;
			case 12:
				this.writer.write("\\f");
				break;
			case 13:
				this.writer.write("\\r");
				break;
			case 47:
				this.writer.write("\\/");
				break;
			default:
				if ((j >= 32) && (j <= 126)) {
					this.writer.write(j);
				} else {
					this.writer.write("\\u");
					this.writer.write(rightAlignedZero(Integer.toHexString(j), 4));
				}
			}
		}

		this.writer.write(34);

		return this;
	}

	/**
	 * 
	 * @param paramObject
	 * @return
	 * @throws IOException
	 */
	public JSONSerializer write(Object paramObject) throws IOException {
		if (null == paramObject) {
			return writeNull();
		}

		if (paramObject instanceof Number) {
			return writeNumber((Number) paramObject);
		}

		if (paramObject instanceof String) {
			return writeString((String) paramObject);
		}

		if (paramObject instanceof Boolean) {
			return writeBoolean((Boolean) paramObject);
		}

		if (paramObject instanceof Set) {
			return writeSet((Set<?>) paramObject);
		}

		if (paramObject instanceof Map) {
			return writeMap((Map<?, ?>) paramObject);
		}

		if (paramObject instanceof List) {
			return writeList((List<?>) paramObject);
		}

		if (paramObject.getClass().isArray()) {
			Class<?> componentType = paramObject.getClass().getComponentType();
			boolean componentTypeIsPrimitive = componentType.isPrimitive();
			if (!componentTypeIsPrimitive) {
				return writeArray((Object[]) paramObject);
			} else {
				String component = "" + componentType;
				Object[] objectArray;
				PrimitiveType pptype = PrimitiveType.get(component);
				switch (pptype) {
				case INT:
					objectArray = new Object[((int[]) paramObject).length];
					for (int indx = 0; indx < ((int[]) paramObject).length; indx++) {
						objectArray[indx] = Integer.valueOf(((int[]) paramObject)[indx]);
					}
					break;
				case BOOLEAN:
					objectArray = new Object[((boolean[]) paramObject).length];
					for (int indx = 0; indx < ((boolean[]) paramObject).length; indx++) {
						objectArray[indx] = Boolean.valueOf(((boolean[]) paramObject)[indx]);
					}
					break;
				case BYTE:
					objectArray = new Object[((byte[]) paramObject).length];
					for (int indx = 0; indx < ((byte[]) paramObject).length; indx++) {
						objectArray[indx] = Integer.valueOf(((byte[]) paramObject)[indx]);
					}
					break;
				case CHAR:
					objectArray = new Object[((char[]) paramObject).length];
					for (int indx = 0; indx < ((char[]) paramObject).length; indx++) {
						objectArray[indx] = Integer.valueOf(((char[]) paramObject)[indx]);
					}
					break;
				case DOUBLE:
					objectArray = new Object[((double[]) paramObject).length];
					for (int indx = 0; indx < ((double[]) paramObject).length; indx++) {
						objectArray[indx] = Double.valueOf(((double[]) paramObject)[indx]);
					}
					break;
				case FLOAT:
					objectArray = new Object[((float[]) paramObject).length];
					for (int indx = 0; indx < ((float[]) paramObject).length; indx++) {
						objectArray[indx] = Float.valueOf(((float[]) paramObject)[indx]);
					}
					break;
				case LONG:
					objectArray = new Object[((long[]) paramObject).length];
					for (int indx = 0; indx < ((long[]) paramObject).length; indx++) {
						objectArray[indx] = Long.valueOf(((long[]) paramObject)[indx]);
					}
					break;
				case SHORT:
					objectArray = new Object[((short[]) paramObject).length];
					for (int indx = 0; indx < ((short[]) paramObject).length; indx++) {
						objectArray[indx] = Integer.valueOf(((short[]) paramObject)[indx]);
					}
					break;
				default:
					return writeNull();
				}
				return writeArray((Object[]) objectArray);
			}
		}
		return writeObject(paramObject);

	}

	/**
	 * Serializacja pojedynczego obiektu.
	 * 
	 * @param singleObject
	 * @return
	 * @throws IOException
	 */
	public JSONSerializer writeObject(Object singleObject) throws IOException {

		if (null == singleObject) {
			return writeNull();
		}

		writeRawString("{");
		final Class<?> clazz = singleObject.getClass();
		final Class<?>[] methodParams = new Class<?>[0];

		List<Field> fields = getAnnotatedFields(clazz);
		if (!fields.isEmpty()) {
			/* buduję JSON'a, bo mam z czego - start */
			indentPush();
			int i = 0;
			for (Field field : fields) {
				if (i != 0) {
					writeRawString(",");
				}
				JSONProperty ann = field.getAnnotation(JSONProperty.class);
				String propertyName = ann.name();
				if (StringUtils.isBlank(propertyName)) {
					propertyName = field.getName();
				}
				String methodName = "get" + field.getName().substring(0, 1).toUpperCase()
						+ field.getName().substring(1);
				Method method = null;
				Object propertyValue = null;
				try {
					method = clazz.getMethod(methodName, methodParams);
					propertyValue = method.invoke(singleObject);
				} catch (SecurityException | NoSuchMethodException | IllegalArgumentException | IllegalAccessException
						| InvocationTargetException e) {
					throw new IOException(e);
				}
				newLine();
				indent();
				writeString(propertyName);
				writeRawString(":");
				space();
				write(propertyValue);
				i++;
			}
			indentPop();
			newLine();
			indent();
			/* buduję JSON'a, bo mam z czego - koniec */
		} else if (log.isWarnEnabled()) {
			log.warn("Obiekt klasy {} nie posiada pól oznaczonych annotacją JSONProperty.", new Object[] { clazz });
		}
		writeRawString("}");

		return ((JSONSerializer) this);
	}

	/**
	 * Serializacja macierzy obiektów
	 * 
	 * @param objectArray
	 * @return
	 * @throws IOException
	 */
	public JSONSerializer writeArray(Object[] objectArray) throws IOException {
		if (null == objectArray)
			return writeNull();

		writeRawString("[");
		indentPush();

		for (int i = 0; i < objectArray.length; i++) {
			if (i != 0) {
				writeRawString(",");
			}
			newLine();
			indent();
			write(objectArray[i]);
		}

		indentPop();
		newLine();
		indent();
		writeRawString("]");

		return this;
	}

	/**
	 * Serializacja mapy obiektów - warunek: klucz musi być {@link String}
	 * 
	 * @param objectMap
	 * @return
	 * @throws IOException
	 */
	public JSONSerializer writeMap(Map<?, ?> objectMap) throws IOException {
		if (objectMap == null || objectMap.isEmpty()) {
			return writeNull();
		}
		writeRawString("{");
		/* buduję JSON'a, bo mam z czego - start */
		indentPush();
		int i = 0;
		for (Entry<?, ?> field : objectMap.entrySet()) {
			if (i != 0) {
				writeRawString(",");
			}

			String propertyName;
			if (field.getKey() instanceof String) {
				propertyName = (String) field.getKey();
			} else {
				propertyName = field.getKey().toString();
			}
			Object propertyValue = field.getValue();
			newLine();
			indent();
			writeString(propertyName);
			writeRawString(":");
			space();
			write(propertyValue);
			i++;
		}
		indentPop();
		newLine();
		indent();
		/* buduję JSON'a, bo mam z czego - koniec */
		writeRawString("}");
		return this;
	}

	/**
	 * Serializacja set'a obiektów - prosta sprawa: set zamieniany jest do macierzy
	 * obiektów i wywoływana jest metoda {@link #writeArray(Object[])}.
	 * 
	 * @param objectSet
	 * @return
	 * @throws IOException
	 */
	public JSONSerializer writeSet(Set<?> objectSet) throws IOException {
		if (objectSet == null || objectSet.isEmpty()) {
			return writeNull();
		}
		Object[] array = new Object[objectSet.size()];
		int i = 0;
		for (Object obj : objectSet) {
			array[i] = obj;
			i++;
		}
		return writeArray(array);
	}

	/**
	 * Serializacja listy obiektów - prosta sprawa: lista zamieniana jest do
	 * macierzy obiektów i wywoływana jest metoda {@link #writeArray(Object[])}.
	 * 
	 * @param objectList
	 * @return
	 * @throws IOException
	 */
	public JSONSerializer writeList(List<?> objectList) throws IOException {
		if (objectList == null || objectList.isEmpty()) {
			return writeNull();
		}
		Object[] array = new Object[objectList.size()];
		int i = 0;
		for (Object obj : objectList) {
			array[i] = obj;
			i++;
		}
		return writeArray(array);
	}

	/**
	 * Dodanie spacji - pod warunkiem pola {@link #format} ustawionego na
	 * {@code true}
	 * 
	 * @throws IOException
	 */
	public void space() throws IOException {
		if (format) {
			this.writer.append(" ");
		}
	}

	/**
	 * Dodanie nowej linii - pod warunkiem pola {@link #format} ustawionego na
	 * {@code true}
	 * 
	 * @throws IOException
	 */
	public void newLine() throws IOException {
		if (format) {
			this.writer.append("\n");
		}
	}

	public void indent() throws IOException {
		// TODO - metoda do implementacji
	}

	public void indentPush() {
		// TODO - metoda do implementacji
	}

	public void indentPop() {
		// TODO - metoda do implementacji
	}

	private static final Map<Class<?>, List<Field>> classFields = new HashMap<>();

	/**
	 * Metoda wyciągająca pola danego obiektu z adnotacją {@link JSONProperty}.
	 * Implementacja zawiera możliwość sięgnięcia do pól "superklasy", ale tylko i
	 * wyłącznie do "superklasy", która jest bezpośrednio związana z badanym
	 * obiektem.
	 * 
	 * @param obj
	 *            badany obiekt
	 * @return lista pól badanego obiektu
	 */
	private static List<Field> getAnnotatedFields(Class<?> clazz) {

		/* czy pola są dostępne w pamięci podręcznej? */
		List<Field> fList = classFields.get(clazz);
		if (fList != null) {
			/* zwracam już kiedyś przetworzony wynik */
			return fList;
		}

		/* zbieram informacje o wszystkich polach - start */
		Field[] fields;
		Field[] localfields = clazz.getDeclaredFields();
		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null && !superclass.getCanonicalName().startsWith("java")
				&& !superclass.getCanonicalName().startsWith("com.sun")) {

			Field[] superfields = superclass.getDeclaredFields();
			if (superfields.length != 0) {
				int superfieldsSize = superfields.length;
				int localfieldsSize = localfields.length;
				fields = new Field[superfieldsSize + localfieldsSize];
				int i = 0;
				for (Field f : localfields) {
					fields[i] = f;
					i++;
				}
				for (Field f : superfields) {
					fields[i] = f;
					i++;
				}
			} else {
				fields = localfields;
			}
		} else {
			fields = localfields;
		}
		/* zbieram informacje o wszystkich polach - koniec */

		/* do wyniku zbieram tylko pola opatrzone odpowiednią adnotacją */
		fList = new ArrayList<>();
		if (fields != null && fields.length != 0) {
			for (Field f : fields) {
				if (f.isAnnotationPresent(JSONProperty.class)) {
					fList.add(f);
				}
			}
		}
		classFields.put(clazz, fList);
		return fList;
	}

	/**
	 * @return the {@link #format}
	 */
	public boolean isFormat() {
		return format;
	}

	/**
	 * @param format
	 *            the {@link #format} to set
	 */
	public void setFormat(boolean format) {
		this.format = format;
	}

}
