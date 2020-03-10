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
package pl.slawas.common.mail.smtp.api;

/**
 * IMailSenderProviderStandAlone
 * 
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface IMailSenderProviderStandAlone extends IMailSenderProvider {

	/**
	 * @param password
	 *            the {@link #password} to set
	 */
	void setPassword(String password);

	/**
	 * @return the {@link #username}
	 */
	String getUsername();

	/**
	 * @return the {@link #port}
	 */
	String getPort();

	/**
	 * @param port
	 *            the {@link #port} to set
	 */
	void setPort(String port);

	/**
	 * @return the {@link #auth}
	 */
	boolean isAuth();

	/**
	 * @param auth
	 *            the {@link #auth} to set
	 */
	void setAuth(boolean auth);

	/**
	 * @return the {@link #tls}
	 */
	boolean isTls();

	/**
	 * @param tls
	 *            the {@link #tls} to set
	 */
	void setTls(boolean tls);

	/**
	 * @return the {@link #ssl}
	 */
	boolean isSsl();

	/**
	 * @param ssl
	 *            the {@link #ssl} to set
	 */
	void setSsl(boolean ssl);

	/**
	 * @return the {@link #host}
	 */
	String getHost();

	/**
	 * @return the {@link #connectiontimeout}
	 */
	int getConnectiontimeout();

	/**
	 * @param connectiontimeout
	 *            the {@link #connectiontimeout} to set
	 */
	void setConnectiontimeout(int connectiontimeout);

	String getSocketFactoryClass();

	/**
	 * @param socketFactoryClass
	 *            the {@link #socketFactoryClass} to set
	 */
	void setSocketFactoryClass(String socketFactoryClass);

}
