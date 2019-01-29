package pl.slawas.common.ldap.provider.helpers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * 
 * ProviderOptionsHelper
 *
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl&gt;
 * @version $Revision: 1.1 $
 *
 */
public class ProviderOptionsHelper {

	private ProviderOptionsHelper() {
	}

	/**
	 * Ładowanie hasła z pliku
	 * 
	 * @param clazz
	 * @param fileName
	 * @return przechowywane w pliku hasło
	 * @throws IOException
	 */
	public static String loadPasswordWord(Class<?> clazz, String fileName) throws IOException {

		String passwordWord = null;
		BufferedReader d = null;
		try {
			d = getBufferedReader(clazz, fileName);
			if (d == null) {
				return null;
			}
			String inputLine;
			while ((inputLine = d.readLine()) != null) {
				passwordWord = inputLine;
			}
		} finally {
			if (d != null) {
				d.close();
			}
		}

		return passwordWord;
	}

	private static BufferedReader getBufferedReader(Class<?> clazz, String fileName) throws FileNotFoundException {
		BufferedReader d = null;
		URL resource = clazz.getResource(fileName);
		if (resource == null) {
			FileInputStream fis = new FileInputStream(fileName);
			try {
				d = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				/* ignoruję, bo to nie może się zdarzyć */
			}
		} else {
			try {
				d = new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(fileName), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
				/* ignoruję, bo to nie może się zdarzyć */
			}
		}
		return d;
	}
}
