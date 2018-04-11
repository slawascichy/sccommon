package pl.slawas.common.cache.api;

public class UserSynchronizationCacheGetterResult {

	private final UserSynchronizationInfo userInfo;

	private final boolean elementInitialized;

	private final boolean locked;

	public UserSynchronizationCacheGetterResult(UserSynchronizationInfo userInfo, boolean elementInitialized,
			boolean locked) {
		super();
		this.userInfo = userInfo;
		this.elementInitialized = elementInitialized;
		this.locked = locked;
	}

	/**
	 * @return the {@link #userInfo}
	 */
	public UserSynchronizationInfo getUserInfo() {
		return userInfo;
	}

	/**
	 * @return the {@link #elementInitialized}
	 */
	public boolean isElementInitialized() {
		return elementInitialized;
	}

	/**
	 * @return the {@link #locked}
	 */
	public boolean isLocked() {
		return locked;
	}

}
