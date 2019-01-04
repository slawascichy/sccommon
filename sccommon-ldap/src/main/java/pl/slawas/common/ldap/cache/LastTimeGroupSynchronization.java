package pl.slawas.common.ldap.cache;

import java.io.Serializable;

/**
 * 
 * LastTimeGroupSynchronization klasa pomocnicza
 * 
 * @author Sławomir Cichy &lt;scichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LastTimeGroupSynchronization implements Serializable {

	private static final long serialVersionUID = 4167819195192815086L;

	private Long value = 0L;

	/**
	 * @return the {@link #value}
	 */
	public Long getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the {@link #value} to set
	 */
	public void setValue(Long value) {
		this.value = value;
	}

}
