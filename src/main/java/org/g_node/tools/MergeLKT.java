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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.g_node.MergeTool;
import org.g_node.srv.AppUtils;
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
     * Option mergeRDF: Returns merge RDF file option required to merge a given RDF file with another
     * RDF file from the command line. Commandline option shorthands are "-i" and "-merge-file".
     * This option will always be "required".
     *
     * Option mainRDF: Returns main RDF file option required to merge a given RDF file with another
     * RDF file from the command line. Commandline option shorthands are "-m" and "-main-file".
     * This option will always be "required".
     *
     * @return Available {@link CommandLine} {@link Options}.
     */
    public final Options options() {

        final Options options = new Options();

        final Option opHelp = CliOptionService.getHelpOpt("");

        final Option opMergeFile = Option.builder("i")
                .longOpt("merge-file")
                .desc("RDF file that will be merged with a main RDF file.")
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        final String mainDesc = "RDF file containing the main database. Entries from the merge RDF file "
                + "will be integrated into this file. Duplicate entries between the merge RDF file and this file "
                + "will be removed from the main database and replaced by the entries found in the merge RDF file.";

        final Option opMainFile = Option.builder("m")
                .longOpt("main-file")
                .desc(mainDesc)
                .required()
                .hasArg()
                .valueSeparator()
                .build();

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
    public final void run(final CommandLine cmd) {
        final String mergeFile = cmd.getOptionValue("i");
        final String mainFile = cmd.getOptionValue("m");
        final String outputFormat = cmd.getOptionValue("f", "TTL").toUpperCase(Locale.ENGLISH);
        final String outputFile = cmd.getOptionValue("o", mainFile);

        final Model mainModel = RDFService.openModelFromFile(mainFile);
        final Model addModel = RDFService.openModelFromFile(mergeFile);

        Model mergeModel = ModelFactory.createDefaultModel();

        mergeModel.setNsPrefixes(mainModel.getNsPrefixMap());
        mergeModel.setNsPrefixes(addModel.getNsPrefixMap());

        mergeModel = ModelUtils.removePropertiesFromModel(addModel, mainModel, true);
        mergeModel.add(addModel);

        if (mainFile.equals(outputFile)) {
            final Path mainPath = Paths.get(mainFile);
            final String fileName = mainPath.getFileName().toString();
            final String ts = AppUtils.getTimeStamp("yyyyMMddHHmm");
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
