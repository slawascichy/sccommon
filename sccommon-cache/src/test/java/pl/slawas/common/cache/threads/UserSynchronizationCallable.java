package pl.slawas.common.cache.threads;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pl.slawas.common.cache.api.UserSynchronizationCacheGetterResult;
import pl.slawas.common.cache.api.UserSynchronizationInfo;
import pl.slawas.common.cache.beans.CachedElement;
import pl.slawas.common.cache.beans.UserSynchronizationCache;
import pl.slawas.common.cache.exceptions.CacheErrorException;

public class UserSynchronizationCallable implements Callable<Integer> {

	private final Logger logger = LoggerFactory.getLogger(getClass());

	private List<CachedElement> elements = new ArrayList<CachedElement>();

	private Long syncPeriod = 1000L;

	private UserSynchronizationCache userCache;

	public UserSynchronizationCallable(Long syncPeriod, UserSynchronizationCache userCache) {
		super();
		this.syncPeriod = syncPeriod;
		this.userCache = userCache;
	}

	@Override
	public Integer call() throws Exception {
		String currentThreadName = Thread.currentThread().getName();
		logger.debug("-->UserSynchronizationCallable.call: {}", currentThreadName);
		int synced = 0;
		for (CachedElement el : elements) {
			try {

				String currentUserName = el.getName();
				String userKey = currentUserName.toLowerCase();

				UserSynchronizationCacheGetterResult cacheResult = getUserCache().get(currentUserName, userKey);
				UserSynchronizationInfo usInfo = cacheResult.getUserInfo();
				Long currTime = Long.valueOf(Calendar.getInstance().getTimeInMillis());
				Long lastTimeUpdate = usInfo.getLastTimeUpdate();

				try {
					String status;
					if (!cacheResult.isLocked() && (cacheResult.isElementInitialized()
							|| (currTime.longValue() - lastTimeUpdate.longValue() > getSyncPeriod()))) {
						status = "SYNC";
					} else {
						status = "SKIP";
					}
					logger.debug("-->UserSynchronizationCallable.call: {}: {}: currentUserName={} [{}, {}, {}]...",
							new Object[] { currentThreadName, status, currentUserName,
									cacheResult.isElementInitialized() ? "init" : "exists",
									(currTime.longValue() - lastTimeUpdate.longValue() > getSyncPeriod()) ? "Expired"
											: "Not expired",
									cacheResult.isLocked() ? "Locked by " + usInfo.getLockedBy() : "Not locked" });

					if (!cacheResult.isLocked() && (cacheResult.isElementInitialized()
							|| (currTime.longValue() - lastTimeUpdate.longValue() > getSyncPeriod()))) {
						Object perUserNameLock = createPerUserNameLock(userKey);
						synchronized (perUserNameLock) {
							Thread.sleep(1000);
							synced++;
							lastTimeUpdate = Calendar.getInstance().getTimeInMillis();
						}
					} else {
						Thread.sleep(100);
					}
				} finally {
					if (!cacheResult.isLocked() && usInfo.getLocked()) {
						usInfo.setLocked(Boolean.FALSE);
						usInfo.setLastTimeUpdate(lastTimeUpdate);
						getUserCache().update(usInfo, userKey);
					}
				}
			} catch (CacheErrorException e) {
				e.printStackTrace();
				return 0;
			}
		}
		return synced;
	}

	public void add(CachedElement element) {
		elements.add(element);
	}

	public int sizeOfProcessingList() {
		return elements.size();
	}

	protected long getSyncPeriod() {
		return syncPeriod;
	}

	private String createPerUserNameLock(String userKey) {
		return (new StringBuilder()).append("perUserNameLockForSync").append(userKey).toString().intern();
	}

	/**
	 * @return the {@link #userCache}
	 */
	public UserSynchronizationCache getUserCache() {
		return userCache;
	}

}
