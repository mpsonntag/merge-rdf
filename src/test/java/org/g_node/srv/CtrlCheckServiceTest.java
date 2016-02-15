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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        final File currTestFile = this.testFileFolder.resolve(this.testFileName).toFile();
        FileUtils.write(currTestFile, "This is a normal test file");
    }

    /**
     * Remove all created folders and files after the tests are done.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
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

}
