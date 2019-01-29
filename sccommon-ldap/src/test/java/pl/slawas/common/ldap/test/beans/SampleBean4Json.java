package pl.slawas.common.ldap.test.beans;

import pl.slawas.common.ldap.utils.JSONProperty;

public class SampleBean4Json {

	public static final String[] keys = new String[] { "name", "sn", "wiek" };

	@JSONProperty
	private String name;

	@JSONProperty
	private String sn;

	@JSONProperty(name = "wiek")
	private Integer age;

	public SampleBean4Json(String name, String sn, Integer age) {
		super();
		this.name = name;
		this.sn = sn;
		this.age = age;
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
	 * @return the {@link #sn}
	 */
	public String getSn() {
		return sn;
	}

	/**
	 * @param sn
	 *            the {@link #sn} to set
	 */
	public void setSn(String sn) {
		this.sn = sn;
	}

	/**
	 * @return the {@link #age}
	 */
	public Integer getAge() {
		return age;
	}

	/**
	 * @param are
	 *            the {@link #age} to set
	 */
	public void setAge(Integer are) {
		this.age = are;
	}

}
