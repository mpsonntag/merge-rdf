/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.micro.commons;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link FileService} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class FileServiceTest {

    private ByteArrayOutputStream outStream;
    private PrintStream stdout;

    private final String tmpRoot = System.getProperty("java.io.tmpdir");
    private final String testFolderName = this.getClass().getSimpleName();
    private final String testFileName = "test.txt";
    private final Path testFileFolder = Paths.get(tmpRoot, testFolderName);

    /**
     * Create a temporary folder and the main test file in the java temp directory.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        final File currTestFile = this.testFileFolder.resolve(this.testFileName).toFile();
        FileUtils.write(currTestFile, "This is a normal test file");

        this.stdout = System.out;
        this.outStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(this.outStream));

        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.INFO);
        rootLogger.addAppender(
                new ConsoleAppender(
                        new PatternLayout("[%-5p] %m%n")
                )
        );
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
     * Check, that a file without a file extension or a file extension that
     * is not supported, returns false and test that a file with a supported
     * file extension returns true for both checkFileExtension methods.
     * This test creates two additional files.
     * @throws Exception
     */
    @Test
    public void testCheckFileExtension() throws Exception {
        final String testFileNoExtension = "test";
        final String testFileUnsupportedExtension = "test.tex";

        final File currTestFileNoExtension = this.testFileFolder.resolve(testFileNoExtension).toFile();
        final File currTestFileUnsuppExt = this.testFileFolder.resolve(testFileUnsupportedExtension).toFile();

        FileUtils.write(currTestFileNoExtension, "This is a normal test file");
        FileUtils.write(currTestFileUnsuppExt, "This is a normal test file");

        final Set<String> testFileExtensions = Collections.singleton("TXT");

        assertThat(
                FileService.checkFileExtension(
                        currTestFileNoExtension.getAbsolutePath(),
                        testFileExtensions)
        ).isFalse();

        assertThat(
                FileService.checkFileExtension(
                        currTestFileUnsuppExt.getAbsolutePath(),
                        testFileExtensions)
        ).isFalse();

        assertThat(
                FileService.checkFileExtension(
                        this.testFileFolder
                                .resolve(this.testFileName)
                                .toAbsolutePath().normalize().toString(),
                        testFileExtensions)
        ).isTrue();

        final String testFileExtension = "TXT";

        assertThat(
                FileService.checkFileExtension(
                        currTestFileNoExtension.getAbsolutePath(),
                        testFileExtension)
        ).isFalse();

        assertThat(
                FileService.checkFileExtension(
                        currTestFileUnsuppExt.getAbsolutePath(),
                        testFileExtension)
        ).isFalse();

        assertThat(
                FileService.checkFileExtension(
                        this.testFileFolder
                                .resolve(this.testFileName)
                                .toAbsolutePath().normalize().toString(),
                        testFileExtension)
        ).isTrue();
    }

    /**
     * Check that a file copy of a given file is created using the proper timestamp as part of the filename.
     * Also check, that the method returns false if the file already exists.
     * @throws Exception
     */
    @Test
    public void testCreateTimeStampBackupFile() throws Exception {
        final String timeStamp = "yyyyMMddHH";
        final String errorMessage = "FileAlreadyExistsException";
        final Path mainPath = this.testFileFolder.resolve(this.testFileName);

        assertThat(FileService.createTimeStampBackupFile(mainPath.toString(), timeStamp))
                .isTrue();

        final String fileName = mainPath.getFileName().toString();
        final String backupName = String.join("", AppUtils.getTimeStamp(timeStamp), "_backup_", fileName);
        final String backupPath = mainPath.toString().replaceFirst(fileName, backupName);

        assertThat(Files.exists(Paths.get(backupPath).toAbsolutePath())).isTrue();

        // Check that a backup file with the same filename cannot be created.
        assertThat(FileService.createTimeStampBackupFile(mainPath.toString(), timeStamp))
                .isFalse();
        assertThat(this.outStream.toString()).contains(errorMessage);
    }

}
