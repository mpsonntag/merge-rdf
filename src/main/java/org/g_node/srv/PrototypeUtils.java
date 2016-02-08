/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.srv;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.jena.riot.RDFFormat;
import org.g_node.micro.commons.RDFService;

/**
 * This utility class contains all methods during the prototyping phase and will be removed later. All
 * methods that work as they should will be cleaned of debug messages and moved to proper classes.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class PrototypeUtils {
    /**
     * Prototype method - walk through existing {@link Resource}s of a model, check for Resources
     * identical by URI with another model and remove all Property and anonymous RDFNodes
     * from these identical Resources of the second model.
     * @param addModel RDF {@link Model} from which the Resources are printed and checked for in the removeFromModel.
     * @param removeFromModel RDF {@link Model} from which the resources are removed if identical with resources
     *                  in the addModel.
     * @param removeAnonNodes If true, anonymous RDFNodes that are referenced by properties
     *                        that are to be removed, are removed as well.
     * @return The {@link Model} removeFromModel from which all Properties and anonymous Nodes
     * of {@link Model} addModel identical by URI have been removed.
     */
    public static Model removePropertiesFromModel(final Model addModel, final Model removeFromModel,
                                                  final boolean removeAnonNodes) {
        // TODO check how filters work and filter for o.isURIResource() before doing the .forEachRemaining()
        addModel.listObjects().forEachRemaining(o -> {
                if (o.isURIResource()
                    && o.asResource().listProperties().hasNext()
                    && removeFromModel.containsResource(o.asResource())) {

                    System.out.println(String.join("", "[DEBUG] URI Res: ",
                            o.toString(), " contained in removeFromModel"));

                    final Resource checkRemoveProps = removeFromModel.getResource(o.asResource().getURI());
                    final List<Statement> mainProps = checkRemoveProps.listProperties().toList();
                    mainProps.forEach(c -> System.out.println(String.join("", "[DEBUG] ", c.toString())));
                    final List<Statement> addProps = o.asResource().listProperties().toList();
                    System.out.println("[DEBUG] \n");
                    addProps.forEach(c -> System.out.println(String.join("", "[DEBUG] ", c.toString())));
                    // Problem: Cannot be identical, because the object properties can differ.
                    System.out.println(
                            String.join("",
                                    "[DEBUG] CurrRes: ", o.asResource().getURI(),
                                    " mainProps length: ", Integer.toString(mainProps.size()),
                                    " addProps length: ", Integer.toString(addProps.size()),
                                    " identical: ", Boolean.toString(mainProps.containsAll(addProps)))
                    );

                    // Remove anonymous nodes of the current Resource
                    if (removeAnonNodes) {
                        removeAnonProperties(checkRemoveProps.listProperties());
                    }

                    checkRemoveProps.removeProperties();
                }
            });

        System.out.println("[DEBUG]\n[DEBUG] List remaining objects:\n");
        removeFromModel.listObjects().forEachRemaining(
                o -> System.out.println(String.join("", "[DEBUG] ", o.toString())));

        return removeFromModel;
    }

    /**
     * Prototype method - check if a statement has an anonymous RDFNode as
     * an RDF Object and remove all properties of such an anonymous RDFNode
     * from the containing model.
     * @param checkAnon This {@link StmtIterator} contains a list of statements.
     */
    private static void removeAnonProperties(final StmtIterator checkAnon) {
        while (checkAnon.hasNext()) {
            final Statement currStmt = checkAnon.nextStatement();
            if (currStmt.getObject().isAnon()) {
                System.out.println(String.join("", "[DEBUG] Current property |",
                        currStmt.getPredicate().getLocalName(),
                        "| of resource ",
                        currStmt.getSubject().getURI(),
                        " is anonNode: ",
                        currStmt.getObject().toString()));

                final Resource currAnonRes = currStmt.getObject().asResource();

                System.out.println(
                        String.join("", "[DEBUG] ", currAnonRes.toString(),
                                " has properties: ",
                                Integer.toString(currAnonRes.listProperties().toList().size()))
                );

                currAnonRes.removeProperties();
            }
        }
    }

    /**
     * Prototype method - walk through existing resources of a model and print the content.
     * @param currModel RDF {@link Model} from which the resources are printed.
     */
    public static void walkResources(final Model currModel) {
        currModel.listObjects().forEachRemaining(o -> {
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
     * Prototype method - find duplicate anon nodes and remove the duplicates from the model.
     * @param mergeModel Model that's supposed to cleaned from duplicate blank nodes.
     */
    public static void removeDuplicateAnonNodes(final Model mergeModel) {
        // Handling just the hasWeight anonNodes for now to see how to deal with duplicate
        // anonymous nodes.
        final Property hasWeight = ResourceFactory
                .createProperty("https://github.com/G-Node/neuro-ontology/", "hasWeight");

        final ResIterator getSubjLogEntries = mergeModel.listResourcesWithProperty(hasWeight);
        getSubjLogEntries.forEachRemaining(c -> {
                System.out.println(c.asResource().getURI());
                if (c.listProperties(hasWeight).toList().size() > 1) {
                    //Set<Resource> removeUs = new HashSet<>();

                    final List<Statement> currBNL = c.listProperties(hasWeight).toList();
                    int idx = 0;
                    while (idx < c.listProperties(hasWeight).toList().size()) {
                        //Resource currRes = currBNL.get(idx).getObject().asResource();
                        System.out.println(currBNL.get(idx).getSubject().getLocalName());
                        System.out.println(currBNL.get(idx).getPredicate().getLocalName());
                        System.out.println(currBNL.get(idx).getObject().asResource().getId());
                        idx = idx + 1;
                    }
                        /*
                        // layer SubjectLogEntry
                        final List<Statement> l = c.listProperties(hasWeight).toList();
                        // check if this somehow works with int streams as well.
                        for (Statement s : l) {
                            // layer Anonymous node
                            System.out.println(
                                    s.getSubject().getLocalName()
                            );
                            System.out.println(
                                    s.getObject().asResource().listProperties().toList()
                            );
                        }

                        //System.out.println(Integer.toString(c.listProperties(hasWeight).toList().size()));
                        //c.listProperties(hasWeight).forEachRemaining(
                            d -> System.out.println(d.getObject().asResource().listProperties().)
                        );
                        */
                }
            });
        // do lkt specific stuff here e.g. removing of double blank node entries.
    }

    /**
     * Prototype method - construct queries for all resources containing literals.
     * @param m RDF {@link Model} from which the queries are extracted.
     */
    public static void constructQuery(final Model m) {

        final NodeIterator it = m.listObjects();
        while (it.hasNext()) {

            final RDFNode o = it.next();
            if (o.isURIResource()
                    && o.asResource().listProperties().hasNext()
                    && o.asResource().hasProperty(RDF.type)) {

                String currQ;
                currQ = "SELECT ?node WHERE {\n";
                currQ = String.join("", currQ, "\t?node ",
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
                currQ += String.join("", currQ, "}");

                System.out.println(String.join("", "Query: ", currQ));
            }
        }
    }

    /**
     * Wrapper for prototype query methods.
     * @param currModel RDF model to query.
     */
    public static void queryResources(final Model currModel) {
        PrototypeUtils.walkResources(currModel);
        PrototypeUtils.constructQuery(currModel);
    }

    /**
     * Wrapper for prototype remove anonymous nodes methods.
     * @param mainModel Model containing RDF database information.
     * @param addModel Model containing RDF to add to the database model.
     * @param mergeModel Empty model in which to merge DB and add model.
     * @param outFileName Path and filename to save the output file to.
     * @param outFormat RDFFormat in which the output file is saved as.
     */
    public static void testRemoveAnonNodes(final Model mainModel, final Model addModel, final Model mergeModel,
                                            final String outFileName, final RDFFormat outFormat) {
        // merge and save models
        mergeModel.add(mainModel);
        mergeModel.add(addModel);

        PrototypeUtils.removeDuplicateAnonNodes(mergeModel);

        RDFService.plainSaveModelToFile(mergeModel, outFileName, outFormat);
    }

    /**
     * Wrapper to test Jena Model intersect.
     * @param mainModel Model containing RDF database information.
     * @param addModel Model containing RDF to add to the database model.
     * @param mainPath Path to save the output file to.
     * @param outFormat RDFFormat in which the output file is saved as.
     */
    public static void testIntersect(final Model mainModel, final Model addModel,
                                      final String mainPath, final RDFFormat outFormat) {
        // determine intersection and difference of the two models.
        final Model intersectModel = mainModel.intersection(addModel);
        final String intersectOutFile = String.join("", mainPath, "test_insersect_out.ttl");
        RDFService.plainSaveModelToFile(intersectModel, intersectOutFile, outFormat);
    }

    /**
     * Wrapper to test Jena Model diff.
     * @param mainModel Model containing RDF database information.
     * @param addModel Model containing RDF to add to the database model.
     * @param mainPath Path to save the output file to.
     * @param outFormat RDFFormat in which the output file is saved as.
     */
    public static void testDiff(final Model mainModel, final Model addModel,
                                 final String mainPath, final RDFFormat outFormat) {
        final Model diffModel = mainModel.difference(addModel);
        final String diffOutFile = String.join("", mainPath, "test_diff_out.ttl");
        RDFService.plainSaveModelToFile(diffModel, diffOutFile, outFormat);
    }

    /**
     * Prototype method - test various hashing mechanisms.
     */
    public static void testHashing() {
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
