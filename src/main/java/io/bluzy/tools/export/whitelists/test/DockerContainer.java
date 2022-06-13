package io.bluzy.tools.export.whitelists.test;

import com.github.dockerjava.api.model.Container;

import java.io.File;
import java.io.IOException;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class DockerContainer {

    private String dockerUrl;

    private String repoName;

    private String repoTag;

    private String imageName;

    private String tmpFs;

    private int[] port;

    private Long shm;

    private String[] env;

    private boolean pullImage;

    private String publishHostIp = "127.0.0.1";

    private DockerConnection dockerConnection;

    public DockerContainer(String dockerUrl, String repoName, String repoTag, String imageName, String tmpFs, int[] port,
                           Long shm, String[] env, boolean pullImage, String publishHostIp) {
        this.dockerUrl = dockerUrl;
        this.repoName = repoName;
        this.repoTag = repoTag;
        this.imageName = imageName;
        this.tmpFs = tmpFs;
        this.port = port;
        this.shm = shm;
        this.env = env;
        this.pullImage = pullImage;
        if(nonNull(publishHostIp)) {
            this.publishHostIp = publishHostIp;
        }
    }

    public void startUp(boolean wait) {
        if(isNull(dockerConnection)) {
            dockerConnection = new DockerConnection(dockerUrl, false);
        }

        if(pullImage) {
            dockerConnection.removeImage(repoName, repoTag);
            dockerConnection.pullImage(repoName, repoTag);
        }

        if(isNull(getContainer())) {
            dockerConnection.createContainer(repoName, repoTag, imageName, port, shm, asList(env), tmpFs, publishHostIp);
            dockerConnection.startContainer(imageName);
            if (wait) {
                dockerConnection.waitTillContainerIsHealthy(imageName, 500);
            }
        }
    }

    public void pushFiles(File[] files, String remotePath) throws IOException {
        if(isNull(dockerConnection)) {
            dockerConnection = new DockerConnection(dockerUrl, false);
        }
        dockerConnection.copyFileToContainer(imageName, files, remotePath);
        dockerConnection.restartContainer(imageName);
        dockerConnection.waitTillContainerIsHealthy(imageName, 500);
    }

    public Container getContainer() {
        if(isNull(dockerConnection)) {
            dockerConnection = new DockerConnection(dockerUrl, false);
        }

        return dockerConnection.getContainer(imageName);
    }

    public void shutDown() throws IOException {
        if(isNull(dockerConnection)) {
            dockerConnection = new DockerConnection(dockerUrl, false);
        }
        dockerConnection.removeContainer(imageName);
        dockerConnection.disconnect();
    }
}
