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

import com.google.common.base.Charsets;
import com.threewks.thundr.view.View;

import javax.servlet.http.HttpServletResponse;

public class RestView implements View {

	private Object output;
	private int status;
	private String characterEncoding;

	public RestView(Object output) {
		this(output, HttpServletResponse.SC_OK);
	}

	public RestView(Object output, int status) {
		this(output, status, Charsets.UTF_8.toString());
	}

	public RestView(Object output, int status, String characterEncoding) {
		this.output = output;
		this.status = status;
		this.characterEncoding = characterEncoding;
	}

	public Object getOutput() {
		return output;
	}

	public int getStatus() {
		return status;
	}

	public String getCharacterEncoding() {
		return characterEncoding;
	}
}
