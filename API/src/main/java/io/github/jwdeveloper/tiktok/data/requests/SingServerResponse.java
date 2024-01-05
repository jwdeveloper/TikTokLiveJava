package io.github.jwdeveloper.tiktok.data.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SingServerResponse
{
    private String signedUrl;

    private String userAgent;
}
