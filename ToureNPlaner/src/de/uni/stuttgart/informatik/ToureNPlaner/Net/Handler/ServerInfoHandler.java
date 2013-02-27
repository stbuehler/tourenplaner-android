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

import android.widget.Toast;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni.stuttgart.informatik.ToureNPlaner.ClientSideCompute.ClientSideComputeAlgorithmInfo;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.AlgorithmInfo;
import de.uni.stuttgart.informatik.ToureNPlaner.Data.Constraints.ConstraintType;
import de.uni.stuttgart.informatik.ToureNPlaner.Handler.Observer;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.JacksonManager;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.NetworkAlgorithmInfo;
import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
import de.uni.stuttgart.informatik.ToureNPlaner.R;
import de.uni.stuttgart.informatik.ToureNPlaner.ToureNPlanerApplication;
import de.unistuttgart.informatik.OfflineToureNPlaner.Handler.OfflineAlgorithmInfo;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class ServerInfoHandler extends SimpleNetworkHandler {
	private final String url;

	public ServerInfoHandler(Observer listener, String url) {
		super(listener);
		this.url = url;
	}

	@Override
	protected HttpURLConnection getHttpUrlConnection() throws Exception {
		URL info_url = new URL(url + "/info");
		HttpURLConnection urlConnection = (HttpURLConnection) info_url.openConnection();
		if (urlConnection instanceof HttpsURLConnection) {
			((HttpsURLConnection) urlConnection).setSSLSocketFactory(ToureNPlanerApplication.getSslContext().getSocketFactory());
		}
		return urlConnection;
	}

	@Override
	protected void handleOutput(OutputStream connection) throws Exception {}

	private static URL parseServerInfo(String url, JsonNode object) throws MalformedURLException {
		int port = -1;
		String hostname = "localhost";
		try {
			URL uri = new URL(url);
			hostname = uri.getHost();
			port = uri.getPort();
			if (port == uri.getDefaultPort()) port = -1;
		} catch (MalformedURLException e) {
			// Should never happen
			e.printStackTrace();
		}

		final boolean isPrivate = "PRIVATE".equals(object.get("servertype").asText().toUpperCase());
		final int SslPort = object.get("sslport").asInt();

		if (isPrivate) {
			port = -1;
			if (SslPort != 0 && SslPort != -1 && SslPort != 443) port = SslPort;
		}

		final String server_url =
			(isPrivate ? "https://" : "http://")
			+ hostname + (port != -1 ? ":" + port : "");
		// final String version = object.get("version").asText();

		return new URL(server_url);
	}

	private static AlgorithmInfo parseAlgorithmInfo(JsonNode object) {
		final String version = object.path("version").asText();
		final String name = object.path("name").asText();
		String description = object.path("description").asText();
		final String urlsuffix = object.path("urlsuffix").asText();

		if (description == null || description.equals("")) {
			if ("sp".equals(urlsuffix)) {
				description = ToureNPlanerApplication.getContext().getString(R.string.sp_description);
			} else if ("tsp".equals(urlsuffix)) {
				description = ToureNPlanerApplication.getContext().getString(R.string.tsp_description);
			} else if ("csp".equals(urlsuffix)) {
				description = ToureNPlanerApplication.getContext().getString(R.string.csp_description);
			}
		}

		int minPoints = 0, maxPoints = 0;
		boolean sourceIsTarget = false, isHidden = false;

		JsonNode details = object.get("details");
		if (details != null) {
			minPoints = details.path("minpoints").asInt(0);
			maxPoints = details.path("maxpoints").asInt(Integer.MAX_VALUE);
			if (details.path("maxpoints").isMissingNode())
				maxPoints = Integer.MAX_VALUE;
			sourceIsTarget = details.path("sourceistarget").asBoolean();
			isHidden = details.path("hidden").asBoolean();
		}

		ArrayList<ConstraintType> constraintTypes, pointConstraintTypes;

		JsonNode constraints = object.get("constraints");
		if (constraints == null) {
			constraintTypes = new ArrayList<ConstraintType>();
		} else {
			constraintTypes = new ArrayList<ConstraintType>(constraints.size());
			for (JsonNode constraint : constraints) {
				constraintTypes.add(ConstraintType.parseType(constraint));
			}
		}

		JsonNode pointconstraints = object.get("pointconstraints");
		if (pointconstraints == null) {
			pointConstraintTypes = new ArrayList<ConstraintType>();
		} else {
			pointConstraintTypes = new ArrayList<ConstraintType>(pointconstraints.size());
			for (JsonNode constraint : pointconstraints) {
				pointConstraintTypes.add(ConstraintType.parseType(constraint));
			}
		}

		return new NetworkAlgorithmInfo(version, name, description, urlsuffix, minPoints, maxPoints,
				sourceIsTarget, isHidden, constraintTypes, pointConstraintTypes);
	}

	@Override
	protected Object handleInput(JacksonManager.ContentType type, InputStream inputStream) throws Exception {
		ObjectMapper mapper = JacksonManager.getMapper(type);
		JsonNode object = mapper.readValue(inputStream, JsonNode.class);

		final JsonNode json_algorithms = object.get("algorithms");
		final ArrayList<AlgorithmInfo> algorithms = new ArrayList<AlgorithmInfo>(2 + json_algorithms.size());

		for (JsonNode node : json_algorithms) {
			algorithms.add(parseAlgorithmInfo(node));
		}
		// not listed by server, but still needs network connection
		algorithms.add(new ClientSideComputeAlgorithmInfo());

		Session session = new Session();
		session.setURL(parseServerInfo(url, object));
		session.setAlgorithms(algorithms);
		return session;
	}

	@Override
	public void onPostExecute(Object object) {
		if (object instanceof Session) {
			Session session = (Session) object;
			final ArrayList<AlgorithmInfo> algorithms = session.getAlgorithms();
			algorithms.add(new OfflineAlgorithmInfo());
		} else {
			Toast.makeText(ToureNPlanerApplication.getContext(),"Error:\n" + object.toString(), Toast.LENGTH_LONG).show();

			final ArrayList<AlgorithmInfo> algorithms = new ArrayList<AlgorithmInfo>(1);
			algorithms.add(new OfflineAlgorithmInfo());

			Session session = new Session();
			URL fallbackurl = null;
			try {
				fallbackurl = new URL(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			if (fallbackurl != null) session.setURL(fallbackurl);
			session.setAlgorithms(algorithms);
			object = session;
		}
		super.onPostExecute(object);
	}
}
