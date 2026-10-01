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
package io.github.jwdeveloper.tiktok.data.settings;

import io.github.jwdeveloper.tiktok.data.dto.ProxyData;
import lombok.*;

import java.net.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@Getter
@Setter
public class ProxyClientSettings implements Iterator<ProxyData>, Iterable<ProxyData>
{
    private volatile boolean enabled, autoDiscard = true, fallback = true, allowWebsocket = true;
    private volatile Rotation rotation = Rotation.CONSECUTIVE;
    private final List<ProxyData> proxyList = new ArrayList<>();
    private int index;
    private volatile Proxy.Type type = Proxy.Type.DIRECT;
    private volatile Consumer<ProxyData> onProxyUpdated = x -> {};
    @Getter(AccessLevel.NONE)
    private final ThreadLocal<ProxyData> lastSelectedProxy = new ThreadLocal<>();

    public boolean addProxy(String addressPort) {
        return addProxy(ProxyData.map(addressPort).toSocketAddress());
    }

    public boolean addProxy(String address, int port) {
        return addProxy(new InetSocketAddress(address, port));
    }

    public synchronized boolean addProxy(InetSocketAddress inetAddress) {
        return proxyList.add(new ProxyData(inetAddress.getHostString(), inetAddress.getPort()));
    }

    public void addProxies(List<String> list) {
        list.forEach(this::addProxy);
    }

    /**
     * Returns the mutable list for compatibility. Use {@link #getProxiesSnapshot()}
     * for iteration while requests are selecting or discarding proxies, and use
     * {@code addProxy} and {@code remove} to change the list concurrently.
     */
    public synchronized List<ProxyData> getProxyList() {
        return proxyList;
    }

    public synchronized List<ProxyData> getProxiesSnapshot() {
        return List.copyOf(proxyList);
    }

    public synchronized int getProxyCount() {
        return proxyList.size();
    }

    @Override
    public synchronized boolean hasNext() {
        return !proxyList.isEmpty();
    }

    @Override
    public synchronized ProxyData next() {
        if (proxyList.isEmpty())
            throw new NoSuchElementException("No more proxies available!");

        normalizeIndex();
        if (rotation == Rotation.RANDOM)
            index = ThreadLocalRandom.current().nextInt(proxyList.size());

        var nextProxy = proxyList.get(index);
        if (rotation == Rotation.CONSECUTIVE)
            index = (index + 1) % proxyList.size();

        lastSelectedProxy.set(nextProxy);
        onProxyUpdated.accept(nextProxy);
        return nextProxy;
    }

    /** Removes the most recent proxy selected by this thread. */
    @Override
    public synchronized void remove() {
        var selectedProxy = lastSelectedProxy.get();
        if (selectedProxy == null)
            throw new IllegalStateException("Call next() before removing a proxy!");
        lastSelectedProxy.remove();
        remove(selectedProxy);
    }

    /**
     * Removes the selected endpoint even if another request has advanced rotation.
     * Returns false if another request has already removed it.
     */
    public synchronized boolean remove(ProxyData proxyData) {
        Objects.requireNonNull(proxyData, "proxyData");
        if (proxyData.equals(lastSelectedProxy.get()))
            lastSelectedProxy.remove();

        var removedIndex = proxyList.indexOf(proxyData);
        if (removedIndex < 0)
            return false;

        proxyList.remove(removedIndex);
        if (removedIndex < index)
            index--;
        normalizeIndex();
        return true;
    }

    private void normalizeIndex() {
        index = proxyList.isEmpty() ? 0 : Math.floorMod(index, proxyList.size());
    }

    public synchronized int getIndex() {
        normalizeIndex();
        return index;
    }

    public synchronized void setIndex(int index) {
        if (index == 0 && proxyList.isEmpty())
            this.index = 0;
        else {
            if (index < 0 || index >= proxyList.size())
                throw new IndexOutOfBoundsException("Index " + index + " exceeds list of size: " + proxyList.size());
            this.index = index;
        }
    }

    public synchronized void setRotation(Rotation rotation) {
        this.rotation = Objects.requireNonNull(rotation, "rotation");
    }

    @Override
    public synchronized ProxyClientSettings clone() {
        ProxyClientSettings settings = new ProxyClientSettings();
        settings.setEnabled(enabled);
        settings.setAutoDiscard(autoDiscard);
        settings.setFallback(fallback);
        settings.setAllowWebsocket(allowWebsocket);
        settings.setRotation(rotation);
        settings.setType(type);
        settings.setOnProxyUpdated(onProxyUpdated);
        settings.proxyList.addAll(proxyList);
        normalizeIndex();
        settings.setIndex(index);
        return settings;
    }

    @Override
    public synchronized String toString() {
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
