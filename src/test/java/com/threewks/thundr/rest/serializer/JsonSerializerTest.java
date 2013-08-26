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
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class JsonSerializerTest {

	private static String json = "{\"message\":\"hello\"}";
	private static String jsonp = "test(" + json + ");";

	private JsonSerializer serializer;

	@Before
	public void setup() {
		serializer = new JsonSerializer();
	}

	@Test
	public void testMarshal() {
		String result = serializer.marshal(new MessageDto("hello"));
		assertThat(result, is(json));
	}

	@Test
	public void testMarshalAndUnmarshalWithDateTimeGetter() {
		ClassWithDateTimeGetter myObject = new ClassWithDateTimeGetter(new DateTime("2013-07-10T15:37:58.340+10:00"));

		String marshaled = serializer.marshal(myObject);
		assertThat(marshaled, is("{\"dateTime\":\"2013-07-10T15:37:58.340+10:00\"}"));

		ClassWithDateTimeGetter unmarshaled = serializer.unmarshal(ClassWithDateTimeGetter.class, marshaled);
		assertThat(unmarshaled, is(myObject));
	}

		@Test
	public void testMarshalWithEmptyOption() {
		Map<String, String> options = Maps.newHashMap();

		String result = serializer.marshal(new MessageDto("hello"), options);
		assertThat(result, is(json));
	}

	@Test
	public void testMarshalWithCallbackOption() {
		Map<String, String> options = Maps.newHashMap();
		options.put("callback", "test");

		String result = serializer.marshal(new MessageDto("hello"), options);
		assertThat(result, is(jsonp));
	}

	@Test
	public void testUnmarshal() {
		MessageDto message = serializer.unmarshal(MessageDto.class, json);
		assertThat(message.message, is("hello"));
	}
}
