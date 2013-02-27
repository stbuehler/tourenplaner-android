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

package de.uni.stuttgart.informatik.ToureNPlaner.Data;

import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.ConstraintType;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequestNN;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.AlgorithmRequest;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;

import java.io.Serializable;
import java.util.ArrayList;

public interface AlgorithmInfo extends Serializable {
	public abstract String getName();

	public abstract String getDescription();

	public abstract boolean sourceIsTarget();

	public abstract String getUrlsuffix();

	public abstract boolean isHidden();

	public abstract int getMinPoints();

	public abstract int getMaxPoints();

	public abstract ArrayList<ConstraintType> getPointConstraintTypes();

	public abstract ArrayList<ConstraintType> getConstraintTypes();

	/**
	 * Start a request for the currently set points
	 *
	 * @param listener  object to report results to
	 * @param session   session containing current server URL, points and constraint settings
	 * @return {@link AlgorithmRequest}
	 */
	public abstract AlgorithmRequest execute(Observer listener, Session session);

	/**
	 * Search the real node closest to the given location
	 *
	 * @param listener  object to report results to
	 * @param session   session containing current server URL
	 * @param node      the location to search for
	 * @return {@link AlgorithmRequestNN}
	 */
	public abstract AlgorithmRequestNN executeNN(Observer listener, Session session, Node node);
}
