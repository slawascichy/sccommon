package pl.slawas.common.cache;

import java.io.Closeable;

/**
 * 
 * _IObjectWithCache interfejs dla obiektów używających cache (pamięci
 * podręcznej)
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface IObjectWithCache extends Closeable {

	/**
	 * Pobranie cache'a
	 * 
	 * @return
	 */
	IObjectCache getCache();

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
	 * Metoda sprawdzająca czy pamięć podręczna dla danego obiektu jest zdefiniowana
	 * i dostępna
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
