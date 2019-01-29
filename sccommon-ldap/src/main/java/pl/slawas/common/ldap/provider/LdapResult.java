package pl.slawas.common.ldap.provider;

import java.util.HashMap;
import java.util.List;

public class LdapResult extends HashMap<String, List<LdapValue>> {

	private static final long serialVersionUID = -849796034289209122L;

	private String name;

	private boolean relative;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRelative() {
		return relative;
	}

	public void setRelative(boolean relative) {
		this.relative = relative;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + (relative ? 1231 : 1237);
		return result;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		LdapResult other = (LdapResult) obj;
		if (relative != other.relative) {
			return false;
		}
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name)) {
			return false;
		}
		return super.equals(other);
	}

}
