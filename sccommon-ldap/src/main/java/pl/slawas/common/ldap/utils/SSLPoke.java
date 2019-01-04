package pl.slawas.common.ldap.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * SSLPoke - sprawdzenie poprawności połączenia SSL
 * 
 * https://confluence.atlassian.com/display/BAMKB/Unable+to+Connect+to+SSL+
 * Services+due+to+PKIX+Path+Building+Failed+sun.security.provider.certpath.
 * SunCertPathBuilderException
 * 
 * https://confluence.atlassian.com/download/attachments
 * /218272870/SSLPoke.java?version=1&modificationDate=1275292817293&api=v2
 * 
 * @version $Revision: 1.1 $
 * 
 */
public class SSLPoke {

	public static void main(String[] args) {
		if (args.length != 2) {
			System.out.println("Usage: " + SSLPoke.class.getName()
					+ " <host> <port>");
			System.exit(1);
		}
		OutputStream out = null;
		InputStream in = null;
		try {
			SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory
					.getDefault();
			SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(
					args[0], Integer.parseInt(args[1]));

			in = sslsocket.getInputStream();
			out = sslsocket.getOutputStream();

			// Write a test byte to get a reaction :)
			out.write(1);

			while (in.available() > 0) {
				System.out.print(in.read());
			}
			System.out.println("Successfully connected");

		} catch (Exception exception) {
			exception.printStackTrace();
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
