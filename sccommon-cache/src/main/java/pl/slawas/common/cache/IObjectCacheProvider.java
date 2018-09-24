package pl.slawas.common.cache;

import java.net.URL;
import java.util.Properties;

import org.jdom.Element;

/**
 * 
 * IObjectCacheProvider
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $ 
 *
 * @param <Conf>
 */
public interface IObjectCacheProvider<Conf> {

	/**
	 * Inicjalizacja konfiguracji managera pamięci podręcznej.
	 * 
	 * @param additionalProps
	 *            dodatkowe parametry, np. dotyczące nowo tworzonych regionów
	 * @return czy manager został zainicjalizowany podczas wywołania tej metody?
	 *         {@code false} oznacz, że był zainicjalizowany już wcześniej,
	 *         {@code true}, że został zainicjalizowany właśnie teraz.
	 */
	CacheProviderConfiguration<Conf> initConfiguration(Properties additionalProps);

	/**
	 * Inicjalizacja provider'a pamięci podręcznej
	 * 
	 * @return informacja czy powstała nowa instancja manager'a. {@code false}
	 *         oznacza, że instancja już istniała.
	 */
	boolean init();

	/**
	 * Nazwa instancji managera pamięci podręcznej.
	 * 
	 * @return nazwa managera
	 */
	String getName();

	String[] getCacheNames();

	IObjectCache getCache(String name);

	void removeCache(String name);

	void close();

	/**
	 * Pobieranie statystyk dla wszystkich regionów pamięci podręcznej.
	 * 
	 * @param offset
	 *            pozycja, od której ma zaczynać się lista statystyk - domyślnie 0
	 * @param pageSize
	 *            maksymalna liczba rekordów jaka ma być zwrócona, rozmiar strony -
	 *            domyślnie {@link CacheConstants#DEFAULT_MAX_RESULT_SIZE}
	 * @return lista statystyk regionów w postaci {@link Element}
	 */
	ObjectCacheStatisticsList getAllStatistics(Integer offset, Integer pageSize);

	/**
	 * Pobieranie statystyk pojedynczego regionu
	 * 
	 * @param regionName
	 *            nazwa regionu
	 * @return statystyka
	 */
	IObjectCacheStatistics getStatistics(String regionName);

	/**
	 * Usuwa dane z wybranego cache.
	 * 
	 * @param cacheName
	 */
	void clearCache(String cacheName);

	/**
	 * Czyszczenie statystyk wybranej pamięci podręcznej cache.
	 * 
	 * @param cacheName
	 */
	@Deprecated
	void clearStatistics(String cacheName);

	/**
	 * Definicja prvider'a pamięci podręcznej. Zaraz po utworzeniu instancji
	 * provider'a NIE MOŻE i nie powinna BYĆ PUSTA!!!!!
	 * 
	 * @return
	 */
	CacheProviderEnum getAssociatedProvider();

	/**
	 * Pobranie ścieżki konfiguracji. Zaraz po utworzeniu instancji provider'a NIE
	 * MOŻE i nie powinna BYĆ PUSTA!!!!!
	 * 
	 * @see CacheProviderFactory#loadInstance(_IObjectCacheProvider, Properties)
	 * 
	 * @return ścieżka konfiguracji
	 */
	URL getConfigurationFileURL();

	/**
	 * Ustawianie dodatkowych parametrów.
	 * 
	 * @param additionalProps
	 */
	void setAdditionalProps(Properties additionalProps);

}