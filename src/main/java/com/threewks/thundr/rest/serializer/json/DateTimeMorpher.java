package com.threewks.thundr.rest.serializer.json;

import net.sf.ezmorph.ObjectMorpher;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

class DateTimeMorpher implements ObjectMorpher {
	private static final DateTimeFormatter DateTimeFormatter = ISODateTimeFormat.dateTimeParser().withOffsetParsed();

	@Override
	public Object morph(Object o) {
		return DateTimeFormatter.parseDateTime(o.toString());
	}

	@Override
	public Class morphsTo() {
		return DateTime.class;
	}

	@Override
	public boolean supports(Class aClass) {
		return aClass.isAssignableFrom(String.class);
	}
}
