package pl.slawas.common.ldap.cache;

/**
 * 
 * ForceSyncUserGroups obiekt wykorzystywany do blokowania równoczesnych
 * operacji synchronizacji wszystkich grup z AD. Operacja synchronizacji grup
 * może w danym momencie występować tylko raz.
 * 
 * <p>
 * IBPM.PRO S.A. Copyright &copy; 2013
 * </p>
 * 
 * @author Sławomir Cichy &lt;scichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 * 
 */
public final class ForceSyncUserGroups {

	private ForceSyncUserGroups() {
	}

	private static final ForceSyncUserGroups instance = new ForceSyncUserGroups();

	/**
	 * Pobranie singleton'a obiektu blokującego.
	 * 
	 * @return instancja obiektu blokującego
	 */
	public static ForceSyncUserGroups getInstance() {
		return instance;
	}

	/**
	 * Flaga czy można wykonywać pełną synchronizację grup.
	 */
	private boolean ok = false;

	/**
	 * Informacja o tym, kto teraz wykonuje pełną synchronizację grup.
	 */
	private String userName = null;

	public boolean isOk() {
		return ok;
	}

	/**
	 * Ustawienie {@link #ok} na wartość {@code true}
	 */
	public void setOk2True() {
		this.ok = true;
	}

	/**
	 * Ustawienie {@link #ok} na wartość {@code false}
	 */
	public void setOk2False() {
		this.ok = false;
	}

	/**
	 * Pobranie nazwy użytkownika blokującego operację pełnej synchronizacji
	 * 
	 * @return nazwa użytkownika {@link #userName}
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Ustawienie nazwy użytkownika blokującego pełną synchronizację grup
	 * 
	 * @param userName
	 *            nazwa użytkownika
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * Resetowanie nazwy użytkownika (ustawianie {@link #userName} na {@code null})
	 */
	public void resetUserName() {
		this.userName = null;
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

}
