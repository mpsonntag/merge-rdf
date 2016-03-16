/**
 * Copyright (c) 2015, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.micro.rdf;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

/**
 * Class contains small helper functions when dealing with Jena RDF models.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class RdfUtilsJena {
    /**
     * Method adds an RDF Literal only to a Jena Resource, if the Literal String
     * actually contains a value.
     * @param res Jena Resource.
     * @param p Jena Property.
     * @param litStr String containing the value of the Literal.
     */
    public static void addNonEmptyLiteral(final Resource res, final Property p, final String litStr) {
        if (litStr != null && !litStr.isEmpty()) {
            res.addLiteral(p, litStr);
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
     * from the containing model without removing the anonymous RDFNode itself.
     * @param checkAnon This {@link StmtIterator} contains a list of statements.
     */
    public static void removeAnonProperties(final StmtIterator checkAnon) {
        while (checkAnon.hasNext()) {
            final Statement currStmt = checkAnon.nextStatement();
            if (currStmt.getObject().isAnon()) {
                currStmt.getObject().asResource().removeProperties();
            }
        }
    }

}
