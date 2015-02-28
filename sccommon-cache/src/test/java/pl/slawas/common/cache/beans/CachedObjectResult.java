package pl.slawas.common.cache.beans;

import java.io.Serializable;

import pl.slawas.entities.NameValuePair;

/**
 * 
 * CachedObjectResult obiekt wyniku operacji na pamięci podręcznej
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class CachedObjectResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5272175077030070702L;

	private final NameValuePair obj;

	private final Boolean fromCache;

	public CachedObjectResult(NameValuePair obj, Boolean fromCache) {
		super();
		this.obj = obj;
		this.fromCache = fromCache;
	}

	/**
	 * @return the {@link #obj}
	 */
	public NameValuePair getObj() {
		return obj;
	}

	/**
	 * @return the {@link #fromCache}
	 */
	public Boolean getFromCache() {
		return fromCache;
	}

}
