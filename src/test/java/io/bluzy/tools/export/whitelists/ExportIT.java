package io.bluzy.tools.export.whitelists;

import org.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.stream.Collectors;

import static io.bluzy.tools.export.whitelists.config.URLs.*;
import static io.bluzy.tools.export.whitelists.test.PingConfig.PING_FEDERATE_HOST;
import static java.text.MessageFormat.format;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;
import static org.apache.hc.core5.http.Method.PUT;
import static org.junit.jupiter.api.Assertions.*;

public class ExportIT {

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

        apiConfigurator.applyConfigChange(PING_ACC_WEB_SESSIONS_URL, "/WebSession1.json", null);
        apiConfigurator.applyConfigChange(PING_ACC_WEB_SESSIONS_URL, "/WebSession2.json", null);

        apiConfigurator.applyConfigChange(PING_ACC_SITES_URL, "/Site1.json", null);
        apiConfigurator.applyConfigChange(PING_ACC_SITES_URL, "/Site2.json", null);

        String pfCertificates = apiConfigurator.getConfig(PING_FED_SSL_SERV_URL);
        String certId = new JSONObject(pfCertificates).getJSONArray("items").getJSONObject(0).getString("id");
        String PING_FED_SSL_SERV_CERT_URL = format("https://{0}:9999/pf-admin-api/v1/keyPairs/sslServer/{1}/certificate", PING_FEDERATE_HOST, certId);
        String cert = apiConfigurator.getConfig(PING_FED_SSL_SERV_CERT_URL);
        cert = cert.lines().filter(l->!l.contains("CERTIFICATE")).collect(Collectors.joining());
        JSONObject certReq = new JSONObject();
        certReq.put("alias", "PingFederateCertificate");
        certReq.put("fileData", cert);
        apiConfigurator.postConfig(PING_ACC_CERTIFICATES_URL, certReq.toString());
        apiConfigurator.applyConfigChange(PING_ACC_CERT_GROUPS_URL, "/CertificateGroup.json", null);
        apiConfigurator.applyConfigChange(PING_ACC_FED_RUNTIME_URL, "/PingFederateRuntime.json", null, PUT);

        apiConfigurator.applyConfigChange(PING_ACC_APPLICATIONS_URL, "/Application1.json", null);
        apiConfigurator.applyConfigChange(PING_ACC_APPLICATIONS_URL, "/Application2.json", null);

        apiConfigurator.applyConfigChange(PING_FED_CLIENTS_URL, "/oAuthClient1.json", null);
        apiConfigurator.applyConfigChange(PING_FED_CLIENTS_URL, "/oAuthClient2.json", null);
    }
}
