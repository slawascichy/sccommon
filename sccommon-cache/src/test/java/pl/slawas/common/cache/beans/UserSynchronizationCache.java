package pl.slawas.common.cache.beans;

import java.util.Calendar;
import java.util.Properties;

import pl.slawas.common.cache.CacheUsage;
import pl.slawas.common.cache.ObjectWithCacheSupport;
import pl.slawas.common.cache.api.IUserSynchronizationCache;
import pl.slawas.common.cache.api.UserSynchronizationCacheGetterResult;
import pl.slawas.common.cache.api.UserSynchronizationInfo;
import pl.slawas.common.cache.exceptions.CacheErrorException;

public class UserSynchronizationCache extends ObjectWithCacheSupport implements IUserSynchronizationCache {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3999358849220726083L;

	/**
	 * nazwa regionu pamięci podręcznej obsługującego {@link UserSynchronization}
	 */
	public static final String CACHE_REGION_NAME = "UserSynchronizationCache";

	public UserSynchronizationCache(Properties providerOptions) {
		super(CacheUsage.TO_USE, CACHE_REGION_NAME, false, providerOptions);
	}

	/* Overridden (non-Javadoc) */
	/**
	 * implementacja na przykładzie
	 * http://www.ehcache.org/documentation/2.7/apis/explicitlocking.html
	 */
	@Override
	public UserSynchronizationCacheGetterResult get(String userName, String userKey) throws CacheErrorException {

		try {
			boolean isLocked;
			UserSynchronizationInfo userInfo = null;
			if (cacheIsAllowed()) {
				userInfo = (UserSynchronizationInfo) getCache().get(userKey);
			}
			boolean initCache = false;
			if (userInfo == null) {
				initCache = true;
				isLocked = false;
				Long currTime = Long.valueOf(Calendar.getInstance().getTimeInMillis());
				userInfo = new UserSynchronizationInfo(userName);
				userInfo.setLastTimeUpdate(currTime);
				userInfo.setLocked(Boolean.TRUE);
				userInfo.setLockedBy(Thread.currentThread().getName());
				if (cacheIsAllowed()) {
					getCache().put(userKey, userInfo);
				}
			} else {
				isLocked = userInfo.getLocked();
				if (!isLocked) {
					userInfo.setLocked(Boolean.TRUE);
					userInfo.setLockedBy(Thread.currentThread().getName());
					getCache().put(userKey, userInfo);
				}
			}
			return new UserSynchronizationCacheGetterResult(userInfo, initCache, isLocked);
		} finally {

		}
	}

	/* Overridden (non-Javadoc) */
	/**
	 * implementacja na przykładzie
	 * http://www.ehcache.org/documentation/2.7/apis/explicitlocking.html
	 */
	@Override
	public void update(UserSynchronizationInfo userInfo, String userKey) throws CacheErrorException {
		try {
			Long currTime = Long.valueOf(Calendar.getInstance().getTimeInMillis());
			Long lastTimeUpdate = userInfo.getLastTimeUpdate();
			if (lastTimeUpdate == null) {
				lastTimeUpdate = currTime;
				userInfo.setLastTimeUpdate(lastTimeUpdate);
				if (cacheIsAllowed()) {
					getCache().put(userKey, userInfo);
				}
			}
		} finally {

		}
	}
}
