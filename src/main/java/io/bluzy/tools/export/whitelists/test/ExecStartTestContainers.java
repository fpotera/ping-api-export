package io.bluzy.tools.export.whitelists.test;

import java.io.File;

public class ExecStartTestContainers {

    public static final String INTERFACE1 = "127.0.0.1";
    public static final String INTERFACE2 = "192.168.178.51";

    private static PingFederateContainer pingFederateContainer = new PingFederateContainer(false, INTERFACE2);

    private static PingAccessContainer pingAccessContainer = new PingAccessContainer(false, INTERFACE1);

    public static void main(String[] args) throws Exception {

        pingFederateContainer.startUp(true);
        pingAccessContainer.startUp(true);

        new File("jssecacerts").delete();

        InstallCert.main(new String[] {INTERFACE1+":9000", "--quiet"});
        InstallCert.main(new String[] {INTERFACE2+":9999", "--quiet"});
        InstallCert.main(new String[] {"10.100.16.103:443", "--quiet"});
    }
}
