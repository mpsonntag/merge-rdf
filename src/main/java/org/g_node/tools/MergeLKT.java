/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.g_node.MergeTool;
import org.g_node.srv.CliOptionService;
import org.g_node.srv.ModelUtils;
import org.g_node.srv.RDFService;

/**
 * Class handling the merging of RDF documents of the Use case of Kay Thurley.
 * TODO rename this class!
 * TODO add more in depth description.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class MergeLKT implements MergeTool {
    /**
     * Access to the main LOGGER.
     */
    private static final Logger LOGGER = Logger.getLogger(MergeLKT.class.getName());

    /**
     * Method returning the commandline options of the LKT merge tool.
     *
     * @return Available {@link CommandLine} {@link Options}.
     */
    public Options options() {
        final Options options = new Options();

        final Option opHelp = CliOptionService.getHelpOpt("");
        final Option opMergeFile = CliOptionService.getMergeFileOpt("");
        final Option opMainFile = CliOptionService.getMainFileOpt("");
        final Option opOut = CliOptionService.getOutFileOpt("");
        final Option opFormat = CliOptionService.getOutFormatOpt("");

        options.addOption(opHelp);
        options.addOption(opMergeFile);
        options.addOption(opMainFile);
        options.addOption(opOut);
        options.addOption(opFormat);

        return options;
    }

    /**
     * Method to merge two RDF files, one named mergeRDF, the other mainRDF. Information will be merged from the
     * mergeRDF file with the mainRDF file. If an output filename is provided, the merged RDF graph will be
     * saved to this output file. If no output filename is specified, a backup of the mainRDF file will be created,
     * using the format '[date_time]_backup_[main file name]' and the mainRDF file will be replaced with the merged
     * RDF graph.
     * If RDF Resources with the same URI are encountered in both mergeRDF and mainRDF, this Resource including all
     * referenced Blank Nodes will be removed from the mainRDF before the merge of the two graphs is performed. This
     * ensures, that only the information of the mergeRDF graph will be present in the merged final graph.
     *
     * @param cmd User provided {@link CommandLine} input containing information about the mergeRDF file, mainRDF file,
     *            the output filename and the output format.
     */
    public void run(final CommandLine cmd) {
        final String mergeFile = cmd.getOptionValue("i");
        final String mainFile = cmd.getOptionValue("m");
        final String outputFormat = cmd.getOptionValue("f", "TTL").toUpperCase(Locale.ENGLISH);
        final String outputFile = cmd.getOptionValue("o", mainFile);

        final Model mainModel = RDFService.openModelFromFile(mainFile);
        final Model addModel = RDFService.openModelFromFile(mergeFile);

        Model mergeModel = ModelFactory.createDefaultModel();

        // TODO check if importing prefixes of both models causes trouble anywhere.
        mergeModel.setNsPrefixes(mainModel.getNsPrefixMap());
        mergeModel.setNsPrefixes(addModel.getNsPrefixMap());

        mergeModel = ModelUtils.removePropertiesFromModel(addModel, mainModel, true);
        mergeModel.add(addModel);

        if (mainFile.equals(outputFile)) {
            final Path mainPath = Paths.get(mainFile);
            final String fileName = mainPath.getFileName().toString();
            final String ts = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            final String backupName = String.join("", ts, "_backup_", fileName);
            final String backupPath = mainPath.toString().replaceFirst(fileName, backupName);

            try {
                Files.copy(mainPath, Paths.get(backupPath));
            } catch (IOException e) {
                MergeLKT.LOGGER.error(
                        String.join("", "Error saving backup file '", backupName, "'")
                );
                e.printStackTrace();
            }
        }

        RDFService.saveModelToFile(outputFile, mergeModel, outputFormat);

    }

}
