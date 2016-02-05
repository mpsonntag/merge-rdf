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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;
import org.g_node.srv.ModelUtils;
import org.g_node.srv.RDFService;

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
    private final Map<String, String> mergers;

    /**
     * Constructor.
     */
    App() {
        this.mergers = new HashMap<>();
        this.mergers.put("default", "default merge: remove Resources with identical ID from the main model before merge");
        this.mergers.put("plainmerge", "merge two models without any modifications");
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

        if (args.length < 1) {
            currApp.runPrototype();
        } else if (currApp.mergers.containsKey(args[0])) {
            System.out.println(
                    String.join("", "[DEBUG] Parse CLI arguments: ",
                            Integer.toString(args.length)
                    )
            );

            // TODO Implement CLI parser here.

        } else {
            App.LOGGER.error(
                    String.join("", "No proper merger selected!",
                            "\n\t Please use syntax 'java -jar merge-rdf.jar [merger] [options]'",
                            "\n\t e.g. 'java -jar merge-rdf.jar default -i mergeRDF.ttl -f mainRDF.ttl -o out.ttl'",
                            "\n\t Currently available mergers: ", currApp.mergers.keySet().toString())
            );
        }
    }

    /**
     * Setting up input strings for developing the prototype application. Will be removed.
     */
    private void runPrototype() {

        final String useCase = "default";

        final String mainFileName = "rdfTestFiles/LKT_merge_test_01a.ttl";
        final String addFileName = "rdfTestFiles/LKT_merge_test_01b.ttl";

        final String outPath = "/home/msonntag/work/spielwiese/KayRDF/";
        final RDFFormat outFormat = RDFFormat.TURTLE_PRETTY;

        String outFileName;

        if ("default".equals(useCase)) {
            outFileName = String.join("", outPath, "curr_out.ttl");
        } else if ("plainmerge".equals(useCase)) {
            outFileName = String.join("", outPath, "curr_out_merge_only.ttl");
        } else {
            outFileName = String.join("", outPath, "tmp.ttl");
        }

        final String loadMainFile = ClassLoader.getSystemClassLoader().getResource(mainFileName).toString();
        final String loadAddFile = ClassLoader.getSystemClassLoader().getResource(addFileName).toString();

        this.run(useCase, loadMainFile, loadAddFile, outFileName, outFormat);
    }

    /**
     * Main method to select and execute different merge methods.
     * @param useCase String containing the selected merge method.
     * @param mainFileName File name of the main RDF file.
     * @param addFileName File name of the RDF file that is supposed to be merged with the main RDF file.
     * @param outFileName Path to the output file.
     * @param outFormat RDF format of the output file.
     */
    private void run(final String useCase,
                            final String mainFileName, final String addFileName,
                            final String outFileName, final RDFFormat outFormat) {

        final Model mainModel = RDFService.openModelFromFile(mainFileName);
        final Model addModel = RDFService.openModelFromFile(addFileName);

        Model mergeModel = ModelFactory.createDefaultModel();

        // Can be a problem, if the addModel has differing prefixes.
        mergeModel.setNsPrefixes(mainModel.getNsPrefixMap());

        if ("default".equals(useCase)) {
            mergeModel = ModelUtils.removePropertiesFromModel(addModel, mainModel, true);
            mergeModel.add(addModel);
        } else if ("plainmerge".equals(useCase)) {
            mergeModel.add(mainModel);
            mergeModel.add(addModel);
        }

        // TODO remove this hard coded output format!
        RDFService.writeModelToFile(outFileName, mergeModel, "TTL");
    }

}
