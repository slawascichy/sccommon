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
package pl.slawas.common.mail.sql;

import pl.slawas.common.mail.box.MailBox;
import pl.slawas.common.mail.helpers.MessageHelper;

/**
 * FolderMetaData - metadane do prezentowania listy folderów (skrzynek
 * pocztowych).
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class FolderMetaData extends AbstractMetaData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1025065938755515560L;
	private static final String SUFFIX_TABLE_NAME = "_Folders";

	public FolderMetaData(MailBox mailBox) {
		super(MessageHelper.FolderFields.values().length);
		for (MessageHelper.FolderFields field : MessageHelper.FolderFields
				.values()) {
			super.addColumnMataData(mailBox.getServer(), mailBox.getUsername(),
					mailBox.getName() + SUFFIX_TABLE_NAME, STRING_CLASS,
					SMALL_SIZE, field.name(), field.name(), STRING_DB_TYPE);
		}

	}

}
