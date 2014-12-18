package pl.slawas.common.cache;

import java.io.Serializable;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Statistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * EhCacheStatistics
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.2 $
 * 
 */
public class EhCacheStatistics implements Serializable, _IObjectCacheStatistics {

	private static final Logger logger = LoggerFactory
			.getLogger(EhCacheStatistics.class);

	private static final long serialVersionUID = 3606940454221918725L;

	private final transient Statistics statistics;

	private final String associatedCacheName;

	private final int size;

	/**
	 * Creates a new statistics object, associated with a Cache
	 * 
	 * @param cache
	 *            The cache that {@link #clearStatistics()} will call, if not
	 *            disconnected
	 * @param statisticsAccuracy
	 * @param cacheHits
	 * @param onDiskHits
	 * @param inMemoryHits
	 * @param misses
	 * @param size
	 */
	public EhCacheStatistics(Ehcache cache) {
		logger.debug("Tworze statystyke dla {}", cache.getStatistics()
				.getAssociatedCache());
		this.statistics = cache.getStatistics();
		this.associatedCacheName = cache.getName();
		this.size = cache.getSize();
	}

	public void clearStatistics() {
		if (this.statistics == null) {
			throw new IllegalStateException(
					"This statistics object no longer references a Cache.");
		}
		try {
			this.statistics.clearStatistics();
		} catch (IllegalStateException e) {
			if (logger.isDebugEnabled()) {
				logger.info("Ignoring call because " + e.getMessage());
			}
		}
	}

	public long getCacheHits() {
		return this.statistics.getCacheHits();
	}

	public long getInMemoryHits() {
		return this.statistics.getInMemoryHits();
	}

	public long getOnDiskHits() {
		return this.statistics.getOnDiskHits();
	}

	public long getCacheMisses() {
		return this.statistics.getCacheMisses();

	}

	public long getObjectCount() {
		return this.statistics.getObjectCount();

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
				.append(" size = ").append(size).append(" ]");

		return dump.toString();
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return size;
	}

	public double getHitsRatio() {
		return (getCacheHits() != 0) ? ((double) (getCacheHits() - getCacheMisses()) / ((double) getCacheHits())) * 100
				: 0;
	}

}
