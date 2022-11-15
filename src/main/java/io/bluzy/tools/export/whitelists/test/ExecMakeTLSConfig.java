package io.bluzy.tools.export.whitelists.test;

import java.io.File;
import java.net.Inet4Address;

import static io.bluzy.tools.export.whitelists.test.PingConfig.PING_ACCESS_HOST;
import static io.bluzy.tools.export.whitelists.test.PingConfig.PING_FEDERATE_HOST;

public class ExecMakeTLSConfig {

    public static void main(String[] args) throws Exception {

        String pingAccHost = Inet4Address.getByName(PING_ACCESS_HOST).getHostAddress();
        String pingFedHost = Inet4Address.getByName(PING_FEDERATE_HOST).getHostAddress();

        new File("jssecacerts").delete();

        InstallCert.main(new String[] {pingAccHost+":9000", "--quiet"});
        InstallCert.main(new String[] {pingFedHost+":9999", "--quiet"});
    }
}
