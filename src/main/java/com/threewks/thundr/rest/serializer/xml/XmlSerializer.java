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
package com.threewks.thundr.rest.serializer.xml;


import com.threewks.thundr.rest.serializer.json.JsonSerializer;
import com.threewks.thundr.rest.serializer.Serializer;
import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.*;
import java.util.Map;

public class XmlSerializer extends JsonSerializer implements Serializer {

	public static final String OPTION_ROOT_ELEMENT_NAME = "rootElementName";

	@Override
	public String marshal(Object object) {
		return marshal(object, null);
	}

	protected String toXml(JSON json, String rootElement) {
		XMLSerializer xmlSerializer = new XMLSerializer();
		xmlSerializer.setRootName(rootElement);
		xmlSerializer.setElementName("element");
		xmlSerializer.setTypeHintsEnabled(false);
		xmlSerializer.setTypeHintsCompatibility(false);

		// clean up line separators leftover from xom serialization
		return cleanLineSeparators(xmlSerializer.write(json));
	}

	private String cleanLineSeparators(String xml) {
		try {
			BufferedReader reader = new BufferedReader(new StringReader(xml));
			StringWriter out = new StringWriter();
			BufferedWriter writer = new BufferedWriter(out);
			String line = reader.readLine();

			while (line != null) {
				writer.write(line);
				line = reader.readLine();
				if (line != null) {
					writer.newLine();
				}
			}

			writer.flush();
			return out.toString();
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String marshal(Object object, Map<String, String> options) {
		Class<?> type = object.getClass();
		String rootElement = type.getName();

		if (options != null && options.containsKey(OPTION_ROOT_ELEMENT_NAME)) {
			// Root element option overrides XmlRootElement annotation
			rootElement = options.get(OPTION_ROOT_ELEMENT_NAME);
		}
		else if (type.isAnnotationPresent(XmlRootElement.class)) {
			rootElement = type.getAnnotation(XmlRootElement.class).name();
		}

		JSON json = toJson(object);
		return toXml(json, rootElement);
	}

	@Override
	public <T> T unmarshal(Class<T> type, String xml) {
		XMLSerializer serializer = new XMLSerializer();
		JSON json = serializer.read(xml);
		return super.unmarshal(type, json.toString());
	}
}
