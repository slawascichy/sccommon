package pl.slawas.common.cache;

import java.util.Properties;

public class CacheProviderConfiguration<T> {

	private final T configuration;

	private final Properties additionalProps;

	private final String managerName;

	public CacheProviderConfiguration(T configuration, Properties additionalProps) {
		this.configuration = configuration;
		this.additionalProps = new Properties();
		if (additionalProps != null) {
			this.managerName = additionalProps.getProperty(CacheConstants.PROP_PROVIDER_NAME,
					CacheConstants.DEFAULT_PROVIDER_NAME);
			this.additionalProps.putAll(additionalProps);
		} else {
			this.managerName = CacheConstants.DEFAULT_PROVIDER_NAME;
		}
	}

	public CacheProviderConfiguration(String managerName, T configuration, Properties additionalProps) {
		super();
		this.configuration = configuration;
		this.additionalProps = new Properties();
		if (additionalProps != null) {
			this.additionalProps.putAll(additionalProps);
		}
		this.managerName = managerName;
	}

	/**
	 * @return the {@link #managerName}
	 */
	public String getManagerName() {
		return managerName;
	}

	/**
	 * @return the {@link #configuration}
	 */
	public T getConfiguration() {
		return configuration;
	}

	public CacheProviderConfiguration<T> putCustomProperty(String propertyName, Object value) {
		this.additionalProps.put(propertyName, value);
		return this;
	}

	public CacheProviderConfiguration<T> putAllCustomProperties(Properties customProperties) {
		this.additionalProps.putAll(customProperties);
		return this;
	}

	public Object getCustomProperty(String propertyName) {
		return this.additionalProps.get(propertyName);
	}

	public Object getCustomProperty(String propertyName, Object defaultValue) {
		return this.additionalProps.getOrDefault(propertyName, defaultValue);
	}

}
