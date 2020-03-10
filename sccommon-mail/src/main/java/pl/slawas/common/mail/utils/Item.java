package pl.slawas.common.mail.utils;

import javax.mail.FetchProfile;

public class Item extends FetchProfile.Item {
	public static final Item HEADERS = new Item("HEADERS");

	/** @deprecated */
	public static final Item SIZE = new Item("SIZE");

	public static final Item MESSAGE = new Item("MESSAGE");

	public Item(String name) {
		super(name);
	}

}
