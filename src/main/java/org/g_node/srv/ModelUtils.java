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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;

/**
 * Utility class used to handle RDF model functions.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class ModelUtils {
    /**
     * Helper method saving an RDF model to a file in a specified RDF format.
     * @param m Model that's supposed to be saved.
     * @param fileName Path and Name of the output file.
     * @param format Specified {@link RDFFormat} of the output file.
     */
    public static void saveModelToFile(final Model m, final String fileName, final RDFFormat format) {
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
     * Prototype method - find duplicate anon nodes and remove the duplicates from the model.
     * @param mergeModel Model that's supposed to cleaned from duplicate blank nodes.
     */
    public static void removeDuplicateAnonNodes(Model mergeModel) {
        // Handling just the hasWeight anonNodes for now to see how to deal with duplicate
        // anonymous nodes.
        Property hasWeight = ResourceFactory.createProperty("https://github.com/G-Node/neuro-ontology/", "hasWeight");

        ResIterator getSubjLogEntries = mergeModel.listResourcesWithProperty(hasWeight);
        getSubjLogEntries.forEachRemaining(c -> {
            System.out.println(c.asResource().getURI());
            if (c.listProperties(hasWeight).toList().size() > 1) {
                Set<Resource> removeUs = new HashSet<>();

                List<Statement> currBNL = c.listProperties(hasWeight).toList();
                int idx = 0;
                while (idx < c.listProperties(hasWeight).toList().size()) {
                    //Resource currRes = currBNL.get(idx).getObject().asResource();
                    System.out.println(currBNL.get(idx).getSubject().getLocalName());
                    System.out.println(currBNL.get(idx).getPredicate().getLocalName());
                    System.out.println(currBNL.get(idx).getObject().asResource().getId());
                    idx++;
                }
/*
// layer SubjectLogEntry
                List<Statement> l = c.listProperties(hasWeight).toList();
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
                //c.listProperties(hasWeight).forEachRemaining(d -> System.out.println(d.getObject().asResource().listProperties().));
*/

            }

        });


        // do lkt specific stuff here e.g. removing of double blank node entries.
    }

    /**
     * Prototype method - walk through existing resources of a model and print the content.
     * @param m RDF {@link Model} from which the resources are printed.
     */
    public static void walkResources(final Model m) {
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
    public static void printQuery(final Model m) {
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
    public static void constructQuery(final Model m) {
        final NodeIterator it = m.listObjects();

        while (it.hasNext()) {

            final RDFNode o = it.next();

            if (o.isURIResource() && o.asResource().listProperties().hasNext()
                    && o.asResource().hasProperty(RDF.type)) {

                String currQ;
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
