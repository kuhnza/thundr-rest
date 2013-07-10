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
package com.threewks.thundr.rest.parser;

import com.google.gson.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class JsonParser {
	private static final String DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSz";
	private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTime(); // .forPattern(DATE_TIME_FORMAT);
	private static Gson gson;

	static {
		new JsonParser();
	}

	private JsonParser() {
		gson = new GsonBuilder()
		.registerTypeAdapter(Date.class, dateSerializer)
		.registerTypeAdapter(Date.class, dateDeserializer)
		.registerTypeAdapter(DateTime.class, dateTimeSerializer)
		.registerTypeAdapter(DateTime.class, dateTimeDeserializer)
		.create();
	}

	public static <T> T fromJson(String json, Class<T> type)
	{
		return gson.fromJson(json, type);
	}

	@SuppressWarnings("unchecked")
	public static String toJson(Object object)
	{
		return gson.toJson(object);

	}

	private Date toLongDate(String date)
	{
		return new Date(Long.valueOf(date));
	}

	private static Date toIsoDate(String date)
	{
		try {
			return new SimpleDateFormat(DATE_TIME_FORMAT).parse(date.replace("+", "GMT+"));
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
	}

	JsonSerializer<Date> dateSerializer = new JsonSerializer<Date>() {

		@Override
		public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
			return src == null ? null : new JsonPrimitive(src.getTime());
		}
	};

	JsonDeserializer<Date> dateDeserializer = new JsonDeserializer<Date>() {
		@Override
		public Date deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			return json == null ? null : toIsoDate(json.getAsString());
		}
	};

	JsonSerializer<DateTime> dateTimeSerializer = new JsonSerializer<DateTime>() {

		@Override
		public JsonElement serialize(DateTime src, Type typeOfSrc, JsonSerializationContext context) {
			return src == null ? null : new JsonPrimitive(src.toString());
		}
	};

	JsonDeserializer<DateTime> dateTimeDeserializer = new JsonDeserializer<DateTime>() {
		@Override
		public DateTime deserialize(JsonElement json, Type typeOfT,
				JsonDeserializationContext context) throws JsonParseException {
			return json == null ? null : DateTime.parse(json.getAsString(), dateTimeFormatter);
		}
	};
}
