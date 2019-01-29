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

package pl.slawas.common.ldap.provider.beans;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * UserAttributeDefinition - reprezentacja definicji atrybutu. W konfiguracji
 * parametrowi {@link ProviderOptions#extendedAttributies} przypisywany jest
 * łańcuch znakowy z listą dodatkowych atrybutów użytkownika przechowywanych w
 * LDAP mapowanych na atrybuty użytkownika IBM BPM. Poszczególne sekcje
 * mapowania odseparowane są znakiem zdefiniowanym w
 * {@link ProviderOptions#ATTRIBUTE_SEPARATOR}. Przykłady opracowane są dla
 * znaku przecinka (','). Ma on następującą postać:
 * <p>
 * {@code
 * ldapAttr1[|twAttr1[|ldapAttr1Value1@twAttr1Value1:[ldapAttr1Value2@twAttr1Value2
 * ]:...]],}
 * {@code
 * ldapAttr2[|twAttr2[|ldapAttr1Value2@twAttr1Value2:[ldapAttr1Value2@twAttr2Value2
 * ]:...]],}
 * </p>
 * 
 * gdzie (w powyższym przykładzie) poszczególne wartości oznaczają:
 * <ul>
 * <li><u>ldapAttr<i>X</i></u> - nazwa atrybutu w LDAP np. {@code IsChief}.</li>
 * <li><u>twAttr<i>X</i></u> - (opcjonalne, wymagane gdy podane jest mapowanie
 * wartości) jeżeli <b>nazwa atrybutu w systemie IBM BPM</b> odpowiadająca
 * atrybutowi w LDAP jest inna dodajemy nazwę tego atrybutu tak aby było możliwe
 * zrobienie mapowania wartości atrybutów. Przykład: nazwa atrybutu w IBM BPM,
 * dla atrybutu {@code IsChief} w LDAP, ma nazwę {@code isChief} (różnica
 * wielkich i małych liter).</li>
 * <li><u>ldapAttr<i>X</i>Value<i>X1</i>@twAttr<i>X</i>Value<i>X1</i></u> -
 * (opcjonalne) może się zdarzyć, że również wartości odpowiadające temu samemu
 * atrybutowi po jednej i drugiej stronie są różne. Można dodać mapowanie
 * wartości. Idealnym przykładem takiego przypadku może być atrybut typu
 * {@link Boolean} - wtedy dodajemy mapowanie: {@code Y@true:N@false}, które
 * oznacza: wartość {@code Y} atrybutu w LDAP przekształć do wartości
 * {@code true} w IBM BPM; wartość {@code N} atrybutu w LDAP przekształć do
 * wartości {@code false} w IBM BPM;</li>
 * </ul>
 * <p>
 * Pełen przykład uwzględniający wszystkie przypadki:<br/>
 * 
 * <pre>
 * IsChief|isChief|Y@true:N@false,mailAlternateAddress|userPrincipalName,mobile,HRid|hrid,businessCategory,physicalDeliveryOfficeName
 * </pre>
 * 
 * Opis znaczenia przykładu:
 * <ul>
 * <li>atrybut o nazwie {@code IsChief} w LDAP mapowany do atrybutu o nazwie
 * {@code isChief} w IBM BPM. Dodatkowo trzeba przeprowadzić mapowanie wartości:
 * wartość {@code Y} atrybutu w LDAP przekształć do wartości {@code true} w IBM
 * BPM; wartość {@code N} atrybutu w LDAP przekształć do wartości {@code false}
 * w IBM BPM.</li>
 * <li>atrybut o nazwie {@code mailAlternateAddress} w LDAP mapowany do atrybutu
 * o nazwie {@code userPrincipalName} w IBM BPM.</li>
 * <li>atrybut o nazwie {@code mobile} w LDAP i w IBM BPM ma taką samą nazwę.</li>
 * <li>atrybut o nazwie {@code HRid} w LDAP mapowany do atrybutu o nazwie
 * {@code hrid} w IBM BPM.</li>
 * <li>atrybut o nazwie {@code businessCategory} w LDAP i w IBM BPM ma taką samą
 * nazwę.</li>
 * <li>atrybut o nazwie {@code physicalDeliveryOfficeName} w LDAP i w IBM BPM ma
 * taką samą nazwę.</li>
 * </ul>
 * 
 * </p>
 * 
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class UserAttributeDefinition implements Serializable {

	private static final long serialVersionUID = -529269999543017581L;

	/**
	 * BPM'owa (teamworks'owa) nazwa atrybutu użytkownika, w którym
	 * przechowywany jest jego avatar. Jest on przechowywany w osobnej tablicy o
	 * nazwie BPM_USR_AVATAR.
	 */
	public static final String TW_ATTR_IMAGE = "Image";

	/**
	 * Nazwa atrybutu użytkownika zdefiniowana w IBM BPM
	 */
	private String twAttrName;

	/**
	 * Odpowiadająca {@link #twAttrName} nazwa atrybutu użytkownika zdefiniowana
	 * w LDAP
	 */
	private String ldapAttrName;

	/**
	 * Mapa wartości. Klucz jest wartością jaka jest przechowywania w LDAP,
	 * elementem jest wartość jaka jest przechowywana w IBM BPM. Pole stworzone
	 * dla kompatybilności wstecz,
	 * <p>
	 * np. Kiedyś w corpdir (Open LDAP) atrybut isPROM przyjmował wartości
	 * ['Y'|'N'] i tak został też zbudowany atrybut w TW. W AD atrybut ma inną
	 * nazwę (stąd mapowanie atrybutów), ale również i wartości zmieniły format
	 * na ['true'|'false']
	 * </p>
	 */
	private Hashtable<String, String> valueMapLDAP2TW;

	/**
	 * Mapa wartości. Klucz jest wartością jaka jest przechowywania w IBM BPM,
	 * elementem jest wartość jaka jest przechowywana w LDAP. Pole stworzone dla
	 * kompatybilności wstecz,
	 * <p>
	 * np. Kiedyś w corpdir (Open LDAP) atrybut isPROM przyjmował wartości
	 * ['Y'|'N'] i tak został też zbudowany atrybut w IBM BPM. W AD atrybut ma
	 * inną nazwę (stąd mapowanie atrybutów), ale również i wartości zmieniły
	 * format na ['true'|'false']
	 * </p>
	 */
	private Hashtable<String, String> valueMapTW2LDAP;

	/**
	 * Pobieranie wartości występującej w TW odpowiadającej wartości w LDAP
	 * 
	 * @param ldapValue
	 *            wartość występująca w LDAP
	 * @return wartość występująca w IBM BPM
	 */
	public String getTwValue(String ldapValue) {
		return valueMapLDAP2TW.get(ldapValue);
	}

	/**
	 * Pobieranie wartości występującej w LDAP odpowiadającej wartości w IBM BPM
	 * 
	 * @param twValue
	 *            wartość występująca w IBM BPM
	 * @return wartość występująca w LDAP
	 */
	public String getLdapValue(String twValue) {
		return valueMapTW2LDAP.get(twValue);
	}

	/**
	 * Dodanie mapowania wartości
	 * 
	 * @param ldapValue
	 *            wartość występująca w LDAP
	 * @param twValue
	 *            wartość występująca w IBM BPM
	 */
	public void addValueMap(String ldapValue, String twValue) {
		if (this.valueMapLDAP2TW == null) {
			this.valueMapLDAP2TW = new Hashtable<>();
		}
		if (this.valueMapTW2LDAP == null) {
			this.valueMapTW2LDAP = new Hashtable<>();
		}
		this.valueMapLDAP2TW.put(ldapValue, twValue);
		this.valueMapTW2LDAP.put(twValue, ldapValue);
	}

	/**
	 * Informacja czy występuje mapowanie wartości atrybutów dla danej definicji
	 * atrybutu.
	 * 
	 * @return {@code true} - tak występuje mapowanie wartości; {@code false} -
	 *         nie występuje mapowanie wartości
	 */
	public boolean isValueMapping() {
		return (this.valueMapLDAP2TW != null);
	}

	public String getTwAttrName() {
		return twAttrName;
	}

	public void setTwAttrName(String twAttrName) {
		this.twAttrName = twAttrName;
	}

	public String getLdapAttrName() {
		return ldapAttrName;
	}

	/**
	 * Pobranie wartości LDAP odpowiadających danemu atrybutowi
	 * 
	 * @return wartości LDAP
	 */
	public void setLdapAttrName(String ldapAttrName) {
		this.ldapAttrName = ldapAttrName;
	}

	/**
	 * Pobranie wartości IBM BPM odpowiadających danemu atrybutowi
	 * 
	 * @return wartości IBM BPM
	 */
	public Enumeration<String> getTwValues() {
		return this.valueMapTW2LDAP.keys();
	}

	public Enumeration<String> getLdapValues() {
		return this.valueMapLDAP2TW.keys();
	}

	@Override
	public String toString() {
		return "UserAttributeDefinition [ldapAttrName=" + ldapAttrName
				+ ", twAttrName=" + twAttrName + ", valueMapLDAP2TW="
				+ valueMapLDAP2TW + ", valueMapTW2LDAP=" + valueMapTW2LDAP
				+ "]";
	}
}
