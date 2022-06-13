package io.bluzy.tools.export.whitelists;

import io.bluzy.tools.export.whitelists.config.ApplicationOptions;
import ch.qos.logback.classic.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.stream.Collectors;

import static io.bluzy.tools.export.whitelists.config.ConfigProperties.*;
import static java.lang.System.exit;
import static java.util.Objects.nonNull;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String DEFAULT_CONFIG_PROPERTIES_FILE_NAME = "config.properties";

    private static final String CONFIG_PROPERTIES_ENV_VAR = "CONFIG_PROPERTIES";

    private ApplicationOptions appOptions;
    private Map<String,Object> properties;

    private Exporter exporter;

    public Main(String[] args) {
        setRootLoggingLevel(Level.INFO);
        loadConfiguration();
        setRootLoggingLevel(Level.valueOf(String.valueOf(properties.get(LOG_LEVEL_ROOT))));
        appOptions = new ApplicationOptions(args, formatAvailableZones());
    }

    public static void main(String[] args) throws Exception {
        new Main(args).run();
    }

    public void run() throws Exception {
        if(appOptions.getCommandLine().hasOption(ApplicationOptions.HELP_OPTION)) {
            appOptions.printHelp();
            return;
        }

        if(!appOptions.getCommandLine().hasOption(ApplicationOptions.ZONE_OPTION)) {
            System.err.println("please specify -zone attribute");
            System.err.flush();
            appOptions.printHelp();
            exit(1);
        }

        String zoneName = appOptions.getCommandLine().getOptionValue(ApplicationOptions.ZONE_OPTION);

        String appName = null;
        if(appOptions.getCommandLine().hasOption(ApplicationOptions.APP_OPTION)) {
            appName = appOptions.getCommandLine().getOptionValue(ApplicationOptions.APP_OPTION);
        }

        if(appOptions.getCommandLine().hasOption(ApplicationOptions.FILE_OPTION)) {
            properties.put(EXPORT_FILE, appOptions.getCommandLine().getOptionValue(ApplicationOptions.FILE_OPTION));
        }

        exporter = new Exporter(zoneName, appName, (Map<String, Map<String, String>>) (Object) properties);
        exporter.export();
    }

    private void loadConfiguration() {
        String configPropertiesFileName = System.getenv(CONFIG_PROPERTIES_ENV_VAR);
        configPropertiesFileName = nonNull(configPropertiesFileName)? configPropertiesFileName: DEFAULT_CONFIG_PROPERTIES_FILE_NAME;

        logger.info("config properties used: {}", configPropertiesFileName);

        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configPropertiesFileName)) {
            if (input == null) {
                logger.error("Sorry, unable to find: {}", configPropertiesFileName);
                exit(1);
            }
            properties = loadProperties(input);

            logger.info("properties: {}", properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String formatAvailableZones() {
        return properties.entrySet().stream()
                .filter(e->e.getValue() instanceof Map)
                .map(Map.Entry::getKey)
                .collect(Collectors.joining("|"));
    }

    private void setRootLoggingLevel(Level level) {
        ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }
}
