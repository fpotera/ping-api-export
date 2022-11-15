package io.bluzy.tools.export.whitelists.test;

public interface DockerConfig {
//    String DOCKER_URL = "tcp://localhost:2375";
    String DOCKER_URL = "unix:///var/run/docker.sock";
}
