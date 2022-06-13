package io.bluzy.tools.export.whitelists;

import io.bluzy.tools.export.whitelists.auth.OAuthPasswordGrantFlow;
import io.bluzy.tools.export.whitelists.test.PingAccessContainer;
import io.bluzy.tools.export.whitelists.test.PingFederateContainer;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.apache.hc.core5.http.Method.PUT;
import static org.junit.jupiter.api.Assertions.*;

public class ExportIT {

    public static final String INTERFACE1 = "127.0.0.1";
    public static final String INTERFACE2 = "192.168.178.51";

    private static PingFederateContainer pingFederateContainer = new PingFederateContainer(false, INTERFACE2);

    private static PingAccessContainer pingAccessContainer = new PingAccessContainer(false, INTERFACE1);

    @Test
    void testToken() throws IOException {
        String token = new OAuthPasswordGrantFlow("https://idp.tst.pi.wien01.rbgi.at/as/token.oauth2",
                "WhiteListExporter", "Test1234!", "WhiteListExporter", "PAAPIRead").passwordGrantFlow();
        JSONObject jsonObject = new JSONObject(token);
        assertDoesNotThrow(()->jsonObject.get("access_token"), "OAuth call to obtain access token failed.");
    }

    @Test
    void test1App() throws Exception {
        Main.main(new String[]{"-zone", "TEST", "-app", "TestApp1"});

        byte[] bytesExported = Files.readAllBytes(Paths.get(Path.of("export.json").toUri()));
        byte[] bytesToTest = getClass().getClassLoader().getResourceAsStream("export1.json").readAllBytes();

        assertEquals(-1, Arrays.mismatch(bytesExported, bytesToTest));
    }

    @Test
    void testAllApps() throws Exception {
        Main.main(new String[] {"-zone", "TEST"});

        byte[] bytesExported = Files.readAllBytes(Paths.get(Path.of("export.json").toUri()));
        byte[] bytesToTest = getClass().getClassLoader().getResourceAsStream("exportAll.json").readAllBytes();

        assertEquals(-1, Arrays.mismatch(bytesExported, bytesToTest));
    }

    @BeforeAll
    static void beforeAll() throws IOException, URISyntaxException {
        PingFederateAPIConfigurator apiConfigurator = new PingFederateAPIConfigurator();

        apiConfigurator.applyConfigChange("https://localhost:9000/pa-admin-api/v3/webSessions", "/WebSession1.json", null);
        apiConfigurator.applyConfigChange("https://localhost:9000/pa-admin-api/v3/webSessions", "/WebSession2.json", null);

        apiConfigurator.applyConfigChange("https://localhost:9000/pa-admin-api/v3/sites", "/Site1.json", null);
        apiConfigurator.applyConfigChange("https://localhost:9000/pa-admin-api/v3/sites", "/Site2.json", null);

        String pfCertificates = apiConfigurator.getConfig("https://192.168.178.51:9999/pf-admin-api/v1/keyPairs/sslServer");
        String certId = new JSONObject(pfCertificates).getJSONArray("items").getJSONObject(0).getString("id");
        String cert = apiConfigurator.getConfig("https://192.168.178.51:9999/pf-admin-api/v1/keyPairs/sslServer/"+certId+"/certificate");
        cert = cert.lines().filter(l->!l.contains("CERTIFICATE")).collect(Collectors.joining());
        JSONObject certReq = new JSONObject();
        certReq.put("alias", "PingFederateCertificate");
        certReq.put("fileData", cert);
        apiConfigurator.postConfig("https://localhost:9000/pa-admin-api/v3/certificates", certReq.toString());
        apiConfigurator.applyConfigChange("https://localhost:9000/pa-admin-api/v3/trustedCertificateGroups", "/CertificateGroup.json", null);
        apiConfigurator.applyConfigChange("https://localhost:9000/pa-admin-api/v3/pingfederate/runtime", "/PingFederateRuntime.json", null, PUT);

        apiConfigurator.applyConfigChange("https://localhost:9000/pa-admin-api/v3/applications", "/Application1.json", null);
        apiConfigurator.applyConfigChange("https://localhost:9000/pa-admin-api/v3/applications", "/Application2.json", null);

        apiConfigurator.applyConfigChange("https://192.168.178.51:9999/pf-admin-api/v1/oauth/clients", "/oAuthClient1.json", null);
        apiConfigurator.applyConfigChange("https://192.168.178.51:9999/pf-admin-api/v1/oauth/clients", "/oAuthClient2.json", null);
    }

    @AfterAll
    static void afterAll() throws Exception {
        pingAccessContainer.shutDown();
        pingFederateContainer.shutDown();
    }
}
