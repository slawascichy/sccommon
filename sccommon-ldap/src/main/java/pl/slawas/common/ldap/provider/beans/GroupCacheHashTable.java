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

import java.util.Hashtable;
import java.util.Map;

import pl.slawas.common.ldap.api.ILdapUserGroup;

/**
 * GroupCacheHashTable
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class GroupCacheHashTable<G extends ILdapUserGroup> extends Hashtable<String, G> {

	private static final long serialVersionUID = -861847226278043417L;

	public GroupCacheHashTable() {
		super();
	}

	public GroupCacheHashTable(int capacity, float loadFactor) {
		super(capacity, loadFactor);
	}

	public GroupCacheHashTable(int capacity) {
		super(capacity);
	}

	public GroupCacheHashTable(Map<String, G> map) {
		super(map);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#get(java.lang.Object)
	 */
	@Override
	public synchronized G get(Object key) {
		return super.get(((String) key).toUpperCase());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#put(java.lang.Object, java.lang.Object)
	 */
	@Override
	public synchronized G put(String key, G value) {
		return super.put(key.toUpperCase(), value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#containsKey(java.lang.Object)
	 */
	@Override
	public synchronized boolean containsKey(Object key) {
		return super.containsKey(((String) key).toUpperCase());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Hashtable#putAll(java.util.Map)
	 */
	@Override
	public synchronized void putAll(Map<? extends String, ? extends G> map) {
		for (Map.Entry<? extends String, ? extends G> entry : map.entrySet()) {
			String key = entry.getKey();
			G value = entry.getValue();
			this.put(key, value);
		}
	}
}
