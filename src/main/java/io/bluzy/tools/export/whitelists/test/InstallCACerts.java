package io.bluzy.tools.export.whitelists.test;

public class InstallCACerts {
    public static void main(String[] args) throws Exception {
        InstallCert.main(new String[] {"10.100.16.103:443", "--quiet"});
        InstallCert.main(new String[] {"10.100.16.166:443", "--quiet"});
    }
}
