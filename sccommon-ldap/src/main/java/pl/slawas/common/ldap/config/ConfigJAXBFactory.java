/*
 * Slawas.pl Copyright &copy; 2007-2012 
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.helpers.DefaultValidationEventHandler;

import org.apache.commons.lang.StringUtils;

import pl.slawas.helpers.ConfigException;
import pl.slawas.helpers.ConfigFactory;

/**
 * 
 * ConfigJAXBFactory fabryka analizy, parsowania konfiguracji z pliku XML Plik
 * został przeniesiony z biblioteki schelper - nie możemy teraz podnieść jego
 * wersji, ale trzeba było poprawić stronę kodową odczytywanej konfiguracji.
 * 
 * @author Slawomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.5 $
 * 
 * @param <Obj>
 *            klasa obiektu konfiguracji
 */
public abstract class ConfigJAXBFactory<Obj> implements ConfigFactory<Obj> {

	/** ścieżka do pliku z konfiguracją */
	private String url;

	/** Klasa obiektu konfiguracji - obiekt pomocniczy */
	private final Class<Obj> persistentClass;

	/**
	 * Klasa pomocnicza pozwalająca na odszukanie pliku konfiguracji w pakiecie jar
	 */
	private Class<?> resourceClass;

	private final boolean readConfigFromJar;

	private final String configString;

	/**
	 * 1 przykład użycia:
	 * 
	 * <pre>
	 * ConfigFactory&lt;IndexerConfig&gt; factory = new IndexerConfigFactory(getClass().getResource(&quot;config.xml&quot;).getFile(),
	 * 		false);
	 * </pre>
	 * 
	 * 2 przykład użycia:
	 * 
	 * <pre>
	 * ConfigFactory&lt;IndexerConfig&gt; factory = new IndexerConfigFactory(&quot;config.xml&quot;, true);
	 * factory.setResourceClass(getClass());
	 * </pre>
	 * 
	 * 
	 * @param url
	 *            ściezka do pliku z konfiguracją
	 * @param readConfigFromJar
	 *            czy plik konfiguracyjny ma być czytany z biblioteki JAR
	 */
	@SuppressWarnings("unchecked")
	protected ConfigJAXBFactory(String url, boolean readConfigFromJar) {
		this.url = url;
		this.persistentClass = (Class<Obj>) ((ParameterizedType) this.getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		this.readConfigFromJar = readConfigFromJar;
		this.configString = null;
	}

	@SuppressWarnings("unchecked")
	protected ConfigJAXBFactory(String configString) {
		this.persistentClass = (Class<Obj>) ((ParameterizedType) this.getClass().getGenericSuperclass())
				.getActualTypeArguments()[0];
		this.url = null;
		this.readConfigFromJar = false;
		this.configString = configString;
	}

	@SuppressWarnings("unchecked")
	public Obj makeIndexConfig() {
		Obj config = null;
		InputStream is = null;
		BufferedReader br = null;
		try {
			JAXBContext ctx = JAXBContext.newInstance(persistentClass);
			Unmarshaller unmarshaller = ctx.createUnmarshaller();
			unmarshaller.setEventHandler(new DefaultValidationEventHandler());
			if (StringUtils.isNotBlank(url)) {
				if (readConfigFromJar) {
					if (resourceClass != null)
						is = resourceClass.getResourceAsStream(url);
					else
						is = persistentClass.getResourceAsStream(url);
					/* dodanie strony kodowej odczytywanej konfiguracji */
					br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
					config = (Obj) unmarshaller.unmarshal(br);
				} else {
					/* dodanie strony kodowej odczytywanej konfiguracji */
					config = (Obj) unmarshaller.unmarshal(new InputStreamReader(new FileInputStream(url), "UTF-8"));
				}
			} else if (StringUtils.isNotBlank(configString)) {
				is = new ByteArrayInputStream(configString.getBytes());
				/* dodanie strony kodowej odczytywanej konfiguracji */
				br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
				config = (Obj) unmarshaller.unmarshal(br);
			} else {
				throw new ConfigException(
						"Configuration's this.url or this.configString must be defined (not null)! See constructors description.");
			}
		} catch (Exception e) {
			throw new ConfigException(e);
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e) {
				// ignore
			}
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// ignore
			}
		}

		return config;
	}

	/**
	 * @return the {@link #resourceClass}
	 */
	public Class<?> getResourceClass() {
		return resourceClass;
	}

	/**
	 * @param resourceClass
	 *            the {@link #resourceClass} to set
	 */
	public void setResourceClass(Class<?> resourceClass) {
		this.resourceClass = resourceClass;
	}

}
