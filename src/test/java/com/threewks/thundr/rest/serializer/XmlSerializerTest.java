/*
 * This file is a component of thundr, a software library from 3wks.
 * Read more: http://www.3wks.com.au/thundr
 * Copyright (C) 2013 3wks, <thundr@3wks.com.au>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.threewks.thundr.rest.serializer;


import com.google.common.collect.Maps;
import com.threewks.thundr.rest.dto.MessageDto;
import com.threewks.thundr.rest.serializer.xml.XmlSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class XmlSerializerTest {

	private static String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<messages><message>hello</message></messages>";
	private XmlSerializer serializer;

	@Before
	public void setup() {
		serializer = new XmlSerializer();
	}

	@Test
	public void testMarshal() {
		String result = serializer.marshal(new MessageDto("hello"));
		assertEquals(xml, result);
	}

	@Test
	public void testMarshalWithRootElementOption() {
		Map<String, String> options = Maps.newHashMap();
		options.put("rootElementName", "root");

		String result = serializer.marshal(new MessageDto("hello"), options);
		assertEquals(xml.replace("messages", "root"), result);
	}

	@Test
	public void testMarshalAndUnmarshalWithDateTimeGetter() {
		DateTime dateTime = ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime("2013-07-10T15:37:58.340+02:00");
		ClassWithDateTimeGetter myObject = new ClassWithDateTimeGetter(dateTime);

		Map<String, String> options = Maps.newHashMap();
		options.put("rootElementName", "root");

		String marshaled = serializer.marshal(myObject, options);
		assertThat(marshaled, is("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root><dateTime>2013-07-10T15:37:58.340+02:00</dateTime></root>"));

		ClassWithDateTimeGetter unmarshaled = serializer.unmarshal(ClassWithDateTimeGetter.class, marshaled);
		assertThat(unmarshaled, equalTo(myObject));
	}

	@Test
	public void testUnmarshal() {
		MessageDto dto = serializer.unmarshal(MessageDto.class, xml);
		assertEquals("hello", dto.message);
	}
}
