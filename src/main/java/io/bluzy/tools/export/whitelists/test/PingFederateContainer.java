package io.bluzy.tools.export.whitelists.test;

public class PingFederateContainer extends DockerContainer implements DockerConfig {

    private static final String REPO = "pingidentity/pingfederate";
    private static final String TAG = "edge";
    private static final String IMAGE_NAME = "pingfederate";
    private static final String TMPFS = "/run/secrets";
    private static final int[] PORT = {9999,9031};
    private static final Long SHM = null;
    private static final String[] ENV = new String[] {"SERVER_PROFILE_URL=https://github.com/pingidentity/pingidentity-server-profiles.git",
            "SERVER_PROFILE_PATH=getting-started/pingfederate",
//            "SERVER_PROFILE_PATH=baseline/pingfederate",
            "PING_IDENTITY_ACCEPT_EULA=YES",
            "PING_IDENTITY_DEVOPS_USER=florinnicolae.potera@bat.at",
            "PING_IDENTITY_DEVOPS_KEY=7a898547-d650-4435-37a0-1522a77ee7a8"
    };

    public PingFederateContainer(boolean pullImage, String publishHostIp) {
        super(DOCKER_URL, REPO, TAG, IMAGE_NAME, TMPFS, PORT, SHM, ENV, pullImage, publishHostIp);
    }
}
