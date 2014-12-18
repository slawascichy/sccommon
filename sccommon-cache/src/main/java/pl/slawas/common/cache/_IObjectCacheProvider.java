package pl.slawas.common.cache;

import java.util.List;

import org.jdom.Element;

public interface _IObjectCacheProvider {

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
	public List<_IObjectCacheStatistics> getAllStatistics(Integer maxRecords);

}