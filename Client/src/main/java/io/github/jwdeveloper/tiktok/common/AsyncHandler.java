package io.github.jwdeveloper.tiktok.common;

import lombok.Getter;

import java.util.concurrent.*;

public class AsyncHandler
{
	@Getter
	private static final ScheduledExecutorService heartBeatScheduler = Executors.newScheduledThreadPool(1, r -> {
		Thread t = new Thread(r, "heartbeat-pool");
		t.setDaemon(true);
		return t;
	});

	@Getter
	private static final ScheduledExecutorService reconnectScheduler = Executors.newScheduledThreadPool(0, r -> {
		Thread t = new Thread(r, "reconnect-pool");
		t.setDaemon(true);
		return t;
	});
}