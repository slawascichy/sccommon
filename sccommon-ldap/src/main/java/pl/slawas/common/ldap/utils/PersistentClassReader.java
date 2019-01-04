package pl.slawas.common.ldap.utils;

import java.lang.reflect.ParameterizedType;

import pl.slawas.common.ldap.cache.LdapGroupsCacheLists;

/**
 * 
 * PersistentClassReader - prywatna klasa pomocnicza pozwalająca na przeczytanie
 * parametru metody - na razie nie mam innego pomysłu jak to rozwiązać.
 * 
 * @see LdapGroupsCacheLists#getCache()
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 * @param <G>
 *            implementacja/interfejs obiektu którego klasę badamy
 */
public class PersistentClassReader<G> {

	/** klasa parametru */
	private final Class<G> persistentClass;

	@SuppressWarnings("unchecked")
	public PersistentClassReader() {
		super();
		Class<?> tmpClass = this.getClass();
		while (!(tmpClass.getGenericSuperclass() instanceof ParameterizedType)) {
			tmpClass = tmpClass.getSuperclass();
		}
		this.persistentClass = (Class<G>) ((ParameterizedType) tmpClass.getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	/**
	 * @return the {@link #persistentClass}
	 */
	public Class<G> getPersistentClass() {
		return persistentClass;
	}

}
