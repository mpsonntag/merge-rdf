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
import org.g_node.srv.AppUtils;

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
        //final String mainPath = "D:\\Software\\Crawler\\";
/*
        final String mainFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_out.ttl");
        final String addFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_overlap_out.ttl");
        final String outFileName = String.join("", mainPath, "test_merge_out.ttl");

        final String mainFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_out.ttl");
        final String addFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_overlap_out.ttl");
        final String outFileName = String.join("", mainPath, "test_merge_out.ttl");

        final String mainFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_checkNS.ttl");
        final String addFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_overlap_checkNS.ttl");
        final String outFileName = String.join("", mainPath, "test_merge_checkNS.ttl");
*/
        final String mainFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_checkNS.ttl");
        final String addFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_overlap_checkNS.ttl");
        final String outFileName = String.join("", mainPath, "test_merge_checkNS.ttl");
/*
        final String mainFileName = String.join("", mainPath, "Labbook_testfile_min_out.ttl");
        final String addFileName = String.join("", mainPath, "Labbook_testfile_min_ol_out.ttl");
        final String outFileName = String.join("", mainPath, "test_merge_out.ttl");
*/

        final Model mainModel = RDFDataMgr.loadModel(mainFileName);
        final Model addModel = RDFDataMgr.loadModel(addFileName);
        final Model mergeModel = ModelFactory.createDefaultModel();

        mergeModel.setNsPrefixes(mainModel.getNsPrefixMap());

        // merge and save models
        mergeModel.add(mainModel);
        mergeModel.add(addModel);

        ModelUtils.removeDuplicateAnonNodes(mergeModel);

        ModelUtils.saveModelToFile(mergeModel, outFileName, outFormat);

        boolean isActive = false;
        if (isActive) {
            // determine intersection and difference of the two models.
            final Model intersectModel = mainModel.intersection(addModel);
            final String intersectOutFile = String.join("", mainPath, "test_insersect_out.ttl");
            ModelUtils.saveModelToFile(intersectModel, intersectOutFile, outFormat);

            final Model diffModel = mainModel.difference(addModel);
            final String diffOutFile = String.join("", mainPath, "test_diff_out.ttl");
            ModelUtils.saveModelToFile(diffModel, diffOutFile, outFormat);

            ModelUtils.walkResources(addModel);

            ModelUtils.printQuery(addModel);

            ModelUtils.constructQuery(addModel);

            AppUtils.testHashing();
        }
    }

}
