package io.bluzy.tools.export.whitelists;

import io.bluzy.tools.export.whitelists.auth.Authentication;
import io.bluzy.tools.export.whitelists.auth.BasicAuthentication;
import io.bluzy.tools.export.whitelists.auth.BearerAuthentication;
import io.bluzy.tools.export.whitelists.config.ConfigProperties;
import io.bluzy.tools.export.whitelists.config.Params;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class RestAPIGetter {
    private static final Logger logger = LoggerFactory.getLogger(RestAPIGetter.class);

    private final OkHttpClient client = new OkHttpClient.Builder()
            .hostnameVerifier((hostname, session) -> true)
            .authenticator((route, response) -> {
                if (response.request().header(Params.AUTHORIZATION_HEADER) != null)
                    return null;

                return response.request().newBuilder().build();
            })
            .readTimeout(30, TimeUnit.SECONDS)
            .build();

    private final String baseUrl;

    private String credential;

    public RestAPIGetter(String baseUrl, ConfigProperties.AuthType authType, Authentication auth) {
        this.baseUrl = baseUrl;
        switch (authType) {
            case BASIC:
                BasicAuthentication basicAuth = (BasicAuthentication) auth;
                credential = Credentials.basic(basicAuth.getUser(), basicAuth.getPassword());
                break;
            case BEARER:
                BearerAuthentication bearerAuth = (BearerAuthentication) auth;
                credential = "Bearer " + bearerAuth.getBearerToken();
                break;
        }
    }

    public String getConfigJSON(String path) throws IOException {
        logger.info("access url: {}", baseUrl+path);

        Request request = new Request.Builder()
                .url(baseUrl+path)
                .get()
                .addHeader("X-XSRF-Header", "PingFederate")
                .addHeader(Params.AUTHORIZATION_HEADER, credential)
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
