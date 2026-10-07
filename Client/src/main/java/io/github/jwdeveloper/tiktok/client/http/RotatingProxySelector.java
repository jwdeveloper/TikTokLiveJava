/*
 * Copyright (c) 2023-2024 jwdeveloper jacekwoln@gmail.com
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
package io.github.jwdeveloper.tiktok.client.http;

import io.github.jwdeveloper.tiktok.api.data.dto.ProxyData;

import java.io.IOException;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/** The caller selects the endpoint; the JDK captures it when the send starts. */
final class RotatingProxySelector extends ProxySelector
{
    private final ThreadLocal<Proxy> boundProxy = new ThreadLocal<>();

    Proxy bind(ProxyData endpoint) {
        if (boundProxy.get() != null)
            throw new IllegalStateException("Nested proxy sends are not supported");
        var proxy = endpoint == null ? Proxy.NO_PROXY : new Proxy(Proxy.Type.HTTP, endpoint.toSocketAddress());
        boundProxy.set(proxy);
        return proxy;
    }

    void clear(Proxy proxy) {
        if (boundProxy.get() != proxy) {
            boundProxy.remove();
            throw new IllegalStateException("Proxy selection binding changed during request");
        }
        boundProxy.remove();
    }

    @Override
    public List<Proxy> select(URI uri) {
        Objects.requireNonNull(uri, "uri");
        var proxy = boundProxy.get();
        if (proxy == null)
            throw new IllegalStateException("An HTTP request was sent without a selected route");
		return List.of(proxy);
	}

    @Override
    public void connectFailed(URI uri, SocketAddress address, IOException exception) {
        // The request caller handles failures for its selected proxy.
    }
}