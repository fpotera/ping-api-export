package io.bluzy.tools.export.whitelists.test;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.nio.file.Files.*;
import static java.util.Arrays.stream;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;

public class DockerConnection {

    private String host;
    private boolean tls;
    private DockerClient dockerClient;

    public DockerConnection(String host, boolean tls) {
        this.host = host;
        this.tls = tls;
        connect();
    }

    public void disconnect() throws IOException {
        dockerClient.close();
    }

    public void pullImage(String repository, String tag) {
        Thread thread = Thread.currentThread();

        dockerClient.pullImageCmd(repository).withTag(tag).exec(new ResultCallback<PullResponseItem>() {

            @Override
            public void close() throws IOException {}

            @Override
            public void onStart(Closeable closeable) {}

            @Override
            public void onNext(PullResponseItem object) {}

            @Override
            public void onError(Throwable throwable) {
                throwable.printStackTrace();
            }

            @Override
            public void onComplete() {
                System.out.println("pull image finished");
                synchronized(thread) {
                    thread.notify();
                }
            }
        });

        try {
            synchronized(thread) {
                thread.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void removeImage(String imageName, String imageTag) {
        Optional<Image> image = dockerClient.listImagesCmd().withImageNameFilter(imageName).exec()
                .stream().filter(i -> nonNull(i.getRepoTags()) && i.getRepoTags()[0].equals(imageName+":"+imageTag)).findFirst();
        image.ifPresent(i -> dockerClient.removeImageCmd(i.getId()).exec());
    }

    public void createContainer(String repository, String tag, String name, int[] port, Long shm, List<String> env, String tmpFs,
                                String publishHostIp) {
        HostConfig hostConfig = new HostConfig();
        if(nonNull(shm)) {
            hostConfig.withShmSize(shm);
        }

        if(nonNull(tmpFs)) {
            Mount mount = new Mount().withType(MountType.TMPFS).withTarget(tmpFs);
            hostConfig.withMounts(singletonList(mount));
        }

        PortBinding[] portBindings = stream(port)
                .mapToObj(p->new PortBinding(Ports.Binding.bindIpAndPort(publishHostIp, p), new ExposedPort(p, InternetProtocol.TCP)))
                .toArray(PortBinding[]::new);
        hostConfig.withPortBindings( portBindings);

        dockerClient.createContainerCmd(repository+":"+tag)
                .withName(name)
                .withHostConfig(hostConfig)
                .withEnv(env)
                .exec();
    }

    public void startContainer(String containerName) {
        String containerId = getContainerId(containerName);
        if (nonNull(containerId)) {
            dockerClient.startContainerCmd(containerId).exec();



/*            String networkId = getNetwork("PingNetwork").getId();

            System.out.println("###### containerId: "+containerId);
            System.out.println("###### networkId: "+networkId);

            Object res = dockerClient.connectToNetworkCmd()
                    .withContainerId(getContainerId(containerName))
                    .withNetworkId(getNetwork("PingNetwork").getId())
                    .exec();

            System.out.println("###### res: "+res);

 */
        }
    }

    public void removeContainer(String containerName) {
        String containerId = getContainerId(containerName);
        if(nonNull(containerId)) {
            dockerClient.removeContainerCmd(containerId).withForce(true).exec();
        }
    }

    public String getContainerId(String name) {
        Container container =  dockerClient.listContainersCmd().withShowAll(true)
                .withNameFilter(Collections.singletonList(name))
                .exec().stream().findFirst().orElse(null);
        return nonNull(container)?container.getId():null;
    }

    public Container getContainer(String name) {
        return dockerClient.listContainersCmd().withShowAll(true)
                .withNameFilter(Collections.singletonList(name))
                .exec().stream().findFirst().orElse(null);
    }

    public void copyFileToContainer(String containerName, File[] files, String remotePath) throws IOException {
        String containerId = getContainerId(containerName);
        if(nonNull(containerId)) {
            Path tarFile = createTempFile("", ".tar");
            TarArchiveOutputStream tarStream = new TarArchiveOutputStream(newOutputStream(tarFile));

            stream(files).forEach(file->{
                try {
                    ArchiveEntry archiveEntry = new TarArchiveEntry(file, file.getName());
                    tarStream.putArchiveEntry(archiveEntry);
                    try (FileInputStream inputStream = new FileInputStream(file)) {
                        IOUtils.copy(inputStream, tarStream);
                    }
                    tarStream.closeArchiveEntry();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            tarStream.close();
            dockerClient.copyArchiveToContainerCmd(containerId)
                    .withRemotePath(remotePath).withTarInputStream(newInputStream(tarFile))
                    .withDirChildrenOnly(true).exec();
        }
    }

    public void restartContainer(String containerName) {
        String containerId = getContainerId(containerName);
        if(nonNull(containerId)) {
            dockerClient.restartContainerCmd(containerId).exec();
        }
    }

    public void waitTillContainerIsHealthy(String containerName, long millis) {
        boolean sleep = !getContainer(containerName).getStatus().contains("healthy");
        while(sleep) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sleep = !getContainer(containerName).getStatus().contains("healthy");
        }
    }

    public void createNetwork(String networkName, boolean replace) {
        List<Network> networks = dockerClient.listNetworksCmd().exec();
        Optional<String> netId = networks.stream().filter(n->n.getName().equals(networkName)).map(Network::getId).findFirst();
        if(netId.isEmpty()) {
            dockerClient.createNetworkCmd().withName(networkName).exec();
        }
        if(netId.isPresent() && replace) {
            dockerClient.removeNetworkCmd(netId.get()).exec();
            dockerClient.createNetworkCmd().withName(networkName).exec();
        }
    }

    public Network getNetwork(String networkName) {
        List<Network> networks = dockerClient.listNetworksCmd().exec();
        return networks.stream().filter(n->n.getName().equals(networkName))
                .findFirst().orElse(null);
    }

    private void connect() {
        DockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder()
                .withDockerHost(host)
                .withDockerTlsVerify(tls)
                .build();

        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .maxConnections(100)
                .connectionTimeout(Duration.ofSeconds(30))
                .responseTimeout(Duration.ofSeconds(45))
                .build();

        dockerClient = DockerClientImpl.getInstance(config, httpClient);
    }
}
