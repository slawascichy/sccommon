package pl.slawas.common.ldap.utils;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * GroupUtils - klasa pomocnicza do obróbki parametrów grup.
 * 
 * @author Sławomir Cichy &lt;scichy@ibpm.pro&gt;
 * @version $Revision: 1.2 $
 * 
 */
public class GroupUtils {

	private GroupUtils() {
	}

	private static final String OU = "OU=";

	/**
	 * Metoda dekodująca z DN obiekt OU= jako rozszerzenie nazwy grupy do
	 * prezentacji.
	 * 
	 * @param dn
	 *            DN grupy
	 * @return słowo {@code ' (<ou>)'}, gdzie &lt;ou> to nazwa OU.
	 */
	public static String decodeGroupDisplayNameExtention(String dn) {
		if (StringUtils.isBlank(dn) || dn.toUpperCase().indexOf(OU) < 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		sb.append(" (");
		sb.append(decodeGroupOrganizationUnit(dn));
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Metoda dekodująca z DN wartość OU (Organization Unit)
	 * 
	 * @param dn
	 *            DN grupy
	 * @return wartość OU (Organization Unit).
	 */
	public static String decodeGroupOrganizationUnit(String dn) {
		if (StringUtils.isBlank(dn) || dn.toUpperCase().indexOf(OU) < 0) {
			return "";
		}
		int firstOUPosition = dn.toUpperCase().indexOf(OU) + OU.length();
		String tmp = dn.substring(firstOUPosition);
		return (tmp.indexOf(',') > 0 ? tmp.substring(0, tmp.indexOf(',')) : tmp);
	}

}
