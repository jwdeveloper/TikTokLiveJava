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
import io.github.jwdeveloper.tiktok.data.settings.*;
import io.github.jwdeveloper.tiktok.exceptions.*;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.ResponseInfo;
import java.security.*;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

public class HttpProxyClient extends HttpClient {

	private final ProxyClientSettings proxySettings;

	public HttpProxyClient(HttpClientSettings httpClientSettings, String url) {
		super(httpClientSettings, url);
		this.proxySettings = httpClientSettings.getProxyClientSettings();
	}

	public ActionResult<HttpResponse<byte[]>> toResponse() {
		return switch (proxySettings.getType()) {
			case HTTP, DIRECT -> handleHttpProxyRequest();
			default -> handleSocksProxyRequest();
		};
	}

	public ActionResult<HttpResponse<byte[]>> handleHttpProxyRequest() {
		var builder = java.net.http.HttpClient.newBuilder()
			.followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
			.cookieHandler(new CookieManager())
			.connectTimeout(httpClientSettings.getTimeout());

		while (proxySettings.hasNext()) {
			try {
				InetSocketAddress address = proxySettings.next().toSocketAddress();
				builder.proxy(ProxySelector.of(address));

				httpClientSettings.getOnClientCreating().accept(builder);
				var client = builder.build();
				var request = prepareGetRequest();

				var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
				if (response.statusCode() != 200)
					continue;
				return ActionResult.success(response);
			} catch (HttpConnectTimeoutException | ConnectException e) {
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

	private ActionResult<HttpResponse<byte[]>> handleSocksProxyRequest() {
		try {
			SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, new TrustManager[]{ new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {}
				public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {}
				public X509Certificate[] getAcceptedIssuers() { return null; }
			}}, null);

			URL url = toUri().toURL();

			if (proxySettings.hasNext()) {
				try {
					Proxy proxy = new Proxy(Proxy.Type.SOCKS, proxySettings.next().toSocketAddress());

					HttpsURLConnection socksConnection = (HttpsURLConnection) url.openConnection(proxy);
					socksConnection.setSSLSocketFactory(sc.getSocketFactory());
					socksConnection.setConnectTimeout(httpClientSettings.getTimeout().toMillisPart());
					socksConnection.setReadTimeout(httpClientSettings.getTimeout().toMillisPart());
					httpClientSettings.getHeaders().forEach(socksConnection::setRequestProperty);

					byte[] body = socksConnection.getInputStream().readAllBytes();

					Map<String, List<String>> headers = socksConnection.getHeaderFields()
						.entrySet()
						.stream()
						.filter(entry -> entry.getKey() != null)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

					var responseInfo = createResponseInfo(socksConnection.getResponseCode(), headers);

					var response = createHttpResponse(body, toUri(), responseInfo);

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

	private ResponseInfo createResponseInfo(int code, Map<String, List<String>> headers) {
		return new ResponseInfo() {
			@Override
			public int statusCode() {
				return code;
			}

			@Override
			public HttpHeaders headers() {
				return HttpHeaders.of(headers, (s, s1) -> s != null);
			}

			@Override
			public java.net.http.HttpClient.Version version() {
				return java.net.http.HttpClient.Version.HTTP_2;
			}
		};
	}

	private HttpResponse<byte[]> createHttpResponse(byte[] body,
												   URI uri,
												   ResponseInfo info) {
		return new HttpResponse<>()
		{
			@Override
			public int statusCode() {
				return info.statusCode();
			}

			@Override
			public HttpRequest request() {
				throw new UnsupportedOperationException("TODO");
			}

			@Override
			public Optional<HttpResponse<byte[]>> previousResponse() {
				return Optional.empty();
			}

			@Override
			public HttpHeaders headers() {
				return info.headers();
			}

			@Override
			public byte[] body() {
				return body;
			}

			@Override
			public Optional<SSLSession> sslSession() {
				throw new UnsupportedOperationException("TODO");
			}

			@Override
			public URI uri() {
				return uri;
			}

			@Override
			public java.net.http.HttpClient.Version version() {
				return info.version();
			}
		};
	}
}