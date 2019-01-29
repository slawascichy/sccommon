package pl.slawas.common.ldap.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import pl.slawas.common.ldap.api.ILdapContextFactory;
import pl.slawas.common.ldap.api.ILdapEntry;
import pl.slawas.common.ldap.api.ILdapEntry4Changes;
import pl.slawas.common.ldap.provider.LdapResult;
import pl.slawas.twl4j.Logger;

public abstract class LdapAOSupport<T extends ILdapEntry> {

	/**
	 * Utworzenie nowej instancji obiektu reprezentującego entry
	 * 
	 * @param login
	 *            nazwa entry
	 * @return nowa instancja obiektu reprezentującego entry
	 */
	protected abstract T createNewInstance(String name);

	/**
	 * Wyszukiwanie entry na podstawie zadanego filtru LDAP
	 * 
	 * @param factory
	 *            fabryka kontekstu LDAP
	 * @param searchFilter
	 *            filtr wyszukiwania
	 * @return lista znalezionych obiektów
	 * @throws NamingException
	 */
	protected List<LdapResult> loadByFilter(ILdapContextFactory factory, String[] attrs, String searchFilter)
			throws NamingException {
		return factory.manyEntrySearch(attrs, searchFilter);
	}

	/**
	 * Dodawanie obiektu do LDAP.
	 * 
	 * @param factory
	 *            fabryka kontekstu LDAP
	 * @param ldapEntry
	 *            dodawany obiekt LDAP
	 * @throws NamingException
	 */
	protected void insert(ILdapContextFactory factory, ILdapEntry4Changes ldapEntry) throws NamingException {
		factory.addEntry(ldapEntry, getEntryNameAttribute());
	}

	/**
	 * Modyfikacja obiektu w LDAP
	 * 
	 * @param factory
	 *            fabryka kontekstu LDAP
	 * @param ldapEntry
	 *            modyfikowany obiekt LDAP
	 * @throws NamingException
	 */
	protected void update(ILdapContextFactory factory, ILdapEntry4Changes ldapEntry) throws NamingException {
		factory.modifyEntry(ldapEntry, getEntryNameAttribute());
	}

	/**
	 * Usunięcie obiektu z LDAP
	 * 
	 * @param factory
	 *            fabryka kontekstu LDAP
	 * @param entryDN
	 *            DN usuwanego entry
	 * @throws NamingException
	 */
	protected void delete(ILdapContextFactory factory, String entryDN) throws NamingException {
		factory.removeEntry(entryDN);
	}

	public abstract T load(Object id) throws NamingException;

	protected abstract ILdapContextFactory getFactory();

	protected abstract Logger getLog();

	public T insert(ILdapEntry4Changes ldapEntry) throws NamingException {
		if (ldapEntry == null) {
			return null;
		}
		getLog().debug("insert -> entryDN: {}", ldapEntry.getDn());
		insert(getFactory(), ldapEntry);
		if (ldapEntry.getName() != null) {
			return load(ldapEntry.getName());
		}
		return null;
	}

	public T update(ILdapEntry4Changes ldapEntry) throws NamingException {
		if (ldapEntry == null) {
			return null;
		}
		getLog().debug("update -> entryDN: {}", ldapEntry.getDn());
		update(getFactory(), ldapEntry);
		if (ldapEntry.getName() != null) {
			return load(ldapEntry.getName());
		}
		return null;
	}

	public void delete(String entryDN) throws NamingException {
		getLog().debug("delete -> entryDN: {}", entryDN);
		delete(getFactory(), entryDN);
	}

	public abstract List<T> loadByFilter(String searchFilter) throws NamingException;

	public List<T> loadByWildcard(String wildcardCredentials) throws NamingException {
		String searchFilter = getFactory().getLdapOptions().getBaseFilter(new String[] { wildcardCredentials });
		return loadByFilter(searchFilter);

	}

	protected abstract Map<String, T> prepareAllAsMap(String searchFilter, Map<String, T> target,
			boolean checkOldElement) throws NamingException;

	public Map<String, T> loadByWildcardAsMap(String wildcardCredentials) throws NamingException {
		Map<String, T> map = new HashMap<>();
		String searchFilter = getFactory().getLdapOptions().getBaseFilter(new String[] { wildcardCredentials });
		getLog().debug("--> loadByWildcardAsMap: Sarch Filter: {}", searchFilter);
		return prepareAllAsMap(searchFilter, map, false);
	}

	protected abstract String getEntryNameAttribute();

}
