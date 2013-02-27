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

package de.uni.stuttgart.informatik.ToureNPlaner.Handler;

import de.uni.stuttgart.informatik.ToureNPlaner.Data.AlgorithmInfo;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Node;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;

/**
 * Interface to keep track of a running
 * {@link AlgorithmInfo#executeNN(Observer, Session, Node) request}.
 *
 * @author Stefan Bühler
 */
public interface AlgorithmRequestNN {
	/**
	 * @return node the request is searching for
	 */
	public Node getNode();

	/**
	 * Cancel request.
	 */
	public boolean cancel(boolean mayInterruptIfRunning);

	/**
	 * Set Observer to report result to.
	 *
	 * @param listener
	 */
	public void setListener(Observer listener);
}
