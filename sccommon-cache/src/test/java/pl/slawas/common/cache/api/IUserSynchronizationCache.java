package pl.slawas.common.cache.api;

import pl.slawas.common.cache.IObjectWithCache;
import pl.slawas.common.cache.exceptions.CacheErrorException;

public interface IUserSynchronizationCache extends IObjectWithCache {

	UserSynchronizationCacheGetterResult get(String userName, String userKey) throws CacheErrorException;

	void update(UserSynchronizationInfo userName, String userKey) throws CacheErrorException;
}