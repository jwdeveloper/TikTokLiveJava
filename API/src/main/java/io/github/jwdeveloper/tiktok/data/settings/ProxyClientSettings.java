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
package io.github.jwdeveloper.tiktok.data.settings;

import io.github.jwdeveloper.tiktok.data.dto.ProxyData;
import lombok.*;

import java.net.*;
import java.util.*;
import java.util.function.Consumer;

@Getter
@Setter
public class ProxyClientSettings implements Iterator<ProxyData>, Iterable<ProxyData>
{
    private boolean enabled, autoDiscard = true, fallback = true;
    private Rotation rotation = Rotation.CONSECUTIVE;
    private final List<ProxyData> proxyList = new ArrayList<>();
    private int index;
    private Proxy.Type type = Proxy.Type.DIRECT;
    private Consumer<ProxyData> onProxyUpdated = x -> {};

    public boolean addProxy(String addressPort) {
        return addProxy(ProxyData.map(addressPort).toSocketAddress());
    }

    public boolean addProxy(String address, int port) {
        return addProxy(new InetSocketAddress(address, port));
    }

    public boolean addProxy(InetSocketAddress inetAddress) {
        return proxyList.add(new ProxyData(inetAddress.getHostString(), inetAddress.getPort()));
    }

    public void addProxies(List<String> list) {
        list.forEach(this::addProxy);
    }

    @Override
    public synchronized boolean hasNext() {
        return !proxyList.isEmpty();
    }

    @Override
    public synchronized ProxyData next() {
        try {
            var nextProxy = proxyList.get(index);
            onProxyUpdated.accept(nextProxy);
            return nextProxy;
        } finally {
            switch (rotation) {
                case CONSECUTIVE -> index = ++index % proxyList.size();
                case RANDOM -> index = (int) (Math.random() * proxyList.size());
                case NONE -> index = Math.max(index, 0);
            }
        }
    }

    @Override
    public synchronized void remove() {
        proxyList.remove(index);
    }

    public void setIndex(int index) {
        if (index == 0 && proxyList.isEmpty())
            this.index = 0;
        else {
            if (index < 0 || index >= proxyList.size())
                throw new IndexOutOfBoundsException("Index " + index + " exceeds list of size: " + proxyList.size());
            this.index = index;
        }
    }

    @Override
    public ProxyClientSettings clone() {
        ProxyClientSettings settings = new ProxyClientSettings();
        settings.setEnabled(enabled);
        settings.setRotation(rotation);
        settings.setIndex(index);
        settings.setType(type);
        settings.setOnProxyUpdated(onProxyUpdated);
        proxyList.forEach(proxyData -> settings.addProxy(proxyData.getAddress(), proxyData.getPort()));
        return settings;
    }

    @Override
    public String toString() {
        return "ProxyClientSettings{" +
            "enabled=" + enabled +
            ", autoDiscard=" + autoDiscard +
            ", fallback=" + fallback +
            ", rotation=" + rotation +
            ", proxyList=" + proxyList +
            ", index=" + index +
            ", type=" + type +
            '}';
    }

    /**
     * With {@code Iterable<ProxyData>} interface, you can use this object inside for loop!
     */
    @Override
    public Iterator<ProxyData> iterator() {
        return this;
    }

    public enum Rotation
    {
        /** Rotate addresses consecutively, from proxy 0 -> 1 -> 2 -> ...etc. */
        CONSECUTIVE,
        /** Rotate addresses randomly, from proxy 0 -> 69 -> 420 -> 1 -> ...etc. */
        RANDOM,
        /** Don't rotate addresses at all, pin to the indexed address. */
        NONE
    }
}