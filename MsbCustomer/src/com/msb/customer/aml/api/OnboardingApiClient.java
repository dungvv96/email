package com.msb.customer.aml.api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Lightweight REST client (no third-party HTTP libraries) that calls the
 * InitiateOnboardingService API using {@link HttpURLConnection}. The request
 * body is built from {@link OnboardingRequest} and serialized with Jackson.
 */
public class OnboardingApiClient {

	private final String endpoint;
	private final ObjectMapper objectMapper = new ObjectMapper();

	private int connectTimeoutMs = 30000;
	private int readTimeoutMs = 60000;

	/** Basic auth header value, e.g. "Basic YW1sdXNlcjphYmMxMjM=". */
	private String authorization;

	/** API gateway key sent in the "Msb-Api-Key" header. */
	private String apiKey;

	/** Optional cookie header. */
	private String cookie;

	public OnboardingApiClient(String endpoint) {
		this.endpoint = endpoint;
	}

	public OnboardingApiClient setBasicAuth(String token) {
//		String token = Base64.getEncoder()
//				.encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
		this.authorization = "Basic " + token;
		return this;
	}

	public OnboardingApiClient setAuthorizationHeader(String authorization) {
		this.authorization = authorization;
		return this;
	}

	public OnboardingApiClient setApiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}

	public OnboardingApiClient setCookie(String cookie) {
		this.cookie = cookie;
		return this;
	}

	public OnboardingApiClient setConnectTimeoutMs(int connectTimeoutMs) {
		this.connectTimeoutMs = connectTimeoutMs;
		return this;
	}

	public OnboardingApiClient setReadTimeoutMs(int readTimeoutMs) {
		this.readTimeoutMs = readTimeoutMs;
		return this;
	}

	/**
	 * Serializes the request to JSON and POSTs it to the configured endpoint.
	 *
	 * @param request the request payload object
	 * @return the HTTP status code and response body
	 * @throws IOException on network / serialization errors
	 */
	public ApiResponse initiateOnboarding(OnboardingRequest request) throws IOException {
		String jsonBody = objectMapper.writeValueAsString(request);
		return post(jsonBody);
	}

	/**
	 * Serializes the request, POSTs it and deserializes the JSON response into
	 * {@link OnboardingResponse}.
	 *
	 * @param request the request payload object
	 * @return the parsed response object
	 * @throws IOException on network / (de)serialization errors, or when the
	 *                     server returns a non-2xx status code
	 */
	public OnboardingResponse initiateOnboardingTyped(OnboardingRequest request) throws IOException {
		ApiResponse response = initiateOnboarding(request);
		if (!response.isSuccessful()) {
			throw new IOException("Onboarding API returned HTTP " + response.getStatusCode() + ": "
					+ response.getBody());
		}
		return objectMapper.readValue(response.getBody(), OnboardingResponse.class);
	}

	/**
	 * POSTs a raw JSON body to the endpoint.
	 */
	public ApiResponse post(String jsonBody) throws IOException {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(endpoint);
			conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setConnectTimeout(connectTimeoutMs);
			conn.setReadTimeout(readTimeoutMs);
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Accept", "application/json");
			if (authorization != null) {
				conn.setRequestProperty("Authorization", authorization);
			}
			if (apiKey != null) {
				conn.setRequestProperty("Msb-Api-Key", apiKey);
			}
			if (cookie != null) {
				conn.setRequestProperty("Cookie", cookie);
			}

			byte[] payload = jsonBody.getBytes(StandardCharsets.UTF_8);
			conn.setRequestProperty("Content-Length", String.valueOf(payload.length));
			try (OutputStream os = conn.getOutputStream()) {
				os.write(payload);
			}

			int statusCode = conn.getResponseCode();
			String responseBody = readBody(conn, statusCode);
			return new ApiResponse(statusCode, responseBody);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	/**
	 * Performs a GET request against the TableToJson "search_status" service and
	 * deserializes the response into {@link AmlQueryResponse}.
	 *
	 * @param baseUrl   the service URL without query string, e.g.
	 *                  "https://.../tabletojson/createtabletojson"
	 * @param mappingId value for the "mappingid" query parameter (e.g. "search_status")
	 * @param requestId value for the "requestid" query parameter (onboarding RequestId)
	 * @param caseId    value for the "caseid" query parameter (onboarding CaseId)
	 * @return the parsed response object
	 * @throws IOException on network / (de)serialization errors, or a non-2xx status
	 */
	public AmlQueryResponse amlQuery(String baseUrl, String mappingId, String requestId, String caseId)
			throws IOException {
		String url = baseUrl
				+ "?mappingId=" + encode(mappingId)
				+ "&requestId=" + encode(requestId)
				+ "&caseId=" + encode(caseId);
		ApiResponse response = get(url);
		if (!response.isSuccessful()) {
			throw new IOException("AML Query API returned HTTP " + response.getStatusCode() + ": "
					+ response.getBody());
		}
		return objectMapper.readValue(response.getBody(), AmlQueryResponse.class);
	}

	/**
	 * Performs a GET request against an absolute URL using the configured headers.
	 */
	public ApiResponse get(String url) throws IOException {
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("POST");
			conn.setConnectTimeout(connectTimeoutMs);
			conn.setReadTimeout(readTimeoutMs);
			conn.setRequestProperty("Accept", "application/json");
			if (authorization != null) {
				conn.setRequestProperty("Authorization", authorization);
			}
			if (apiKey != null) {
				conn.setRequestProperty("Msb-Api-Key", apiKey);
			}
			if (cookie != null) {
				conn.setRequestProperty("Cookie", cookie);
			}

			int statusCode = conn.getResponseCode();
			String responseBody = readBody(conn, statusCode);
			return new ApiResponse(statusCode, responseBody);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private static String encode(String value) {
		try {
			return java.net.URLEncoder.encode(value == null ? "" : value, "UTF-8");
		} catch (java.io.UnsupportedEncodingException e) {
			throw new IllegalStateException("UTF-8 not supported", e);
		}
	}

	private String readBody(HttpURLConnection conn, int statusCode) throws IOException {
		InputStream stream = (statusCode >= 200 && statusCode < 400) ? conn.getInputStream() : conn.getErrorStream();
		if (stream == null) {
			return "";
		}
		try (InputStream in = stream; ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
			byte[] chunk = new byte[4096];
			int read;
			while ((read = in.read(chunk)) != -1) {
				buffer.write(chunk, 0, read);
			}
			return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
		}
	}

	/** Simple holder for the HTTP status code and response body. */
	public static class ApiResponse {
		private final int statusCode;
		private final String body;

		public ApiResponse(int statusCode, String body) {
			this.statusCode = statusCode;
			this.body = body;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public String getBody() {
			return body;
		}

		public boolean isSuccessful() {
			return statusCode >= 200 && statusCode < 300;
		}

		@Override
		public String toString() {
			return "ApiResponse{statusCode=" + statusCode + ", body=" + body + "}";
		}
	}
}
