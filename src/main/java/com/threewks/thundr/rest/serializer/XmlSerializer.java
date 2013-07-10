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
import com.threewks.thundr.rest.RestException;
import org.apache.commons.io.IOUtils;

import javax.xml.bind.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class XmlSerializer implements Serializer {

	public static final String OPTION_ROOT_ELEMENT_NAME = "rootElementName";

	private static ConcurrentMap<Class, JAXBContext> jaxbContextCache = Maps.newConcurrentMap();

	@Override
	public String marshall(Object object) {
		return marshall(object, null);
	}

	@Override
	public String marshall(Object object, Map<String, String> options) {
		StringWriter sw = new StringWriter();

		try {
			Class outputType = object.getClass();
			JAXBContext context = getJaxbContext(outputType);
			Marshaller marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
			marshaller.setAdapter(new DateTimeAdapter());

			if (options != null && options.containsKey(OPTION_ROOT_ELEMENT_NAME)) {
				// Root element option overrides XmlRootElement annotation
				String rootElementName = options.get(OPTION_ROOT_ELEMENT_NAME);
				JAXBElement element = new JAXBElement(new QName(null, rootElementName), outputType, object);
				marshaller.marshal(element, sw);
			} else if (!outputType.isAnnotationPresent(XmlRootElement.class)) {
				// Where root element is missing, substitute with class name
				String rootElementName = outputType.getName();
				JAXBElement element = new JAXBElement(new QName(null, rootElementName), outputType, object);
				marshaller.marshal(element, sw);
			} else {
				marshaller.marshal(object, sw);
			}

			return sw.toString();
		} catch (JAXBException e) {
			throw new RestException(e, "Error serializing to XML");
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T unmarshall(Class<T> type, String object) {
		try {
			JAXBContext context = getJaxbContext(type);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			return (T) unmarshaller.unmarshal(IOUtils.toInputStream(object));
		} catch (JAXBException e) {
			throw new RestException(e, "Error deserializing from XML");
		}
	}

	private <T> JAXBContext getJaxbContext(Class<T> type) {
		JAXBContext context = jaxbContextCache.get(type);
		if (context != null) {
			return context;
		}

		try {
			context = JAXBContext.newInstance(type);
			JAXBContext last = jaxbContextCache.putIfAbsent(type, context);
			if (last != null) {
				context = last;
			}
		} catch (JAXBException e) {
			throw new RestException(e);
		}

		return context;
	}
}
