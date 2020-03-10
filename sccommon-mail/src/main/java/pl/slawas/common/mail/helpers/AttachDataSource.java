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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;
import javax.activation.FileTypeMap;

import org.apache.commons.lang.StringUtils;

/**
 * AttachDataSource
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class AttachDataSource implements DataSource {

	private final File _file;

	private FileTypeMap typeMap;

	public static final String UNIX_SEPARATOR = "/";
	public static final String WINDOWS_SEPARATOR = "\\";

	public AttachDataSource(File paramFile) {
		this.typeMap = null;
		this._file = paramFile;
	}

	public AttachDataSource(String paramString) {
		this(new File(paramString));
	}

	/**
	 * Tu nie zamykamy stream'a bo liczymy na to, że standardowe mechanizmy
	 * mail'a to zrobią
	 */
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(this._file);
	}

	/**
	 * Tu nie zamykamy stream'a bo liczymy na to, że standardowe mechanizmy
	 * mail'a to zrobią
	 */
	public OutputStream getOutputStream() throws IOException {
		return new FileOutputStream(this._file);
	}

	public String getContentType() {
		if (this.typeMap == null) {
			return FileTypeMap.getDefaultFileTypeMap().getContentType(
					this._file);
		}
		return this.typeMap.getContentType(this._file);
	}

	public String getName() {
		return this._file.getName();
	}

	public File getFile() {
		return this._file;
	}

	public void setFileTypeMap(FileTypeMap paramFileTypeMap) {
		this.typeMap = paramFileTypeMap;
	}

	public String getShortName() {
		String name = this._file.getName();
		name = StringUtils.replace(name, WINDOWS_SEPARATOR, UNIX_SEPARATOR);
		int lastPos = name.lastIndexOf(UNIX_SEPARATOR);
		return name.substring(lastPos + 1);
	}

}
