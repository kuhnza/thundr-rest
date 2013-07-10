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
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class XmlSerializerTest {

	private static String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><messages><message>hello</message></messages>";
	private XmlSerializer serializer;

	@Before
	public void setup() {
		serializer = new XmlSerializer();
	}

	@Test
	public void testMarshal() {
		String result = serializer.marshall(new MessageDto("hello"));
		assertEquals(xml, result);
	}

	@Test
	public void testMarshalWithRootElementOption() {
		Map<String, String> options = Maps.newHashMap();
		options.put("rootElementName", "root");

		String result = serializer.marshall(new MessageDto("hello"), options);
		assertEquals(xml.replace("messages", "root"), result);
	}

	@Test
	public void testUnmarshall() {
		MessageDto dto = serializer.unmarshall(MessageDto.class, xml);
		assertEquals("hello", dto.message);
	}
}
