/*
 * Slawas.pl Copyright &copy; 2011-2012 
 * http://slawas.pl 
 * All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN
 * NO EVENT SHALL SŁAWOMIR CICHY BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package pl.slawas.common.ldap.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.ldap.api.ILdapUser;
import pl.slawas.common.ldap.api.ILdapUserGroup;
import pl.slawas.common.ldap.api.ILdapUserGroupAO;
import pl.slawas.common.ldap.dao.LdapAOHelper;
import pl.slawas.common.ldap.provider.LdapObjectFactorySupport;
import pl.slawas.common.ldap.provider.ProviderOptions;
import pl.slawas.common.ldap.provider.beans.GroupCacheHashTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GroupCacheLists - pomocnicze tablice z parametrami synchronizowanych grup. Za
 * pomocą obiektów przechowywanych zmniejszam liczbę odwołań do LDAP'a w celu
 * weryfikacji danych grupy podczas identyfikacji grup nadrzędnych czy też ról
 * managerów.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LdapGroupsCache<G extends ILdapUserGroup> {

	private static final Logger logger = LoggerFactory.getLogger(LdapGroupsCache.class.getName());

	private final Object cacheLock = new Object();

	/**
	 * Mapa grup z nazwą grupy jako kluczem.
	 */
	private final Map<String, GroupCacheHashTable<G>> groupsCacheByName = new HashMap<>();

	/**
	 * Mapa grup z DN'em jako kluczem.
	 */
	private final Map<String, GroupCacheHashTable<G>> groupsCacheByDN = new HashMap<>();

	/**
	 * Ostatni czas synchronizacji listy z nazwą jednostki jako kluczem.
	 */
	private final Map<String, LastTimeGroupSynchronization> lastTimeGroupSynchronizationMap = new HashMap<>();

	/**
	 * Metoda czyszcząca wszystkie obiekty z pamięci podręcznych.
	 */
	public void clear() {
		synchronized (cacheLock) {
			groupsCacheByName.clear();
			groupsCacheByDN.clear();
			lastTimeGroupSynchronizationMap.clear();
		}
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
		GroupCacheHashTable<G> regionOfgroupsCacheByName = groupsCacheByName.get(organizationalUnitName.toUpperCase());
		return (regionOfgroupsCacheByName != null ? regionOfgroupsCacheByName.get(roleName) : null);
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
		if (ldapUserGroup == null || ldapUserGroup.getName() == null) {
			return;
		}
		GroupCacheHashTable<G> regionOfgroupsCacheByName = null;
		synchronized (groupsCacheByName) {
			regionOfgroupsCacheByName = groupsCacheByName.get(organizationalUnitName.toUpperCase());
			if (regionOfgroupsCacheByName == null) {
				regionOfgroupsCacheByName = new GroupCacheHashTable<>();
				groupsCacheByName.put(organizationalUnitName.toUpperCase(), regionOfgroupsCacheByName);
			}
		}
		Object lockaName = (getClass().getSimpleName() + ".putIntoCacheByName." + ldapUserGroup.getName().toUpperCase())
				.intern();
		synchronized (lockaName) {
			regionOfgroupsCacheByName.put(ldapUserGroup.getName(), ldapUserGroup);
		}
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
		GroupCacheHashTable<G> regionOfgroupsCacheByDN = groupsCacheByDN.get(organizationalUnitName.toUpperCase());
		return (regionOfgroupsCacheByDN != null ? regionOfgroupsCacheByDN.get(dn) : null);
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
		if (ldapUserGroup == null || ldapUserGroup.getDn() == null) {
			return;
		}
		GroupCacheHashTable<G> regionOfgroupsCacheByDN = null;
		synchronized (groupsCacheByDN) {
			regionOfgroupsCacheByDN = groupsCacheByDN.get(organizationalUnitName.toUpperCase());
			if (regionOfgroupsCacheByDN == null) {
				regionOfgroupsCacheByDN = new GroupCacheHashTable<>();
				groupsCacheByDN.put(organizationalUnitName.toUpperCase(), regionOfgroupsCacheByDN);
			}
		}
		Object lockaName = (getClass().getSimpleName() + ".putIntoCacheByDN." + ldapUserGroup.getDn().toUpperCase())
				.intern();
		synchronized (lockaName) {
			regionOfgroupsCacheByDN.put(ldapUserGroup.getDn(), ldapUserGroup);
		}
	}

	/**
	 * Pobranie czasu ostatniej synchronizacji grup.
	 * 
	 * @param organizationalUnitName
	 *            nazwa jednostki organizacyjnej (regionu pamięci podręcznej).
	 * @return obiekt zawierające dane o czasie ostatniej synchronizacji
	 */
	public LastTimeGroupSynchronization getLastTimeGroupSynchronization(String organizationalUnitName) {
		synchronized (lastTimeGroupSynchronizationMap) {
			LastTimeGroupSynchronization lastTimeGroupSynchronization = lastTimeGroupSynchronizationMap
					.get(organizationalUnitName.toUpperCase());
			if (lastTimeGroupSynchronization == null) {
				lastTimeGroupSynchronization = new LastTimeGroupSynchronization();
				lastTimeGroupSynchronizationMap.put(organizationalUnitName.toUpperCase(), lastTimeGroupSynchronization);
			}
			return lastTimeGroupSynchronization;
		}
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
		String organizationalUnitName = laoObject.getOrganizationalUnitName();
		if (StringUtils.isBlank(organizationalUnitName)) {
			organizationalUnitName = LdapAOHelper.DEFAULT_LDAP_CONTEXT_NAME;
		}
		organizationalUnitName = organizationalUnitName.toUpperCase();
		if (logger.isDebugEnabled()) {
			logger.debug("-->loadRegionUserGroupCacheList: {}", organizationalUnitName);
		}

		synchronized (groupsCacheByName) {

			GroupCacheHashTable<G> oCacheByName = groupsCacheByName.get(organizationalUnitName);
			if (oCacheByName == null) {
				oCacheByName = new GroupCacheHashTable<>();
			}
			if (oCacheByName.isEmpty() || rebuild) {
				// przebudowuję obiekty pamięci podręcznej.
				/* na początek z nazwą grupy jako klucz */
				try {
					laoObject.mergeAllAsMap(oCacheByName);
				} catch (NamingException e) {
					logger.error("listAllGroups", e);
					oCacheByName.clear();
				}
				/* zapisuję wynik do pamięci podręcznej */
				groupsCacheByName.put(organizationalUnitName, oCacheByName);

				/* teraz z DN grupy jako klucz */
				GroupCacheHashTable<G> oCacheByDN = new GroupCacheHashTable<>();
				for (G g : oCacheByName.values()) {
					if (g.getDn() != null) {
						oCacheByDN.put(g.getDn(), g);
					}
				}
				/* zapisuję wynik do pamięci podręcznej */
				groupsCacheByDN.put(organizationalUnitName, oCacheByDN);

				if (logger.isTraceEnabled()) {
					StringBuilder sb = new StringBuilder();
					for (Entry<String, G> entry : oCacheByName.entrySet()) {
						String key = entry.getKey();
						ILdapUserGroup value = entry.getValue();
						sb.append("\n\tkey: ").append(key);
						sb.append("\nvalue: ").append(value.toString(true));
					}
					logger.trace("-->loadRegionUserGroupCacheList: created groupCacheByName for {} with {} entries: {}",
							new Object[] { organizationalUnitName, oCacheByName.size(), sb.toString() });
					sb = new StringBuilder();
					for (Entry<String, G> entry : oCacheByDN.entrySet()) {
						String key = entry.getKey();
						ILdapUserGroup value = entry.getValue();
						sb.append("\n\tkey: ").append(key);
						sb.append("\nvalue: ").append(value.toString(true));
					}
					logger.trace("-->loadRegionUserGroupCacheList: created groupCacheByDN for {} with {} entries: {}",
							new Object[] { organizationalUnitName, oCacheByDN.size(), sb.toString() });
				}
			} else if (logger.isDebugEnabled()) {
				logger.debug("-->loadRegionUserGroupCacheList: exists for {} with {} entries",
						new Object[] { organizationalUnitName, oCacheByName.size() });
			}
			return oCacheByName;
		}
	}

	/**
	 * Metoda podręczna pobierania grup z LDAP'a
	 * 
	 * @param objectFactory
	 *            implementacja fabryki obiektów dostępu do repozytorium
	 *            użytkowników.
	 * @param ldapOptions
	 *            obiekt opcji połączenia do LDAP
	 * @param rebuild
	 *            czy mają być przebudowane lokalne tablice (pamięć podręczna)
	 * @return mapa nazwa grupy - obiekt grupy
	 */
	@SuppressWarnings("deprecation")
	public <L extends ILdapUser<G>> GroupCacheHashTable<G> loadUserGroupCacheList(
			LdapObjectFactorySupport<L, G> objectFactory, ProviderOptions ldapOptions, boolean rebuild) {
		String[] organizationalUnitArray = ldapOptions.getGroupsOrganizationalUnitNames();

		GroupCacheHashTable<G> result = new GroupCacheHashTable<>();

		for (String organizationalUnit : organizationalUnitArray) {
			/** modyfikacja/aktualizacja/pobieranie regionu groupsCacheByName */
			ILdapUserGroupAO<G> laoObject = null;
			try {
				laoObject = objectFactory.getNewInstanceUserGroupAO(ldapOptions, organizationalUnit);
				GroupCacheHashTable<G> regionOfCache = loadRegionUserGroupCacheList(laoObject, rebuild);
				result.putAll(regionOfCache);
			} finally {
				if (laoObject != null) {
					laoObject.close();
				}
			}
		}
		return result;
	}

}
