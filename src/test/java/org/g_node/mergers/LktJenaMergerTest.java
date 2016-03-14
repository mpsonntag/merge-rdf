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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import static org.assertj.core.api.Assertions.assertThat;
import org.apache.commons.io.FileUtils;
import org.g_node.App;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the LktJenaMerger class. Two small RDF files are created and used to test the merge method.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class LktJenaMergerTest {

    private final String tmpRoot = System.getProperty("java.io.tmpdir");
    private final String testFolderName = "LktCliControllerTest";
    private final Path testFileFolder = Paths.get(tmpRoot, testFolderName);
    private final String testMainRdfFileName = "testMain.ttl";
    private final File testMainRdfFile = this.testFileFolder.resolve(this.testMainRdfFileName).toFile();
    private final String testMergeRdfFileName = "testMerge.ttl";
    private final File testMergeRdfFile = this.testFileFolder.resolve(this.testMergeRdfFileName).toFile();

    /**
     * Set test RDF files for the merge.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
        final String miniMainTTL = "@prefix foaf: <http://xmlns.com/foaf/0.1/> . _:a foaf:name \"MainName\"";
        FileUtils.write(this.testMainRdfFile, miniMainTTL);

        final String miniMergeTTL = "@prefix foaf: <http://xmlns.com/foaf/0.1/> . _:a foaf:name \"MergeName\"";
        FileUtils.write(this.testMergeRdfFile, miniMergeTTL);
    }

    /**
     * Clean up test folder.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
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

        final List<String> readFile = Files.lines(outputFile).collect(Collectors.toList());
        assertThat(readFile.size()).isEqualTo(5);
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

}