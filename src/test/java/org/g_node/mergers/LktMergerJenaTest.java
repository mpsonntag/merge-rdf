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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import static org.assertj.core.api.Assertions.assertThat;
import org.g_node.App;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link LktMergerJena} class. Two small RDF files are created and used to test the merge method.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LktMergerJenaTest {

    private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private PrintStream stdout;

    private final String tmpRoot = System.getProperty("java.io.tmpdir");
    private final String testFolderName = this.getClass().getSimpleName();
    private final Path testFileFolder = Paths.get(tmpRoot, testFolderName);
    private final String testMainRdfFileName = "testMain.ttl";
    private final File testMainRdfFile = this.testFileFolder.resolve(this.testMainRdfFileName).toFile();
    private final String testMergeRdfFileName = "testMerge.ttl";
    private final File testMergeRdfFile = this.testFileFolder.resolve(this.testMergeRdfFileName).toFile();

    /**
     * Set up test RDF files for the merge within the test folder and redirect stdout to an outstream.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.stdout = System.out;
        this.outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(this.outStream));

        final String miniMainTTL = "@prefix foaf: <http://xmlns.com/foaf/0.1/> . _:a foaf:name \"MainName\"";
        FileUtils.write(this.testMainRdfFile, miniMainTTL);

        final String miniMergeTTL = "@prefix foaf: <http://xmlns.com/foaf/0.1/> . _:a foaf:name \"MergeName\"";
        FileUtils.write(this.testMergeRdfFile, miniMergeTTL);

        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(
                new ConsoleAppender(
                        new PatternLayout("[%-5p] %m%n")
                )
        );
    }

    /**
     * Reset outstream to console and remove test folder including all temporary files.
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
    public void testPlainMergeAndSave() throws Exception {
        final String useCase = "lkt";
        final Path outputFile = this.testFileFolder.resolve("out.ttl");

        final String[] cliArgs = new String[7];
        cliArgs[0] = useCase;
        cliArgs[1] = "-m";
        cliArgs[2] = this.testMainRdfFile.getAbsolutePath();
        cliArgs[3] = "-i";
        cliArgs[4] = this.testMergeRdfFile.getAbsolutePath();
        cliArgs[5] = "-o";
        cliArgs[6] = outputFile.toString();

        App.main(cliArgs);
        assertThat(Files.exists(outputFile)).isTrue();

        final Stream<String> fileStream = Files.lines(outputFile);
        final List<String> readFile = fileStream.collect(Collectors.toList());
        assertThat(readFile.size()).isEqualTo(5);
        fileStream.close();
    }

    @Test
    public void testBackupMergeAndSave() throws Exception {
        final String useCase = "lkt";
        final String backupNameTail = String.join("", "backup_", this.testMainRdfFileName);

        final String[] cliArgs = new String[5];
        cliArgs[0] = useCase;
        cliArgs[1] = "-m";
        cliArgs[2] = this.testMainRdfFile.getAbsolutePath();
        cliArgs[3] = "-i";
        cliArgs[4] = this.testMergeRdfFile.getAbsolutePath();

        App.main(cliArgs);
        assertThat(Files.exists(this.testMainRdfFile.toPath())).isTrue();

        final Object[] findFileArray = Files.find(this.testFileFolder, 1, (path, attr) ->
                String.valueOf(path).endsWith(backupNameTail)).toArray();

        assertThat(findFileArray.length).isEqualTo(1);
    }

    @Test
    public void testBackupFail() throws Exception {
        final String useCase = "lkt";
        final String mainFile = this.testMainRdfFile.getAbsolutePath();
        final String errorMessage = String.join("", "[ERROR ] While saving backup for file '", mainFile, "'");

        final String[] cliArgs = new String[5];
        cliArgs[0] = useCase;
        cliArgs[1] = "-m";
        cliArgs[2] = mainFile;
        cliArgs[3] = "-i";
        cliArgs[4] = this.testMergeRdfFile.getAbsolutePath();

        App.main(cliArgs);
        App.main(cliArgs);
        assertThat(this.outStream.toString()).contains(errorMessage);
    }

}
