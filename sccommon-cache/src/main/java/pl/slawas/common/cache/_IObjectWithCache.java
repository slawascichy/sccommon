package pl.slawas.common.cache;

/**
 * 
 * _IObjectWithCache interfejs dla obiektów używających cache (pamięci
 * podręcznej)
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface _IObjectWithCache {

	/**
	 * Pobranie cache'a
	 * 
	 * @return
	 */
	_IObjectCache getCache();

	/**
	 * Czas życia elementu w cache'u
	 * 
	 * @return wartość w milisekundach
	 */
	long getCacheTimeToLive();

	/**
	 * Ustawianie czasu życia elementów w cache'u
	 * 
	 * @param cacheTimeToLive
	 *            wartość w milisekundach
	 */
	void setCacheTimeToLive(long cacheTimeToLive);

	/**
	 * Metoda sprawdzająca czy pamięć podręczna dla danego obiektu jest
	 * zdefiniowana i dostępna
	 * 
	 * @return {@code true} albo {@code false}
	 */
	boolean cacheIsAllowed();

	/**
	 * Nazwa cache'a, regionu składowania elementów
	 * 
	 * @return nazwa regionu
	 */
	String getCacheRegionName();

	/**
	 * Metoda zamykająca obiekt, odpowiedzialna również za usunięcie instancji
	 * cache'a w przypadku gdy pamięć podręczna nie jest współdzielony.
	 * 
	 * @see #cacheIsShared()
	 */
	void close();

	/**
	 * Informacja czy używany pamięć podręczna jest współdzielona pomiędzy
	 * instancjami obiektów. Ważne z punktu widzenia implementacji metody
	 * {@link #close()}
	 * 
	 * @return {@code true} albo {@code false}
	 */
	boolean cacheIsShared();

	/**
	 * Pobranie obiektu dostawcy pamięci współdzielonej.
	 * 
	 * @return
	 */
	CacheProviderEnum getCacheProvider();

}
