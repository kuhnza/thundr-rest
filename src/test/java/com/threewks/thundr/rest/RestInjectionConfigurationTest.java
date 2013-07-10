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

import com.threewks.thundr.action.method.ActionInterceptorRegistry;
import com.threewks.thundr.injection.InjectionContextImpl;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.rest.intercept.Rest;
import com.threewks.thundr.rest.intercept.RestActionInterceptor;
import com.threewks.thundr.view.ViewResolverRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import static org.mockito.Mockito.*;

public class RestInjectionConfigurationTest {

	private UpdatableInjectionContext injectionContext = new InjectionContextImpl();

	@Before
	public void setup() {
		ViewResolverRegistry viewResolverRegistry = mock(ViewResolverRegistry.class);
		injectionContext.inject(viewResolverRegistry).as(ViewResolverRegistry.class);

		ActionInterceptorRegistry actionInterceptorRegistry = mock(ActionInterceptorRegistry.class);
		injectionContext.inject(actionInterceptorRegistry).as(ActionInterceptorRegistry.class);

		new RestInjectionConfiguration().configure(injectionContext);
	}

	@Test
	public void shouldInjectRestViewResolver() {
		ViewResolverRegistry registry = injectionContext.get(ViewResolverRegistry.class);
		verify(registry).addResolver(
				Matchers.eq(RestView.class),
				Matchers.any(RestViewResolver.class));
	}

	@Test
	public void shouldInjectRestActionInterceptor() {
		ActionInterceptorRegistry registry = injectionContext.get(ActionInterceptorRegistry.class);
		verify(registry).registerInterceptor(
				Matchers.eq(Rest.class),
				Matchers.any(RestActionInterceptor.class));
	}
}
