package pl.slawas.common.mail.sql;

import java.sql.SQLException;

public class ResultSetColumn {

	private final String name;

	private final String columnName;

	public ResultSetColumn(AbstractMetaData metadata, int index) throws SQLException {
		columnName = metadata.getColumnName(index);
		name = metadata.getColumnLabel(index);
	}

	public String getName() {
		return name;
	}

	public String getColumnName() {
		return columnName;
	}

}
