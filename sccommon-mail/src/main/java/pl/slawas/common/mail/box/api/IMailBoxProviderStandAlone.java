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
package pl.slawas.common.mail.box.api;

import pl.slawas.common.mail.helpers.MailProtocol;

/**
 * _MailBoxProviderStandAlone
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface IMailBoxProviderStandAlone extends IMailBoxProvider {

	/**
	 * @param password
	 *            the {@link #password} to set
	 */
	void setPassword(String password);

	/**
	 * @return the {@link #protocol}
	 */
	MailProtocol getProtocol();

	/**
	 * @return the {@link #startTSLEnable}
	 */
	boolean isStartTSLEnable();

	/**
	 * @param startTSLEnable
	 *            the {@link #startTSLEnable} to set
	 */
	void setStartTSLEnable(boolean startTSLEnable);

	/**
	 * @return the {@link #socketFactoryProvider}
	 */
	String getSocketFactoryProvider();

	/**
	 * @param socketFactoryProvider
	 *            the {@link #socketFactoryProvider} to set
	 */
	void setSocketFactoryProvider(String socketFactoryProvider);

	/**
	 * @return the {@link #socketFactoryClass}
	 */
	String getSocketFactoryClass();

	/**
	 * @param socketFactoryClass
	 *            the {@link #socketFactoryClass} to set
	 */
	void setSocketFactoryClass(String socketFactoryClass);

	/**
	 * @return the {@link #connectiontimeout}
	 */
	int getConnectiontimeout();

	/**
	 * @param connectiontimeout
	 *            the {@link #connectiontimeout} to set
	 */
	void setConnectiontimeout(int connectiontimeout);

	String getPasswordEncoderClass();

	void setPasswordEncoderClass(String passwordEncoderClass);

}
