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

import pl.slawas.common.ldap.api.Constants;
import pl.slawas.common.ldap.api.ILdapAO;
import pl.slawas.common.ldap.api.ILdapUserGroupAO;
import pl.slawas.common.ldap.beans.LdapUser;
import pl.slawas.common.ldap.beans.LdapUserGroup;
import pl.slawas.common.ldap.dao.LdapUserAO;
import pl.slawas.common.ldap.dao.LdapUserGroupAO;

/**
 * LdapObjectFactoryBean implementacja obiektu, będącego wsparciem dla dostępu
 * do obiektów AO.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class LdapObjectFactoryBean extends LdapObjectFactorySupport<LdapUser, LdapUserGroup> {

	private static LdapObjectFactoryBean instance = new LdapObjectFactoryBean();

	/**
	 * @return the {@link #instance}
	 */
	public static LdapObjectFactorySupport<LdapUser, LdapUserGroup> getInstance() {
		return instance;
	}

	/* Overridden (non-Javadoc) */
	@Override
	public ILdapAO<LdapUser> getNewInstanceUserAO(ProviderOptions ldapOptions, String organizationalUnit) {
		return new LdapUserAO(ldapOptions, organizationalUnit, loadAllUserGroupAOs(ldapOptions));
	}

	@Override
	public ILdapUserGroupAO<LdapUserGroup> getNewInstanceUserGroupAO(ProviderOptions ldapOptions,
			String organizationalUnit) {
		return new LdapUserGroupAO(ldapOptions, organizationalUnit);
	}

	public String getManagerGroupSuffix() {
		return Constants.DEFAULT_MANAGER_PRIMARY_GROUP_SUFFIX;
	}

	public String getTeamGroupSuffix() {
		return Constants.DEFAULT_WORKERS_PRIMARY_GROUP_SUFFIX;
	}

}
