package me.mxngo.tiers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import com.google.gson.Gson;

public abstract class APIWrapper {
	protected final HttpClient httpClient = HttpClient.newHttpClient();
	protected final Gson gson = new Gson();
	
	protected abstract String getApiUrl();
	
	public String getApiUrl(APIEndpoint endpoint, String... args) {
		return getApiUrl().concat(endpoint.getPath(args));
	}
	
	protected CompletableFuture<HttpResponse<String>> fetch(APIEndpoint endpoint, String... args) {
		System.out.println("REQUESTING " + getApiUrl(endpoint, args));
		HttpRequest request = HttpRequest.newBuilder(URI.create(getApiUrl(endpoint, args))).GET().build();
		return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
	}
	
	protected interface APIEndpoint {
		public String getPath(String... args);
		default String getPath() {
			return getPath(new String[0]);
		}
	}
}
