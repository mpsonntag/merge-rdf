/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.micro.rdf;

/**
 * Class providing constants associated with RDF but independent from any implemented RDF library.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class RdfConstants {
    /**
     * Namespace used to identify RDF resources.
     */
    public static final String RDF_NS_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    /**
     * RDF namespace prefix.
     */
    public static final String RDF_NS_RDF_ABR = "rdf";
    /**
     * Namespace used to identify RDFS resources.
     */
    public static final String RDF_NS_RDFS = "http://www.w3.org/2000/01/rdf-schema#";
    /**
     * RDFS namespace prefix.
     */
    public static final String RDF_NS_RDFS_ABR = "rdfs";
    /**
     * Namespace used to identify XSD resources.
     */
    public static final String RDF_NS_XSD = "http://www.w3.org/2001/XMLSchema#";
    /**
     * XSD namespace prefix.
     */
    public static final String RDF_NS_XSD_ABR = "xs";
    /**
     * Namespace used to identify FOAF RDF resources.
     */
    public static final String RDF_NS_FOAF = "http://xmlns.com/foaf/0.1/";
    /**
     * FOAF Namespace prefix.
     */
    public static final String RDF_NS_FOAF_ABR = "foaf";
    /**
     * Namespace used to identify Dublin core resources.
     */
    public static final String RDF_NS_DC = "http://purl.org/dc/terms/";
    /**
     * Dublin core Namespace prefix.
     */
    public static final String RDF_NS_DC_ABR = "dc";
    /**
     * Core Ontology for neuroscientific metadata defined by the G-Node.
     */
    public static final String RDF_NS_GN_ONT = "https://github.com/G-Node/neuro-ontology/";
    /**
     * G-Node ontology namespace prefix.
     */
    public static final String RDF_NS_GN_ONT_ABR = "gn";
}
