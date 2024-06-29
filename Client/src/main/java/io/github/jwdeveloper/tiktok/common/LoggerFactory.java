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
package io.github.jwdeveloper.tiktok.common;

import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.utils.ConsoleColors;

import java.util.logging.*;

public class LoggerFactory
{
	public static Logger create(String name, LiveClientSettings settings) {
		Logger logger = Logger.getLogger(name);
		if (logger.getHandlers().length == 0) {
			var handler = new ConsoleHandler();
			handler.setFormatter(new Formatter()
			{
				@Override
				public String format(LogRecord record) {
					var sb = new StringBuilder();
					sb.append(ConsoleColors.GREEN).append("[").append(record.getLoggerName()).append("] ");
					sb.append(ConsoleColors.GREEN).append("[").append(record.getLevel()).append("]: ");
					sb.append(ConsoleColors.WHITE_BRIGHT).append(record.getMessage());
					sb.append(ConsoleColors.RESET).append("\n");
					return sb.toString();
				}
			});
			logger.setUseParentHandlers(false);
			logger.addHandler(handler);
			logger.setLevel(settings.getLogLevel());
			if (!settings.isPrintToConsole())
				logger.setLevel(Level.OFF);
		}
		return logger;
	}
}