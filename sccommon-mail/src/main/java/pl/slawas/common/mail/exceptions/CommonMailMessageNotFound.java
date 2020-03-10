package pl.slawas.common.mail.exceptions;

public class CommonMailMessageNotFound extends CommonMailException {

	private static final long serialVersionUID = 7361230493574810007L;

	/**
	 * 
	 * @param msgID
	 *            - identyfikator wiadomości
	 */
	public CommonMailMessageNotFound(String msgID) {
		super("Nie znalazłem wiadomości o numerze " + msgID);
	}

	/**
	 * 
	 * @param msgIDs
	 *            - identyfikatory wiadomości
	 */
	public CommonMailMessageNotFound() {
		super("Nie znalazłem żadnej wiadomości");
	}

}
