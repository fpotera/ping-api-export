package io.bluzy.tools.export.whitelists;

import io.bluzy.tools.export.whitelists.auth.BasicAuthentication;
import io.bluzy.tools.export.whitelists.auth.BearerAuthentication;
import io.bluzy.tools.export.whitelists.auth.OAuthPasswordGrantFlow;
import io.bluzy.tools.export.whitelists.config.ConfigProperties;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.google.common.collect.Streams.stream;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;

public class Exporter {

    private static final Logger logger = LoggerFactory.getLogger(Exporter.class);

    private String zoneName;
    private String appName;
    private Map<String, Map<String, String>> properties;

    private RestAPIGetter pfRestClient;
    private RestAPIGetter paRestClient;

    public Exporter(String zoneName, String appName, Map<String, Map<String, String>> properties) throws IOException {
        this.zoneName = zoneName;
        this.appName = appName;
        this.properties = properties;

        Map<String, String> zone = properties.get(zoneName);

        String authType = valueOf(properties.get(ConfigProperties.AUTH_TYPE));
        switch (ConfigProperties.AuthType.valueOf(authType)) {
            case BASIC:
                BasicAuthentication basicAuth = new BasicAuthentication(valueOf(properties.get(ConfigProperties.AUTH_USER)), valueOf(properties.get(ConfigProperties.AUTH_PASS)));
                pfRestClient = new RestAPIGetter(zone.get(ConfigProperties.ZONE_PF_BASEURL), ConfigProperties.AuthType.BASIC, basicAuth);
                paRestClient = new RestAPIGetter(zone.get(ConfigProperties.ZONE_PA_BASEURL), ConfigProperties.AuthType.BASIC, basicAuth);
                break;
            case BEARER:
                String jsonToken = new OAuthPasswordGrantFlow(valueOf(properties.get(ConfigProperties.OAUTH_PASSWORD_GRANT_URL)),
                        valueOf(properties.get(ConfigProperties.AUTH_USER)), valueOf(properties.get(ConfigProperties.AUTH_PASS)),
                        valueOf(properties.get(ConfigProperties.OAUTH_PASSWORD_GRANT_CLIENT_ID)), valueOf(properties.get(ConfigProperties.OAUTH_PASSWORD_GRANT_SCOPES)))
                        .passwordGrantFlow();
                JSONObject json = new JSONObject(jsonToken);
                BearerAuthentication bearerAuth = new BearerAuthentication(json.getString("access_token"));
                pfRestClient = new RestAPIGetter(zone.get(ConfigProperties.ZONE_PF_BASEURL), ConfigProperties.AuthType.BEARER, bearerAuth);
                paRestClient = new RestAPIGetter(zone.get(ConfigProperties.ZONE_PA_BASEURL), ConfigProperties.AuthType.BEARER, bearerAuth);
                break;
        }
    }

    public void export() {
        logger.info("--start export--");
        logger.info("zone: {} app: {}", zoneName, nonNull(appName)?appName:"ALL");

        try {
            JSONArray apps = new JSONArray();

            if(nonNull(appName)) {
                JSONObject app = buildApplication(appName, getApplications(), getSites(), getWebSessions(), getAppResources(),
                        getVirtualHosts(), getOAuthClients());
                apps.put(app);

                logger.debug("exported app: {}", app);
            }
            else {
                var applications = getApplications();
                var sites = getSites();
                var webSessions = getWebSessions();
                var appResources = getAppResources();
                var virtualHosts = getVirtualHosts();
                var oAuthClients = getOAuthClients();

                for (String application: extractApplications(applications)) {
                    JSONObject app = buildApplication(application, applications, sites, webSessions, appResources,
                            virtualHosts, oAuthClients);
                    apps.put(app);

                    logger.debug("exported app: {}", app);
                }
            }

            JSONObject export = new JSONObject();
            export.put("items", apps);

            String exportFile = Utils.cast((Object)properties.get(ConfigProperties.EXPORT_FILE));
            try(FileWriter fileWriter = new FileWriter(exportFile)) {
                export.write(fileWriter, 4, 1);
            }
        }
        catch (Exception ex) {
            logger.error("export error", ex);
            throw new RuntimeException(ex);
        }

        logger.info("--end export--");
    }

    private JSONObject buildApplication(String appName, JSONObject apps, JSONObject sites, JSONObject webSessions,
                                        JSONObject appResources, JSONObject virtualHosts, JSONObject oAuthClients) {

        JSONObject obj = new JSONObject();

        logger.debug("apps: {}", apps);
        logger.debug("sites: {}", sites);
        logger.debug("webSessions: {}", webSessions);
        logger.debug("appResources: {}", appResources);
        logger.debug("virtualHosts: {}", virtualHosts);
        logger.debug("oAuthClients: {}", oAuthClients);

        obj.put("name", appName);

        String contextRoot = extractContextRoot(appName, apps);
        obj.put("contextRoot", contextRoot);

        List<Integer> virtualHostIds =  extractVirtualHostIds(appName, apps);
        List<JSONObject> appVirtualHosts = extractVirtualHosts(virtualHostIds, virtualHosts);
        obj.put("virtualHosts", appVirtualHosts);

        Integer appId = extractApplicationId(appName, apps);
        JSONArray resources = extractResources(appId, appResources);
        obj.put("resources", resources);

        JSONArray fullPaths = extractFullPaths(appId, contextRoot, appResources);
        obj.put("fullPaths", fullPaths);

        Integer webSessionId = extractWebSessionId(appName, apps);
        String clientId = extractClientId(webSessionId, webSessions);
        if(nonNull(clientId)) {
            clientId = clientId.replace("-WEB", "");
            JSONObject oAuthClient = extractOAuthClient(clientId, oAuthClients);
            obj.put("client", oAuthClient);
        }

        return obj;
    }

