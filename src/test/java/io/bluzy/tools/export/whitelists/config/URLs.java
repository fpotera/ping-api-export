package io.bluzy.tools.export.whitelists.config;

import static io.bluzy.tools.export.whitelists.test.PingConfig.PING_ACCESS_HOST;
import static io.bluzy.tools.export.whitelists.test.PingConfig.PING_FEDERATE_HOST;
import static java.text.MessageFormat.format;

public interface URLs {
    String PING_ACC_WEB_SESSIONS_URL = format("https://{0}:9000/pa-admin-api/v3/webSessions", PING_ACCESS_HOST);

    String PING_ACC_SITES_URL = format("https://{0}:9000/pa-admin-api/v3/sites", PING_ACCESS_HOST);

    String PING_ACC_CERTIFICATES_URL = format("https://{0}:9000/pa-admin-api/v3/certificates", PING_ACCESS_HOST);

    String PING_ACC_CERT_GROUPS_URL = format("https://{0}:9000/pa-admin-api/v3/trustedCertificateGroups", PING_ACCESS_HOST);

    String PING_ACC_FED_RUNTIME_URL = format("https://{0}:9000/pa-admin-api/v3/pingfederate/runtime", PING_ACCESS_HOST);

    String PING_ACC_APPLICATIONS_URL = format("https://{0}:9000/pa-admin-api/v3/applications", PING_ACCESS_HOST);

    String PING_FED_SSL_SERV_URL = format("https://{0}:9999/pf-admin-api/v1/keyPairs/sslServer", PING_FEDERATE_HOST);

    String PING_FED_CLIENTS_URL = format("https://{0}:9999/pf-admin-api/v1/oauth/clients", PING_FEDERATE_HOST);
}
