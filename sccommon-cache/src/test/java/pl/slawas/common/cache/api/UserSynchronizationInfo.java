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

package pl.slawas.common.cache.api;

import java.io.Serializable;

/**
 * UserSynchronizationInfo - obiekt danych o ostatniej synchronizacji
 * użytkownika pomiędzy LDAP a IBM BPM.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class UserSynchronizationInfo implements Serializable {

	private static final long serialVersionUID = 6249310504460917282L;

	/** nazwa użytkownika (login) */
	private final String userName;

	/** czas ostatniej aktualizacji w milisekundach */
	private Long lastTimeUpdate;

	/** DN użytkownika w LDAP */
	private String userDn;

	/**
	 * @param userName
	 *            nazwa użytkownika
	 */
	public UserSynchronizationInfo(String userName) {
		super();
		this.userName = userName;
	}

	/**
	 * @return the {@link #lastTimeUpdate}
	 */
	public Long getLastTimeUpdate() {
		return lastTimeUpdate;
	}

	/**
	 * @param lastTimeUpdate
	 *            the {@link #lastTimeUpdate} to set
	 */
	public void setLastTimeUpdate(Long lastTimeUpdate) {
		this.lastTimeUpdate = lastTimeUpdate;
	}

	/**
	 * @return the {@link #userName}
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the {@link #userDn}
	 */
	public String getUserDn() {
		return userDn;
	}

	/**
	 * @param userDn
	 *            the {@link #userDn} to set
	 */
	public void setUserDn(String userDn) {
		this.userDn = userDn;
	}

}
