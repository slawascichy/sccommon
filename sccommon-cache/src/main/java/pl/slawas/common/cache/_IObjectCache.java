package pl.slawas.common.cache;

import java.util.List;
import java.util.Map;

import pl.slawas.common.cache.exceptions.CacheErrorException;

public interface _IObjectCache {

	/**
	 * Gets a value of an element which matches the given key.
	 * 
	 * @param key
	 *            the key of the element to return.
	 * @return The value placed into the cache with an earlier put, or null if
	 *         not found or expired
	 * @throws CacheErrorException
	 */
	Object get(Object key) throws CacheErrorException;

	Object read(Object key) throws CacheErrorException;

	/**
	 * Puts an object into the cache.
	 * 
	 * @param key
	 *            a key
	 * @param value
	 *            a value
	 * @throws CacheErrorException
	 *             if the {@link CacheManager} is shutdown or another
	 *             {@link Exception} occurs.
	 */
	void update(Object key, Object value) throws CacheErrorException;

	/**
	 * Puts an object into the cache.
	 * 
	 * @param key
	 *            a key
	 * @param value
	 *            a value
	 * @throws CacheErrorException
	 *             if the {@link CacheManager} is shutdown or another
	 *             {@link Exception} occurs.
	 */
	void put(Object key, Object value) throws CacheErrorException;

	/**
	 * Removes the element which matches the key.
	 * <p/>
	 * If no element matches, nothing is removed and no Exception is thrown.
	 * 
	 * @param key
	 *            the key of the element to remove
	 * @throws CacheErrorException
	 */
	void remove(Object key) throws CacheErrorException;

	/**
	 * Remove all elements in the cache, but leave the cache in a useable state.
	 * 
	 * @throws CacheErrorException
	 */
	void clear() throws CacheErrorException;

	/**
	 * Remove the cache and make it unuseable.
	 * 
	 * @throws CacheErrorException
	 */
	void destroy() throws CacheErrorException;

	String getRegionName();

	/**
	 * Warning: This method can be very expensive to run. Allow approximately 1
	 * second per 1MB of entries. Running this method could create liveness
	 * problems because the object lock is held for a long period
	 * <p/>
	 * 
	 * @return the approximate size of memory ehcache is using for the
	 *         MemoryStore for this cache
	 */
	long getSizeInMemory();

	long getElementCountInMemory() throws CacheErrorException;

	long getElementCountOnDisk();

	@SuppressWarnings("rawtypes")
	Map toMap() throws CacheErrorException;

	String toString();

	_IObjectCacheStatistics getStatistics();

	public void setTimeToLiveSeconds(long timeToLiveSeconds);

	public long getTimeToLiveSeconds();

	List<?> getKeys();

}