    private JSONObject getApplications() throws Exception {
        String apps = paRestClient.getConfigJSON((String)(Object)properties.get(ConfigProperties.PA_APPLICATIONS_PATH));
        return new JSONObject(apps);
    }

    private JSONObject getSites() throws Exception {
        String sites = paRestClient.getConfigJSON((String)(Object)properties.get(ConfigProperties.PA_SITES_PATH));
        return new JSONObject(sites);
    }

    private JSONObject getWebSessions() throws Exception {
        String webSessions = paRestClient.getConfigJSON((String)(Object)properties.get(ConfigProperties.PA_WEB_SESSIONS_PATH));
        return new JSONObject(webSessions);
    }

    private JSONObject getAppResources() throws Exception {
        String appResources = paRestClient.getConfigJSON((String)(Object)properties.get(ConfigProperties.PA_APP_RESOURCES_PATH));
        return new JSONObject(appResources);
    }

    private JSONObject getVirtualHosts() throws Exception {
        String virtualHosts = paRestClient.getConfigJSON((String)(Object)properties.get(ConfigProperties.PA_VIRTUAL_HOSTS_PATH));
        return new JSONObject(virtualHosts);
    }

    private JSONObject getOAuthClients() throws Exception {
        String oAuthClients = pfRestClient.getConfigJSON((String)(Object)properties.get(ConfigProperties.PF_OAUTH_CLIENTS_PATH));
        return new JSONObject(oAuthClients);
    }

    private JSONArray extractFullPaths(Integer appId, String contextRoot, JSONObject appResources) {
        return stream(appResources.getJSONArray("items")).filter(o->((JSONObject)o)
                        .getInt("applicationId") == appId)
                .map(o->stream(((JSONObject) o).getJSONArray("pathPatterns"))
                        .map(p->contextRoot+((JSONObject)p).getString("pattern"))
                        .collect(JSONArray::new, JSONArray::put, JSONArray::putAll)
                )
                .collect(JSONArray::new, JSONArray::putAll, JSONArray::putAll);
    }

    private String extractClientId(Integer webSessionId, JSONObject webSessions) {
        return Utils.cast(stream(webSessions.getJSONArray("items"))
                .filter(o->((JSONObject)o)
                        .getInt("id") == webSessionId)
                .map(o->((JSONObject)o).get("clientCredentials"))
                .map(o->((JSONObject)o).getString("clientId"))
                .findFirst().orElse(null));
    }

    private Integer extractWebSessionId(String appName, JSONObject apps) {
        return Utils.cast(stream(apps.getJSONArray("items")).filter(o->((JSONObject)o)
                        .getString("name").equals(appName))
                .map(o->((JSONObject)o).get("webSessionId"))
                .findFirst().get());
    }
    private List<String> extractApplications(JSONObject apps) {
        return Utils.cast(stream(apps.getJSONArray("items"))
                .map(o->((JSONObject)o).get("name"))
                .collect(Collectors.toList()));
    }

    private List<Integer> extractVirtualHostIds(String appName, JSONObject apps) {
        return Utils.cast(stream(apps.getJSONArray("items")).filter(o->((JSONObject)o)
                        .getString("name").equals(appName))
                .map(o->((JSONObject)o).getJSONArray("virtualHostIds"))
                .map(JSONArray::toList).findFirst().get());
    }

    private List<JSONObject> extractVirtualHosts(List<Integer> virtualHostIds, JSONObject virtualHosts) {
        return Utils.cast(stream(virtualHosts.getJSONArray("items")).filter(o->virtualHostIds.contains(((JSONObject)o)
                .getInt("id"))).collect(Collectors.toList()));
    }

    private String extractContextRoot(String appName, JSONObject apps) {
        return Utils.cast(stream(apps.getJSONArray("items")).filter(o->((JSONObject)o)
                .getString("name").equals(appName))
                .map(o->((JSONObject)o).get("contextRoot"))
                .findFirst().get());
    }

    private Integer extractApplicationId(String appName, JSONObject apps) {
        return Utils.cast(stream(apps.getJSONArray("items")).filter(o->((JSONObject)o)
                        .getString("name").equals(appName))
                .map(o->((JSONObject)o).get("id"))
                .findFirst().get());
    }

    private JSONArray extractResources(Integer appId,  JSONObject appResources) {
        return stream(appResources.getJSONArray("items")).filter(o->((JSONObject)o)
                .getInt("applicationId") == appId)
                .map(o->{
                    JSONObject json = new JSONObject();
                    json.put("name", ((JSONObject) o).getString("name"));
                    json.put("paths", ((JSONObject) o).getJSONArray("pathPrefixes"));
                    return json;
                })
                .collect(JSONArray::new, JSONArray::put, JSONArray::putAll);
    }

    private JSONObject extractOAuthClient(String clientId, JSONObject oAuthClients) {
        return Utils.cast(stream(oAuthClients.getJSONArray("items")).filter(o->((JSONObject)o)
                        .getString("clientId").equals(clientId))
                .map(o->{
                    JSONObject json = new JSONObject();
                    json.put("clientId", ((JSONObject) o).getString("clientId"));
                    json.put("redirectUris", ((JSONObject) o).getJSONArray("redirectUris"));
                    json.put("grantTypes", ((JSONObject) o).getJSONArray("grantTypes"));
                    json.put("exclusiveScopes", ((JSONObject) o).getJSONArray("exclusiveScopes"));
                    return json;
                })
                .findFirst().orElse(null));
    }
}
