package pl.slawas.common.cache.ehcache;

import java.time.Duration;
import java.util.function.Supplier;

import org.ehcache.expiry.ExpiryPolicy;

public class CustomExpiry<K, V> implements ExpiryPolicy<K, V> {

	private Duration timeToLiveExpiration;

	private Duration timeToIdleExpiration;

	@Override
	public Duration getExpiryForCreation(K key, V value) {
		return timeToLiveExpiration;
	}

	@Override
	public Duration getExpiryForAccess(K key, Supplier<? extends V> value) {
		return timeToIdleExpiration;
	}

	@Override
	public Duration getExpiryForUpdate(K key, Supplier<? extends V> oldValue, V newValue) {
		return timeToIdleExpiration;
	}

	/**
	 * @return the {@link #timeToLiveExpiration}
	 */
	public Duration getTimeToLiveExpiration() {
		return timeToLiveExpiration;
	}

	/**
	 * @param timeToLiveExpiration
	 *            the {@link #timeToLiveExpiration} to set
	 */
	public void setTimeToLiveExpiration(Duration timeToLiveExpiration) {
		this.timeToLiveExpiration = timeToLiveExpiration;
	}

	/**
	 * @return the {@link #timeToIdleExpiration}
	 */
	public Duration getTimeToIdleExpiration() {
		return timeToIdleExpiration;
	}

	/**
	 * @param timeToIdleExpiration
	 *            the {@link #timeToIdleExpiration} to set
	 */
	public void setTimeToIdleExpiration(Duration timeToIdleExpiration) {
		this.timeToIdleExpiration = timeToIdleExpiration;
	}

}
