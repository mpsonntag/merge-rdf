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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.log4j.Logger;

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

        final String mainPath = "/home/msonntag/work/spielwiese/KayRDF/";
/*
        final Model mainModel = RDFDataMgr.loadModel(String.join("", mainPath, "Labbook_testfile_merge_test_01_out.ttl"));
        final Model addModel = RDFDataMgr.loadModel(String.join("", mainPath, "Labbook_testfile_merge_test_01_overlap_out.ttl"));
        mainModel.removeNsPrefix("http://g-node.org/orcid/0000-0003-4857-1083/lkt/home/msonntag/work/spielwiese/KayRDF/Labbook_testfile_merge_test_01_out.ttl/");
        addModel.removeNsPrefix("http://g-node.org/orcid/0000-0003-4857-1083/lkt/home/msonntag/work/spielwiese/KayRDF/Labbook_testfile_merge_test_01_overlap_out.ttl/");

        final Model mergeModel = ModelFactory.createDefaultModel();
        mergeModel.add(mainModel);
        mergeModel.add(addModel);
        saveModelToFile(mergeModel, String.join("", mainPath, "test_merge_out.ttl"), RDFFormat.TURTLE_PRETTY);

        final Model intersectModel = mainModel.intersection(addModel);
        saveModelToFile(intersectModel, String.join("", mainPath, "test_insersect_out.ttl"), RDFFormat.TURTLE_PRETTY);

        final Model diffModel = mainModel.difference(addModel);
        saveModelToFile(diffModel, String.join("", mainPath, "test_diff_out.ttl"), RDFFormat.TURTLE_PRETTY);
*/

        final Model mainModel = RDFDataMgr.loadModel(String.join("", mainPath, "Labbook_testfile_merge_test_01_checkNS.ttl"));
        final Model addModel = RDFDataMgr.loadModel(String.join("", mainPath, "Labbook_testfile_merge_test_01_overlap_checkNS.ttl"));

        final Model mergeModel = ModelFactory.createDefaultModel();
        mergeModel.add(mainModel);
        mergeModel.add(addModel);
        saveModelToFile(mergeModel, String.join("", mainPath, "test_merge_checkNS.ttl"), RDFFormat.TURTLE_PRETTY);


    }

    private static void saveModelToFile(Model m, String fileName, RDFFormat format) {
        final File file = new File(fileName);

        try {
            final FileOutputStream fos = new FileOutputStream(file);
            try {
                RDFDataMgr.write(fos, m, format);
                fos.close();
            } catch (IOException ioExc) {
                ioExc.printStackTrace();
            }
        } catch (FileNotFoundException exc) {
            exc.printStackTrace();
        }
    }

}
