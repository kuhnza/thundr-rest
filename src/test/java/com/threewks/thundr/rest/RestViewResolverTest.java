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
package com.threewks.thundr.rest;


import com.threewks.thundr.rest.dto.MessageDto;
import com.threewks.thundr.rest.exception.NotAcceptableException;
import com.threewks.thundr.rest.serializer.JsonSerializer;
import com.threewks.thundr.rest.serializer.Serializer;
import com.threewks.thundr.rest.serializer.XmlSerializer;
import com.threewks.thundr.test.mock.servlet.MockHttpServletRequest;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;
import jodd.util.MimeTypes;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class RestViewResolverTest {
	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private RestViewResolver viewResolver;

	@Before
	public void setup() {
		viewResolver = new RestViewResolver();
		viewResolver.addSerializer(MimeTypes.MIME_APPLICATION_JSON, new JsonSerializer());
		viewResolver.addSerializer(MimeTypes.MIME_APPLICATION_XML, new XmlSerializer());
	}

	@Test
	public void testDefaultConstructor() {
		viewResolver = new RestViewResolver();
		assertThat(viewResolver.getDefaultContentType(), is(MimeTypes.MIME_APPLICATION_JSON));
	}

	@Test
	public void testConstructorWithCustomDefaultMimeType() {
		viewResolver = new RestViewResolver(MimeTypes.MIME_APPLICATION_XML);
		assertThat(viewResolver.getDefaultContentType(), is(MimeTypes.MIME_APPLICATION_XML));
	}

	@Test
	public void testAddSerializer() {
		RestViewResolver viewResolver = new RestViewResolver();
		assertThat(viewResolver.getSerializers().size(), is(0));

		viewResolver.addSerializer(MimeTypes.MIME_APPLICATION_JSON, new JsonSerializer());
		assertThat(viewResolver.canSerialize(MimeTypes.MIME_APPLICATION_JSON), is(true));
		assertThat(viewResolver.getSerializers().size(), is(1));
	}

	@Test
	public void testDetermineResponseContentTypeWithValidFormatParameter() {
		HttpServletRequest request = new MockHttpServletRequest().method("GET").parameter("format", "xml");
		String contentType = viewResolver.determineResponseContentType(request);
		assertThat(contentType, is(MimeTypes.MIME_APPLICATION_XML));

		request = new MockHttpServletRequest().method("GET").parameter("format", "json");
		contentType = viewResolver.determineResponseContentType(request);
		assertThat(contentType, is(MimeTypes.MIME_APPLICATION_JSON));
	}

	@Test
	public void testDetermineResponseContentTypeWithJsonpCallbackParameter() {
		HttpServletRequest request = new MockHttpServletRequest()
				.method("GET")
				.parameter("format", "json")
				.parameter("callback", "test");
		String contentType = viewResolver.determineResponseContentType(request);
		assertThat(contentType, is(MimeTypes.MIME_APPLICATION_JAVASCRIPT));
	}

	@Test
	public void testDetermineResponseContentTypeWithBogusFormatParameter() {
		HttpServletRequest request = new MockHttpServletRequest().method("GET").parameter("format", "bogus");
		String contentType = viewResolver.determineResponseContentType(request);
		assertThat(contentType, is("bogus"));
	}

	@Test
	public void testDetermineResponseContentTypeWithNoFormatParameter() {
		HttpServletRequest request = new MockHttpServletRequest().method("GET");
		String contentType = viewResolver.determineResponseContentType(request);
		assertThat(contentType, is(viewResolver.getDefaultContentType()));
	}

	@Test
	public void testDetermineResponseContentTypeWithAcceptHeader() {
		HttpServletRequest request = new MockHttpServletRequest()
				.method("GET").header("Accept", MimeTypes.MIME_APPLICATION_XML);
		String contentType = viewResolver.determineResponseContentType(request);
		assertThat(contentType, is(MimeTypes.MIME_APPLICATION_XML));
	}

	@Test
	public void testDetermineResponseContentTypeWithWildcardAcceptHeader() {
		HttpServletRequest request = new MockHttpServletRequest()
				.method("GET").header("Accept", "*/*");
		String contentType = viewResolver.determineResponseContentType(request);
		assertThat(contentType, is(viewResolver.getDefaultContentType()));
	}

	@Test
	public void testDetermineResponseContentTypeWithAcceptHeaderThatHasParams() {
		HttpServletRequest request = new MockHttpServletRequest()
				.method("GET").header("Accept", "application/xml; charset=utf-8");
		String contentType = viewResolver.determineResponseContentType(request);
		assertThat(contentType, is(MimeTypes.MIME_APPLICATION_XML));
	}

	@Test
	public void testDetermineResponseContentTypeWithMultivalueAcceptHeader() {
		HttpServletRequest request = new MockHttpServletRequest()
				.method("GET").header("Accept", "*/*, text/html, application/json, application/xml");
		String contentType = viewResolver.determineResponseContentType(request);
		assertThat(contentType, is(MimeTypes.MIME_APPLICATION_XML));
	}

	@Test
	public void testResolveWithValidFormatParameter() {
		HttpServletRequest request = new MockHttpServletRequest()
				.method("GET")
				.parameter("format", "json");
		MockHttpServletResponse response = new MockHttpServletResponse();

		viewResolver.resolve(request, response, new RestView(new MessageDto("hello")));

		assertThat(response.getContentType(), is(MimeTypes.MIME_APPLICATION_JSON));
		assertThat(response.content(), is("{\"message\":\"hello\"}"));
	}

	@Test
	public void testResolveWithUnknownFormatParameter() {
		thrown.expect(NotAcceptableException.class);
		thrown.expectMessage("Not acceptable: yaml");

		HttpServletRequest request = new MockHttpServletRequest()
				.method("GET")
				.parameter("format", "yaml");
		MockHttpServletResponse response = new MockHttpServletResponse();

		viewResolver.resolve(request, response, new RestView(new MessageDto("hello")));
	}

	@Test
	public void testResolveWithNullOutputObject() {
		HttpServletRequest request = new MockHttpServletRequest()
				.method("GET")
				.parameter("format", "json");
		MockHttpServletResponse response = new MockHttpServletResponse();

		viewResolver.resolve(request, response, new RestView(null, HttpServletResponse.SC_NO_CONTENT));
		assertThat(response.content(), is(""));
	}

	@Test
	public void testResolveWithUnserializableObject() {
		HttpServletRequest request = new MockHttpServletRequest()
				.method("GET")
				.parameter("format", "xml");
		MockHttpServletResponse response = new MockHttpServletResponse();

		Serializer serializer = spy(new XmlSerializer());
		Object object = new Object();
		doThrow(new RuntimeException("Intentional"))
				.when(serializer).marshal(Mockito.eq(object), Mockito.anyMapOf(String.class, String.class));

		viewResolver = new RestViewResolver();
		viewResolver.addSerializer(MimeTypes.MIME_APPLICATION_XML, serializer);

		viewResolver.resolve(request, response, new RestView(object));
	}
}
