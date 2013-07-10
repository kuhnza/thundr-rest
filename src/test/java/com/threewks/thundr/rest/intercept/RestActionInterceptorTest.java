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
package com.threewks.thundr.rest.intercept;


import com.threewks.thundr.http.exception.HttpStatusException;
import com.threewks.thundr.rest.RestView;
import com.threewks.thundr.rest.RestViewResolver;
import com.threewks.thundr.rest.dto.ErrorDto;
import com.threewks.thundr.test.mock.servlet.MockHttpServletResponse;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.servlet.http.HttpServletResponse;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class RestActionInterceptorTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	private RestActionInterceptor interceptor;

	@Before
	public void setup() {
		interceptor = new RestActionInterceptor(new RestViewResolver());
	}

	@Test
	public void testExceptionHandledAndSerialized() {
		Exception exception = new RuntimeException("Intentional");
		RestView view = interceptor.exception(null, exception, null, null);
		assertThat(view, is(notNullValue()));

		assertThat(view.getOutput(), is(instanceOf(ErrorDto.class)));
		ErrorDto dto = (ErrorDto) view.getOutput();
		assertThat(dto.message, is(exception.getMessage()));
		assertThat(view.getStatus(), is(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
	}

	@Test
	public void testHttpStatusExceptionHandledAndSerialized() {
		Exception exception = new HttpStatusException(HttpServletResponse.SC_BAD_REQUEST, "Intentional");
		MockHttpServletResponse response = new MockHttpServletResponse();
		RestView view = interceptor.exception(null, exception, null, response);
		assertThat(view, is(notNullValue()));

		assertThat(view.getOutput(), is(instanceOf(ErrorDto.class)));
		ErrorDto dto = (ErrorDto) view.getOutput();
		assertThat(dto.message, is(exception.getMessage()));
		assertThat(view.getStatus(), is(HttpServletResponse.SC_BAD_REQUEST));
	}
}
