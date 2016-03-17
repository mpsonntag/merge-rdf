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

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.g_node.micro.commons.CliToolController;
import org.g_node.micro.rdf.RdfFileServiceJena;
import org.g_node.srv.CliOptionService;
import org.g_node.srv.CtrlCheckService;

/**
 * Class handling validating commandline input and handling the merging of RDF documents
 * for the main use case of Kay Thurley.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LktCliController implements CliToolController {
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

        final Option opHelp = CliOptionService.getHelpOption("");

        final Option opMergeFile = Option.builder("i")
                .longOpt("merge-file")
                .desc("RDF file that will be merged with a main RDF file.")
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        final String mainDesc = String.join("",
                "RDF file containing the main database. Entries from the merge RDF file ",
                "will be integrated into this file. Duplicate entries between the merge RDF file and this file ",
                "will be removed from the main database and replaced by the entries found in the merge RDF file.");

        final Option opMainFile = Option.builder("m")
                .longOpt("main-file")
                .desc(mainDesc)
                .required()
                .hasArg()
                .valueSeparator()
                .build();

        final Option opOut = CliOptionService.getOutFileOption("");
        final Option opFormat = CliOptionService.getOutFormatOption("");

        options.addOption(opHelp);
        options.addOption(opMergeFile);
        options.addOption(opMainFile);
        options.addOption(opOut);
        options.addOption(opFormat);

        return options;
    }

    /**
     * Method validates the commandline input for this merger. When all checks pass, all relevant information is
     * passed to the actual class that merges the content of the two RDF files.
     *
     * @param cmd User provided {@link CommandLine} input containing information about the mergeRDF file, mainRDF file,
     *            the output filename and the output format.
     */
    public final void run(final CommandLine cmd) {

        final Set<String> rdfFormatsKeyMap = RdfFileServiceJena.RDF_FORMAT_MAP.keySet();
        final Map<String, String> rdfFormatExtensionMap = RdfFileServiceJena.RDF_FORMAT_EXTENSION;

        final String mergeFile = cmd.getOptionValue("i");
        if (!CtrlCheckService.isExistingFile(mergeFile)) {
            return;
        }

        final String mainFile = cmd.getOptionValue("m");
        if (!CtrlCheckService.isExistingFile(mainFile)) {
            return;
        }

        final String outputFormat = cmd.getOptionValue("f", "TTL").toUpperCase(Locale.ENGLISH);
        if (!CtrlCheckService.isSupportedOutputFormat(outputFormat, rdfFormatsKeyMap)) {
            return;
        }

        String outputFile = cmd.getOptionValue("o", mainFile);
        if (!outputFile.toLowerCase().endsWith(rdfFormatExtensionMap.get(outputFormat))) {
            outputFile = String.join("", outputFile, ".", rdfFormatExtensionMap.get(outputFormat));
        }

        if (!RdfFileServiceJena.isValidRdfFile(mainFile) || !RdfFileServiceJena.isValidRdfFile(mergeFile)) {
            return;
        }

        LktJenaMerger.mergeAndSave(mainFile, mergeFile, outputFile, outputFormat);
    }

}
