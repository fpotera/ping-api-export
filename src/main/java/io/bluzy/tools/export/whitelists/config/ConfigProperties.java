package io.bluzy.tools.export.whitelists.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.stream.IntStream.range;

public class ConfigProperties {
    public static final String ZONES = "zones";

    public static final String ZONE_X_NAME = "zone.%s.name";

    public static final String ZONE_X_PF_BASEURL = "zone.%s.pf.baseurl";
    public static final String ZONE_PF_BASEURL = "zone.pf.baseurl";
    public static final String ZONE_X_PA_BASEURL = "zone.%s.pa.baseurl";
    public static final String ZONE_PA_BASEURL = "zone.pa.baseurl";

    public static final String PA_SITES_PATH = "pa.sites.path";
    public static final String PA_APPLICATIONS_PATH = "pa.applications.path";
    public static final String PA_WEB_SESSIONS_PATH = "pa.web.sessions.path";
    public static final String PA_VIRTUAL_HOSTS_PATH = "pa.virtual.hosts.path";
    public static final String PA_APP_RESOURCES_PATH = "pa.app.resources.path";
    public static final String PF_OAUTH_CLIENTS_PATH = "pf.oauth.clients.path";

    public static final String EXPORT_FILE = "export.file";

    public static final String AUTH_TYPE = "auth.type";
    public static final String AUTH_USER = "auth.user";
    public static final String AUTH_PASS = "auth.pass";
    public static final String OAUTH_PASSWORD_GRANT_URL = "oauth.password_grant.url";
    public static final String OAUTH_PASSWORD_GRANT_CLIENT_ID = "oauth.password_grant.client.id";
    public static final String OAUTH_PASSWORD_GRANT_SCOPES = "oauth.password_grant.scopes";

    public static final String LOG_LEVEL_ROOT = "log.level.root";

    public enum AuthType { BASIC, BEARER }

    public static Map<String, Object> loadProperties(InputStream input) throws IOException {
        Properties prop = new Properties();
        prop.load(input);

        int zones = Integer.parseInt(prop.getProperty(ZONES));
        Map<String, Object> properties = range(0, zones).mapToObj(i->{
            Map<String, String> map = new HashMap<>();
            map.put(ZONE_PF_BASEURL, prop.getProperty(format(ZONE_X_PF_BASEURL, valueOf(i))));
            map.put(ZONE_PA_BASEURL, prop.getProperty(format(ZONE_X_PA_BASEURL, valueOf(i))));

            return new AbstractMap.SimpleImmutableEntry<>(prop.getProperty(format(ZONE_X_NAME, valueOf(i))), map);
        }).collect(Collectors.toMap(AbstractMap.SimpleImmutableEntry::getKey, AbstractMap.SimpleImmutableEntry::getValue));

        properties.put(PA_APPLICATIONS_PATH, prop.getProperty(PA_APPLICATIONS_PATH));
        properties.put(PA_SITES_PATH, prop.getProperty(PA_SITES_PATH));
        properties.put(PA_VIRTUAL_HOSTS_PATH, prop.getProperty(PA_VIRTUAL_HOSTS_PATH));
        properties.put(PA_WEB_SESSIONS_PATH, prop.getProperty(PA_WEB_SESSIONS_PATH));
        properties.put(PA_APP_RESOURCES_PATH, prop.getProperty(PA_APP_RESOURCES_PATH));
        properties.put(PF_OAUTH_CLIENTS_PATH, prop.getProperty(PF_OAUTH_CLIENTS_PATH));

        properties.put(EXPORT_FILE, prop.getProperty(EXPORT_FILE));

        properties.put(AUTH_TYPE, prop.getProperty(AUTH_TYPE));
        properties.put(AUTH_USER, prop.getProperty(AUTH_USER));
        properties.put(AUTH_PASS, prop.getProperty(AUTH_PASS));
        properties.put(OAUTH_PASSWORD_GRANT_URL, prop.getProperty(OAUTH_PASSWORD_GRANT_URL));
        properties.put(OAUTH_PASSWORD_GRANT_CLIENT_ID, prop.getProperty(OAUTH_PASSWORD_GRANT_CLIENT_ID));
        properties.put(OAUTH_PASSWORD_GRANT_SCOPES, prop.getProperty(OAUTH_PASSWORD_GRANT_SCOPES));

        properties.put(LOG_LEVEL_ROOT, prop.getProperty(LOG_LEVEL_ROOT));

        return properties;
    }
}
