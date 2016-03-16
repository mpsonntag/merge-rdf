/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.mergers;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.log4j.Logger;
import org.g_node.micro.commons.FileService;
import org.g_node.micro.commons.RDFService;
import org.g_node.micro.rdf.RdfUtilsJena;

/**
 * Class handling the merging of RDF documents using the Apache Jena RDF suite.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LktJenaMerger {
    /**
     * Access to the main LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(LktJenaMerger.class.getName());

    /**
     * Method merges two RDF files, one named mergeRDF, the other mainRDF. Information will be merged from the
     * mergeRDF file with the mainRDF file. If an output filename is provided, the merged RDF graph will be
     * saved to this output file. If no output filename is specified, a backup of the mainRDF file will be created,
     * using the format '[date_time]_backup_[main file name]' and the mainRDF file will be replaced with the merged
     * RDF graph.
     * If RDF Resources with the same URI are encountered in both mergeRDF and mainRDF, this Resource including all
     * referenced Blank Nodes will be removed from the mainRDF before the merge of the two graphs is performed. This
     * ensures, that only the information of the mergeRDF graph will be present in the merged final graph.
     * @param mainFile Main RDF file. Information will be merged into this file.
     * @param mergeFile Merge RDF file. Information from this file will be merged into the mainFile.
     * @param outputFile Name and Path of the output file.
     * @param outputFormat RDF format of the output file.
     */
    public static void mergeAndSave(final String mainFile, final String mergeFile,
                             final String outputFile, final String outputFormat) {

        final Model mainModel = RDFService.openModelFromFile(mainFile);
        final Model addModel = RDFService.openModelFromFile(mergeFile);

        Model mergeModel = ModelFactory.createDefaultModel();

        mergeModel.setNsPrefixes(mainModel.getNsPrefixMap());
        mergeModel.setNsPrefixes(addModel.getNsPrefixMap());

        mergeModel = RdfUtilsJena.removePropertiesFromModel(addModel, mainModel, true);
        mergeModel.add(addModel);

        // TODO test if this conditional works as required and maybe come up with a better solution.
        // Create backup, if the output file is the same as the main RDF file.
        if (mainFile.equals(outputFile) && !FileService.createTimeStampBackupFile(mainFile, "yyyyMMddHHmm")) {
            LktJenaMerger.LOGGER.error(
                    String.join("", "[ERROR ] While saving backup for file '", mainFile, "'")
            );
            return;
        }

        RDFService.saveModelToFile(outputFile, mergeModel, outputFormat);
    }
}
