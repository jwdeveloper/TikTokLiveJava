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