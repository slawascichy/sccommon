package pl.slawas.common.cache.ehcache;

import java.io.Serializable;

/**
 * 
 * Element
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 */
public class Element implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4216172520386729789L;

	private final Serializable value;

	public Element(Serializable value) {
		super();
		this.value = value;
	}

	/**
	 * @return the {@link #value}
	 */
	public Serializable getObjectValue() {
		return value;
	}

}
