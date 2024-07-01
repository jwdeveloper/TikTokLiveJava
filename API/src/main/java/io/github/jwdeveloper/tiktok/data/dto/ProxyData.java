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
package io.github.jwdeveloper.tiktok.data.dto;

import lombok.*;

import java.net.*;

@Data
@AllArgsConstructor
public class ProxyData
{
	private final String address;
	private final int port;

	public static ProxyData map(String string) {
		if (string == null || string.isBlank())
			throw new IllegalArgumentException("Provided address cannot be null or empty!");
		int portIndex = string.lastIndexOf(':');
		try {
			String address = string.substring(0, portIndex);
			int port = Integer.parseInt(string.substring(portIndex+1));

			// Port validation
			if (port < 0 || port > 65535)
				throw new IndexOutOfBoundsException("Port out of range: "+port);

			// IP Validation
			InetAddress res = InetAddress.getByName(address);

			return new ProxyData(address, port);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Port must be a valid integer!", e);
		} catch (UnknownHostException e) {
			throw new IllegalArgumentException("Address must be valid IPv4, IPv6, or domain name!", e);
		}
	}

	public InetSocketAddress toSocketAddress() {
		return new InetSocketAddress(address, port);
	}
}