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
 * _LdapUserAO
 *
 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
 * @version $Revision: 1.1 $
 *
 * @param <T>
 *            reprezentacja obiektu entry użytkownika
 * @param <G>
 *            reprezentacja obiektu entry grupy
 */
public interface ILdapUserAO<T extends ILdapUser<G>, G extends ILdapUserGroup> extends ILdapAO<T> {

	/**
	 * Lista obiektów dostępu do grup użytkowników, z którymi są związane dane
	 * użytkowników.
	 * 
	 * @return lista obiektów.
	 */
	List<ILdapUserGroupAO<G>> getGroupLdapAO();

	/**
	 * Zmiana hasła w entry użytkownika
	 * 
	 * @param user
	 *            entry użytkownika
	 * @param passwordAttrName
	 *            nazwa atrybutu w którym przechowywane jest hasło
	 * @param newPassword
	 *            nowa wartość hasła (zakodowana) - metoda kodowania hasła należy od
	 *            mechanizmów wywołujących daną metodę
	 * @param oldPassword
	 *            stara wartość hasła (zakodowana) - metoda kodowania hasła należy
	 *            od mechanizmów wywołujących daną metodę
	 * @throws NamingException
	 */
	void changePassword(T user, String passwordAttrName, String newPassword, String oldPassword) throws NamingException;

	/**
	 * Zmiana hasła w entry użytkownika
	 * 
	 * @param userName
	 *            entry użytkownika
	 * @param passwordAttrName
	 *            nazwa atrybutu w którym przechowywane jest hasło
	 * @param newPassword
	 *            nowa wartość hasła (zakodowana) - metoda kodowania hasła należy od
	 *            mechanizmów wywołujących daną metodę
	 * @param oldPassword
	 *            stara wartość hasła (zakodowana) - metoda kodowania hasła należy
	 *            od mechanizmów wywołujących daną metodę
	 * @throws NamingException
	 */
	void changePassword(T user, String passwordAttrName, byte[] newPassword, byte[] oldPassword) throws NamingException;

}
