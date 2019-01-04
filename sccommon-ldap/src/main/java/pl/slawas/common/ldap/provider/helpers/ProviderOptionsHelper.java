package pl.slawas.common.ldap.provider.helpers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;

public class ProviderOptionsHelper {

	/**
	 * Ładowanie hasła z pliku
	 * 
	 * @param clazz
	 * @param fileName
	 * @return przechowywane w pliku hasło
	 * @throws IOException
	 */
	public static String loadPasswordWord(Class<?> clazz, String fileName)
			throws IOException {

		String passwordWord = null;
		BufferedReader d = null;
		try {
			d = getBufferedReader(clazz, fileName);
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

	private static BufferedReader getBufferedReader(Class<?> clazz,
			String fileName) throws FileNotFoundException {
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
				d = new BufferedReader(new InputStreamReader(clazz
						.getResourceAsStream(fileName), "UTF-8"));
			} catch (UnsupportedEncodingException e) {
			}
		}
		return d;
	}
}
