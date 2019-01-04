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

package pl.slawas.common.ldap.provider;

import java.io.Serializable;
import java.security.Security;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.ldap.api.ILdapConnectionFactory;
import pl.slawas.common.ldap.api.ILdapContextFactory;
import pl.slawas.common.ldap.api.ILdapUserGroup;
import pl.slawas.common.ldap.dao.LdapAOHelper;
import pl.slawas.common.ldap.dao.LdapUserAO;
import pl.slawas.common.ldap.dao.LdapUserGroupAO;
import pl.slawas.helpers.Strings;
import pl.slawas.twl4j.Logger;
import pl.slawas.twl4j.LoggerFactory;

/**
 * LdapConnectionFactoryBean klasa pomocnicza obsługująca wyszukiwanie w LDAP.
 * Obiekt zarządzający parametrami połączenia do repozytorium LDAP.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LdapConnectionFactoryBean implements Serializable, ILdapConnectionFactory {

	private static final long serialVersionUID = 6652495401454679123L;

	static {
		String sslSocketFactoryProvider = Security.getProperty("ssl.SocketFactory.provider");
		if (StringUtils.isNotBlank(sslSocketFactoryProvider)
				&& sslSocketFactoryProvider.equals("com.ibm.websphere.ssl.protocol.SSLSocketFactory")) {
			Security.setProperty("ssl.SocketFactory.provider", "com.ibm.jsse2.SSLSocketFactoryImpl");
			Security.setProperty("ssl.ServerSocketFactory.provider", "com.ibm.jsse2.SSLServerSocketFactoryImpl");
		}
	}

	final private static Logger log = LoggerFactory.getLogger(LdapConnectionFactoryBean.class.getName());

	/** obiekt połączenia (kontekstu) LDAP */
	private transient ILdapContextFactory ldapContextFactory = null;

	/** czy zainicjalizowany (połączenie zestawione)? */
	private boolean isNotInicjalized = true;

	/**
	 * Nazwa jednostki organizacyjnej, w której gałęzi przechowywane są dane
	 * użytkowników. Jeżeli zostanie zdefiniowany to do podstawowego DN wyszukiwania
	 * zostanie dodany prefix {@code OU=}
	 * {@link #usersOrganizationalUnitName}{@code ,}. Opcjonalny parametr (może być
	 * {@code null}).
	 */
	private final String organizationalUnitName;

	/**
	 * Parametry inicjalizacyjne kontekstu LDAP, zdefiniowane w dostawcy opcji.
	 */
	private final ProviderOptions ldapOptions;

	/** obiekt pozwalający na ustawienie blokady podczas inicjalizacji kontekstu */
	private Object initLock = new Object();

	private final Class<?> parentClazz;

	/**
	 * @param ldapOptions
	 *            parametry inicjalizacyjne kontekstu LDAP, zdefiniowane w dostawcy
	 *            opcji.
	 * @param organizationalUnitName
	 *            Opcjonalny parametr (może być {@code null}). Nazwa jednostki
	 *            organizacyjnej. Jeżeli zostanie zdefiniowany to do {@link #ctxDN}
	 *            zostanie dodany prefix {@code OU=}
	 *            {@link #usersOrganizationalUnitName}{@code ,}
	 * @param parentClazz
	 *            klasa nadrzędnego obiektu dostępu
	 */
	public LdapConnectionFactoryBean(ProviderOptions ldapOptions, String organizationalUnitName, Class<?> parentClazz) {
		super();
		this.organizationalUnitName = organizationalUnitName;
		this.ldapOptions = ldapOptions;
		this.parentClazz = parentClazz;
	}

	private void init() {
		/* sprawdzam czy kontekst dotyczy grup i ustawiam odpowiednią flagę */
		boolean isGroupContext = this.parentClazz.getSimpleName().equals(ILdapUserGroup.class.getSimpleName());
		try {
			this.ldapContextFactory = new LdapContextFactoryBean(this.ldapOptions, this.organizationalUnitName,
					isGroupContext);
			log.debug("Utworzylem kontekst (polaczenie) dla {}", LdapAOHelper.getContextName(organizationalUnitName));
		} catch (Exception e) {
			throw new RuntimeException("Blad inicjalizacji kontekstu LDAP'a", e);
		}
		this.isNotInicjalized = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.scbpm.lib.ldap.dao._LdapAOFactory#getLdapContextFactory()
	 */
	public ILdapContextFactory getLdapContextFactory() {
		synchronized (initLock) {
			if (isNotInicjalized) {
				init();
			}
			return ldapContextFactory;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.scbpm.lib.ldap.dao._LdapAOFactory#isInicjalized()
	 */
	public boolean isInicjalized() {
		return !isNotInicjalized;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.scbpm.lib.ldap.dao._LdapAOFactory#close()
	 */
	public void close() {
		synchronized (initLock) {
			if (this.ldapContextFactory != null) {
				try {
					this.ldapContextFactory.close();
				} catch (Exception ignore) {
					// ignore
					log.warn("Blad zamkniecia kontekstu LDAP'a mozna zignorowac...", ignore);
				}
			}
			this.ldapContextFactory = null;
			this.isNotInicjalized = true;
		}
	}

	/**
	 * Bindowanie argumentów do definicji filtra używanego do wyszukiwania w LDAP.
	 * Zasada jest prosta. Łańcuch filtra (argument {@code filter}) zawiera klauzule
	 * '{nr_pozycji_w_macierzy}' np.
	 * 
	 * <pre>
	 * (&(sAMAccountName={0})(objectClass=user))
	 * </pre>
	 * 
	 * Ta klauzula jest podmieniana wartością zdefiniowaną na odpowiedniej pozycji w
	 * macierzy argumentów {@code args} i tak dla powyższego przykładu wartość '{0}'
	 * zostanie podmieniona odpowiednią wartością {@code args[0]}.
	 * 
	 * @param args
	 *            macierz argumentów do bindowania
	 * @param filter
	 *            klauzula filtra
	 * @return zbudowane odpowiednio zapytanie LDAP
	 */
	public static String bindFilterVariables(String[] args, String filter) {

		String localFilter = filter;
		if (args != null) {
			int i = 0;
			for (String arg : args) {
				log.debug("local Filter arg{}: {}", new Object[] { i, arg });
				localFilter = Strings.replaceAll(localFilter, "{" + i++ + "}", arg);
			}
		}
		return localFilter;
	}

	/**
	 * Czytanie (rozkodowanie) wartości atrybutu z wyniku LDAP.
	 * 
	 * @param result
	 *            obiekt rezultatu (wyniku) z LDAP
	 * @param attrName
	 *            nazwa atrybutu LDAP
	 * @return wartość atrybutu w postaci string'a
	 */
	public static String readValue(LdapResult result, String attrName) {
		if (result.get(attrName) != null) {
			log.trace("readValue: {}={}", new Object[] { attrName, result.get(attrName).get(0).getValue() });
			return (String) result.get(attrName).get(0).getValue();
		}
		log.trace("readValue: {}={}", new Object[] { attrName, result.get(attrName) });
		return null;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.scbpm.lib.ldap.dao._LdapAOFactory#getOrganizationalUnitName()
	 */
	public String getOrganizationalUnitName() {
		return organizationalUnitName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.scbpm.lib.ldap.dao._LdapAOFactory#isDefaultCtxDN()
	 */
	public boolean isDefaultCtxDN() {
		return StringUtils.isBlank(organizationalUnitName);
	}

	/**
	 * @return the {@link #serialversionuid}
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the {@link #log}
	 */
	public static Logger getLog() {
		return log;
	}

	/**
	 * @return the {@link #parentClazz}
	 */
	public Class<?> getParentClazz() {
		return parentClazz;
	}

	/**
	 * @return the {@link #ldapOptions}
	 */
	public ProviderOptions getLdapOptions() {
		return ldapOptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.scbpm.lib.ldap.dao._LdapAOFactory#getGroupLao()
	 */
	@Deprecated
	public LdapUserGroupAO getGroupLao() {
		if (isNotInicjalized) {
			init();
		}
		// return new LdapUserGroupAO(this.ldapContextFactory);
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.scbpm.lib.ldap.dao._LdapAOFactory#getUserLao()
	 */
	@Deprecated
	public LdapUserAO getUserLao() {
		if (isNotInicjalized) {
			init();
		}
		// return new LdapUserAO(this.ldapContextFactory, getGroupLao());
		return null;
	}

}
