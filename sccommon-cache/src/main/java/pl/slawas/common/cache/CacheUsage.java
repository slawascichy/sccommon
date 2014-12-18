package pl.slawas.common.cache;

/**
 * 
 * CacheUsage - metody/statusy użycia elementów w cache (pamięci podręcznej)
 * 
 * @see #NONE
 * @see #TO_USE
 * @see #REFRESH
 * @see #TO_REMOVE
 * @see #TO_USE_IN_FIRST_LEVEL_CACHE
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.2 $
 * 
 */
public enum CacheUsage {

	/**
	 * Nie używaj cache podczas pobierania elementu.
	 */
	NONE,
	/**
	 * Użyj cache podczas pobierania elementu.
	 */
	TO_USE,
	/**
	 * Użyj cache, ale odśwież element tam się znajdujący.
	 */
	REFRESH,
	/**
	 * W trakcie aktywnej transakcji, użyj pamięci podręcznej z elementem, który
	 * właśnie zostanie dodany został‚ dodany, i którego jeszcze nie ma w bazie
	 * danych, użyj go tylko w lokalnej pamięci podręcznej .
	 */
	TO_USE_IN_FIRST_LEVEL_CACHE,
	/**
	 * Pobierz element z cache i jeżeli go tam znajdziesz to go stamtąd usuń.
	 */
	TO_REMOVE;
}
