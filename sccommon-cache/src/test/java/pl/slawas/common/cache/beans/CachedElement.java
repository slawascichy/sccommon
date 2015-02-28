package pl.slawas.common.cache.beans;

import org.apache.commons.lang.StringUtils;

import pl.slawas.entities.NameValuePair;
import pl.slawas.xml.XMLNameValuePairUtils;

public class CachedElement implements NameValuePair {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3791615455764068602L;

	private String name;

	private String value;

	public CachedElement(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}

	/**
	 * @return the {@link #name}
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the {@link #name} to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the {@link #value}
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the {@link #value} to set
	 */
	public void setValue(String value) {
		this.value = value;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public String toXMLItem() {
		return XMLNameValuePairUtils.toXMLItem(this).toString();
	}

	/* Overridden (non-Javadoc) */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ (StringUtils.isBlank(name) ? 0 : name.hashCode());
		result = prime * result
				+ (StringUtils.isBlank(value) ? 0 : value.hashCode());
		return result;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CachedElement other = (CachedElement) obj;
		if (StringUtils.isBlank(name)) {
			if (StringUtils.isNotBlank(other.name))
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (StringUtils.isBlank(value)) {
			if (StringUtils.isNotBlank(other.value))
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
