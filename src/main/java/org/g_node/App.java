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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.digest.DigestUtils;
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

/*
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

        testHashing();
*/
    }

    /**
     * Prototype method - test various hashing mechanisms.
     */
    private static void testHashing() {
        final String hashMe = "HASH ME";
        final String hashMeResult = "41bc1af4a1782d40a4d3c0fe5396f57d4201f02b1e232e330ea430a642f3d4d0";

        // test hashing - java built in
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] byteDigest = md.digest(hashMe.getBytes("UTF-8"));

            final String sha256hexMD = DatatypeConverter.printHexBinary(byteDigest)
                                            .toLowerCase(Locale.ENGLISH);
            final Boolean corrHashMD = hashMeResult.equals(sha256hexMD);

            System.out.println(
                    String.join("", "MessageDigest sha256: ", sha256hexMD,
                            " (correct hash: ", corrHashMD.toString(), ")")
            );

            md = MessageDigest.getInstance("SHA");
            byteDigest = md.digest(hashMe.getBytes("UTF-8"));

            final String sha1hexMD = DatatypeConverter.printHexBinary(byteDigest).toLowerCase(Locale.ENGLISH);

            System.out.println(
                    String.join("", "MessageDigest sha-1: ", sha1hexMD)
            );

            md = MessageDigest.getInstance("SHA-224");
            byteDigest = md.digest(hashMe.getBytes("UTF-8"));

            final String sha224hexMD = DatatypeConverter.printHexBinary(byteDigest)
                                            .toLowerCase(Locale.ENGLISH);

            System.out.println(
                    String.join("", "MessageDigest sha-224: ", sha224hexMD)
            );

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        // hashing apache common codec
        final String sha256hex = DigestUtils.sha256Hex(hashMe);
        final Boolean corrHash = hashMeResult.equals(sha256hex);
        System.out.println(
                String.join("", "Apache common sha256: ", sha256hex,
                        " (correct hash: ", corrHash.toString(), ")"));

        final String sha1hex = DigestUtils.shaHex(hashMe);
        System.out.println(String.join("", "Apache common sha-1: ", sha1hex));

        final String sha512hex = DigestUtils.sha512Hex(hashMe);
        System.out.println(String.join("", "Apache common sha-512: ", sha512hex));
    }
}
