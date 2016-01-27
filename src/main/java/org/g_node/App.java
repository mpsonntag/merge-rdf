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
import org.g_node.srv.AppUtils;
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
        //final String mainPath = "D:\\Software\\Crawler\\";

        final String mainFileName = "testFiles/Labbook_testfile_merge_test_01_checkNS.ttl";
        final String addFileName = "testFiles/Labbook_testfile_merge_test_01_overlap_checkNS.ttl";
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

        // to merge stuff:
        // 1) define main model and add model
        // 2) iterate through all resources of the add model and check if they are contained within the main model
        // 3) if a resource is contained within the main model
        // 3.1) check if the data properties of the add model are contained within the resource of the main model;
        // if that is the case remove the corresponding data property from the main model.
        // 3.2) check if the add model and the main model contain a blank node, if yes remove the blank node from the
        // main model.
        // 3.3) in theory check, if the main model and the add model both contain object properties, if yes, check
        // if there is an existing ontology and check if the corresponding class of the object property is allowed
        // to contain more than one. if this is not the case, remove the object property of the main model. This might
        // lead to the interesting case of a disconnected resource in the main model, if the corresponding resource
        // is not further linked by other object properties. In this case removing of this resource might be required.

        // It seems there is a problem with listing Resources, if a Resource is a top level node.
        // Top level node is a node that has a URI, references other nodes, but is not referenced
        // by other nodes. In this case, Apache Jena will not list it as a Resource.
        // In the use case this is true for the Project Resource which is not referenced by any other node.
        mergeModel = ModelUtils.removePropertiesFromModel(addModel, mainModel, true);

        mergeModel.add(addModel);
        ModelUtils.saveModelToFile(mergeModel, outFileName, outFormat);

        final boolean isActive = false;
        if (isActive) {
            testRemoveAnonNodes(mainModel, addModel, mergeModel, outFileName, outFormat);
            testIntersect(mainModel, addModel, mainPath, outFormat);
            testDiff(mainModel, addModel, mainPath, outFormat);
            queryResources(addModel);
            AppUtils.testHashing();
        }
    }

    /**
     * Wrapper for prototype query methods.
     * @param currModel RDF model to query.
     */
    private static void queryResources(final Model currModel) {
        ModelUtils.walkResources(currModel);
        ModelUtils.printQuery(currModel);
        ModelUtils.constructQuery(currModel);
    }

    /**
     * Wrapper for prototype remove anonymous nodes methods.
     * @param mainModel Model containing RDF database information.
     * @param addModel Model containing RDF to add to the database model.
     * @param mergeModel Empty model in which to merge DB and add model.
     * @param outFileName Path and filename to save the output file to.
     * @param outFormat RDFFormat in which the output file is saved as.
     */
    private static void testRemoveAnonNodes(final Model mainModel, final Model addModel, final Model mergeModel,
                                            final String outFileName, final RDFFormat outFormat) {
        // merge and save models
        mergeModel.add(mainModel);
        mergeModel.add(addModel);

        ModelUtils.removeDuplicateAnonNodes(mergeModel);
        ModelUtils.saveModelToFile(mergeModel, outFileName, outFormat);
    }

    /**
     * Wrapper to test Jena Model intersect.
     * @param mainModel Model containing RDF database information.
     * @param addModel Model containing RDF to add to the database model.
     * @param mainPath Path to save the output file to.
     * @param outFormat RDFFormat in which the output file is saved as.
     */
    private static void testIntersect(final Model mainModel, final Model addModel,
                                      final String mainPath, final RDFFormat outFormat) {
        // determine intersection and difference of the two models.
        final Model intersectModel = mainModel.intersection(addModel);
        final String intersectOutFile = String.join("", mainPath, "test_insersect_out.ttl");
        ModelUtils.saveModelToFile(intersectModel, intersectOutFile, outFormat);
    }

    /**
     * Wrapper to test Jena Model diff.
     * @param mainModel Model containing RDF database information.
     * @param addModel Model containing RDF to add to the database model.
     * @param mainPath Path to save the output file to.
     * @param outFormat RDFFormat in which the output file is saved as.
     */
    private static void testDiff(final Model mainModel, final Model addModel,
                                 final String mainPath, final RDFFormat outFormat) {
        final Model diffModel = mainModel.difference(addModel);
        final String diffOutFile = String.join("", mainPath, "test_diff_out.ttl");
        ModelUtils.saveModelToFile(diffModel, diffOutFile, outFormat);
    }

}
