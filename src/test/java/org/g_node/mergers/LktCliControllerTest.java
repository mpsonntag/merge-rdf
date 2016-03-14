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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.g_node.App;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the LktCliController class. Output and Error streams are redirected
 * from the console to a different PrintStream and reset after tests are finished
 * to avoid mixing tool error messages with actual test error messages.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LktCliControllerTest {

    private ByteArrayOutputStream outStream;
    private PrintStream stdout;

    private final String tmpRoot = System.getProperty("java.io.tmpdir");
    private final String testFolderName = "LktCliControllerTest";
    private final Path testFileFolder = Paths.get(tmpRoot, testFolderName);
    private final File testRdfFile = this.testFileFolder.resolve("test.ttl").toFile();

    /**
     * Redirect Error and Out stream.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.stdout = System.out;
        this.outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(this.outStream));

        final String miniTTL = "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n\n_:a foaf:name\t\"TestName\"";
        FileUtils.write(this.testRdfFile, miniTTL);

        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(
                new ConsoleAppender(
                        new PatternLayout("[%-5p] %m%n")
                )
        );
    }

    /**
     * Reset Out stream to the console after the tests are done.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        System.setOut(this.stdout);

        if (Files.exists(this.testFileFolder)) {
            FileUtils.deleteDirectory(this.testFileFolder.toFile());
        }
    }

    @Test
    public void testRunNonExistingInFile() throws Exception {
        final String useCase = "lkt";
        final String testNonExistingFile = "iDoNotExist";

        final String[] cliArgs = new String[5];
        cliArgs[0] = useCase;
        cliArgs[1] = "-m";
        cliArgs[2] = this.testRdfFile.getAbsolutePath();
        cliArgs[3] = "-i";
        cliArgs[4] = testNonExistingFile;

        App.main(cliArgs);
        assertThat(this.outStream.toString()).contains(
                String.join("", "File ", testNonExistingFile, " does not exist.")
        );
    }

    @Test
    public void testRunNonExistingMergeFile() throws Exception {
        final String useCase = "lkt";
        final String testNonExistingFile = "iDoNotExist";

        final String[] cliArgs = new String[5];
        cliArgs[0] = useCase;
        cliArgs[1] = "-m";
        cliArgs[2] = testNonExistingFile;
        cliArgs[3] = "-i";
        cliArgs[4] = this.testRdfFile.getAbsolutePath();

        App.main(cliArgs);
        assertThat(this.outStream.toString()).contains(
                String.join("", "File ", testNonExistingFile, " does not exist.")
        );
    }

    @Test
    public void testSupportedOutFormat() throws Exception {
        final String useCase = "lkt";
        final String invalidOutputFormat = "iDoNotExist";
        final String parserMessage = String.join("", "Unsupported output format: '",
                                                        invalidOutputFormat.toUpperCase(Locale.ENGLISH), "'");

        final String[] cliArgs = new String[7];
        cliArgs[0] = useCase;
        cliArgs[1] = "-m";
        cliArgs[2] = this.testRdfFile.getAbsolutePath();
        cliArgs[3] = "-i";
        cliArgs[4] = this.testRdfFile.getAbsolutePath();
        cliArgs[5] = "-f";
        cliArgs[6] = invalidOutputFormat;

        App.main(cliArgs);
        assertThat(this.outStream.toString()).contains(parserMessage);
    }

    @Test
    public void testValidRDFFiles() throws Exception {
        final File testInvalidRdfFile = this.testFileFolder.resolve("test").toFile();
        final String text = "I am a normal text file.";
        FileUtils.write(testInvalidRdfFile, text);

        final String useCase = "lkt";
        final String parserMessage = String.join("", "Failed to load file '",
                testInvalidRdfFile.getAbsolutePath(), "'. Ensure it is a valid RDF file.");

        final String[] cliArgs = new String[5];
        cliArgs[0] = useCase;
        cliArgs[1] = "-m";
        cliArgs[2] = testInvalidRdfFile.getAbsolutePath();
        cliArgs[3] = "-i";
        cliArgs[4] = this.testRdfFile.getAbsolutePath();

        App.main(cliArgs);
        assertThat(this.outStream.toString()).contains(parserMessage);

        cliArgs[2] = this.testRdfFile.getAbsolutePath();
        cliArgs[4] = testInvalidRdfFile.getAbsolutePath();

        App.main(cliArgs);
        assertThat(this.outStream.toString()).contains(parserMessage);
    }

}
