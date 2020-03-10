package pl.slawas.common.mail;

import java.io.UnsupportedEncodingException;

import javax.mail.Address;
import javax.mail.internet.AddressException;

import junit.framework.TestCase;
import pl.slawas.common.mail.helpers.MessageHelper;

public class AddressEmailTest extends TestCase {

	public void testAddressEmail() throws AddressException, UnsupportedEncodingException {

		Address address = MessageHelper.prepareRecipientsAddresses("bpmmailtst@office365.umed.pl", "UTF-8");
		System.out.println("-->testAdderessEmail: " + address.toString());

	}

}
