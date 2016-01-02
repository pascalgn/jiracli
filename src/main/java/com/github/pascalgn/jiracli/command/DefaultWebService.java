package com.github.pascalgn.jiracli.command;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;

public class DefaultWebService implements WebService {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultWebService.class);

	static {
		SSLContext sslcontext;
		try {
			sslcontext = SSLContexts.custom().loadTrustMaterial(null, new TrustSelfSignedStrategy()).build();
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, NoopHostnameVerifier.INSTANCE);
		CloseableHttpClient httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		Unirest.setHttpClient(httpclient);
	}

	private final String rootURL;
	private final String username;
	private final String password;

	private transient Map<String, JSONObject> issueCache;
	private transient Map<String, String> fieldMapping;

	public DefaultWebService(String rootURL, String username, String password) {
		this.rootURL = rootURL;
		this.username = username;
		this.password = password;
	}

	@Override
	public synchronized JSONObject getIssue(String issue) {
		JSONObject result;
		if (issueCache == null) {
			issueCache = new HashMap<String, JSONObject>();
			result = null;
		} else {
			result = issueCache.get(issue);
		}
		if (result == null) {
			result = call("/rest/api/latest/issue/{issue}", "issue", issue).getObject();
			issueCache.put(issue, result);
		}
		return result;
	}

	@Override
	public synchronized Map<String, String> getFieldMapping() {
		if (fieldMapping == null) {
			fieldMapping = new HashMap<String, String>();
			JSONArray array = call("/rest/api/latest/field").getArray();
			for (Object obj : array) {
				JSONObject field = (JSONObject) obj;
				String id = field.getString("id");
				String name = field.getString("name");
				boolean custom = field.getBoolean("custom");
				if (custom) {
					fieldMapping.put(name, id);
				}
			}
		}
		return fieldMapping;
	}

	private JsonNode call(String path, String... routeParams) {
		if (!path.startsWith("/")) {
			throw new IllegalArgumentException("Invalid path: " + path);
		}
		if (routeParams.length % 2 != 0) {
			throw new IllegalArgumentException("Invalid route parameters: " + Arrays.toString(routeParams));
		}
		GetRequest request = Unirest.get(rootURL + path).basicAuth(username, password);
		for (int i = 0; i < routeParams.length; i += 2) {
			request = request.routeParam(routeParams[i], routeParams[i + 1]);
		}
		LOGGER.debug("Fetching URL: {}", request.getUrl());
		HttpResponse<JsonNode> json;
		try {
			json = request.asJson();
		} catch (UnirestException e) {
			throw new IllegalStateException("Error fetching URL: " + request.getUrl(), e);
		}
		return json.getBody();
	}

	@Override
	public void close() {
		issueCache = null;
		fieldMapping = null;
		try {
			Unirest.shutdown();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to shutdown!", e);
		}
	}
}
