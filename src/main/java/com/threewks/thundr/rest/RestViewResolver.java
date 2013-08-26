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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.threewks.thundr.logger.Logger;
import com.threewks.thundr.rest.dto.ErrorDto;
import com.threewks.thundr.rest.exception.NotAcceptableException;
import com.threewks.thundr.rest.serializer.Serializer;
import com.threewks.thundr.view.ViewResolutionException;
import com.threewks.thundr.view.ViewResolver;
import jodd.util.MimeTypes;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;

public class RestViewResolver implements ViewResolver<RestView> {

	private static final String ContentTypeAny = "*/*";

	private String defaultContentType;
	private Map<String, Serializer> serializers = Maps.newHashMap();

	public RestViewResolver() {
		this(MimeTypes.MIME_APPLICATION_JSON);
	}

	public RestViewResolver(String defaultResponseContentType) {
		this.defaultContentType = defaultResponseContentType;
	}

	public void addSerializer(String contentType, Serializer serializer) {
		serializers.put(contentType, serializer);
	}

	public boolean canSerialize(String contentType) {
		return serializers.containsKey(contentType);
	}

	public Map<String, Serializer> getSerializers() {
		return serializers;
	}

	@Override
	public void resolve(HttpServletRequest req, HttpServletResponse res, RestView view) {
		String responseContentType = determineResponseContentType(req);
		String body = serializeOutput(responseContentType, view.getOutput(), flattenParameterMap(req.getParameterMap()));
		String charset = view.getCharacterEncoding();

		res.setCharacterEncoding(charset);
		res.setContentType(responseContentType);
		res.setContentLength(body.getBytes(Charset.forName(charset)).length);
		res.setStatus(view.getStatus());

		try {
			res.getWriter().write(body);
		} catch (IOException e) {
			throw new ViewResolutionException(
					e, "Failed to resolve data view for content-type: %s body: %s", responseContentType, body);
		}
	}

	public String determineResponseContentType(HttpServletRequest req) {
		// Check for presence of an explicit format parameter
		String contentType = determineContentTypeFromFormatParameter(req);
		if (contentType != null) {
			return contentType;
		}

		// No dice, check for Accept header. Here we accept the first matching
		// header element or a wildcard in which case we fall through to the default.
		contentType = determineContentTypeFromAcceptHeader(req);
		if (contentType != null) {
			return contentType;
		}

		// No format specified so lets go with the default
		return defaultContentType;
	}

	public String getDefaultContentType() {
		return defaultContentType;
	}

	private String serializeOutput(String contentType, Object output, Map<String, String> options) {
		Serializer serializer = serializers.get(contentType);
		if (serializer == null) {
			// Fine to throw exception here to be handled by container since we don't have any idea about what
			// an acceptable format might be. Everything else ought to be serialized as the requested content type.
			throw new NotAcceptableException("Not acceptable: %s", contentType);
		}

		if (output == null) {
			return "";
		}

		try {
			return serializer.marshal(output, options);
		} catch (Throwable t) {
			Logger.error("Unhandled exception when serializing output object in RestViewResolver: %s",
					ExceptionUtils.getStackTrace(t));
			return serializer.marshal(new ErrorDto(t.getMessage()));
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, String> flattenParameterMap(Map parameterMap) {
		Map<String, String> flatMap = Maps.newHashMap();
		for (Object o : parameterMap.entrySet()) {
			Map.Entry<String, String[]> entry = (Map.Entry<String, String[]>) o;
			flatMap.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
		}
		return flatMap;
	}

	private String determineContentTypeFromFormatParameter(HttpServletRequest req) {
		String format = req.getParameter("format");
		if (format != null) {
			String contentType = MimeTypes.lookupMimeType(format);
			if (contentType == null) {
				return format;
			} else if (MimeTypes.MIME_APPLICATION_JSON.equals(contentType)) {
				String callback = req.getParameter("callback");
				if (callback == null) {
					// It's plain old JSON
					return contentType;
				} else {
					// It's JSONP!
					return MimeTypes.MIME_APPLICATION_JAVASCRIPT;
				}
			} else {
				return contentType;
			}
		}

		return null;
	}

	private String determineContentTypeFromAcceptHeader(HttpServletRequest req) {
		String accept = req.getHeader("Accept");
		if (accept != null) {
			// Accept header often contains a range of different content types which *should*
			// ordered from least specific (e.g. */*) to most (e.g. application/xml) - see RFC26216-sec14.
			// Here we attempt to match the most specific content type for which we have
			// a serializer for.
			for (String contentType : Lists.reverse(Arrays.asList(StringUtils.split(accept, ',')))) {
				if (contentType.contains(";")) {
					// Discard accept params and extensions, we don't care about them
					contentType = contentType.replaceAll(";.*", "");
				}

				contentType = contentType.trim();
				if (serializers.containsKey(contentType)) {
					return contentType;
				}
			}
		}

		return null;
	}
}
