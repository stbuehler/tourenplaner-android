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

package de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler;

import de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;

/**
 * @author  Niklas Schnelle
 */
public abstract class SessionAwareHandler extends AsyncHandler {
	protected Session session;


	public SessionAwareHandler(Observer listener, Session session) {
		super(listener);
		this.session = session;
	}
}
