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
package pl.slawas.common.mail.sql;

import java.io.Serializable;

import pl.slawas.scjdbc4jpa.jdbc.types.SimpleSQLType;

/**
 * ColumnMetaData
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
class ColumnMetaData implements Serializable {

	private static final long serialVersionUID = -7173592921558041814L;

	private final String catalogName;

	private final String columnClassName;

	private final int columnDisplaySize;

	private final String columnLabel;

	private final String columnName;

	private final SimpleSQLType columnType;

	private final String schemaName;

	private final String tableName;

	/**
	 * 
	 * @param catalogName
	 *            nazwa katalogu
	 * @param schemaName
	 *            nazwa schematu
	 * @param tableName
	 *            nazwa tabeli
	 * @param columnClassName
	 *            nazwa klasy kolumny
	 * @param columnDisplaySize
	 *            rozmiar do prezentacji
	 * @param columnLabel
	 *            etykieta kolumny
	 * @param columnName
	 *            nazwa kolumny
	 * @param columnTypeName
	 *            typ kolumny
	 */
	public ColumnMetaData(String catalogName, String schemaName, String tableName, String columnClassName,
			int columnDisplaySize, String columnLabel, String columnName, SimpleSQLType columnType) {
		super();
		this.catalogName = catalogName;
		this.columnClassName = columnClassName;
		this.columnDisplaySize = columnDisplaySize;
		this.columnLabel = columnLabel;
		this.columnName = columnName;
		this.columnType = columnType;
		this.schemaName = schemaName;
		this.tableName = tableName;
	}

	/**
	 * @return the {@link #catalogName}
	 */
	public String getCatalogName() {
		return catalogName;
	}

	/**
	 * @return the {@link #columnClassName}
	 */
	public String getColumnClassName() {
		return columnClassName;
	}

	/**
	 * @return the {@link #columnDisplaySize}
	 */
	public int getColumnDisplaySize() {
		return columnDisplaySize;
	}

	/**
	 * @return the {@link #columnLabel}
	 */
	public String getColumnLabel() {
		return columnLabel;
	}

	/**
	 * @return the {@link #columnName}
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * @return the {@link #columnType}
	 */
	public SimpleSQLType getColumnType() {
		return columnType;
	}

	/**
	 * @return the {@link #schemaName}
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @return the {@link #tableName}
	 */
	public String getTableName() {
		return tableName;
	}

}
