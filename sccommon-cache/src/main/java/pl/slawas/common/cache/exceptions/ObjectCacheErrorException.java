package pl.slawas.common.cache.exceptions;

/**
 * 
 * AresCacheErrorException
 * <p>
 * Klasa informaująca o błędzie kesza (przestrzeni zapasowej).
 * </p>
 * 
 * @author Sławomir Cichy &lt;slawas@scisoftware.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class ObjectCacheErrorException extends Throwable {

	private static final long serialVersionUID = 1602501396214379967L;

	/**
	 * 
	 */
	public ObjectCacheErrorException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ObjectCacheErrorException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public ObjectCacheErrorException(String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public ObjectCacheErrorException(Throwable cause) {
		super(cause);
	}

}
