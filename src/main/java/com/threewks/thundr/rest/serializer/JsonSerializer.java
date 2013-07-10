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


import com.threewks.thundr.rest.parser.JsonParser;

import java.util.Map;

public class JsonSerializer implements Serializer {

	public static final String OPTION_CALLBACK = "callback";

	public JsonSerializer() {
	}

	@Override
	public String marshall(Object object) {
		return marshall(object, null);
	}

	@Override
	public String marshall(Object object, Map<String, String> options) {
		String output = JsonParser.toJson(object);

		// If callback option is present wrap response in function call ala JSONP
		if (options != null && options.containsKey(OPTION_CALLBACK)) {
			output = options.get(OPTION_CALLBACK) + "(" + output + ");";
		}
		return output;
	}

	@Override
	public <T> T unmarshall(Class<T> type, String object) {
		return JsonParser.fromJson(object, type);
	}
}
