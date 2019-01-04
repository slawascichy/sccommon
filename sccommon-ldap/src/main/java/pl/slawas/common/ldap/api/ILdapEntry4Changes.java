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

package pl.slawas.common.ldap.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * _LdapEntry4Changes - ogólny interfejs związany z entry, która ma być
 * zmieniona.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public interface ILdapEntry4Changes extends Serializable, ILdapBaseEntry {

	/**
	 * Mapa zmienionych atrybutów dla operacji modyfikacji.
	 * 
	 * @return mapa, której kluczem jest nazwa atrybutu w LDAP
	 */
	Map<String, List<ILdapAttribute>> getChangesMap();

	/**
	 * Ustawienie mapy zmienionych atrybutów. Zasady ustawiania:
	 * <ul>
	 * <li>dodawanie atrybutu: nowa wartość
	 * {@link ILdapAttribute#setValue(Object)}, stara wartość ustawiona na
	 * {@code null} za pomocą {@link ILdapAttribute#setOldValue(Object)}</li>
	 * <li>zmiana atrybutu: nowa wartość {@link ILdapAttribute#setValue(Object)}
	 * , stara wartość ustawiona za pomocą
	 * {@link ILdapAttribute#setOldValue(Object)}</li>
	 * <li>usunięcie atrybutu: nowa wartość ustawiona na {@code null}
	 * {@link ILdapAttribute#setValue(Object)} , stara wartość ustawiona za
	 * pomocą {@link ILdapAttribute#setOldValue(Object)}</li>
	 * </ul>
	 * 
	 * @param changesMap
	 *            mapa, której kluczem jest nazwa atrybutu w LDAP
	 */
	void setChangesMap(Map<String, List<ILdapAttribute>> changesMap);

}
