package io.bluzy.tools.export.whitelists.config;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;
import static org.apache.commons.cli.Option.builder;

public class ApplicationOptions {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationOptions.class);

    public static final String HELP_OPTION = "help";
    public static final String ZONE_OPTION = "zone";
    public static final String APP_OPTION = "app";

    public static final String FILE_OPTION = "file";

    private Options options = new Options();
    private CommandLine commandLine;

    public ApplicationOptions(String[] args, String availableZones) {
        Option help = builder(HELP_OPTION)
                .hasArg(false)
                .desc("print this message")
                .build();
        Option zone = builder(ZONE_OPTION)
                .argName("ZONE")
                .hasArg()
                .desc(format("available zone names:  %s", availableZones))
                .build();
        Option app = builder(APP_OPTION)
                .argName("NAME")
                .hasArg()
                .desc("application name")
                .build();
        Option file = builder(FILE_OPTION)
                .argName("FILE")
                .hasArg()
                .desc("file name")
                .build();

        options.addOption(help);
        options.addOption(zone);
        options.addOption(app);
        options.addOption(file);

        CommandLineParser parser = new DefaultParser();
        try {
            commandLine = parser.parse(options, args);
        }
        catch (ParseException exp) {
            logger.error("Parsing failed.  Reason: " + exp.getMessage());
            throw new RuntimeException(exp);
        }
    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("ping-tools", options, true);
    }
}
