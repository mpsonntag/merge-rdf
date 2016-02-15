/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.g_node.mergers.LktCliController;
import org.g_node.micro.commons.AppUtils;
import org.g_node.micro.commons.CliToolController;

/**
 * Main application class used to parse command line input and pass
 * information to the appropriate modules.
 *
 * This application is a prototype, don't hate me if stuff is partially suboptimal or outright sucks.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class App {

    /**
     * Access to the main log4j LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    /**
     * Hash map of all implemented merger mergers mapping a string to the corresponding controller class.
     */
    private static final Map<String, CliToolController> REGISTRY = Collections.unmodifiableMap(
            new HashMap<String, CliToolController>() {
                {
                    put("lkt", new LktCliController());
                }
            }
    );
    /**
     * Main method of the merge-rdf framework.
     * @param args Command line input arguments.
     */
    public static void main(final String[] args) {

        App.LOGGER.info("\n");
        App.LOGGER.info(
                String.join("", AppUtils.getTimeStamp("dd.MM.yyyy HH:mm"), ", Starting merge-rdf logfile.")
        );
        App.LOGGER.info(
                String.join("", "Input arguments: '", String.join(" ", args), "'")
        );

        if (args.length > 0 && App.REGISTRY.containsKey(args[0])) {

            final HelpFormatter printHelp = new HelpFormatter();
            final CommandLineParser parser = new DefaultParser();
            final Options useOptions = App.REGISTRY.get(args[0]).options();

            try {
                final CommandLine cmd = parser.parse(useOptions, args, false);
                if (cmd.hasOption("h")) {
                    printHelp.printHelp("Help", useOptions);
                    return;
                }
                App.REGISTRY.get(args[0]).run(cmd);

            } catch (final ParseException exp) {
                printHelp.printHelp("Help", useOptions);
                App.LOGGER.error(
                        String.join("", "\n", exp.getMessage(), "\n")
                );
            }

        } else {
            App.LOGGER.error(
                    String.join("", "No existing merge tool selected!",
                            "\n\t Please use syntax 'java -jar merge-rdf.jar [merger] [options]'",
                            "\n\t e.g. 'java -jar merge-rdf.jar lkt -i mergeRDF.ttl -m mainRDF.ttl -o out.ttl'",
                            "\n\t Currently available merge mergers: ", App.REGISTRY.keySet().toString())
            );
        }
    }

}
