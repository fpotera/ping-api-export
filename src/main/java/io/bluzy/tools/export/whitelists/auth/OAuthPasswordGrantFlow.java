package io.bluzy.tools.export.whitelists.auth;

import okhttp3.*;

import java.io.IOException;

public class OAuthPasswordGrantFlow {
    private String user;
    private String password;
    private String clientId;
    private String scopes;
    private String url;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .hostnameVerifier((hostname, session) -> true)
            .build();

    public OAuthPasswordGrantFlow(String url, String user, String password, String clientId, String scopes) {
        this.user = user;
        this.password = password;
        this.clientId = clientId;
        this.scopes = scopes;
        this.url = url;
    }

    public String passwordGrantFlow() throws IOException {
        RequestBody formBody = new FormBody.Builder()
                .add("username", user)
                .add("password", password)
                .add("grant_type", "password")
                .add("client_id", clientId)
                .add("scope", scopes)
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(formBody)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }
}
