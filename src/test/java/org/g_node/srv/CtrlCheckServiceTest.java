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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link CtrlCheckService} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class CtrlCheckServiceTest {
    private ByteArrayOutputStream outStream = new ByteArrayOutputStream();
    private PrintStream stdout;

    private final String tmpRoot = System.getProperty("java.io.tmpdir");
    private final String testFolderName = "ctrlCheckServiceTest";
    private final String testFileName = "test.txt";
    private final Path testFileFolder = Paths.get(tmpRoot, testFolderName);

    /**
     * Create a test folder and the main test file in the java temp directory.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        this.stdout = System.out;
        this.outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(this.outStream));

        final File currTestFile = this.testFileFolder.resolve(this.testFileName).toFile();
        FileUtils.write(currTestFile, "This is a normal test file");
    }

    /**
     * Remove all created folders and files after the tests are done.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        System.setOut(this.stdout);

        if (Files.exists(this.testFileFolder)) {
            FileUtils.deleteDirectory(this.testFileFolder.toFile());
        }
    }

    /**
     * Test if the method returns false when presented with a
     * String referencing a non existing file and returns true if it is
     * presented with String referencing an existing file.
     * @throws Exception
     */
    @Test
    public void testExistingFile() throws Exception {
        final String testNonExistingFilePath =
                this.testFileFolder
                        .resolve("IdoNotExist")
                        .toAbsolutePath().normalize().toString();

        assertThat(CtrlCheckService.isExistingFile(testNonExistingFilePath)).isFalse();

        final String testExistingFilePath =
                this.testFileFolder
                        .resolve(this.testFileName)
                        .toAbsolutePath().normalize().toString();

        assertThat(CtrlCheckService.isExistingFile(testExistingFilePath)).isTrue();
    }

    /**
     * Test that the method prints the correct error message if a given String is not
     * found in a given Set.
     * @throws Exception
     */
    @Test
    public void testSupportedInFileType() throws Exception {
        final Set<String> test = Stream.of("TXT", "TTL").collect(Collectors.toSet());

        final String supportedFileType = "/some/path/someFile.txt";
        final String unsupportedFileType = "/some/path/someFile.tex";
        final String unsupportedMissingFileType = "/some/path/someFile";
        final String unsupportedLastEndingIsUsed = "/some/path/someFile.txt.tex";

        final String errorMessage = " cannot be read.";

        assertThat(CtrlCheckService.isSupportedInFileType(supportedFileType, test)).isTrue();

        assertThat(CtrlCheckService.isSupportedInFileType(unsupportedFileType, test)).isFalse();
        assertThat(this.outStream.toString().contains(
                String.join("", unsupportedFileType, errorMessage)
        ));
        assertThat(CtrlCheckService.isSupportedInFileType(unsupportedMissingFileType, test)).isFalse();
        assertThat(this.outStream.toString().contains(
                String.join("", unsupportedMissingFileType, errorMessage)
        ));
        assertThat(CtrlCheckService.isSupportedInFileType(unsupportedLastEndingIsUsed, test)).isFalse();
        assertThat(this.outStream.toString().contains(
                String.join("", unsupportedLastEndingIsUsed, errorMessage)
        ));
    }

    /**
     * Test that the method checks that the method returns true in case of valid RDF files and false of
     * files that are not RDF files. Test, that the method returns proper error messages in
     * case of invalid RDF files.
     * @throws Exception
     */
    @Test
    public void testIsValidRdfFile() throws Exception {
        final String miniTTL = "@prefix foaf: <http://xmlns.com/foaf/0.1/> .\n\n_:a foaf:name\t\"TestName\"";
        final File validRdfFile = this.testFileFolder.resolve("test.ttl").toFile();
        FileUtils.write(validRdfFile, miniTTL);

        final String errorMessage = "Failed to load file '";

        assertThat(CtrlCheckService.isValidRdfFile(validRdfFile.getAbsolutePath())).isTrue();

        final File inValidRdfFile = this.testFileFolder.resolve("test.ttl").toFile();
        FileUtils.write(inValidRdfFile, "Invalid RDF file");

        assertThat(CtrlCheckService.isValidRdfFile(inValidRdfFile.getAbsolutePath())).isFalse();
        assertThat(this.outStream.toString().contains(
                String.join("", errorMessage, inValidRdfFile.getAbsolutePath())));

        assertThat(CtrlCheckService.isValidRdfFile(this.testFileName)).isFalse();
        assertThat(this.outStream.toString().contains(
                String.join("", errorMessage, this.testFileName)));
    }

}
