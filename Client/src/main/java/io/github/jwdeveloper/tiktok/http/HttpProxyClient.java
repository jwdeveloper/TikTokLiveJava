/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.common.ActionResult;
import io.github.jwdeveloper.tiktok.data.settings.HttpClientSettings;
import io.github.jwdeveloper.tiktok.data.settings.ProxyClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokProxyRequestException;
import okhttp3.*;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HttpProxyClient extends HttpClient {

	private final ProxyClientSettings proxySettings;

	public HttpProxyClient(HttpClientSettings httpClientSettings, String url) {
		super(httpClientSettings, url);
		this.proxySettings = httpClientSettings.getProxyClientSettings();
	}

	public ActionResult<Response> toResponse() {
		switch (proxySettings.getType()) {
			case HTTP:
			case DIRECT:
				return this.handleHttpProxyRequest();
			default: return this.handleSocksProxyRequest();
		}
	}

	public ActionResult<Response> handleHttpProxyRequest() {
		OkHttpClient.Builder builder = new OkHttpClient.Builder()
			.followRedirects(true)
			.followSslRedirects(true)
			.cookieJar(new JavaNetCookieJar(new CookieManager()))
			.connectTimeout(httpClientSettings.getTimeout());

		while (proxySettings.hasNext()) {
			try {
				InetSocketAddress address = proxySettings.next().toSocketAddress();
				builder.proxy(new Proxy(Proxy.Type.HTTP, address));

				httpClientSettings.getOnClientCreating().accept(builder);
				OkHttpClient client = builder.build();
				Request request = this.prepareGetRequest();

				Response response = client.newCall(request).execute();
				if (response.code() != 200)
					continue;
				return ActionResult.success(response);
			} catch (SocketTimeoutException | ConnectException e) {
				if (proxySettings.isAutoDiscard())
					proxySettings.remove();
				throw new TikTokProxyRequestException(e);
			} catch (IOException e) {
				if (e.getMessage().contains("503") && proxySettings.isFallback()) // Indicates proxy protocol is not supported
					return super.toResponse();
				throw new TikTokProxyRequestException(e);
			} catch (Exception e) {
				throw new TikTokLiveRequestException(e);
			}
		}
		throw new TikTokLiveRequestException("No more proxies available!");
	}

	private ActionResult<Response> handleSocksProxyRequest() {
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[]{ new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}
				public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}
				public X509Certificate[] getAcceptedIssuers() { return null; }
			}}, null);

			URL url = this.toUrl().toURL();

			if (proxySettings.hasNext()) {
				try {
					Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxySettings.next().toSocketAddress());

					HttpsURLConnection socksConnection = (HttpsURLConnection) url.openConnection(proxy);
					socksConnection.setSSLSocketFactory(sc.getSocketFactory());
					socksConnection.setConnectTimeout((int) httpClientSettings.getTimeout().toMillis());
					socksConnection.setReadTimeout((int) httpClientSettings.getTimeout().toMillis());

					InputStream socksInputStream = socksConnection.getInputStream();
					byte[] body = new byte[socksInputStream.available()];
					DataInputStream dataInputStream = new DataInputStream(socksInputStream);
					dataInputStream.readFully(body);

					Map<String, String> headers = socksConnection.getHeaderFields()
						.entrySet()
						.stream()
						.filter(entry -> entry.getKey() != null)
						.flatMap(entry -> Stream.of(new AbstractMap.SimpleEntry<String, String>(entry.getKey(), String.join(", ", entry.getValue()))))
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

					Response response = this.createHttpResponse(body, this.toUrl(), socksConnection.getResponseCode(), headers);

					return ActionResult.success(response);
				} catch (IOException e) {
					if (e.getMessage().contains("503") && proxySettings.isFallback()) // Indicates proxy protocol is not supported
						return super.toResponse();
					if (proxySettings.isAutoDiscard())
						proxySettings.remove();
					throw new TikTokProxyRequestException(e);
				} catch (Exception e) {
					throw new TikTokLiveRequestException(e);
				}
			}
			throw new TikTokLiveRequestException("No more proxies available!");
		} catch (NoSuchAlgorithmException | MalformedURLException | KeyManagementException e) {
			// Should never be reached!
			System.out.println("handleSocksProxyRequest: If you see this, message us on discord!");
			e.printStackTrace();
		} catch (TikTokLiveRequestException e) {
			e.printStackTrace();
		}
		return ActionResult.failure();
	}

	private Response createHttpResponse(byte[] body,
										URI uri,
										int code,
										Map<String, String> headers)
	{
		Request request = new Request.Builder()
				.url(HttpUrl.get(uri))
				.headers(Headers.of(headers))
				.build();
		return new Response.Builder()
				.protocol(Protocol.HTTP_2)
				.code(code)
				.request(request)
				.body(ResponseBody.create(body, MediaType.get("text/plain")))
				.build();
	}
}
