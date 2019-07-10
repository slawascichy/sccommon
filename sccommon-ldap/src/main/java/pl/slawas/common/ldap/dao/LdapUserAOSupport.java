package pl.slawas.common.ldap.dao;

import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.ldap.api.Constants;
import pl.slawas.common.ldap.api.ILdapAttribute;
import pl.slawas.common.ldap.api.ILdapBaseEntry;
import pl.slawas.common.ldap.api.ILdapConnectionFactory;
import pl.slawas.common.ldap.api.ILdapContextFactory;
import pl.slawas.common.ldap.api.ILdapEntry;
import pl.slawas.common.ldap.api.ILdapEntry4Changes;
import pl.slawas.common.ldap.api.ILdapUser;
import pl.slawas.common.ldap.api.ILdapUserAO;
import pl.slawas.common.ldap.api.ILdapUserGroup;
import pl.slawas.common.ldap.api.ILdapUserGroupAO;
import pl.slawas.common.ldap.api.IUserAttribute;
import pl.slawas.common.ldap.beans.LdapUserAttribute;
import pl.slawas.common.ldap.provider.LdapConnectionFactoryBean;
import pl.slawas.common.ldap.provider.LdapResult;
import pl.slawas.common.ldap.provider.ProviderOptions;
import pl.slawas.common.ldap.utils.EntryUtils;
import pl.slawas.common.ldap.utils.UserAttributeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * LdapUserAOSupport
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 * @param <E>
 *            implementacja obiektu entry użytkownika
 * @param <G>
 *            implementacja obiektu entry grupy
 */
