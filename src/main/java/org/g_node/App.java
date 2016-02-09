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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.g_node.micro.commons.CliToolController;
import org.g_node.tools.CliLKTController;

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
     * Available merge mergers with key and description.
     */
    private final Map<String, CliToolController> mergers;

    /**
     * Constructor.
     */
    App() {
        this.mergers = new HashMap<>();
    }

    /**
     * Method to register all implemented tools with their short hand.
     * The short hand is required to select and run the intended crawler or RDF to RDF converter.
     */
    public final void register() {
        this.mergers.put("default", new CliLKTController());
    }

    /**
     * Main method of the merge-rdf framework.
     * @param args Command line input arguments.
     */
    public static void main(final String[] args) {

        final String currDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        App.LOGGER.info(
                String.join("", currDateTime, ", Starting merge RDF resources logfile.")
        );

        final App currApp = new App();
        currApp.register();

        if (args.length >= 1 && currApp.mergers.containsKey(args[0])) {
            System.out.println(
                    String.join("", "[DEBUG] Parse CLI arguments: ",
                            Integer.toString(args.length)
                    )
            );

            final HelpFormatter printHelp = new HelpFormatter();
            final CommandLineParser parser = new DefaultParser();
            final CliToolController currMerger = currApp.mergers.get(args[0]);
            final Options useOptions = currApp.mergers.get(args[0]).options();

            try {
                final CommandLine cmd = parser.parse(useOptions, args, false);
                if (cmd.hasOption("h")) {
                    printHelp.printHelp("Help", useOptions);
                    return;
                }
                currMerger.run(cmd);

            } catch (final ParseException exp) {
                printHelp.printHelp("Help", useOptions);
                App.LOGGER.error(
                        String.join("", "\n", exp.getMessage(), "\n")
                );
            }

        } else {
            App.LOGGER.error(
                    String.join("", "No proper merger selected!",
                            "\n\t Please use syntax 'java -jar merge-rdf.jar [merger] [options]'",
                            "\n\t e.g. 'java -jar merge-rdf.jar default -i mergeRDF.ttl -f mainRDF.ttl -o out.ttl'",
                            "\n\t Currently available mergers: ", currApp.mergers.keySet().toString())
            );
        }
    }

}
