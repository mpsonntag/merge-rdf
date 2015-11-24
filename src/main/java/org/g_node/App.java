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
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
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

        final RDFFormat outFormat = RDFFormat.TURTLE_PRETTY;

        final String mainPath = "/home/msonntag/work/spielwiese/KayRDF/";
        //final String mainPath = "D:\\Software\\Crawler\\";

        final String mainFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_out.ttl");
        final String addFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_overlap_out.ttl");
        final String outFileName = String.join("", mainPath, "test_merge_out.ttl");

/*
        final String mainFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_out.ttl");
        final String addFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_overlap_out.ttl");
        final String outFileName = String.join("", mainPath, "test_merge_out.ttl");

        final String mainFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_checkNS.ttl");
        final String addFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_overlap_checkNS.ttl");
        final String outFileName = String.join("", mainPath, "test_merge_checkNS.ttl");

        final String mainFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_checkNS.ttl");
        final String addFileName = String.join("", mainPath, "Labbook_testfile_merge_test_01_overlap_checkNS.ttl");
        final String outFileName = String.join("", mainPath, "test_merge_checkNS.ttl");

        final String mainFileName = String.join("", mainPath, "Labbook_testfile_min_out.ttl");
        final String addFileName = String.join("", mainPath, "Labbook_testfile_min_ol_out.ttl");
        final String outFileName = String.join("", mainPath, "test_merge_out.ttl");
*/

        final Model mainModel = RDFDataMgr.loadModel(mainFileName);
        final Model addModel = RDFDataMgr.loadModel(addFileName);
        final Model mergeModel = ModelFactory.createDefaultModel();

        // merge and save models
        mergeModel.add(mainModel);
        mergeModel.add(addModel);
        saveModelToFile(mergeModel, outFileName, outFormat);

        // determine intersection and difference of the two models.
        final Model intersectModel = mainModel.intersection(addModel);
        final String intersectOutFile = String.join("", mainPath, "test_insersect_out.ttl");
        saveModelToFile(intersectModel, intersectOutFile, outFormat);

        final Model diffModel = mainModel.difference(addModel);
        final String diffOutFile = String.join("", mainPath, "test_diff_out.ttl");
        saveModelToFile(diffModel, diffOutFile, outFormat);

        walkResources(addModel);

        printQuery(addModel);

        constructQuery(addModel);
    }

    /**
     * Helper method saving an RDF model to a file in a specified RDF format.
     * @param m Model that's supposed to be saved.
     * @param fileName Path and Name of the output file.
     * @param format Specified {@link RDFFormat} of the output file.
     */
    private static void saveModelToFile(final Model m, final String fileName, final RDFFormat format) {
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

    /**
     * Prototype method - walk through existing resources of a model and print the content.
     * @param m RDF {@link Model} from which the resources are printed.
     */
    private static void walkResources(final Model m) {
        m.listObjects().forEachRemaining(o -> {
                if (o.isURIResource() && o.asResource().listProperties().hasNext()) {
                    System.out.println(
                            String.join("",
                                    "URI Res: ", o.toString(), " (",
                                    o.asResource().getProperty(RDF.type).getObject().toString(), ")")
                    );

                    o.asResource().listProperties().forEachRemaining(c -> {
                            if (c.getObject().isLiteral()) {
                                System.out.println(
                                        String.join("",
                                                "\t has Literal: ", c.getPredicate().toString(),
                                                " ", c.getObject().toString())
                                );

                            } else {
                                System.out.println(
                                        String.join("",
                                                "\t has Resource: ", c.getPredicate().toString(),
                                                " ", c.getObject().toString())
                                );
                            }
                        });
                } else if (o.isResource() && o.isAnon()) {
                    System.out.println(String.join("", "Anon Resource: ", o.toString()));
                }
            });
    }

    /**
     * Prototype method - print queries for all resources containing literals.
     * @param m RDF {@link Model} from which the queries are extracted.
     */
    private static void printQuery(final Model m) {
        m.listObjects().forEachRemaining(o -> {
                if (o.isURIResource() && o.asResource().listProperties().hasNext()) {
                    System.out.println("\nSELECT * WHERE {");
                    System.out.println(
                            String.join("?node ", RDF.type.toString(),
                                    " ", o.asResource().getProperty(RDF.type).getObject().toString(), " ."));

                    o.asResource().listProperties().forEachRemaining(c -> {
                            if (c.getObject().isLiteral()) {
                                System.out.println(
                                        String.join("", "?node ", c.getPredicate().toString(),
                                                " ", c.getObject().toString(), " .")
                                );
                            }
                        });
                    System.out.println("}");
                }
            });
    }

    /**
     * Prototype method - construct queries for all resources containing literals.
     * @param m RDF {@link Model} from which the queries are extracted.
     */
    private static void constructQuery(final Model m) {
        final NodeIterator it = m.listObjects();

        while (it.hasNext()) {

            final RDFNode o = it.next();

            String currQ = "";

            if (o.isURIResource() && o.asResource().listProperties().hasNext()
                    && o.asResource().hasProperty(RDF.type)) {

                currQ = "SELECT ?node WHERE {\n";
                currQ += String.join("", "\t?node ",
                        RDF.type.toString(), " ",
                        o.asResource().getProperty(RDF.type).getObject().toString(),
                        " .\n");

                final StmtIterator pIt = o.asResource().listProperties();

                while (pIt.hasNext()) {
                    final Statement c = pIt.next();
                    if (c.getObject().isLiteral()) {
                        currQ = String.join("", currQ, "\t?node ",
                                c.getPredicate().toString(), " ",
                                c.getObject().toString(), " .\n");
                    }
                }
                currQ += "}";

                System.out.println(String.join("", "Query: ", currQ));
            }
        }
    }

}
