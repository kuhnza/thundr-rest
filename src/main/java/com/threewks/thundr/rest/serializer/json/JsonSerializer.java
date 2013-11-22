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
package com.threewks.thundr.rest.serializer.json;


import com.threewks.thundr.rest.serializer.Ignore;
import com.threewks.thundr.rest.serializer.Serializer;
import net.sf.json.JSON;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.filters.OrPropertyFilter;
import net.sf.json.processors.JsonValueProcessor;
import net.sf.json.util.JSONUtils;
import net.sf.json.util.PropertyFilter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.ObjectUtils;
import org.joda.time.DateTime;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;

public class JsonSerializer implements Serializer {

	public static final String OPTION_CALLBACK = "callback";

	public JsonSerializer() {
	}

	@Override
	public String marshal(Object object) {
		return marshal(object, null);
	}

	@Override
	public String marshal(Object object, Map<String, String> options) {
		String output = toJson(object).toString();

		// If callback option is present wrap response in function call ala JSONP
		if (options != null && options.containsKey(OPTION_CALLBACK)) {
			output = options.get(OPTION_CALLBACK) + "(" + output + ");";
		}
		return output;
	}

	@Override
	public <T> T unmarshal(Class<T> type, String json) {
		JsonConfig config = new JsonConfig();
		config.registerJsonValueProcessor(DateTime.class, new DateTimeValueProcessor());
		config.setRootClass(type);

		JSONUtils.getMorpherRegistry().registerMorpher(new DateTimeMorpher());
		JSONObject jsonObject = JSONObject.fromObject(json, config);
		return (T) JSONObject.toBean(jsonObject, config);
	}

	protected JSON toJson(Object object) {
		JsonConfig config = new JsonConfig();
		// prevent serialization of properties with @Ignore or null values.
		OrPropertyFilter filter = new OrPropertyFilter(new IgnoreAnnotationPropertyFilter(), new NullValuePropertyFilter());
		config.setJsonPropertyFilter(filter);
		config.registerJsonValueProcessor(DateTime.class, new DateTimeValueProcessor());
		return JSONSerializer.toJSON(object, config);
	}

	private static class DateTimeValueProcessor implements JsonValueProcessor {
		@Override
		public Object processArrayValue(Object o, JsonConfig jsonConfig) {
			return ObjectUtils.toString(o, null);
		}

		@Override
		public Object processObjectValue(String s, Object o, JsonConfig jsonConfig) {
			return ObjectUtils.toString(o, null);
		}
	}

	private static class IgnoreAnnotationPropertyFilter implements PropertyFilter {
		@Override
		public boolean apply(Object source, String name, Object value) {
			try {
				PropertyDescriptor descriptor = PropertyUtils.getPropertyDescriptor(source, name);
                if(descriptor != null){
                    Method m = PropertyUtils.getReadMethod(descriptor);
                    if (m.isAnnotationPresent(Ignore.class)) {
                        return true;
                    }
                }
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}

			return false;
		}
	}

	private static class NullValuePropertyFilter implements PropertyFilter {
		@Override
		public boolean apply(Object source, String name, Object value) {
			return value == null;
		}
	}
}
