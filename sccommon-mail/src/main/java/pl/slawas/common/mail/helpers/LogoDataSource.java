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
package pl.slawas.common.mail.helpers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

import pl.slawas.common.mail.box.MailBoxVersion;

/**
 * AttachDataSource
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LogoDataSource implements DataSource {

	private static final String logoFileName = MailBoxVersion.SCISOFTWARE_LOGO_PATH + "/"
			+ MailBoxVersion.SCISOFTWARE_LOGO_FILE_NAME;

	/**
	 * Tu nie zamykamy stream'a bo liczymy na to, że standardowe mechanizmy mail'a
	 * to zrobią
	 */
	public InputStream getInputStream() throws IOException {
		return getClass().getResourceAsStream(logoFileName);
	}

	/**
	 * Tu nie zamykamy stream'a bo liczymy na to, że standardowe mechanizmy mail'a
	 * to zrobią
	 */
	public OutputStream getOutputStream() throws IOException {
		return null;
	}

	public String getContentType() {
		return MailBoxVersion.SCISOFTWARE_LOGO_CONTENT_TYPE;
	}

	public String getName() {
		return MailBoxVersion.SCISOFTWARE_LOGO_FILE_NAME;
	}

	public String getShortName() {
		return getName();
	}

}
