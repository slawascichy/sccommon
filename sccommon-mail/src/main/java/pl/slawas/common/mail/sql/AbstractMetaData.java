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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import pl.slawas.scjdbc4jpa.jdbc.types.SimpleSQLType;

/**
 * AbstractMetaData - klasa abstrakcyjna reprezentująca metadane symulowanego
 * wyniku SQL.
 * 
 * @author Sławomir Cichy &lt;slawas@slawas.pl&gt;
 * @version $Revision: 1.1 $
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractMetaData implements ResultSetMetaData, Serializable {

	public static final int SMALL_SIZE = 256;

	public static final int BIG_SIZE = 1024;

	public static final SimpleSQLType STRING_DB_TYPE = SimpleSQLType.STRING;

	public static final String STRING_CLASS = String.class.getName();

	protected final List<ColumnMetaData> columns;

	/**
	 * Podstawowy konstruktor metadanych
	 * 
	 * @param columnCount
	 *            deklarowana liczba kolumn
	 */
	AbstractMetaData(int columnCount) {
		columns = new ArrayList<>(columnCount);
	}

	/**
	 * Dodawanie kolejnych metadanych pojedynczej kolumny.
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
	protected void addColumnMataData(String catalogName, String schemaName, String tableName, String columnClassName,
			int columnDisplaySize, String columnLabel, String columnName, SimpleSQLType columnType) {
		columns.add(new ColumnMetaData(catalogName, schemaName, tableName, columnClassName, columnDisplaySize,
				columnLabel, columnName, columnType));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getColumnCount()
	 */
	@Override
	public int getColumnCount() throws SQLException {
		return this.columns.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#isAutoIncrement(int)
	 */
	@Override
	public boolean isAutoIncrement(int column) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#isCaseSensitive(int)
	 */
	@Override
	public boolean isCaseSensitive(int column) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#isSearchable(int)
	 */
	@Override
	public boolean isSearchable(int column) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#isCurrency(int)
	 */
	@Override
	public boolean isCurrency(int column) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#isNullable(int)
	 */
	@Override
	public int isNullable(int column) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#isSigned(int)
	 */
	@Override
	public boolean isSigned(int column) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getColumnDisplaySize(int)
	 */
	@Override
	public int getColumnDisplaySize(int column) throws SQLException {
		return this.columns.get(column).getColumnDisplaySize();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getColumnLabel(int)
	 */
	@Override
	public String getColumnLabel(int column) throws SQLException {
		return this.columns.get(column).getColumnLabel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) throws SQLException {
		return this.columns.get(column).getColumnName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getSchemaName(int)
	 */
	@Override
	public String getSchemaName(int column) throws SQLException {
		return this.columns.get(column).getSchemaName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getPrecision(int)
	 */
	@Override
	public int getPrecision(int column) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getScale(int)
	 */
	@Override
	public int getScale(int column) throws SQLException {
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getTableName(int)
	 */
	@Override
	public String getTableName(int column) throws SQLException {
		return this.columns.get(column).getTableName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getCatalogName(int)
	 */
	@Override
	public String getCatalogName(int column) throws SQLException {
		return this.columns.get(column).getCatalogName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getColumnType(int)
	 */
	@Override
	public int getColumnType(int column) throws SQLException {
		return this.columns.get(column).getColumnType().getSqlType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getColumnTypeName(int)
	 */
	@Override
	public String getColumnTypeName(int column) throws SQLException {
		return this.columns.get(column).getColumnType().name();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#isReadOnly(int)
	 */
	@Override
	public boolean isReadOnly(int column) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#isWritable(int)
	 */
	@Override
	public boolean isWritable(int column) throws SQLException {
		return !isReadOnly(column);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#isDefinitelyWritable(int)
	 */
	@Override
	public boolean isDefinitelyWritable(int column) throws SQLException {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.sql.ResultSetMetaData#getColumnClassName(int)
	 */
	@Override
	public String getColumnClassName(int column) throws SQLException {
		return this.columns.get(column).getColumnClassName();
	}

}
