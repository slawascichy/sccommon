package pl.slawas.common.ldap.provider;

import java.util.HashMap;
import java.util.List;

public class LdapResult extends HashMap<String,List<LdapValue>> {

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

	
}
