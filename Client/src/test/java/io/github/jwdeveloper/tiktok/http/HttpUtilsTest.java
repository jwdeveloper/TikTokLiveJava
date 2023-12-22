package io.github.jwdeveloper.tiktok.http;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.util.*;

public class HttpUtilsTest
{
	@Test
	public void parseParameters_EmptyParameters_ShouldHaveNoParameters()
	{
		String parsed = HttpUtils.parseParameters("https://webcast.tiktok.com/webcast/im/fetch/", new HashMap<>());

		Assertions.assertEquals("https://webcast.tiktok.com/webcast/im/fetch/", parsed);
	}

	@Test
	public void parseParameters_ValidParameters_ShouldConstructValidURL()
	{
		LinkedHashMap<String, Object> testMap = new LinkedHashMap<>();
		testMap.put("room_id", 1);
		testMap.put("uniqueId", "randomName");
		String parsed = HttpUtils.parseParameters("https://webcast.tiktok.com/webcast/im/fetch/", testMap);

		Assertions.assertEquals("https://webcast.tiktok.com/webcast/im/fetch/?room_id=1&uniqueId=randomName", parsed);
	}

	@Test
	public void parseParametersEncode_EmptyParameters_ShouldHaveNoParameters()
	{
		String parsed = HttpUtils.parseParametersEncode("https://webcast.tiktok.com/webcast/im/fetch/", new HashMap<>());

		Assertions.assertEquals("https://webcast.tiktok.com/webcast/im/fetch/", parsed);
	}

	@Test
	public void parseParametersEncode_ValidParameters_ShouldConstructValidURL()
	{
		LinkedHashMap<String, Object> testMap = new LinkedHashMap<>();
		testMap.put("room_id", 1);
		testMap.put("root_referer", "https://www.tiktok.com/");
		String parsed = HttpUtils.parseParametersEncode("https://webcast.tiktok.com/webcast/im/fetch/", testMap);

		Assertions.assertEquals("https://webcast.tiktok.com/webcast/im/fetch/?room_id=1&root_referer=https%3A%2F%2Fwww.tiktok.com%2F", parsed);
	}
}