public abstract class LdapUserAOSupport<E extends ILdapUser<G>, G extends ILdapUserGroup> extends LdapAOSupport<E>
		implements ILdapUserAO<E, G> {

	private static final long serialVersionUID = -5039858611173250536L;

	private static final Logger log = LoggerFactory.getLogger(LdapUserAOSupport.class.getName());

	private final transient ILdapConnectionFactory factory;

	private final transient ProviderOptions lo;

	private final List<ILdapUserGroupAO<G>> groupLdapAO;
	/**
	 * Lista atrybutów entry wyznaczona na podstawie konfiguracji security
	 * provider'a wraz z atrybutami dodatkowymi, których dodanie jest możliwe w
	 * sposób programistyczny - np. atrybuty związanie z datą ostatniej modyfikacji,
	 * czy też z policy haseł.
	 */
	private final String[] attrs;
	/**
	 * Pomocnicza lista wszystkich atrybutów do weryfikacji czy dany atrybut został
	 * pobrany razem z entry
	 */
	private final Set<String> attrSet;
	/**
	 * lista atrybutów dodatkowych entry, których dodanie jest możliwe w sposób
	 * programistyczny - np. atrybuty związanie z datą ostatniej modyfikacji, czy
	 * też z policy haseł. Lista definiowana przy użyciu odpowiedniego konstruktora.
	 */
	private final String[] additionalAttrs;

	public LdapUserAOSupport(ProviderOptions ldapOptions, String organizationalUnitName,
			List<ILdapUserGroupAO<G>> groupLdapAO) {
		this(ldapOptions, organizationalUnitName, groupLdapAO, null);
	}

	/**
	 * Konstruktor obiektu dostępu do danych użytkownika składowanych w LDAP.
	 * 
	 * @param ldapOptions
	 *            parametr niezbędne do nawiązania połączenia do LDAP
	 * @param organizationalUnitName
	 *            nazwa jednostki organizacyjnej, w której drzewie znajdują się
	 *            entry użytkowników.
	 * @param groupLdapAO
	 *            lista obiektów dostępu do danych grup
	 */
	public LdapUserAOSupport(ProviderOptions ldapOptions, String organizationalUnitName,
			List<ILdapUserGroupAO<G>> groupLdapAO, String[] additionalAttrs) {
		this.factory = new LdapConnectionFactoryBean(ldapOptions, organizationalUnitName, this.getClass());
		if (groupLdapAO != null) {
			this.groupLdapAO = new ArrayList<>();
			for (ILdapUserGroupAO<G> groupAO : groupLdapAO) {
				this.groupLdapAO.add(groupAO);
			}
		} else {
			this.groupLdapAO = new ArrayList<>();
		}

		lo = factory.getLdapOptions();

		// get list of users attributes
		Set<String> vAttr = new HashSet<>();
		vAttr.add(lo.getUserNameAttribute());
		vAttr.add(lo.getUserDisplayNameAttribute());

		if (lo.getExtendedAttributies() != null) {
			Set<String> ladpAttrNames = lo.getExtendedAttributies().getLdapAttrNames();
			for (String ladpAttrName : ladpAttrNames) {
				vAttr.add(ladpAttrName);
			}
		}
		if (lo.getUserDNAttribute() != null) {
			vAttr.add(lo.getUserDNAttribute());
		}
		vAttr.add(lo.getUserEmailAttribute() != null ? lo.getUserEmailAttribute() : Constants.DEFAULT_EMAIL_ATTR_NAME);
		if (lo.getUserLocaleAttr() != null) {
			vAttr.add(lo.getUserLocaleAttr());
		}
		if (lo.getUserPrimaryGroupAttribute() != null) {
			vAttr.add(lo.getUserPrimaryGroupAttribute());
		}

		// copy the list into final array of attributes
		int attrSize = vAttr.size() + (additionalAttrs != null ? additionalAttrs.length : 0);
		attrs = new String[attrSize];
		attrSet = new HashSet<>(attrSize);

		/*
		 * wpierw przepisuję podstawowe informacje o atrybutach wynikające z
		 * konfiguracji
		 */
		Iterator<String> it = vAttr.iterator();
		int i = 0;
		while (it.hasNext()) {
			attrs[i] = it.next();
			attrSet.add(attrs[i]);
			log.trace("Atrybut {}: {}", new Object[] { i, attrs[i] });
			i++;
		}
		/* teraz dopiszę dodatkowe */
		this.additionalAttrs = additionalAttrs;
		if (additionalAttrs != null && additionalAttrs.length != 0) {
			for (String addAttr : additionalAttrs) {
				attrs[i] = addAttr;
				attrSet.add(attrs[i]);
				log.trace("Atrybut dodatkowy {}: {}", new Object[] { i, attrs[i] });
				i++;
			}
		}

	}

	/**
	 * Transformacja wyniku wyszukiwania (entry) do obiektu użytkownika
	 * {@link ILdapUser}.
	 * 
	 * @param result
	 *            obiekt wyniku zapytania do LDAP
	 * @param loadWithGroup
	 *            czy do obiektu użytkownika mają zostać dołączone informacje o
	 *            grupach (wymaga dodatkowych zapytań do LDAP, które wyciagną
	 *            informacje o grupach danego użytkownika).
	 * @return obiekt użytkownika
	 * @throws NamingException
	 */
	protected E transform2LdapUser(LdapResult result, boolean loadWithGroup) throws NamingException {

		if (result == null) {
			return null;
		}

		String login = LdapAOHelper.readValue(result, lo.getUserNameAttribute());
		E single = createNewInstance(login);

		single.setDisplayName(LdapAOHelper.readValue(result, lo.getUserDisplayNameAttribute()));

		/* ustawiamy dodatkowe atrybuty użytkownika */
		List<IUserAttribute> attributes = UserAttributeUtils.createAtributeList(this.lo, log, this.additionalAttrs,
				result, single, this.attrSet);
		single.setAttributes(attributes);

		String userDN = null;
		if (lo.getUserDNAttribute() != null) {
			userDN = LdapAOHelper.readValue(result, lo.getUserDNAttribute());
			if (StringUtils.isBlank(userDN)) {
				throw new NamingException("Can't follow referal for authentication: " + login);
			}
			single.setDn(userDN);
		} else {
			String name = result.getName();
			if (result.isRelative()) {
				userDN = name + "," + getFactory().getDynamicCtx();
				single.setDn(userDN);
			} else {
				throw new NamingException("Can't follow referal for authentication: " + name);
			}
		}
		log.trace("userDN={}", userDN);

		String email = lo.getUserEmailAttribute() != null ? lo.getUserEmailAttribute()
				: Constants.DEFAULT_EMAIL_ATTR_NAME;
		single.setEmail(LdapAOHelper.readValue(result, email));

		if (lo.getUserLocaleAttr() != null) {
			String userLocale = LdapAOHelper.readValue(result, lo.getUserLocaleAttr());
			if (StringUtils.isBlank(userLocale)) {
				userLocale = Constants.DEFAULT_USER_LOCALE;
			} else {
				userLocale = userLocale.toLowerCase();
			}
			single.setLocale(userLocale);
		}

		if (lo.isUserGroupOptionsAreDefinded()) {
			String[] userArgs = new String[] { single.getName(), single.getDn() };
			String primaryGroupFilter = lo.getUserPrimaryGroupFilter(userArgs);
			List<G> baseGroups = new ArrayList<>();
			G primaryGroup = null;
			String primaryGroupDN = LdapAOHelper.readValue(result, lo.getUserPrimaryGroupAttribute());

			/* ustawianie grup/ról przypisanych bezpośrednio - START */
			for (ILdapUserGroupAO<G> gLAO : groupLdapAO) {
				List<G> groups = gLAO.loadUserGroups(userArgs);
				if (groups != null && !groups.isEmpty()) {
					/*
					 * to ma tylko wtedy sens gdy użytkownik jest przypisany do jakiejkolwiek grupy
					 */
					/* ustawianie domyślnej grupy/roli - START */

					if (StringUtils.isNotBlank(primaryGroupFilter) && primaryGroup == null) {
						primaryGroup = gLAO.loadPrimaryUserGroup(userArgs);
					}
					/* ustawianie domyślnej grupy/roli - KONIEC */
					baseGroups.addAll(groups);
				}

			}

			if (primaryGroup != null) {
				single.setPrimaryGroupDN(primaryGroup.getDn());
				if (loadWithGroup) {
					single.setPrimaryGroup(primaryGroup);
				}
			} else if (StringUtils.isBlank(primaryGroupFilter) && lo.getUserPrimaryGroupAttribute() != null
					&& StringUtils.isNotBlank(primaryGroupDN)) {
				// user primary role, jeżeli jest zdefiniowana jako atrybut
				/* ustawianie domyślnej grupy/roli - START */
				single.setPrimaryGroupDN(primaryGroupDN);
				if (loadWithGroup) {
					for (G baseGroup : baseGroups) {
						if (baseGroup.getDn().equalsIgnoreCase(primaryGroupDN)) {
							single.setPrimaryGroup(baseGroup);
							break;
						}
					}
				}
				/* ustawianie domyślnej grupy/roli - KONIEC */
			}

			if (primaryGroup == null && StringUtils.isBlank(primaryGroupDN)) {
				log.warn("Uzytkownik {} ({}) nie ma zdefiniowanej grupy domyslnej ",
						new Object[] { single.getDisplayName(), single.getName() });
			}

			single.setBaseGroups(baseGroups);
			/* ustawianie grup/ról przypisanych bezposrednio - KONIEC */

			if (baseGroups == null || baseGroups.isEmpty()) {
				log.warn("Uzytkownik {} ({}) nie nalezy do zadnej z grup",
						new Object[] { single.getDisplayName(), single.getName() });
			}
		}
		return single;

	}

	public E load(Object id) throws NamingException {

		String searchFilter = lo.getBaseFilter(new String[] { (String) id });
		log.debug("--> load: Sarch Filter: {}", searchFilter);
		LdapResult result = getFactory().uniqueEntrySearch(attrs, searchFilter);
		if (result != null) {
			E user = transform2LdapUser(result, true);
			if (user == null) {
				log.warn("Nie udało się przetransformować obiektu z LDAP!");
			} else if (log.isDebugEnabled()) {
				log.debug("Przetrasformowałem obiekt z LDAP: {}", user.toString());
			}
			return user;
		}
		return null;
	}

	public E loadBySearchFilter(String searchFilter) throws NamingException {
		log.debug("--> loadBySearchFilter: Sarch Filter: {}", searchFilter);
		LdapResult result = getFactory().uniqueEntrySearch(attrs, searchFilter);
		if (result != null) {
			E user = transform2LdapUser(result, true);
			if (user != null && log.isDebugEnabled()) {
				log.debug("Pobralem z LDAP: {}", user.toString());
			}
			return user;
		}
		return null;
	}

	public List<E> loadAll() throws NamingException {
		String searchFilter = lo.getUsersFilter();
		log.debug("--> loadAll: Sarch Filter: {}", searchFilter);
		return loadByFilter(searchFilter);

	}

	public Map<String, E> loadAllAsMap() throws NamingException {
		Map<String, E> map = new HashMap<>();
		String searchFilter = lo.getUsersFilter();
		log.debug("--> loadAllAsMap: Sarch Filter: {}", searchFilter);
		return prepareAllAsMap(searchFilter, map, false);
	}

	public Map<String, E> mergeAllAsMap(Map<String, E> target) throws NamingException {
		String searchFilter = lo.getUsersFilter();
		log.debug("--> mergeAllAsMap: Sarch Filter: {}", searchFilter);
		return prepareAllAsMap(searchFilter, target, true);
	}

	protected Map<String, E> prepareAllAsMap(String searchFilter, Map<String, E> target, boolean checkOldElement)
			throws NamingException {
		List<LdapResult> resultsList = getFactory().manyEntrySearch(attrs, searchFilter);
		if (!checkOldElement) {
			for (LdapResult result : resultsList) {
				E user = transform2LdapUser(result, true);
				if (user != null) {
					String key = user.getName().toLowerCase();
					target.put(key, user);
				}
			}
		} else {
			for (LdapResult result : resultsList) {
				E user = transform2LdapUser(result, true);
				if (user != null) {
					String key = user.getName().toLowerCase();
					ILdapEntry oldUser = target.get(key);
					if (!user.equals(oldUser)) {
						target.put(key, user);
					}
				}
			}
		}

		return target;
	}

	/**
	 * @return the LDAP context factory
	 */
	protected ILdapContextFactory getFactory() {
		return factory.getLdapContextFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pro.ibpm.ldap.dao._LdapAO#close()
	 */
	@Override
	public void close() {
		factory.close();
		if (this.groupLdapAO != null) {
			for (ILdapUserGroupAO<G> gLdapAO : this.groupLdapAO) {
				gLdapAO.close();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pro.ibpm.ldap.dao._LdapAO#getOrganizationalUnitName()
	 */
	@Override
	public String getOrganizationalUnitName() {
		return this.factory.getOrganizationalUnitName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pro.ibpm.ldap.dao._LdapAOFactory#getLdapContextFactory()
	 */
	@Override
	public ILdapContextFactory getLdapContextFactory() {
		return this.factory.getLdapContextFactory();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pro.ibpm.ldap.dao._LdapAOFactory#isInicjalized()
	 */
	@Override
	public boolean isInicjalized() {
		return this.factory.isInicjalized();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pro.ibpm.ldap.dao._LdapAOFactory#isDefaultCtxDN()
	 */
	@Override
	public boolean isDefaultCtxDN() {
		return this.factory.isDefaultCtxDN();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pro.ibpm.ldap.dao._LdapAOFactory#getLdapOptions()
	 */
	@Override
	public ProviderOptions getLdapOptions() {
		return this.factory.getLdapOptions();
	}

	public ProviderOptions getLo() {
		return lo;
	}

	public List<ILdapUserGroupAO<G>> getGroupLdapAO() {
		return groupLdapAO;
	}

	public String[] getAttrs() {
		return attrs;
	}

	/**
	 * @return the {@link #additionalAttrs}
	 */
	public String[] getAdditionalAttrs() {
		return additionalAttrs;
	}

	/**
	 * @return the {@link #attrSet}
	 */
	public Set<String> getAttrSet() {
		return attrSet;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public List<E> loadByFilter(String searchFilter) throws NamingException {
		List<E> list = new ArrayList<>();
		log.debug("--> loadByFilter: Sarch Filter: {}", searchFilter);
		List<LdapResult> resultsList = super.loadByFilter(getFactory(), attrs, searchFilter);
		for (LdapResult result : resultsList) {
			E user = transform2LdapUser(result, true);
			if (user != null) {
				list.add(user);
			}
		}
		return list;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void changePassword(E ldapUser, String passwordAttrName, String newPassword, String oldPassword)
			throws NamingException {
		if (ldapUser == null) {
			return;
		}
		ILdapEntry4Changes user4Changes = transform4Changes(ldapUser);
		Map<String, List<ILdapAttribute>> changesMap = new HashMap<>();
		ILdapAttribute attr = new LdapUserAttribute(passwordAttrName, newPassword, Types.VARCHAR);
		attr.setOldValue(oldPassword);
		List<ILdapAttribute> values = new ArrayList<>();
		values.add(attr);
		changesMap.put(passwordAttrName, values);
		user4Changes.setChangesMap(changesMap);
		update(user4Changes);
	}

	/* Overridden (non-Javadoc) */
	@Override
	public void changePassword(E ldapUser, String passwordAttrName, byte[] newPassword, byte[] oldPassword)
			throws NamingException {
		if (ldapUser == null) {
			return;
		}
		ILdapEntry4Changes user4Changes = transform4Changes(ldapUser);
		Map<String, List<ILdapAttribute>> changesMap = new HashMap<>();
		ILdapAttribute attr = new LdapUserAttribute(passwordAttrName, newPassword, Types.VARCHAR);
		attr.setOldValue(oldPassword);
		List<ILdapAttribute> values = new ArrayList<>();
		values.add(attr);
		changesMap.put(passwordAttrName, values);
		user4Changes.setChangesMap(changesMap);
		update(user4Changes);
	}

	/**
	 * @return the {@link #log}
	 */
	protected Logger getLog() {
		return log;
	}

	/**
	 * Transformacja do instancji będącej podstawą do realizacji zmian. Aby
	 * przygotować instancję do zmian wystarczy mi, że będzie ona spełniała
	 * interfejs {@link ILdapBaseEntry} - <b>powstała instancja będzie miała pustą
	 * mapę zmian</b>.
	 * 
	 * @see #changePassword(String, String, byte[], byte[])
	 * @see #changePassword(String, String, String, String)
	 * 
	 * @param ldapUser
	 *            przekształcana instancja entry użytkownika.
	 * 
	 * @return instancja obiektu pod modyfikację z pustą mapą zmian (tylko
	 *         podstawowe dane {@link ILdapBaseEntry}).
	 */
	protected ILdapEntry4Changes transform4Changes(ILdapBaseEntry ldapUser) {
		return EntryUtils.transform4Changes(ldapUser);
	}
}
