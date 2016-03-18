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

import java.util.Map;
import java.util.Set;
import org.g_node.mergers.LktMergerJena;
import org.g_node.micro.rdf.RdfFileServiceJena;

/**
 * Class used as a switch between different RDF APIs.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class RdfServiceSwitch {
    /**
     * Switch to the RDF formats supported by the currently used RDF API.
     */
    public static final Set<String> RDF_FORMAT_MAP_KEYS = RdfFileServiceJena.RDF_FORMAT_MAP.keySet();

    /**
     * Switch to the RDF format file extensions supported by the currently used RDF API.
     */
    public static final Map<String, String> RDF_FORMAT_EXTENSION = RdfFileServiceJena.RDF_FORMAT_EXTENSION;

    /**
     * Switch to the method that checks, if the provided file is a valid RDF file. This check is
     * dependent on the used RDF API.
     * @param uri Uri of the file that is to be checked if its a valid RDF file.
     * @return True if the file is a valid RDF file, false otherwise.
     */
    public static boolean isValidRdfFile(final String uri) {
        return RdfFileServiceJena.isValidRdfFile(uri);
    }

    /**
     * Switch to the method merging two RDF files and saving the resulting RDF graph to an output file
     * as a specified RDF format.
     * @param mainFile RDF file.
     * @param mergeFile RDF file that is merged with the mainFile.
     * @param outputFile Path and filename of the file the merged RDF graph is saved to.
     * @param outputFormat RDF format of the result file.
     */
    public static void runMerger(final String mainFile, final String mergeFile,
                                 final String outputFile, final String outputFormat) {
        LktMergerJena.runMerger(mainFile, mergeFile, outputFile, outputFormat);
    }

}
