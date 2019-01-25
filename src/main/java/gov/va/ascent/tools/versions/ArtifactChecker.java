package gov.va.ascent.tools.versions;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class ArtifactChecker {

	private static final int STATUS_SUCCESS = 200;

	/**
	 * ArtifactChecker is a static class, do not instantiate it.
	 */
	private ArtifactChecker() {
		throw new IllegalAccessError("ArtifactChecker is a static class, do not instantiate it.");
	}

	/**
	 * Determine if a specific project version still exists in nexus.<br/>
	 * Should NOT be used for subprojects, (e.g. only the reactor project)
	 *
	 * @param nexusBaseProjectsUrl - the VA artifacts base directory in nexus
	 * @param relativePath - the relative path for the project from GIT_HOME
	 * @param version - the version explicitly declared in the POM
	 * @return true if the artifact/version directory exists in nexus
	 * @throws ClientProtocolException - issue in HttpClient
	 * @throws IOException - issue reaching nexusBaseProjectUrl in nexus
	 */
	static boolean exists(String nexusBaseProjectsUrl, Path relativePath, String version) throws ClientProtocolException, IOException {
		int statusCode = 0;

		String url = makeNexusUrl(nexusBaseProjectsUrl, relativePath, version);

		CloseableHttpClient httpclient = HttpClients.custom().setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE).build();
		HttpGet httpGet = new HttpGet(url);
		CloseableHttpResponse response = httpclient.execute(httpGet);
		try {
			statusCode = response.getStatusLine().getStatusCode();
		} finally {
			if (response != null) {
				response.close();
			}
		}
		return statusCode == STATUS_SUCCESS;
	}

	private static String makeNexusUrl(String nexusBaseProjectsUrl, Path relativePath, String version) {
		Path projectName = relativePath.getNameCount() > 1 ? relativePath.getParent() : relativePath;
		// no need to use URLEncoder - nothing else needs encoding
		String queryString = "name.raw%3D" + projectName.toString() + "%20AND%20attributes.maven2.baseVersion%3D" + version;
		return nexusBaseProjectsUrl + queryString;
	}
}
