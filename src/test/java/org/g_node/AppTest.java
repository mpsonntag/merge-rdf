/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import static org.assertj.core.api.Assertions.assertThat;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the main App. Output and Error streams are redirected
 * from the console to a different PrintStream and reset after tests are finished
 * to avoid mixing tool error messages with actual test error messages.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class AppTest {

    private ByteArrayOutputStream outStream;
    private PrintStream stdout;
    private String tool = "lkt";

    /**
     * Redirect Error and Out stream.
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

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
     * Reset Out stream to the console after the tests are done.
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        System.setOut(this.stdout);
    }

    @Test
    public void testEmptyMain() throws Exception {
        final String[] emptyArgs = new String[0];
        App.main(emptyArgs);
        assertThat(this.outStream.toString()).contains("[ERROR] No existing merge tool selected!");
    }

    @Test
    public void testMainInvalidTool() throws Exception {
        final String[] invalidTool = new String[1];
        invalidTool[0] = "iDoNotExist";
        App.main(invalidTool);
        assertThat(this.outStream.toString()).contains("[ERROR] No existing merge tool selected!");
    }

    @Test
    public void testMainPrintHelp() throws Exception {
        final String[] helpArgs = new String[4];
        helpArgs[0] = this.tool;
        helpArgs[1] = "-h";
        helpArgs[2] = "-i file";
        helpArgs[3] = "-m file";
        App.main(helpArgs);
        assertThat(this.outStream.toString()).contains("usage: Help");
    }

    @Test
    public void testMainInvalidArgs() throws Exception {
        final String invalidArgument = "-nonExistingArgument";
        final String[] invalidArgs = new String[2];
        invalidArgs[0] = this.tool;
        invalidArgs[1] = invalidArgument;
        App.main(invalidArgs);
        assertThat(this.outStream.toString()).contains(String.join("", "Unrecognized option: ", invalidArgument));
    }

}
