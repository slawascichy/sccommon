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

import pl.slawas.common.ldap.api.ILdapUserGroup;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

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
public class LdapGroupsCacheLists {

	private LdapGroupsCacheLists() {
	}

	private static final Logger logger = LoggerFactory.getLogger(LdapGroupsCacheLists.class.getName());

	private static final Map<Class<? extends ILdapUserGroup>, LdapGroupsCache<? extends ILdapUserGroup>> caches = new HashMap<>();
	private static final Object cacheLock = new Object();

	@SuppressWarnings("unchecked")
	public static <G extends ILdapUserGroup> LdapGroupsCache<G> getCache(Class<G> clazz) {
		synchronized (cacheLock) {
			return (LdapGroupsCache<G>) caches.computeIfAbsent(clazz, k -> new LdapGroupsCache<>());
		}

	}

	/** flaga obsługi synchronizacji grup */
	private static final ForceSyncUserGroups isForceSyncUserGroups = ForceSyncUserGroups.getInstance();

	private static final Object lockForceSyncUserGroups = (new StringBuilder())
			.append("lockForceSyncUserGroups.SciSoftware.8.5.x").toString().intern();

	/** Pobranie nazwy użytkownika, który uruchomił synchronizację grup */
	public static String getUserForceSyncUserGroups() {
		return isForceSyncUserGroups.getUserName();
	}

	/**
	 * Ustawianie nazwy użytkownika, który uruchomił pełną synchronizację grup oraz
	 * ustawianie blokady na wykonywanie pełnej synchronizacji grup.
	 * 
	 * @param userName
	 *            nazwa użytkownika.
	 * @return czy udało się założyć blokadę na wykonywanie pełniej synchronizacji
	 *         grup.
	 */
	public static boolean setForceSyncUserGroupsTrue(String userName) {
		synchronized (lockForceSyncUserGroups) {
			if (logger.isDebugEnabled()) {
				logger.debug("setForceSyncUserGroupsTrue start: {}", isForceSyncUserGroups.isOk());
			}
			if (isForceSyncUserGroups.isOk()) {
				if (logger.isDebugEnabled()) {
					logger.debug("setForceSyncUserGroupsTrue failed for {} because locked: {} by {}", new Object[] {
							userName, isForceSyncUserGroups.isOk(), isForceSyncUserGroups.getUserName() });
				}
				return false;
			}
			isForceSyncUserGroups.setOk2True();
			isForceSyncUserGroups.setUserName(userName);
			if (logger.isDebugEnabled()) {
				logger.debug("setForceSyncUserGroupsTrue locked: {} by {}",
						new Object[] { isForceSyncUserGroups.isOk(), isForceSyncUserGroups.getUserName() });
			}
			return isForceSyncUserGroups.isOk();
		}
	}

	/**
	 * Zdjęcie blokady wykonywania operacji pełnej synchronizacji grup.
	 */
	public static void setForceSyncUserGroupsFalse() {
		synchronized (lockForceSyncUserGroups) {
			if (logger.isDebugEnabled()) {
				logger.debug("setForceSyncUserGroupsFalse start: {}", isForceSyncUserGroups.isOk());
			}
			isForceSyncUserGroups.setOk2False();
			isForceSyncUserGroups.resetUserName();
			if (logger.isDebugEnabled()) {
				logger.debug("setForceSyncUserGroupsFalse end: {}", isForceSyncUserGroups.isOk());
			}
		}
	}

	/**
	 * @return the {@link #isForceSyncUserGroups}
	 */
	public static ForceSyncUserGroups getIsforcesyncusergroups() {
		return isForceSyncUserGroups;
	}

}
