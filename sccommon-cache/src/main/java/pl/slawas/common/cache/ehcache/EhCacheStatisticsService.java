package pl.slawas.common.cache.ehcache;

import org.ehcache.impl.internal.statistics.DefaultStatisticsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * EhCacheStatisticsService
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public class EhCacheStatisticsService extends DefaultStatisticsService {

	private static final Logger logger = LoggerFactory.getLogger(EhCacheStatisticsService.class);

	public EhCacheStatisticsService() {
		super();
	}

	/**
	 * 
	 * @param cacheName
	 */
	public void clearStatistics(String cacheName) {
		getCacheStatistics(cacheName).clear();
	}

	/**
	 * 
	 * @param cacheName
	 * @return
	 */
	public long getInMemoryHits(String cacheName) {
		if (logger.isDebugEnabled()) {
			logger.warn("-->getInMemoryHits: implementation lack.");
		}
		/* TODO implementation lack */
		return -1L;
	}

	/**
	 * 
	 * @param cacheName
	 * @return
	 */
	public long getOnDiskHits(String cacheName) {
		if (logger.isDebugEnabled()) {
			logger.warn("-->getOnDiskHits: implementation lack.");
		}
		/* TODO implementation lack */
		return -1L;
	}

	/**
	 * 
	 * @param cacheName
	 * @return
	 */
	public long getObjectCount(String cacheName) {
		if (logger.isDebugEnabled()) {
			logger.warn("-->getObjectCount: implementation lack.");
		}
		/* TODO implementation lack */
		return -1L;
	}

	/**
	 * @param cacheName
	 * @return the size
	 */
	public int getSize(String cacheName) {
		if (logger.isDebugEnabled()) {
			logger.warn("-->getSize: implementation lack.");
		}
		/* TODO implementation lack */
		return -1;
	}

}
