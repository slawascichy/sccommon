/*
 * Copyright 2014 Sci Software Sławomir Cichy
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.	
 */
package pl.slawas.common.ldap.provider;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrBuilder;

import pl.slawas.common.ldap.api.ILdapAO;
import pl.slawas.common.ldap.api.ILdapUser;
import pl.slawas.common.ldap.api.ILdapUserGroup;
import pl.slawas.common.ldap.api.ILdapUserGroupAO;
import pl.slawas.common.ldap.cache.LastTimeGroupSynchronization;
import pl.slawas.common.ldap.cache.LdapGroupsCache;
import pl.slawas.common.ldap.cache.LdapGroupsCacheLists;
import pl.slawas.common.ldap.provider.beans.GroupCacheHashTable;
import pl.slawas.common.ldap.provider.exceptions.LdapObjectFactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * LdapObjectFactorySupport wsparcie dla zarządzania obiektami dostępu do danych
 * repozytorium użytkowników.
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 * @param <L>
 *            reprezentacja obiektu entry użytkownika
 * @param <G>
 *            reprezentacja obiektu entry grupy
 * @param <Lao>
 *            reprezentacja obiektu dostępu do danych użytkownika (AO: Access
 *            Object)
 * @param <Gao>
 *            reprezentacja obiektu dostępu do danych grupy (AO: Access Object)
 */
public abstract class LdapObjectFactorySupport<L extends ILdapUser<G>, G extends ILdapUserGroup> {

	private static final Logger logger = LoggerFactory.getLogger(LdapObjectFactorySupport.class.getName());

	/**
	 * Nazwa grupy dla której podczas analizy kroków jest prezentowany szczegółowy
	 * raport.
	 * 
	 * TODO Trzeba to przenieść do konfiguracji
	 */
	private static final String DEBUG_FOR_GROUP = "DAAiWOGZW-Manager";

	private final Class<L> userClass;
	private final Class<G> groupClass;

	@SuppressWarnings("unchecked")
	public LdapObjectFactorySupport() {
		super();
		Class<?> tmpClass = this.getClass();
		while (!(tmpClass.getGenericSuperclass() instanceof ParameterizedType)) {
			tmpClass = tmpClass.getSuperclass();
		}
		this.userClass = (Class<L>) ((ParameterizedType) tmpClass.getGenericSuperclass()).getActualTypeArguments()[0];
		this.groupClass = (Class<G>) ((ParameterizedType) tmpClass.getGenericSuperclass()).getActualTypeArguments()[1];
	}

	/**
	 * @return suffix nazwy grupy, która pełni rolę kierownika grupy
	 */
	public abstract String getManagerGroupSuffix();

	/**
	 * 
	 * @return suffix nazwy grupy, która pełni rolę zarządzanego zespołu.
	 */
	public abstract String getTeamGroupSuffix();

	/**
	 * Pobieranie nowej instancji obiektu dostępu do repozytorium użytkowników.
	 * 
	 * @param ldapOptions
	 * @param organizationalUnit
	 * @return
	 */
	public abstract ILdapAO<L> getNewInstanceUserAO(ProviderOptions ldapOptions, String organizationalUnit);

	/**
	 * Pobieranie nowej instancji obiektu dostępu do repozytorium grup użytkowników.
	 * 
	 * @param ldapOptions
	 * @param organizationalUnit
	 * @return
	 */
	public abstract ILdapUserGroupAO<G> getNewInstanceUserGroupAO(ProviderOptions ldapOptions,
			String organizationalUnit);

