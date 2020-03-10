package pl.slawas.common.mail.exceptions;

/**
 * 
 * CommonMailException - ogólny komunikat błędu.
 *
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl&gt;
 * @version $Revision: 1.1 $
 *
 */
public class CommonMailException extends Exception {

	private static final long serialVersionUID = 8696281188117738994L;

	public CommonMailException(Throwable cause) {
		super(cause);
	}

	public CommonMailException(String msg) {
		super(msg);
	}

	public CommonMailException(String msg, String msg2, Exception e) {
		super(msg + " " + msg2 + " " + e.getMessage(), e);
	}

}
