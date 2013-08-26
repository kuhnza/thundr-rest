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

import org.joda.time.DateTime;

public class ClassWithDateTimeGetter {
	public DateTime dateTime = null;

	public ClassWithDateTimeGetter() { }

	public ClassWithDateTimeGetter(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	public DateTime getDateTime() {
		return dateTime;
	}

	public void setDateTime(DateTime dateTime) {
		this.dateTime = dateTime;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ClassWithDateTimeGetter that = (ClassWithDateTimeGetter) o;

		if (dateTime != null ? !dateTime.equals(that.dateTime) : that.dateTime != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return dateTime != null ? dateTime.hashCode() : 0;
	}
}
