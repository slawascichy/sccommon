package pl.slawas.common.cache;

/**
 * 
 * CacheSharing informacje o współdzieleniu instancji kesz-a
 * 
 * @see #NONE
 * @see #SHARED
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public enum CacheSharing {

	/**
	 * Kesz nie jest współdzielony
	 */
	NONE(false),
	/**
	 * Kesz jest współdzielony, stanowi jedną instancję
	 */
	SHARED(true);

	private final boolean isShared;

	/**
	 * Konstruktor ustawiający flagę współdzielenia (istnienia tylko jednej
	 * instancji kesz-a)
	 * 
	 * @param isShared
	 */
	private CacheSharing(boolean isShared) {
		this.isShared = isShared;
	}

	/**
	 * Czy dany typ współdzielenia jest współdzielonym (istnieje tylko jedna
	 * instancja kesz-a)?
	 * 
	 * @return {@code true} alebo {@code false}
	 */
	public boolean isShared() {
		return this.isShared;
	}

}
