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

package de.uni.stuttgart.informatik.ToureNPlaner.Data.Edits;

import de.uni.stuttgart.informatik.ToureNPlaner.Data.TBTResult;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;

public class TBTResultEdit extends Edit {
	private final TBTResult result;

	public TBTResultEdit(Session session, TBTResult result) {
		super(session);
		this.result = result;
	}

	@Override
	public void perform() {
		session.settbtResult(result);

		session.notifyChangeListerners(new Session.Change(Session.TBT_RESULT_CHANGE));
	}
}
