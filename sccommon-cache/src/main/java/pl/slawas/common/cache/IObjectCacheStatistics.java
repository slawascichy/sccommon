package pl.slawas.common.cache;

import java.io.Serializable;

/**
 * 
 * IObjectCacheStatistics
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $ 
 *
 */
public interface IObjectCacheStatistics extends Serializable {

	/**
	 * Clears the statistic counters to 0 for the associated Cache.
	 */

	void clearStatistics();

	/**
	 * The number of times a requested item was found in the cache.
	 * 
	 * @return the number of times a requested item was found in the cache
	 */
	long getCacheHits();

	/**
	 * Number of times a requested item was found in the Memory Store.
	 * 
	 * @return the number of times a requested item was found in memory
	 */
	long getInMemoryHits();

	/**
	 * Number of times a requested item was found in the Disk Store.
	 * 
	 * @return the number of times a requested item was found on Disk, or 0 if
	 *         there is no disk storage configured.
	 */
	long getOnDiskHits();

	/**
	 * @return the number of times a requested element was not found in the
	 *         cache
	 */
	long getCacheMisses();

	/**
	 * Gets the number of elements stored in the cache.
	 * 
	 * @return the number of elements in the ehcache, with a varying degree of
	 *         accuracy, depending on accuracy setting.
	 */
	long getObjectCount();

	/**
	 * Zwraca przeliczoną informację o wydajności cache wyrażoną w procentach
	 * [%]
	 * 
	 * @return informacja o wydajności cache wyrażona w procentach [%]
	 */
	double getHitsRatio();

	int getSize();

	/**
	 * Nazwa powiązanego regionu pamięci podręcznej
	 * 
	 * @return the name of the cache, or null is there no associated cache
	 */
	String getAssociatedCacheName();

	/**
	 * Nazwa managera pamięci podręcznej
	 * 
	 * @return nazwa manager'a
	 */
	String getAssociatedManagerName();

	/**
	 * Definicja prvider'a pamięci podręcznej
	 * 
	 * @return
	 */
	CacheProviderEnum getAssociatedProvider();

	/**
	 * Oryginalny obiekt statystyk powiązany z implementacją. Umożliwiamy
	 * pobranie dodatkowych statystyk o ile takie są dostępne.
	 * 
	 * @return oryginalna implementacja statystyk
	 */
	Object getAssociatedStatistics();
	
	boolean isActive();

}