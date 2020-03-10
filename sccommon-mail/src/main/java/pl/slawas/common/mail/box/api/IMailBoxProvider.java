package pl.slawas.common.mail.box.api;

import java.io.Serializable;

import javax.mail.NoSuchProviderException;
import javax.mail.Store;

public interface IMailBoxProvider extends Serializable {

	String getServer();

	String getUsername();

	Store getStore(boolean partialfetch) throws NoSuchProviderException;

}
