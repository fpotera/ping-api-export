package io.bluzy.tools.export.whitelists.auth;

public class BasicAuthentication implements Authentication {
    private String user;
    private String password;

    public BasicAuthentication(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }
}
