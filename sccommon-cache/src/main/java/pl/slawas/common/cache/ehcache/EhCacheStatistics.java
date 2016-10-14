package pl.slawas.common.cache.ehcache;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.statistics.StatisticsGateway;
import pl.slawas.common.cache.CacheProviderEnum;
import pl.slawas.common.cache._IObjectCacheStatistics;

/**
 * 
 * EhCacheStatistics
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.2 $
 * 
 */
public class EhCacheStatistics implements Serializable, _IObjectCacheStatistics {

	private static final long serialVersionUID = 3606940454221918725L;

	private final StatisticsGateway statistics;

	private final String associatedCacheName;

	private final String associatedManagerName;

	EhCacheStatistics(String associatedManagerName, String associatedCacheName) {
		super();
		this.statistics = null;
		this.associatedCacheName = associatedCacheName;
		this.associatedManagerName = associatedManagerName;
	}

	/**
	 * 
	 * @param cache
	 */
	EhCacheStatistics(String associatedManagerName, net.sf.ehcache.Ehcache cache) {
		this.statistics = cache.getStatistics();
		this.associatedCacheName = cache.getName();
		this.associatedManagerName = associatedManagerName;
	}

	public void clearStatistics() {

	}

	public long getCacheHits() {
		return (this.statistics != null ? this.statistics.cacheHitCount() : 0L);
	}

	public long getInMemoryHits() {
		return (this.statistics != null ? this.statistics.localHeapHitCount()
				: 0L);
	}

	public long getOnDiskHits() {
		return (this.statistics != null ? this.statistics.localDiskHitCount()
				: 0L);
	}

	public long getCacheMisses() {
		return (this.statistics != null ? this.statistics.cacheMissCount() : 0L);

	}

	public long getObjectCount() {
		return (this.statistics != null ? this.statistics.getSize() : 0L);

	}

	public String getAssociatedCacheName() {
		return this.associatedCacheName;
	}

	/**
	 * Returns a {@link String} representation of the {@link Ehcache}
	 * statistics.
	 */
	public final String toString() {
		StringBuffer dump = new StringBuffer();
		dump.append("[ ").append(" name = ").append(getAssociatedCacheName())
				.append(" cacheHits = ").append(getCacheHits())
				.append(" onDiskHits = ").append(getOnDiskHits())
				.append(" inMemoryHits = ").append(getInMemoryHits())
				.append(" misses = ").append(getCacheMisses())
				.append(" size = ").append(getSize()).append(" ]");

		return dump.toString();
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return (this.statistics != null ? (int) this.statistics.getSize() : 0);
	}

	public double getHitsRatio() {
		double result = (this.statistics != null ? this.statistics.cacheHitRatio() : 0);
		if (Double.isNaN(result) || Double.isInfinite(result)) {
			return result;
		}
		BigDecimal dividend = BigDecimal.valueOf(result);
		BigDecimal multiplicand = new BigDecimal(100);
		BigDecimal multi = dividend.multiply(multiplicand);
		BigDecimal divisor = new BigDecimal(1);
		BigDecimal divide = multi.divide(divisor, 2, RoundingMode.HALF_UP);
		return divide.doubleValue();
	}

	/* Overridden (non-Javadoc) */
	@Override
	public String getAssociatedManagerName() {
		return this.associatedManagerName;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public CacheProviderEnum getAssociatedProvider() {
		return CacheProviderEnum.EhCache;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public Object getAssociatedStatistics() {
		return this.statistics;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isActive() {
		return this.statistics != null;
	}
}
