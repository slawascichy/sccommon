package pl.slawas.common.cache;

import java.io.Serializable;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.statistics.StatisticsGateway;

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

	/**
	 * 
	 * @param cache
	 */
	public EhCacheStatistics(net.sf.ehcache.Ehcache cache) {
		this.statistics = cache.getStatistics();
		this.associatedCacheName = cache.getName();
	}

	public void clearStatistics() {

	}

	public long getCacheHits() {
		return this.statistics.cacheHitCount();
	}

	public long getInMemoryHits() {
		return this.statistics.localHeapHitCount();
	}

	public long getOnDiskHits() {
		return this.statistics.localDiskHitCount();
	}

	public long getCacheMisses() {
		return this.statistics.cacheMissCount();

	}

	public long getObjectCount() {
		return this.statistics.getSize();

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
		return (int) this.statistics.getSize();
	}

	public double getHitsRatio() {
		return this.statistics.cacheHitRatio();
	}
}
