package pl.slawas.common.ldap.beans;

import java.io.Serializable;
import java.sql.Types;

import pl.slawas.common.ldap.provider.CustomBinaryField;

/**
 * 
 * UserAttributeType - typy atrybutów systemowych
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public enum UserAttributeType implements Serializable {

	/** String */
	STRING(Types.VARCHAR, null),
	/** Pole binarne, które przekształcane jest do base64 */
	BINARY(Types.BINARY, null),
	/**
	 * Specjalne pole binarne, które przekształcane jest do string'a przez obiekt
	 * {@link CustomBinaryField}
	 */
	CUSTOMBINARY(Types.BINARY, null),
	/** Liczba całkowita */
	INTEGER(Types.INTEGER, null),
	/** Liczba zmiennoprzecinkowa */
	NUMERIC(Types.DECIMAL, null),
	/** Inny typ */
	OTHER(Types.OTHER, null),
	/** Macierz stringów - przedstawiana jako String w postaci JSON'a */
	STRINGARRAY(Types.ARRAY, Types.VARCHAR),
	/**
	 * Macierz pól binarnych - przedstawiana jako String w postaci JSON'a. Wartość
	 * przekształcona do base64
	 */
	BINARYARRAY(Types.ARRAY, Types.BINARY),
	/**
	 * Macierz z liczbami całkowitymi - przedstawiana jako String w postaci JSON'a.
	 */
	INTARRAY(Types.ARRAY, Types.INTEGER),
	/**
	 * Macierz z liczbami zmiennoprzecinkowymi - przedstawiana jako String w postaci
	 * JSON'a.
	 */
	NUMARRAY(Types.ARRAY, Types.DECIMAL),
	/** Macierz pól innych typów - przedstawiana jako String w postaci JSON'a. */
	OTHERARRAY(Types.ARRAY, Types.OTHER);

	/** wartość reprezentująca typ SQL */
	private final int sqlId;

	/**
	 * wartość reprezentująca podtyp typ SQL, dla typów prostych przyjmuje wartość
	 * {@code null}
	 */
	private final Integer subSqlId;

	private UserAttributeType(int sqlId, Integer subSqlId) {
		this.sqlId = sqlId;
		this.subSqlId = subSqlId;
	}

	/**
	 * @return the {@link #sqlId}
	 */
	public int getSqlId() {
		return sqlId;
	}

	/**
	 * @return the {@link #subSqlId}
	 */
	public Integer getSubSqlId() {
		return subSqlId;
	}

	public static UserAttributeType getSimpleType(int sqlType) {
		for (UserAttributeType val : UserAttributeType.values()) {
			if (sqlType == val.sqlId) {
				return val;
			}
		}
		return OTHER;
	}

	public static UserAttributeType getArrayType(int sqlType) {
		for (UserAttributeType val : UserAttributeType.values()) {
			if (val.subSqlId != null && sqlType == val.subSqlId.intValue()) {
				return val;
			}
		}
		return OTHERARRAY;
	}

}
