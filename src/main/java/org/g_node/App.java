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
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;
import org.g_node.srv.ModelUtils;

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
 * Main method of the merge-rdf framework.
     * @param args Command line input arguments.
     */
    public static void main(final String[] args) {

        // TODO catching plain exceptions not allowed by checkstyle, implement other catches
        // App.LOGGER.error(e.getMessage(), e.fillInStackTrace());
        final String currDateTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
        App.LOGGER.info(
                String.join("", currDateTime, ", Starting merge RDF resources logfile.")
        );

        final RDFFormat outFormat = RDFFormat.TURTLE_PRETTY;

        final String mainPath = "/home/msonntag/work/spielwiese/KayRDF/";

        final String mainFileName = "rdfTestFiles/LKT_merge_test_01a.ttl";
        final String addFileName = "rdfTestFiles/LKT_merge_test_01b.ttl";
        final String outFileName = String.join("", mainPath, "curr_out.ttl");

        final Model mainModel = RDFDataMgr.loadModel(
                ClassLoader.getSystemClassLoader().getResource(mainFileName).toString()
        );
        final Model addModel = RDFDataMgr.loadModel(
                ClassLoader.getSystemClassLoader().getResource(addFileName).toString()
        );
        Model mergeModel = ModelFactory.createDefaultModel();

        // Can be a problem, if the addModel has additional prefixes.
        mergeModel.setNsPrefixes(mainModel.getNsPrefixMap());

        mergeModel = ModelUtils.removePropertiesFromModel(addModel, mainModel, true);

        mergeModel.add(addModel);
        ModelUtils.saveModelToFile(mergeModel, outFileName, outFormat);
    }

}
