package pl.slawas.common.ldap.api;

public interface ICustomBinaryField {

	/** Separator wykorzystywany podczas konwersji wartości do String'a */
	public static final String VALUE_SEPARATOR = "-";
	
	public static final String SPACE_SEPARATOR = " ";

	/** Wartość reprezentująca niezdefiniowaną długość pola binarnego */
	public static final int UNSPECIFIED_LENGTH = -1;

	/**
	 * Konwersja wartości pola binarnego do String'a
	 * 
	 * @param objectUID
	 *            wartość atrybutu pola binarnego
	 * @return reprezentacja wartości pola binarnego w postaci String'a
	 */
	String value2String(byte[] objectUID);

}
