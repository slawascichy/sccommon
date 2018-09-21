package pl.slawas.common.cache;

/**
 * 
 * EmptyObjectCacheStatistics obiekt z pustymi statystykami.
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public class EmptyObjectCacheStatistics implements IObjectCacheStatistics {

	private static final long serialVersionUID = 1566889499150263806L;
	private String managerName;
	private String regionName;
	private CacheProviderEnum prov;

	public EmptyObjectCacheStatistics(String managerName, String regionName,
			CacheProviderEnum prov) {
		super();
		this.managerName = managerName;
		this.regionName = regionName;
		this.prov = prov;
	}

	@Override
	public int getSize() {
		return 0;
	}

	@Override
	public long getOnDiskHits() {
		return 0;
	}

	@Override
	public long getObjectCount() {
		return 0;
	}

	@Override
	public long getInMemoryHits() {
		return 0;
	}

	@Override
	public double getHitsRatio() {
		return 0;
	}

	@Override
	public long getCacheMisses() {
		return 0;
	}

	@Override
	public long getCacheHits() {
		return 0;
	}

	@Override
	public Object getAssociatedStatistics() {
		return null;
	}

	@Override
	public CacheProviderEnum getAssociatedProvider() {
		return prov;
	}

	@Override
	public String getAssociatedManagerName() {
		return managerName;
	}

	@Override
	public String getAssociatedCacheName() {
		return regionName;
	}

	@Override
	public void clearStatistics() {
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean isActive() {
		return false;
	}

}
