package io.bluzy.tools.export.whitelists.auth;

public class BearerAuthentication implements Authentication {
    private String bearerToken;

    public BearerAuthentication(String bearerToken) {
        this.bearerToken = bearerToken;
    }

    public String getBearerToken() {
        return bearerToken;
    }
}
