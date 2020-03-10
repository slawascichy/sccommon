/*
 * Slawas.pl Copyright &copy; 2011-2012 
 * http://slawas.pl 
 * All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL SŁAWOMIR CICHY BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pl.slawas.common.mail.box;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * ExchangeSSLSocketFactory pomocnicza obsługa IMAP'a z Exchange z SSL'em?
 * Rozwiązanie dla problemu Java Mail to Exchange Server “no login methods
 * supported”
 * 
 * 
 * http://stackoverflow.com/questions/10442877/java-mail-to-exchange-server-no-
 * login-methods-supported
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class ExchangeSSLSocketFactory extends SSLSocketFactory {

	private SSLSocketFactory sslSocketFactory;
	private SocketFactory socketFactory;

	public ExchangeSSLSocketFactory() {
		try {
			socketFactory = SocketFactory.getDefault();

			SSLContext context = SSLContext.getInstance("TLS");
			context.init(null, new TrustManager[] { new EmptyTrustManager() },
					null);
			sslSocketFactory = (SSLSocketFactory) context.getSocketFactory();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private final class EmptyTrustManager implements X509TrustManager {
		public void checkClientTrusted(X509Certificate[] cert, String authType)
				throws CertificateException {
		}

		public void checkServerTrusted(X509Certificate[] cert, String authType)
				throws CertificateException {
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new java.security.cert.X509Certificate[0];
		}
	}

	public static SocketFactory getDefault() {
		return new ExchangeSSLSocketFactory();
	}

	@Override
	public Socket createSocket(Socket socket, String s, int i, boolean flag)
			throws IOException {
		return sslSocketFactory.createSocket(socket, s, i, flag);
	}

	@Override
	public Socket createSocket(InetAddress inaddr, int i, InetAddress inaddr1,
			int j) throws IOException {
		return socketFactory.createSocket(inaddr, i, inaddr1, j);
	}

	@Override
	public Socket createSocket(InetAddress inaddr, int i) throws IOException {
		return socketFactory.createSocket(inaddr, i);
	}

	@Override
	public Socket createSocket(String s, int i, InetAddress inaddr, int j)
			throws IOException {
		return socketFactory.createSocket(s, i, inaddr, j);
	}

	@Override
	public Socket createSocket(String s, int i) throws IOException {
		return socketFactory.createSocket(s, i);
	}

	@Override
	public Socket createSocket() throws IOException {
		return socketFactory.createSocket();
	}

	@Override
	public String[] getDefaultCipherSuites() {
		return sslSocketFactory.getSupportedCipherSuites();
	}

	@Override
	public String[] getSupportedCipherSuites() {
		return sslSocketFactory.getSupportedCipherSuites();
	}

}
