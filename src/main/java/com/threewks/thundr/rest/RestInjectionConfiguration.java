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
import com.threewks.thundr.injection.BaseInjectionConfiguration;
import com.threewks.thundr.injection.UpdatableInjectionContext;
import com.threewks.thundr.rest.intercept.Rest;
import com.threewks.thundr.rest.intercept.RestActionInterceptor;
import com.threewks.thundr.rest.serializer.json.JsonSerializer;
import com.threewks.thundr.rest.serializer.xml.XmlSerializer;
import com.threewks.thundr.view.ViewResolverRegistry;
import jodd.util.MimeTypes;

public class RestInjectionConfiguration extends BaseInjectionConfiguration {
	@Override
	public void configure(UpdatableInjectionContext injectionContext) {
		ViewResolverRegistry viewResolverRegistry = injectionContext.get(ViewResolverRegistry.class);
		RestViewResolver viewResolver = addViewResolvers(viewResolverRegistry);

		ActionInterceptorRegistry actionInterceptorRegistry = injectionContext.get(ActionInterceptorRegistry.class);
		addActionInterceptors(viewResolver, actionInterceptorRegistry);
	}

	protected void addActionInterceptors(RestViewResolver viewResolver, ActionInterceptorRegistry actionInterceptorRegistry) {
		actionInterceptorRegistry.registerInterceptor(Rest.class, new RestActionInterceptor(viewResolver));
	}

	protected RestViewResolver addViewResolvers(ViewResolverRegistry viewResolverRegistry) {
		// Instantiate view resolver and configure it with default/supplied serializers
		RestViewResolver viewResolver = new RestViewResolver();
		viewResolver.addSerializer(MimeTypes.MIME_APPLICATION_JSON, new JsonSerializer());
		viewResolver.addSerializer(MimeTypes.MIME_APPLICATION_JAVASCRIPT, new JsonSerializer());
		viewResolver.addSerializer(MimeTypes.MIME_APPLICATION_XML, new XmlSerializer());

		// Add RestViewResolver to the view resolver registry
		viewResolverRegistry.addResolver(RestView.class, viewResolver);

		return viewResolver;
	}


}