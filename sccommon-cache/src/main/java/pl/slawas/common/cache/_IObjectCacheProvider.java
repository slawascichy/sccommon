package pl.slawas.common.cache;

import java.util.List;
import java.util.Properties;

import org.jdom.Element;

public interface _IObjectCacheProvider {

	/**
	 * Inicjalizacja managera pamięci podręcznej.
	 * 
	 * @param props
	 *            parametry pamięci podręcznej.
	 * @return czy manager został zainicjalizowany podczas wywołania tej metody?
	 *         {@code false} oznacz, że był zainicjalizowany już wcześniej,
	 *         {@code true}, że został zainicjalizowany właśnie teraz.
	 */
	boolean init(Properties props);

	String[] getCacheNames();

	_IObjectCache getCache(String name);

	void removeCache(String name);

	void close();

	/**
	 * Pobieranie statystyk dla wszystkich regionów pamięci podręcznej.
	 * 
	 * @param maxRecords
	 *            maksymalna liczba rekordów jaka ma być zwrócona.
	 * @return lista statystyk regionów w postaci {@link Element}
	 */
	List<_IObjectCacheStatistics> getAllStatistics(Integer maxRecords);

	/**
	 * Usuwa dane z wybranego cache.
	 * 
	 * @param cacheName
	 */
	void clearCache(String cacheName);

	/**
	 * Zwraca listę wszystkich kluczy aktualnie przechowywanych obiektów
	 * 
	 * @param cacheName
	 *            nazwa pamięci podręcznej/regionu
	 * @return lista kluczy
	 */
	List<String> getKeysList(String cacheName);

	/**
	 * Czyszczenie statystyk wybranej pamięci podręcznej cache.
	 * 
	 * @param cacheName
	 */
	@Deprecated
	void clearStatistics(String cacheName);

}