	/**
	 * Pobieranie użytkownika po jego nazwie.
	 * 
	 * @param ldapOptions
	 * @param name
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public L findUserByName(ProviderOptions ldapOptions, String name) {
		String organizationalUnitArray[] = ldapOptions.getUsersOrganizationalUnitNames();

		L user = null;
		for (String organizationalUnitName : organizationalUnitArray) {
			ILdapAO<L> laoObject = getNewInstanceUserAO(ldapOptions, organizationalUnitName);
			try {
				user = laoObject.load(name);
				if (user != null) {
					return user;
				}
			} catch (NamingException e) {
				logger.error(String.format("Błąd poszukiwania użtytkownika o nazwie %s w jednostce %s", name,
						organizationalUnitName), e);
			} finally {
				laoObject.close();
			}
		}
		return user;

	}

	/**
	 * Załadowanie pełnej listy (wszystkich możliwych) obiektów dostępu do grup na
	 * podstawie parametrów konfiguracyjnych.
	 * 
	 * @param ldapOptions
	 *            parametry konfiguracyjne
	 * @return lista obiektów <b>PAMIĘTAJ! później je musisz pozamykać!!!</b>
	 */
	@SuppressWarnings("deprecation")
	public List<ILdapUserGroupAO<G>> loadAllUserGroupAOs(ProviderOptions ldapOptions) {
		String organizationalUnitArray[] = ldapOptions.getGroupsOrganizationalUnitNames();

		List<ILdapUserGroupAO<G>> groupLdapAOs = new ArrayList<>();
		for (int i = 0; i < organizationalUnitArray.length; i++) {
			groupLdapAOs.add(getNewInstanceUserGroupAO(ldapOptions, organizationalUnitArray[i]));
		}
		return groupLdapAOs;
	}

