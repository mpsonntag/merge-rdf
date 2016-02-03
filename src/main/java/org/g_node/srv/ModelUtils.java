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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
     * Walk through existing {@link Resource}s of an input RDF {@link Model}, check for Resources
     * identical by URI with another model and remove all Property and anonymous RDFNodes
     * from these identical Resources of the second model.
     * @param inModel RDF {@link Model} from which the Resources are printed and checked for in the removeFromModel.
     * @param removeFromModel RDF {@link Model} from which the resources are removed if identical with resources
     *                  in the inModel.
     * @param removeAnonNodes If true, anonymous RDFNodes that are referenced by properties
     *                        that are to be removed, are removed as well.
     * @return The {@link Model} removeFromModel from which all Properties and anonymous Nodes
     * of {@link Model} inModel identical by URI have been removed.
     */
    public static Model removePropertiesFromModel(final Model inModel, final Model removeFromModel,
                                                  final boolean removeAnonNodes) {
        inModel.listObjects().forEachRemaining(o -> {
                if (o.isURIResource()
                        && o.asResource().listProperties().hasNext()
                        && removeFromModel.containsResource(o.asResource())) {
                    final Resource resRemoveProps = removeFromModel.getResource(o.asResource().getURI());
                    if (removeAnonNodes) {
                        removeAnonProperties(resRemoveProps.listProperties());
                    }
                    resRemoveProps.removeProperties();
                }
            });
        return removeFromModel;
    }

    /**
     * Check if a statement has an anonymous RDFNode as
     * an RDF Object and remove all properties of such an anonymous RDFNode
     * from the containing model.
     * @param checkAnon This {@link StmtIterator} contains a list of statements.
     */
    private static void removeAnonProperties(final StmtIterator checkAnon) {
        while (checkAnon.hasNext()) {
            final Statement currStmt = checkAnon.nextStatement();
            if (currStmt.getObject().isAnon()) {
                currStmt.getObject().asResource().removeProperties();
            }
        }
    }

}
