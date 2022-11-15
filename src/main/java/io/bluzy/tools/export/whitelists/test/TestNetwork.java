package io.bluzy.tools.export.whitelists.test;

public class TestNetwork extends DockerNetwork implements DockerConfig {

    private static final String NETWORK_NAME = "TestNetwork";

    public TestNetwork() {
        super(DOCKER_URL, NETWORK_NAME);
    }
}
