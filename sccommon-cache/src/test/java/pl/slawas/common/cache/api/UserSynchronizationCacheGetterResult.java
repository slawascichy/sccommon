package pl.slawas.common.cache.api;

public class UserSynchronizationCacheGetterResult {

	private final UserSynchronizationInfo userInfo;

	private final boolean elementInitialized;

	public UserSynchronizationCacheGetterResult(UserSynchronizationInfo userInfo, boolean elementInitialized) {
		super();
		this.userInfo = userInfo;
		this.elementInitialized = elementInitialized;
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

}
