package pl.slawas.common.ldap.config;

import java.io.Serializable;
import java.util.List;

public class OptionsList implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4251186983181114579L;
	private List<String> option;

	/**
	 * @return the {@link #option}
	 */
	public List<String> getOption() {
		return option;
	}

	/**
	 * @param option
	 *            the {@link #option} to set
	 */
	public void setOption(List<String> option) {
		this.option = option;
	}
}
