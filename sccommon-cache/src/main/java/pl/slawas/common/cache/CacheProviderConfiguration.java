package pl.slawas.common.cache;

import java.io.Serializable;

public class CacheProviderConfiguration<T> implements Serializable {

	public CacheProviderConfiguration(String managerName, T configuration) {
		super();
		this.managerName = managerName;
		this.configuration = configuration;
	}

	private static final long serialVersionUID = 4668307382015940044L;

	private String managerName;

	private T configuration;

	/**
	 * @return the {@link #managerName}
	 */
	public String getManagerName() {
		return managerName;
	}

	/**
	 * @param managerName
	 *            the {@link #managerName} to set
	 */
	public void setManagerName(String managerName) {
		this.managerName = managerName;
	}

	/**
	 * @return the {@link #configuration}
	 */
	public T getConfiguration() {
		return configuration;
	}

	/**
	 * @param configuration
	 *            the {@link #configuration} to set
	 */
	public void setConfiguration(T configuration) {
		this.configuration = configuration;
	}

}
