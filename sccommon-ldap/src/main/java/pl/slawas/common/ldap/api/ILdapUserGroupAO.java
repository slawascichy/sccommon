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
package pl.slawas.common.ldap.api;

import java.util.List;

import javax.naming.NamingException;

/**
 * 
 * _LdapUserGroupAO
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 * @param <T>
 */
public interface ILdapUserGroupAO<T extends ILdapEntry> extends ILdapAO<T> {

	T loadPrimaryUserGroup(String[] userArgs) throws NamingException;

	List<T> loadUserGroups(String[] userArgs) throws NamingException;
}
