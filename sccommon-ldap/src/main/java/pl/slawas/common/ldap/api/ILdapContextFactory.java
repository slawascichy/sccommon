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

import java.util.List;

import javax.naming.NamingException;

import pl.slawas.common.ldap.provider.LdapResult;
import pl.slawas.common.ldap.provider.ProviderOptions;

/**
 * 
 * LdapContextFactory
 *
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 *
 */
public interface ILdapContextFactory {

	ProviderOptions getLdapOptions();

	void close() throws NamingException;

	String getDynamicCtx();

	LdapResult uniqueEntrySearch(String[] attrs, String searchFilter) throws NamingException;

	List<LdapResult> manyEntrySearch(String[] attrs, String searchFilter) throws NamingException;

	/**
	 * Dodawanie nowego entry do LDAP, przed dodaniem zostanie zweryfikowana
	 * unikalność nazwy entry.
	 * 
	 * @param newEntry
	 *            nowe entry
	 * @param entryNameAttr
	 *            nazwa atrybutu z nazwą entry
	 * @throws NamingException
	 */
	void addEntry(ILdapEntry4Changes newEntry, String entryNameAttr) throws NamingException;

	/**
	 * Modyfikacja entry w LDAP - zmiana atrybutów - można zmieniać wszystko z
	 * wyjątkiem nazwy unikalnej
	 * 
	 * @param entry
	 *            zmieniana entry - musi mieć ustawioną niepustą mapę
	 *            {@link ILdapEntry#getChangesMap()}
	 * @param entryNameAttr
	 *            nazwa atrybutu z nazwą entry
	 * @throws NamingException
	 */
	void modifyEntry(ILdapEntry4Changes entry, String entryNameAttr) throws NamingException;

	/**
	 * Usunięcie entry z LDAP
	 * 
	 * @param entryDN
	 *            DN usuwanego entry
	 * @throws NamingException
	 */
	void removeEntry(String entryDN) throws NamingException;

}