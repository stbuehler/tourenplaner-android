/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.uni.stuttgart.informatik.ToureNPlaner.Net;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.os.Build;

/**
 * This input stream won't read() after the underlying stream is exhausted.
 * http://code.google.com/p/android/issues/detail?id=14562
 */
public final class DoneHandlerInputStream extends FilterInputStream {
	private boolean done;

	public DoneHandlerInputStream(InputStream stream) {
		super(stream);
	}

	@Override
	public int read(byte[] bytes, int offset, int count) throws IOException {
		if (!done) {
			int result = super.read(bytes, offset, count);
			if (result != -1) {
				return result;
			}
		}
		done = true;
		return -1;
	}

	public static InputStream wrap(InputStream stream) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			return new DoneHandlerInputStream(stream);
		}
		return stream;
	}

	public static InputStream http_get_stream(HttpURLConnection urlConnection) throws IOException {
		InputStream stream = null;
		try {
			stream = urlConnection.getInputStream();
		} catch (IOException e) {
			stream = urlConnection.getErrorStream();
			if (null == stream) throw e;
		}
		return stream != null ? wrap(stream) : null;
	}
}