	/**
	 * Metoda ustalająca rekurencyjnie nazwę grupy manager'a.
	 * 
	 * TODO Grupy memnager'a chyba nie ma co szukać w innej jednostce
	 * organizacyjnej. Jakby co Wrócimy do tego tematu.
	 * 
	 * @param ldapOptions
	 *            parametry połączenia do repozytorium użytkowników
	 * @param currOUnitName
	 *            aktualna jednostka organizacyjna, w której drzewie poszukiwane są
	 *            zależności pomiędzy grupami.
	 * @param currentGroup
	 *            obiekt obecnej grupy
	 * @param recursionLevel
	 * @return
	 * @throws NumberFormatException
	 * @throws LdapObjectFactoryException
	 */
	public String getManagerGroupName(ProviderOptions ldapOptions, String currOUnitName, ILdapUserGroup currentGroup,
			int recursionLevel) throws LdapObjectFactoryException {
		/* pobranie nazwy grupy manager'a z obiektu grupy WP */
		String managerGroupName = currentGroup.getManagerGroupName();
		String memberOf = currentGroup.getMainMemberOf();
		if (logger.isDebugEnabled()) {
			logger.debug(
					"\n-------------\n{} getManagerGroupName:\n iteracja: {}\n grupa managerska: {}\n memberOf: {}\n-------------",
					new Object[] { currentGroup.getName(), recursionLevel, currentGroup.isManagerGroup(), memberOf });
		}
		if (StringUtils.isBlank(managerGroupName) && StringUtils.isNotBlank(memberOf)
				&& currentGroup.isManagerGroup()) {
			/*
			 * nie mamy ustawionej bezpośrednio grupy manager'a, ale grupa jest członkiem
			 * innej grupy oraz jest grupą kierownika niższego szczebla. Trzeba znaleźć
			 * szefa tego kierownika.
			 */
			ILdapUserGroup memberOfGroup = loadMainMemberOf(ldapOptions, currOUnitName, currentGroup);
			if (memberOfGroup != null && StringUtils.isNotBlank(memberOfGroup.getMainMemberOf())) {
				memberOf = memberOfGroup.getMainMemberOf();
				memberOfGroup = loadMainMemberOf(ldapOptions, currOUnitName, memberOfGroup);
				if (memberOfGroup != null) {
					managerGroupName = new StrBuilder().append(memberOfGroup.getName()).append(getManagerGroupSuffix())
							.toString();
				} else if (logger.isDebugEnabled()) {
					logger.debug("{} getManagerGroupName, iteracja {}. Nie znalazłem grupy DN={}",
							new Object[] { currentGroup.getName(), recursionLevel, memberOf });
				}
			} else if (logger.isDebugEnabled()) {
				logger.debug("{} getManagerGroupName, iteracja {}. Nie znalazłem memberOf dla {}",
						new Object[] { currentGroup.getName(), recursionLevel,
								(memberOfGroup != null ? memberOfGroup.getDn() : currentGroup.getDn()) });
			}
		} else if (StringUtils.isBlank(managerGroupName) && StringUtils.isBlank(memberOf)) {
			/*
			 * nie mamy ustawionej bezposrednio grupy manager'a, i grupa nie jest członkiem
			 * innej grupy, zatem zrobilśmy wszystko co w naszej mocy by ustalić primary
			 * group, ale się nie udało -> zwracam null.
			 */
			if (logger.isDebugEnabled()) {
				logger.debug("{} nie mamy ustawionej bezposrednio grupy manager'a, "
						+ "i grupa nie jest czlonkiem innej grupy.", currentGroup.getName());
			}
			return null;
		}

		if (logger.isDebugEnabled()) {
			logger.debug("{} getManagerGroupName, iteracja {}. managerGroupName: '{}'",
					new Object[] { currentGroup.getName(), recursionLevel, managerGroupName });
		}

		ILdapUserGroup managerGroup = null;
		if (StringUtils.isNotBlank(managerGroupName)) {
			GroupCacheHashTable<G> cachedGroups = loadUserGroupCacheList(ldapOptions, false);
			managerGroup = cachedGroups.get(managerGroupName);
			if (managerGroup != null) {
				/* sprawdzenie czy primary group ma członków */
				if (logger.isDebugEnabled()) {
					logger.debug("{} Sprawdzam grupe parentGroup {}, ktora ma {} uzytkownikow.",
							new Object[] { currentGroup.getName(), managerGroup.getName(),
									(managerGroup.getMembers() != null ? managerGroup.getMembers().size() : 0) });
					if (logger.isDebugEnabled() && managerGroupName.equals(DEBUG_FOR_GROUP)) {
						logger.debug("{}: {} uzytkownik na liscie: {}",
								new Object[] { DEBUG_FOR_GROUP,
										(managerGroup.getMembers() != null ? managerGroup.getMembers().size() : 0),
										(managerGroup.getMembers().isEmpty() ? "uff.. empty"
												: managerGroup.getMembers().toArray(new String[] {})[0]) });
					}
				}
				if (managerGroup.getMembers() != null && !managerGroup.getMembers().isEmpty()) {
					return managerGroupName;
				}
			} else {
				logger.warn("Nie udalo sie pobrac obiektu grupy o nazwie {}", managerGroupName);
			}
		}
		if (managerGroup != null && recursionLevel < Integer.parseInt(getRoleRecursion(ldapOptions))) {
			/* grupa nie miała członków - próbuje dalej, w kolejnej iteracji */
			int nextLevel = recursionLevel + 1;
			return getManagerGroupName(ldapOptions, currOUnitName, managerGroup, nextLevel);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("{} Nie udało się ustalic primary group (iteracja {}).",
						new Object[] { currentGroup.getName(), recursionLevel });
			}
			return null;
		}

	}

	/**
	 * Ładowanie grupy nadrzędnej dla obecnej grupy.
	 * 
	 * @param ldapOptions
	 *            parametry połączenia do repozytorium użytkowników
	 * @param organizationalUnitName
	 *            nazwa jednostki organizacyjnej
	 * @param uGroup
	 *            obiekt obecnej grupy
	 * @return obiekt grupy nadrzędnej.
	 */
	@SuppressWarnings("deprecation")
	public G loadMainMemberOf(ProviderOptions ldapOptions, String organizationalUnitName, ILdapUserGroup uGroup) {
		G mainMemberOf = null;
		/*
		 * ustawienie powiązania 'wiele-do-wiele' pomiędzy grupami - start
		 */
		String memberOfValue = uGroup.getMainMemberOf();
		if (StringUtils.isNotBlank(memberOfValue)) {
			if (ldapOptions.getGroup2GroupTokenAttributeIsDN()) {
				/**
				 * Grupa nadrzędna pochodzi z tej samej jednostki organizacyjnej. Zobacz
				 * założenia {@link LdapUserGroup#mainMemberOf}
				 */
				mainMemberOf = getFromCacheByDN(organizationalUnitName, memberOfValue);
				if (logger.isDebugEnabled()) {
					logger.debug("{}nalazłem grupy w pamięci podręcznej dla {}...",
							new Object[] { (mainMemberOf == null ? "Nie z" : "Z"), memberOfValue });

				}

			} else {
				/* Musimy poszukać grupy nadrzędnej */
				String[] organizationalUnitArray = ldapOptions.getGroupsOrganizationalUnitNames();
				for (String gUnit : organizationalUnitArray) {
					mainMemberOf = getFromCacheByName(gUnit, memberOfValue);
					if (mainMemberOf == null) {
						/** pobieram bezpośrednio z LDAP */
						ILdapAO<G> extLaoObject = null;
						try {
							extLaoObject = getNewInstanceUserGroupAO(ldapOptions, gUnit);
							mainMemberOf = extLaoObject.load(memberOfValue);
						} catch (NamingException e) {
							logger.error("Problem z pobraniem grupy nadrzednej w hierarchii", e);
						} finally {
							if (extLaoObject != null) {
								extLaoObject.close();
							}
						}
					}
					if (mainMemberOf != null) {
						/**
						 * No jak mam już grupę to przerywam szukanie
						 */
						break;
					}
				}
			}
		}
		/*
		 * ustawienie powiązania 'wiele-do-wiele' pomiędzy grupami - koniec
		 */
		if (logger.isDebugEnabled()) {
			logger.debug("{} loadMainMemberOf: \n mainMemberOf(current)={}, \n mainMemberOf(next)={}",
					new Object[] { uGroup.getName(), (mainMemberOf == null ? "n/a" : mainMemberOf.getName()),
							(mainMemberOf == null ? "n/a" : mainMemberOf.getMainMemberOf()) });
		}
		return mainMemberOf;
	}

	/**
	 * Ładowanie grup nadrzędnych dla obecnej grupy innych niż grupa główna (o ile
	 * taka istnieje).
	 * 
	 * @see #loadMainMemberOf(ProviderOptions, String, _LdapUserGroup);
	 * 
	 * @param ldapOptions
	 *            parametry połączenia do repozytorium użytkowników
	 * @param uGroup
	 *            obiekt obecnej grupy
	 * @return obiekt grupy nadrzędnej.
	 */
	@SuppressWarnings("deprecation")
	public Set<G> loadOtherMembersOf(ProviderOptions ldapOptions, ILdapUserGroup uGroup) {
		Set<G> membersOf = new HashSet<>();
		/*
		 * ustawienie powiązania 'wiele-do-wiele' pomiędzy grupami - start
		 */
		Set<String> memberOfValues = uGroup.getOtherMembersOf();
		if (memberOfValues != null && !memberOfValues.isEmpty()) {
			if (ldapOptions.getGroup2GroupTokenAttributeIsDN()) {
				/* Musimy poszukać grup nadrzędnych */
				String[] organizationalUnitArray = ldapOptions.getGroupsOrganizationalUnitNames();
				for (String gUnit : organizationalUnitArray) {
					// TODO czy na pewno muszę szukać we wszystkich OU? mam
					// przecież DN'y na podstawie których mogę zidentyfikować
					// odpowiednią jednostkę organizacyjną.
					//
					for (String memberOfValue : memberOfValues) {
						G memberOf = getFromCacheByDN(gUnit, memberOfValue);
						if (memberOf != null) {
							membersOf.add(memberOf);
						}
					}
				}

			} else {
				/* Musimy poszukać grup nadrzędnych */
				String[] organizationalUnitArray = ldapOptions.getGroupsOrganizationalUnitNames();
				for (String gUnit : organizationalUnitArray) {
					for (String memberOfValue : memberOfValues) {
						G memberOf = getFromCacheByName(gUnit, memberOfValue);
						if (memberOf == null) {
							/** pobieram bezpośrednio z LDAP */
							ILdapAO<G> extLaoObject = null;
							try {
								extLaoObject = getNewInstanceUserGroupAO(ldapOptions, gUnit);
								memberOf = extLaoObject.load(memberOfValue);
							} catch (NamingException e) {
								logger.error("Problem z pobraniem grupy nadrzednej w hierarchii", e);
							} finally {
								if (extLaoObject != null) {
									extLaoObject.close();
								}
							}
						}
						if (memberOf != null) {
							membersOf.add(memberOf);
						}
					}
				}
			}
		}
		return membersOf;
	}

	public String getRoleRecursion(ProviderOptions ldapOptions) {
		if (!ProviderOptions.offLine && ldapOptions != null && ldapOptions.getRoleRecursion() != null) {
			return ldapOptions.getRoleRecursion();
		}
		return ProviderOptions.DEFAILT_ROLE_RECURSION;
	}

	/**
	 * @return the {@link #userClass}
	 */
	public Class<L> getUserClass() {
		return userClass;
	}

	/**
	 * @return the {@link #groupClass}
	 */
	public Class<G> getGroupClass() {
		return groupClass;
	}

	/**
	 * Pobieranie z pamięci podręcznej po nazwie grupy
	 * 
	 * @param organizationalUnitName
	 *            region pamięci podręcznej
	 * @param roleName
	 *            nazwa grupy
	 * @return obiekt grupy
	 */
	public G getFromCacheByName(String organizationalUnitName, String roleName) {
		return ((LdapGroupsCache<G>) LdapGroupsCacheLists.getCache(this.groupClass))
				.getFromCacheByName(organizationalUnitName, roleName);
	}

	/**
	 * Wstawianie do pamięci podręcznej uporządkowanej po nazwach grup.
	 * 
	 * @param organizationalUnitName
	 *            region pamięci podręcznej
	 * @param ldapUserGroup
	 *            obiekt grupy
	 */
	public void putIntoCacheByName(String organizationalUnitName, G ldapUserGroup) {
		((LdapGroupsCache<G>) LdapGroupsCacheLists.getCache(this.groupClass)).putIntoCacheByName(organizationalUnitName,
				ldapUserGroup);
	}

	/**
	 * Pobieranie z pamięci podręcznej po DN grupy
	 * 
	 * @param organizationalUnitName
	 *            region pamięci podręcznej
	 * @param dn
	 *            DN grupy
	 * @return obiekt grupy
	 */
	public G getFromCacheByDN(String organizationalUnitName, String dn) {
		return ((LdapGroupsCache<G>) LdapGroupsCacheLists.getCache(this.groupClass))
				.getFromCacheByDN(organizationalUnitName, dn);
	}

	/**
	 * Wstawianie do pamięci podręcznej uporządkowanej po DN grup.
	 * 
	 * @param organizationalUnitName
	 *            region pamięci podręcznej
	 * @param ldapUserGroup
	 *            obiekt grupy
	 */
	public void putIntoCacheByDN(String organizationalUnitName, G ldapUserGroup) {
		((LdapGroupsCache<G>) LdapGroupsCacheLists.getCache(this.groupClass)).putIntoCacheByDN(organizationalUnitName,
				ldapUserGroup);
	}

	/**
	 * Pobranie czasu ostatniej synchronizacji grup.
	 * 
	 * @param organizationalUnitName
	 *            nazwa jednostki organizacyjnej (regionu pamięci podręcznej).
	 * @return obiekt zawierające dane o czasie ostatniej synchronizacji
	 */
	public LastTimeGroupSynchronization getLastTimeGroupSynchronization(String organizationalUnitName) {
		return ((LdapGroupsCache<G>) LdapGroupsCacheLists.getCache(this.groupClass))
				.getLastTimeGroupSynchronization(organizationalUnitName);
	}

	/**
	 * Odświeżenie/synchronizacja regionu pamięci z listami grup.
	 * 
	 * @param laoObject
	 *            PAMIETAJ!.... Obiekt nie jest zamykany! po wyjściu zadbaj by był
	 *            zamknięty!
	 * @param rebuild
	 *            czy przebudować?
	 * @return {@link #groupsCacheByName}
	 */
	public GroupCacheHashTable<G> loadRegionUserGroupCacheList(ILdapUserGroupAO<G> laoObject, boolean rebuild) {
		return ((LdapGroupsCache<G>) LdapGroupsCacheLists.getCache(this.groupClass))
				.loadRegionUserGroupCacheList(laoObject, rebuild);
	}

	/**
	 * Metoda podręczna pobierania grup z LDAP'a
	 * 
	 * @param ldapOptions
	 *            obiekt opcji połączenia do LDAP
	 * @param rebuild
	 *            czy mają być przebudowane lokalne tablice (pamięć podręczna)
	 * @return mapa nazwa grupy - obiekt grupy
	 */
	public GroupCacheHashTable<G> loadUserGroupCacheList(ProviderOptions ldapOptions, boolean rebuild) {
		return ((LdapGroupsCache<G>) LdapGroupsCacheLists.getCache(this.groupClass)).loadUserGroupCacheList(this,
				ldapOptions, rebuild);
	}

}
