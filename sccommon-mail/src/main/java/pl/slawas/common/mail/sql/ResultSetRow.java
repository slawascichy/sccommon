package pl.slawas.common.mail.sql;

import java.util.HashMap;

import org.apache.commons.lang.StringUtils;

import pl.slawas.common.mail.helpers.MessageHelper;

public class ResultSetRow extends HashMap<String, Object> {

	private static final long serialVersionUID = 6377094935115674714L;

	private final String charsetName;

	public ResultSetRow(String charsetName) {
		super();
		this.charsetName = charsetName;
	}

	public String getCharsetName() {
		return StringUtils.isNotBlank(charsetName) ? charsetName : MessageHelper.DEFAULT_CHARSET;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((charsetName == null) ? 0 : charsetName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ResultSetRow other = (ResultSetRow) obj;
		if (charsetName == null) {
			if (other.charsetName != null) {
				return false;
			}
		} else if (!charsetName.equals(other.charsetName)) {
			return false;
		}
		return true;
	}

}
