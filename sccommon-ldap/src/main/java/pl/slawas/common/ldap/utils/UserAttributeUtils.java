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

package pl.slawas.common.ldap.utils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

import pl.slawas.common.ldap.api.Constants;
import pl.slawas.common.ldap.api.ILdapEntry;
import pl.slawas.common.ldap.api.ILdapUser;
import pl.slawas.common.ldap.api.IUserAttribute;
import pl.slawas.common.ldap.beans.UserAttribute;
import pl.slawas.common.ldap.beans.UserAttributeType;
import pl.slawas.common.ldap.provider.CustomBinaryField;
import pl.slawas.common.ldap.provider.LdapResult;
import pl.slawas.common.ldap.provider.LdapValue;
import pl.slawas.common.ldap.provider.ProviderOptions;
import pl.slawas.common.ldap.provider.beans.UserAttributeDefinition;
import pl.slawas.common.ldap.provider.beans.UserAttributeList;
import pl.slawas.helpers.Strings;
import pl.slawas.twl4j.Logger;

/**
 * UserAttributeUtils - narzędzia do obróbki atrybutów użytkowników
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
public class UserAttributeUtils {

	/**
	 * Parsowanie zapisanych w konfiguracji mapowania wartości do postaci tablicy.
	 * 
	 * @param valueMap
	 *            string z mapowaniem wartości w postaci par wartości odseparowanych
	 *            znakiem ':', a wartości w parze odseparowane znakiem '@' np.
	 *            {@code Y@true:N@false}, gdzie pierwsza wartość w parze to wartość
	 *            używana w LDAP, a druga to wartość przechowywana w TW.
	 * @return mapa par wartości LDAP -> TW
	 */
	public static Hashtable<String, String> valueMapParser(String valueMap) {
		Hashtable<String, String> result = new Hashtable<String, String>();
		String[] valuesPairs = valueMap.split("\\" + ProviderOptions.ATTRIBUTE_VALUE_PAIR_MAP_SEPARATOR);
		for (String valuesPair : valuesPairs) {
			String[] values = valuesPair.split("\\" + ProviderOptions.ATTRIBUTE_VALUE_MAP_SEPARATOR);
			if (values.length > 1) {
				result.put(values[0], values[1]);
			} else {
				result.put(values[0], values[0]);
			}
		}

		return result;
	}

	/**
	 * Przekształcenie dowolnej nazwy atrybutu rezydującego w IBM BPM do postaci
	 * akceptowalnej przez filtry LDAP.
	 * 
	 * @param attrName
	 *            oryginalna nazwa atrybutu
	 * @return zmieniona do nazwy używanej w indeksie metadanych.
	 */
	public static String transform2ColumnName(String attrName) {
		String columnName = attrName.toLowerCase();
		return columnName;
	}

	/**
	 * Przekształcenie dowolnej nazwy atrybutu rezydującego w IBM BPM do postaci
	 * akceptowalnej przez filtry LDAP.
	 * 
	 * @param twAttrName
	 *            oryginalna nazwa atrybutu IBM BPM
	 * @return zmieniona np. z {@code Portal Notification New Task To Me} uzyskujemy
	 *         {@code portalnotificationnewtasktome}
	 */
	public static String getUadColumnName(String twAttrName) {
		String uadColumnName = twAttrName.replace(" ", "");
		uadColumnName = uadColumnName.replaceAll("'", "");
		uadColumnName = uadColumnName.replaceAll("\"", "");
		uadColumnName = uadColumnName.replaceAll("@", "a");
		uadColumnName = Strings.codeUTF82ASCIIpl(uadColumnName);
		uadColumnName = transform2ColumnName(uadColumnName);
		return uadColumnName;
	}

	/**
	 * Tworzenie listy atrybutów dla obiektu na podstawie rezultatu
	 * 
	 * @param lo
	 *            parametry provider'a
	 * @param log
	 *            obiekt logger'a
	 * @param additionalAttrs
	 *            lista dodatkowych atrybutów
	 * @param result
	 *            wynik wyszukiwania
	 * @param single
	 *            obiekt, który jest docelowym odbiorcą utworzonej listy
	 * @param attrSet
	 *            pomocnicza lista wszystkich atrybutów do weryfikacji czy dany
	 *            atrybut został pobrany razem z entry
	 * @return lista atrybutów
	 */
	@SuppressWarnings("rawtypes")
	public static List<IUserAttribute> createAtributeList(final ProviderOptions lo, final Logger log,
			final String[] additionalAttrs, LdapResult result, ILdapEntry single, Set<String> attrSet) {
		// user attributes
		List<IUserAttribute> attributes = new ArrayList<IUserAttribute>();
		// other attributes
		UserAttributeList attrList = lo.getExtendedAttributies();
		if (attrList != null && !attrList.isEmpty()) {
			/* ustawianie atrybutów z konfiguracji */
			Enumeration<String> twAttrs = attrList.getTwAttrNames();
			while (twAttrs.hasMoreElements()) {
				/*
				 * Uwaga! na przeciążenie znaczenia atrybutów LDAP - w rzeczywistości to powinna
				 * być lista atrybutów z BPM'a. https://support.ibpm.pro:8443/browse/BPMINT-79
				 * 
				 * Przymykam oko na błędy w konfiguracji związane z brakiem unikalności pośród
				 * atrybutów BPM.
				 */
				String twAttrName = twAttrs.nextElement();
				UserAttributeDefinition uad = attrList.getUserAttrByTwName(twAttrName);
				String ldapAttrName = uad.getLdapAttrName();
				if (attrSet.contains(ldapAttrName)) {
					/* dodajemy tylko te atrybuty, które były pobrane z entry */
					TypeValuePair attr = readAttr(log, result, ldapAttrName);
					UserAttributeType attrType = attr.getAttrType();
					String attrValue = attr.getAttrValue();
					/* Wartość atrybutu nie jest pusta - START */
					if (UserAttributeDefinition.TW_ATTR_IMAGE.equals(twAttrName)
							&& attrType.equals(UserAttributeType.BINARY)) {
						if (single instanceof ILdapUser) {
							/* atrybut to avatar użytkownika */
							((ILdapUser) single).setAvatarBase64(attrValue);
							byte[] avatarValue = DatatypeConverter.parseBase64Binary(attrValue);
							((ILdapUser) single).setAvatar(avatarValue);
						}
						attrValue = Constants.NULL_STRING;
					}
					if (!Constants.NULL_STRING.equals(attrValue)) {
						if (uad.isValueMapping()) {
							String twValue = uad.getTwValue(attrValue);
							attrValue = (twValue != null ? twValue : attrValue);
							if (twValue == null) {
								log.warn(
										"W LDAP istnieje wartosc atrybutu {} o wartosci {}, "
												+ "lecz nie jest mapowana do wartosci TW. Uzylem wartosci LDAP",
										new Object[] { ldapAttrName, attrValue });
							}
						}
					} else {
						attrValue = StringUtils.EMPTY;
					}
					/* Wartość atrybutu nie jest pusta - KONIEC */
					log.trace("Atrybut BPM: {} = {}", new Object[] { twAttrName, attrValue });
					attributes.add(new UserAttribute(twAttrName, attrValue, attrType));
				}
			}
		}

		if (additionalAttrs != null && additionalAttrs.length != 0) {
			/* ustawianie atrybutów dodatkowych */
			for (String ldapAttrName : additionalAttrs) {
				boolean skip = false;
				if (attrList != null && !attrList.isEmpty()) {
					/*
					 * sprawdzam, czy atrybut o takiej nazwie już istnieje pośród wcześniej
					 * zdefiniowanych atrybutów BPM. Jeżeli tak to już tego nie będziemy nadpisywać
					 * - przynajmniej teraz mamy takie założenie
					 */
					UserAttributeDefinition uad = attrList.getUserAttrByTwName(ldapAttrName);
					/*
					 * ustawiamy flagę pomijania, skoro atrybut już został ustawiony
					 */
					skip = (uad != null);
				}
				if (!skip) {
					/* ustawiamy */
					TypeValuePair attr = readAttr(log, result, ldapAttrName);
					UserAttributeType attrType = attr.getAttrType();
					String attrValue = attr.getAttrValue();
					log.trace("Atrybut dodatkowy: {} = {}", new Object[] { ldapAttrName, attrValue });
					attributes.add(new UserAttribute(ldapAttrName, attrValue, attrType));
				} else if (log.isWarnEnabled()) {
					log.warn("Atrybut dodatkowy pominięty (znajduje się na liście atrybutów BPM): {}",
							new Object[] { ldapAttrName });
				}
			}
		}

		return attributes;
	}

	/**
	 * Czytanie wartości i typu atrybutu zawartego w rezultacie wyszukiwanego entry.
	 * 
	 * @param log
	 *            instancja logger'a
	 * @param result
	 *            rezultat wyszukiwania - pojedyncze entry
	 * @param ldapAttrName
	 *            nazwa atrybutu w LDAP - nazwa atrybutu, którego wartość oraz typ
	 *            mają być pobrane z rezultatu
	 * @return obiekt z typem i wartością
	 */
	private static TypeValuePair readAttr(final Logger log, LdapResult result, String ldapAttrName) {

		/* wartości domyślne już ustawione... */
		UserAttributeType attrType = UserAttributeType.STRING;
		String attrValue = Constants.NULL_STRING;
		List<LdapValue> values = result.get(ldapAttrName);

		if (values != null && !values.isEmpty()) {
			/* lista wartości nie jest pusta */
			if (values.size() == 1) {
				/** pole ma tylko jedną wartość - START */
				attrType = UserAttributeType.getSimpleType(values.get(0).getType());
				Object aValue = values.get(0).getValue();
				if (aValue != null) {
					if (aValue instanceof byte[]) {
						/* obsługa specjalnych pól binarnych */
						CustomBinaryField cbf = CustomBinaryField.getCustomBinaryField(ldapAttrName);
						byte[] binaryObject = (byte[]) aValue;
						if (cbf != null) {
							/*
							 * trzeba przekonwertować pole do odpowiedniego String'a
							 */
							log.warn("Pole typu binarnego: {}={}", new Object[] { ldapAttrName, attrValue });
							attrValue = cbf.value2String(binaryObject);
							attrType = UserAttributeType.CUSTOMBINARY;
						} else {
							String base64 = new String(Base64.encodeBase64(binaryObject));
							attrValue = base64;
							attrType = UserAttributeType.BINARY;
						}
					} else if (aValue instanceof String) {
						attrValue = (String) aValue;
					} else {
						attrValue = aValue.toString();
					}
				}

				/** pole ma tylko jedną wartość - KONIEC */
			} else {
				/** pole ma wiele wartości - START */
				/*
				 * aby to miało jakieś ręce i nogi proponuję przekazać tę wartość w postaci
				 * JSONArray, ale mam problem bo nie chcę zmieniać zależności w projekcie, zatem
				 * muszę kombinować...
				 */
				/*
				 * sprawdzam typ pierwszego elementu, bo pozostałe powinny mieć to samo...
				 */
				attrType = UserAttributeType.getArrayType(values.get(0).getType());
				String[] strVals = new String[values.size()];
				int i = 0;
				for (LdapValue lValue : values) {
					Object aValue = lValue.getValue();
					attrValue = Constants.NULL_STRING;
					if (aValue != null) {
						if (aValue instanceof byte[]) {
							byte[] binaryValue = (byte[]) aValue;
							String base64 = new String(Base64.encodeBase64(binaryValue));
							attrValue = base64;
							attrType = UserAttributeType.BINARYARRAY;
						} else if (aValue instanceof String) {
							attrValue = (String) aValue;
						} else {
							attrValue = aValue.toString();
						}
					}
					strVals[i] = attrValue;
					i++;
				}
				try {
					Writer jsonWriter = new StringWriter();
					JSONSerializer serializer = new JSONSerializer(jsonWriter);
					serializer.write(strVals);
					attrValue = jsonWriter.toString();
				} catch (IOException e) {
					log.error("Błąd podczas serializacji wartości atrybutu wielowartościowego '" + ldapAttrName
							+ "' do JSON", e);
					attrValue = Constants.NULL_STRING;
				}

				/** pole ma wiele wartości - KONIEC */
			}
		}
		return new TypeValuePair(attrType, attrValue);

	}

	/**
	 * 
	 * TypeValuePair - obiekt reprezentujący wartość oraz jej typ.
	 *
	 * @author Sławomir Cichy &lt;slawomir.cichy@ibpm.pro&gt;
	 * @version $Revision: 1.1 $
	 *
	 */
	private static class TypeValuePair {

		/** typ wartości */
		private final UserAttributeType attrType;
		/** wartość */
		private final String attrValue;

		public TypeValuePair(UserAttributeType attrType, String attrValue) {
			super();
			this.attrType = attrType;
			this.attrValue = attrValue;
		}

		/**
		 * @return the {@link #attrType}
		 */
		public UserAttributeType getAttrType() {
			return attrType;
		}

		/**
		 * @return the {@link #attrValue}
		 */
		public String getAttrValue() {
			return attrValue;
		}

	}
}
