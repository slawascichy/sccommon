package pl.slawas.common.mail.sql;

import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ResultSetData implements ResultSet {

	private static final String OPERATION_IMPOSSIBLE = "Operation impossible!";

	private static final String METHOD_NOT_IMPLEMENMTED = "Method not implemenmted!";

	private int internalCouner;

	private ResultSetRow currentRow;

	private final List<ResultSetColumn> columns;

	private final List<ResultSetRow> rows;

	private final AbstractMetaData metadata;

	private boolean closed = false;

	public ResultSetData(AbstractMetaData metadata, List<ResultSetColumn> columns, List<ResultSetRow> rows) {
		super();
		this.columns = columns;
		this.rows = rows;
		this.metadata = metadata;
	}

	public List<ResultSetRow> getRows() {
		return rows;
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return false;
	}

	@Override
	public boolean next() throws SQLException {
		boolean hasNext = internalCouner < rows.size();
		if (hasNext) {
			currentRow = rows.get(internalCouner);
			internalCouner++;
		}
		return hasNext;
	}

	@Override
	public void close() throws SQLException {
		columns.clear();
		rows.clear();
		closed = true;
	}

	@Override
	public boolean wasNull() throws SQLException {
		return false;
	}

	protected String getColumnName(int columnIndex) {
		ResultSetColumn column = columns.get(columnIndex);
		return column.getColumnName();
	}

	protected Object getColumnValue(String columnName) {
		return currentRow.get(columnName);
	}

	protected ResultSetColumn find(String columnLabel) throws SQLException {
		for (ResultSetColumn column : columns) {
			if (column.getColumnName().equals(columnLabel)) {
				return column;
			}
		}
		throw new SQLException(String.format("Column '%s' not found in the result set.", columnLabel));
	}

	@Override
	public String getString(int columnIndex) throws SQLException {
		String columnName = getColumnName(columnIndex);
		return (String) getColumnValue(columnName);
	}

	@Override
	public boolean getBoolean(int columnIndex) throws SQLException {
		String columnLabel = getColumnName(columnIndex);
		return getBoolean(columnLabel);
	}

	@Override
	public byte getByte(int columnIndex) throws SQLException {
		String columnLabel = getColumnName(columnIndex);
		return getByte(columnLabel);
	}

	@Override
	public short getShort(int columnIndex) throws SQLException {
		String columnLabel = getColumnName(columnIndex);
		return getShort(columnLabel);
	}

	@Override
	public int getInt(int columnIndex) throws SQLException {
		String columnLabel = getColumnName(columnIndex);
		return getInt(columnLabel);
	}

	@Override
	public long getLong(int columnIndex) throws SQLException {
		String columnLabel = getColumnName(columnIndex);
		return getLong(columnLabel);
	}

	@Override
	public float getFloat(int columnIndex) throws SQLException {
		String columnLabel = getColumnName(columnIndex);
		return getFloat(columnLabel);
	}

	@Override
	public double getDouble(int columnIndex) throws SQLException {
		String columnLabel = getColumnName(columnIndex);
		return getDouble(columnLabel);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		String columnName = getColumnName(columnIndex);
		return (BigDecimal) getColumnValue(columnName);
	}

	@Override
	public byte[] getBytes(int columnIndex) throws SQLException {
		String columnName = getColumnName(columnIndex);
		return getBytes(columnName);
	}

	@Override
	public Date getDate(int columnIndex) throws SQLException {
		String columnName = getColumnName(columnIndex);
		return (Date) getColumnValue(columnName);
	}

	@Override
	public Time getTime(int columnIndex) throws SQLException {
		String columnName = getColumnName(columnIndex);
		return (Time) getColumnValue(columnName);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		String columnName = getColumnName(columnIndex);
		return (Timestamp) getColumnValue(columnName);
	}

	@Override
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public String getString(String columnLabel) throws SQLException {
		return (String) getColumnValue(columnLabel);
	}

	@Override
	public boolean getBoolean(String columnLabel) throws SQLException {
		Boolean colValue = (Boolean) getColumnValue(columnLabel);
		if (colValue != null) {
			return colValue.booleanValue();
		}
		return false;
	}

	@Override
	public byte getByte(String columnLabel) throws SQLException {
		Byte colValue = (Byte) getColumnValue(columnLabel);
		if (colValue != null) {
			return colValue.byteValue();
		}
		return 0;
	}

	@Override
	public short getShort(String columnLabel) throws SQLException {
		Short colValue = (Short) getColumnValue(columnLabel);
		if (colValue != null) {
			return colValue.shortValue();
		}
		return 0;
	}

	@Override
	public int getInt(String columnLabel) throws SQLException {
		Integer colValue = (Integer) getColumnValue(columnLabel);
		if (colValue != null) {
			return colValue.intValue();
		}
		return 0;
	}

	@Override
	public long getLong(String columnLabel) throws SQLException {
		Long colValue = (Long) getColumnValue(columnLabel);
		if (colValue != null) {
			return colValue.longValue();
		}
		return 0;
	}

	@Override
	public float getFloat(String columnLabel) throws SQLException {
		Float colValue = (Float) getColumnValue(columnLabel);
		if (colValue != null) {
			return colValue.floatValue();
		}
		return 0;
	}

	@Override
	public double getDouble(String columnLabel) throws SQLException {
		Double colValue = (Double) getColumnValue(columnLabel);
		if (colValue != null) {
			return colValue.doubleValue();
		}
		return 0;
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
		BigDecimal colValue = (BigDecimal) getColumnValue(columnLabel);
		if (colValue != null) {
			return new BigDecimal(colValue.toBigInteger(), scale);
		}
		return null;
	}

	@Override
	public byte[] getBytes(String columnLabel) throws SQLException {
		Object val = getColumnValue(columnLabel);
		String charsetName = currentRow.getCharsetName();
		if (val instanceof String) {
			try {
				return ((String) val).getBytes(charsetName);
			} catch (UnsupportedEncodingException e) {
				throw new SQLException(e);
			}
		}
		return (byte[]) val;
	}

	@Override
	public Date getDate(String columnLabel) throws SQLException {
		return (Date) getColumnValue(columnLabel);
	}

	@Override
	public Time getTime(String columnLabel) throws SQLException {
		return (Time) getColumnValue(columnLabel);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel) throws SQLException {
		return (Timestamp) getColumnValue(columnLabel);
	}

	@Override
	public InputStream getAsciiStream(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public InputStream getUnicodeStream(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public InputStream getBinaryStream(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public SQLWarning getWarnings() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void clearWarnings() throws SQLException {
		/* nie implementujemy */
	}

	@Override
	public String getCursorName() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public ResultSetMetaData getMetaData() throws SQLException {
		return metadata;
	}

	@Override
	public Object getObject(int columnIndex) throws SQLException {
		String columnName = getColumnName(columnIndex);
		return getColumnValue(columnName);
	}

	@Override
	public Object getObject(String columnLabel) throws SQLException {
		return getColumnValue(columnLabel);
	}

	@Override
	public int findColumn(String columnLabel) throws SQLException {
		int i = 0;
		for (ResultSetColumn column : columns) {
			if (column.getColumnName().equals(columnLabel)) {
				return i;
			}
			i++;
		}
		throw new SQLException(String.format("Column '%s' not found in the result set.", columnLabel));
	}

	@Override
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Reader getCharacterStream(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		String columnName = getColumnName(columnIndex);
		return (BigDecimal) getColumnValue(columnName);
	}

	@Override
	public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
		return (BigDecimal) getColumnValue(columnLabel);
	}

	@Override
	public boolean isBeforeFirst() throws SQLException {
		throw new IllegalAccessError(OPERATION_IMPOSSIBLE);
	}

	@Override
	public boolean isAfterLast() throws SQLException {
		throw new IllegalAccessError(OPERATION_IMPOSSIBLE);
	}

	@Override
	public boolean isFirst() throws SQLException {
		return internalCouner == 0;
	}

	@Override
	public boolean isLast() throws SQLException {
		return (internalCouner + 1) == rows.size();
	}

	@Override
	public void beforeFirst() throws SQLException {
		throw new IllegalAccessError(OPERATION_IMPOSSIBLE);
	}

	@Override
	public void afterLast() throws SQLException {
		throw new IllegalAccessError(OPERATION_IMPOSSIBLE);
	}

	@Override
	public boolean first() throws SQLException {
		internalCouner = 0;
		currentRow = rows.get(internalCouner);
		return true;
	}

	@Override
	public boolean last() throws SQLException {
		internalCouner = rows.size() - 1;
		currentRow = rows.get(internalCouner);
		return true;
	}

	@Override
	public int getRow() throws SQLException {
		return internalCouner;
	}

	@Override
	public boolean absolute(int row) throws SQLException {
		if (row < 0) {
			return false;
		}
		boolean absolute = row < rows.size();
		if (absolute) {
			internalCouner = row;
			currentRow = rows.get(internalCouner);
		}
		return absolute;
	}

	@Override
	public boolean relative(int rowsIn) throws SQLException {
		int newPosition = internalCouner - rowsIn;
		return absolute(newPosition);
	}

	@Override
	public boolean previous() throws SQLException {
		boolean previous = internalCouner > 0;
		if (previous) {
			internalCouner--;
			currentRow = rows.get(internalCouner);
		}
		return previous;
	}

	@Override
	public void setFetchDirection(int direction) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public int getFetchDirection() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void setFetchSize(int rows) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public int getFetchSize() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public int getType() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public int getConcurrency() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public boolean rowUpdated() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public boolean rowInserted() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public boolean rowDeleted() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNull(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateByte(int columnIndex, byte x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateShort(int columnIndex, short x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateInt(int columnIndex, int x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateLong(int columnIndex, long x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateFloat(int columnIndex, float x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateDouble(int columnIndex, double x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateString(int columnIndex, String x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateDate(int columnIndex, Date x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateTime(int columnIndex, Time x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateObject(int columnIndex, Object x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNull(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBoolean(String columnLabel, boolean x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateByte(String columnLabel, byte x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateShort(String columnLabel, short x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateInt(String columnLabel, int x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateLong(String columnLabel, long x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateFloat(String columnLabel, float x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateDouble(String columnLabel, double x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateString(String columnLabel, String x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBytes(String columnLabel, byte[] x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateDate(String columnLabel, Date x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateTime(String columnLabel, Time x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateObject(String columnLabel, Object x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void insertRow() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateRow() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void deleteRow() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void refreshRow() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void cancelRowUpdates() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void moveToInsertRow() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void moveToCurrentRow() throws SQLException {
		/* nie implementujemy, zawsze jesteśmy w aktualnym wierszu */
	}

	@Override
	public Statement getStatement() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Ref getRef(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Blob getBlob(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Clob getClob(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Array getArray(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Ref getRef(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Blob getBlob(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Clob getClob(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Array getArray(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Date getDate(String columnLabel, Calendar cal) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Time getTime(String columnLabel, Calendar cal) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public URL getURL(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public URL getURL(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateRef(String columnLabel, Ref x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBlob(String columnLabel, Blob x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateClob(String columnLabel, Clob x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateArray(int columnIndex, Array x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateArray(String columnLabel, Array x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public RowId getRowId(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public RowId getRowId(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public int getHoldability() throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public boolean isClosed() throws SQLException {
		return closed;
	}

	@Override
	public void updateNString(int columnIndex, String nString) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNString(String columnLabel, String nString) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public NClob getNClob(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public NClob getNClob(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public String getNString(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED + ". Please use getString(int) method.");
	}

	@Override
	public String getNString(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED + ". Please use getString(String) method.");
	}

	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateClob(String columnLabel, Reader reader) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public void updateNClob(String columnLabel, Reader reader) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);

	}

	@Override
	public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

	@Override
	public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
		throw new IllegalAccessError(METHOD_NOT_IMPLEMENMTED);
	}

}
