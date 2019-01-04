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

package pl.slawas.common.ldap.config;

/**
 * 
 * LoadLdapConfiguration - ładowanie konfiguracji provider'a LDAP z pliku
 * zewnętrznego.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LoadLdapConfiguration extends ConfigJAXBFactory<LdapConfig> {

	/**
	 * @param url
	 *            nowa, zewnętrzna lokalizacja pliku konfiguracyjnego
	 * @param readConfigFromJar
	 *            czy plik znajduje się w bibliotece *.jar
	 */
	public LoadLdapConfiguration(String url, boolean readConfigFromJar) {
		super(url, readConfigFromJar);
	}

	/**
	 * 
	 * @param ldapOptionsXML
	 *            XML konfiguracyjny w postaci łańcucha znakowego.
	 */
	public LoadLdapConfiguration(String ldapOptionsXML) {
		super(ldapOptionsXML);
	}
}
