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
import pl.slawas.common.mail.box.api.IMailBox;
import pl.slawas.common.mail.helpers.MailProtocol;
import pl.slawas.common.mail.helpers.MessageHelper;

/**
 * PartMetaData - metadane do prezentowania treści wiadomości elektronicznej.
 * Lista poszczególnych części wiadomości.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class PartMetaData extends AbstractMetaData {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7025065938755515560L;
	private static final String SUFFIX_TABLE_NAME = "MailPart";
	private static final int COLUMN_COUNT = 18;

	public PartMetaData() {
		this(new MailBox(MailProtocol.imap, "server", "username", "name", "passwordEncoderClass"));
	}

	protected void addStringColumnMataData(IMailBox mailBox, String mailProperty) {
		String columnName = MessageHelper.mailProperty2ColumnName(mailProperty);
		super.addColumnMataData(mailBox.getServer(), mailBox.getUsername(), mailBox.getName() + SUFFIX_TABLE_NAME,
				STRING_CLASS, SMALL_SIZE, columnName, mailProperty, STRING_DB_TYPE);
	}

	public PartMetaData(IMailBox mailBox) {
		super(COLUMN_COUNT);
		addStringColumnMataData(mailBox, MessageHelper.MAIL_NUMBER_COLUMN);
		addStringColumnMataData(mailBox, MessageHelper.MAIL_SIZE);
		addStringColumnMataData(mailBox, MessageHelper.MAIL_MESSAGE_ID_COLUMN);
		addStringColumnMataData(mailBox, "Subject");
		addStringColumnMataData(mailBox, "From");
		addStringColumnMataData(mailBox, MessageHelper.MAIL_RECEIVED_DATE_COLUMN);
		addStringColumnMataData(mailBox, MessageHelper.MAIL_SENT_DATE_COLUMN);
		addStringColumnMataData(mailBox, "To");
		addStringColumnMataData(mailBox, "Cc");
		addStringColumnMataData(mailBox, "Bcc");
		addStringColumnMataData(mailBox, MessageHelper.MAIL_PART_CONTENT_TYPE);
		addStringColumnMataData(mailBox, MessageHelper.MAIL_PART_CONTENT_TENCODING_COLUMN);
		addStringColumnMataData(mailBox, MessageHelper.MAIL_PART_CONTENT_DISPOSITION);
		addStringColumnMataData(mailBox, "X-Attachment-Id");
		addStringColumnMataData(mailBox, MessageHelper.MAIL_PART_CONTENT_COLUMN);
		addStringColumnMataData(mailBox, MessageHelper.MAIL_PART_LOCAL_FILE_PATH);
		addStringColumnMataData(mailBox, MessageHelper.MAIL_PART_HTTP_FILE_PATH);
		addStringColumnMataData(mailBox, MessageHelper.MAIL_SOURCE_EML_LOCAL_FILE_PATH);
		addStringColumnMataData(mailBox, MessageHelper.MAIL_SOURCE_EML_HTTP_FILE_PATH);
	}

}
