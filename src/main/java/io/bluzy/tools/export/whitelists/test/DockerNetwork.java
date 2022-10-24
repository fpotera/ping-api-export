package io.bluzy.tools.export.whitelists.test;

import com.github.dockerjava.api.model.Network;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class DockerNetwork {

    private String dockerUrl;

    private String networkName;

    private String networkId;

    private DockerConnection dockerConnection;

    public DockerNetwork(String dockerUrl, String networkName) {
        this.dockerUrl = dockerUrl;
        this.networkName = networkName;
    }

    public String create() {
        startUpDockerConnection();

        if(isNull(networkId)) {
            Network net = dockerConnection.getNetwork(networkName);
            if(isNull(net)) {
                dockerConnection.createNetwork(networkName, false);
            }
            net = dockerConnection.getNetwork(networkName);
            if(nonNull(net)) {
                networkId = net.getId();
            }
        }
        return networkId;
    }

    private void startUpDockerConnection() {
        if(isNull(dockerConnection)) {
            dockerConnection = new DockerConnection(dockerUrl, false);
        }
    }
}
