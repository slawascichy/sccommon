package pl.slawas.common.cache;

import java.util.List;
import java.util.Map;

import pl.slawas.common.cache.exceptions.CacheErrorException;

/**
 * 
 * _IObjectCache ujednolicone API dla wszystkich managerów pamięci podręcznych.
 * Wzorowany na API EhCache
 * http://www.ehcache.org/documentation/2.7/apis/explicitlocking.html
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public interface _IObjectCache {

	/**
	 * Gets a value of an element which matches the given key.
	 * 
	 * @param key
	 *            the key of the element to return.
	 * @return The value placed into the cache with an earlier put, or null if not
	 *         found or expired
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
	 *             if the {@code CacheManager} is shutdown or another
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
	 *             if the {@code CacheManager} is shutdown or another
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
	 * second per 1MB of entries. Running this method could create liveness problems
	 * because the object lock is held for a long period
	 * <p/>
	 * 
	 * @return the approximate size of memory ehcache is using for the MemoryStore
	 *         for this cache
	 */
	long getSizeInMemory();

	long getElementCountInMemory() throws CacheErrorException;

	long getElementCountOnDisk();

	@SuppressWarnings("rawtypes")
	Map toMap() throws CacheErrorException;

	String toString();

	_IObjectCacheStatistics getStatistics();

	void setTimeToLiveSeconds(long timeToLiveSeconds);

	long getTimeToLiveSeconds();

	List<?> getKeys();

	/**
	 * Acquires the proper read lock for a given cache key
	 *
	 * @param key
	 *            - The key that retrieves a value that you want to protect via
	 *            locking
	 */
	void acquireReadLockOnKey(Object key);

	/**
	 * Acquires the proper write lock for a given cache key
	 *
	 * @param key
	 *            - The key that retrieves a value that you want to protect via
	 *            locking
	 */
	void acquireWriteLockOnKey(Object key);

	/**
	 * Try to get a read lock on a given key. If can't get it in timeout millis then
	 * return a boolean telling that it didn't get the lock
	 *
	 * @param key
	 *            - The key that retrieves a value that you want to protect via
	 *            locking
	 * @param timeout
	 *            - millis until giveup on getting the lock
	 * @return whether the lock was awarded
	 * @throws InterruptedException
	 */
	boolean tryReadLockOnKey(Object key, long timeout) throws InterruptedException;

	/**
	 * Try to get a write lock on a given key. If can't get it in timeout millis
	 * then return a boolean telling that it didn't get the lock
	 *
	 * @param key
	 *            - The key that retrieves a value that you want to protect via
	 *            locking
	 * @param timeout
	 *            - millis until giveup on getting the lock
	 * @return whether the lock was awarded
	 * @throws InterruptedException
	 */
	boolean tryWriteLockOnKey(Object key, long timeout) throws InterruptedException;

	/**
	 * Release a held read lock for the passed in key
	 *
	 * @param key
	 *            - The key that retrieves a value that you want to protect via
	 *            locking
	 */
	void releaseReadLockOnKey(Object key);

	/**
	 * Release a held write lock for the passed in key
	 *
	 * @param key
	 *            - The key that retrieves a value that you want to protect via
	 *            locking
	 */
	void releaseWriteLockOnKey(Object key);

	/**
	 * Returns true if a read lock for the key is held by the current thread
	 *
	 * @param key
	 * @return true if a read lock for the key is held by the current thread
	 */
	boolean isReadLockedByCurrentThread(Object key);

	/**
	 * Returns true if a write lock for the key is held by the current thread
	 *
	 * Only Terracotta clustered cache instances currently support querying a
	 * thread's read lock hold status.
	 *
	 * @param key
	 * @return true if a write lock for the key is held by the current thread
	 */
	boolean isWriteLockedByCurrentThread(Object key);

}