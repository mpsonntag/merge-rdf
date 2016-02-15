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

import org.apache.commons.cli.Option;
import org.g_node.micro.commons.RDFService;

/**
 * Class provides CLI {@link Option}s that are common
 * to all mergers of this service.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public final class CliOptionService {

    /**
     * Returns help option.
     * @param altDesc Optional description.
     * @return Help option.
     */
    public static Option getHelpOption(final String altDesc) {

        final String defaultDesc = "Print this message.";
        final String desc = !altDesc.isEmpty() ? altDesc : defaultDesc;

        return new Option("h", "help", false, desc);
    }

    /**
     * Returns option required to parse a given output file name from the command line.
     * Command line option shorthand will always be "-o" and "-out-file". This option is optional.
     * If no output filename is provided, the file name of the main RDF file will be used. The former main file
     * will be renamed to '[Date_time]_backup_[main file name]' to avoid any loss of information.
     * @param altDesc Alternative description replacing the default description.
     * @return CLI option for handling the output file.
     */
    public static Option getOutFileOption(final String altDesc) {

        final String defaultDesc = String.join("",
                "Optional: Path and name of the output file. ",
                "Files with the same name will be overwritten. ",
                "If no file name is provided, the file name of the main RDF file will be used. ",
                "In this case, the original main RDF file will be copied to a backup file ",
                "'[date_time]_backup_[main_file_name]', before it is overwritten.");
        final String desc = !altDesc.isEmpty() ? altDesc : defaultDesc;

        return  Option.builder("o")
                .longOpt("out-file")
                .desc(desc)
                .hasArg()
                .valueSeparator()
                .build();
    }

    /**
     * Returns option required to parse a given output format from the command line.
     * Commandline option shorthand will always be "-f" and "-out-format".
     * @param altDesc Alternative description replacing the default description.
     * @return CLI option for handling the output format.
     */
    public static Option getOutFormatOption(final String altDesc) {

        final String defaultDesc = String.join("",
                "Optional: format of the RDF file that will be written.\n",
                "Supported file formats: ", RDFService.RDF_FORMAT_MAP.keySet().toString(),
                "\nDefault setting is the Turtle (TTL) format.");
        final String desc = !altDesc.isEmpty() ? altDesc : defaultDesc;

        return Option.builder("f")
                .longOpt("out-format")
                .desc(desc)
                .hasArg()
                .valueSeparator()
                .build();
    }

}
