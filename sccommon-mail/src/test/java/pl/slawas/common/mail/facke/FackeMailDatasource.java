package pl.slawas.common.mail.facke;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class FackeMailDatasource implements DataSource {

	private final String testMailFileName;

	public FackeMailDatasource(String testMailFileName) {
		super();
		this.testMailFileName = testMailFileName;
	}

	@Override
	public String getContentType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		File f = new File(testMailFileName);
		return new FileInputStream(f);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

}
