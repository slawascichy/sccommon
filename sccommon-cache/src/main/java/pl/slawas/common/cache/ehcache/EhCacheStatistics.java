package pl.slawas.common.cache.ehcache;

import java.io.Serializable;

import org.ehcache.core.Ehcache;
import org.ehcache.core.spi.service.StatisticsService;

import pl.slawas.common.cache.CacheProviderEnum;
import pl.slawas.common.cache.IObjectCacheStatistics;

/**
 * 
 * EhCacheStatistics
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.2 $
 * 
 */
public class EhCacheStatistics implements Serializable, IObjectCacheStatistics {

	private static final long serialVersionUID = 3606940454221918725L;

	private final transient EhCacheStatisticsService statisticsService;

	private final String associatedCacheName;

	private final String associatedProviderName;

	EhCacheStatistics(String associatedProviderName, String associatedCacheName) {
		super();
		this.associatedProviderName = associatedProviderName;
		this.associatedCacheName = associatedCacheName;
		this.statisticsService = null;
	}

	/**
	 * 
	 * @param cache
	 */
	EhCacheStatistics(String associatedProviderName, EhCache cache) {
		super();
		this.associatedProviderName = associatedProviderName;
		this.associatedCacheName = cache.getRegionName();
		StatisticsService ss = cache.getStatisticsService();
		if (ss instanceof EhCacheStatisticsService) {
			this.statisticsService = (EhCacheStatisticsService) ss;
		} else {
			this.statisticsService = null;
		}
	}

	public void clearStatistics() {
		if (statisticsService == null) {
			return;
		}
		statisticsService.clearStatistics(associatedCacheName);
	}

	public long getCacheHits() {
		if (statisticsService == null) {
			return 0L;
		}
		return statisticsService.getCacheStatistics(associatedCacheName).getCacheHits();
	}

	public long getInMemoryHits() {
		if (statisticsService == null) {
			return 0L;
		}
		return statisticsService.getInMemoryHits(associatedCacheName);
	}

	public long getOnDiskHits() {
		if (statisticsService == null) {
			return 0L;
		}
		return statisticsService.getOnDiskHits(associatedCacheName);
	}

	public long getCacheMisses() {
		if (statisticsService == null) {
			return 0L;
		}
		return statisticsService.getCacheStatistics(associatedCacheName).getCacheMisses();

	}

	public long getObjectCount() {
		if (statisticsService == null) {
			return 0L;
		}
		return statisticsService.getObjectCount(associatedCacheName);
	}

	public String getAssociatedCacheName() {
		return this.associatedCacheName;
	}

	/**
	 * Returns a {@link String} representation of the {@link Ehcache} statistics.
	 */
	public final String toString() {
		StringBuilder dump = new StringBuilder();
		dump.append("[ ").append(" name = ").append(getAssociatedCacheName()).append(" cacheHits = ")
				.append(getCacheHits()).append(" onDiskHits = ").append(getOnDiskHits()).append(" inMemoryHits = ")
				.append(getInMemoryHits()).append(" misses = ").append(getCacheMisses()).append(" size = ")
				.append(getSize()).append(" ]");

		return dump.toString();
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		if (statisticsService == null) {
			return 0;
		}
		return statisticsService.getSize(associatedCacheName);
	}

	public double getHitsRatio() {
		if (statisticsService == null) {
			return 0.0;
		}
		Float hits = statisticsService.getCacheStatistics(associatedCacheName).getCacheHitPercentage();
		return round(hits.doubleValue(), 2);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public String getAssociatedManagerName() {
		return this.associatedProviderName;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public CacheProviderEnum getAssociatedProvider() {
		return CacheProviderEnum.EhCache;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public Object getAssociatedStatistics() {
		return this.statisticsService;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isActive() {
		return this.statisticsService != null;
	}

	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
}
