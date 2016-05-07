/**
 * Copyright (c) 2016, German Neuroinformatics Node (G-Node)
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted under the terms of the BSD License. See
 * LICENSE file in the root of the Project.
 */

package org.g_node.micro.rdf;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.io.FileUtils;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.After;
import org.junit.Test;

/**
 * Unit tests for the {@link RdfUtilsJena} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class RdfUtilsJenaTest {

    private final String tmpRoot = System.getProperty("java.io.tmpdir");
    private final String testFolderName = this.getClass().getSimpleName();
    private final Path testFileFolder = Paths.get(tmpRoot, testFolderName);

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
     * Method tests that a only a nonempty {@link Literal} is added to a Jena {@link Resource}.
     */
    @Test
    public void testAddNonEmptyLiteral() {
        final Model m = ModelFactory.createDefaultModel();
        final Resource r = m.createResource("testResource");
        final Property p = RDFS.comment;
        final String s = "testComment";

        assertThat(r.hasProperty(RDFS.comment)).isFalse();

        RdfUtilsJena.addNonEmptyLiteral(r, p, null);
        assertThat(r.hasProperty(p)).isFalse();
        assertThat(r.listProperties().toList().size()).isEqualTo(0);

        RdfUtilsJena.addNonEmptyLiteral(r, p, "");
        assertThat(r.hasProperty(p)).isFalse();
        assertThat(r.listProperties().toList().size()).isEqualTo(0);

        RdfUtilsJena.addNonEmptyLiteral(r, p, s);
        assertThat(r.hasProperty(RDFS.comment)).isTrue();
        assertThat(r.listProperties().toList().size()).isEqualTo(1);
        assertThat(r.getProperty(RDFS.comment).getLiteral().toString())
                .isEqualTo(String.join("", s, "^^", RdfConstants.RDF_NS_XSD, "string"));
    }

    /**
     * Method tests that all properties are removed from all anonymous nodes of a model.
     * @throws Exception
     */
    @Test
    public void testRemoveAnonProperties() throws Exception {

        final String rdfFileContent = String.join("",
                "@prefix res:   <http://test.org/testResource/> .\n" +
                        "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                        "@prefix xs:    <http://www.w3.org/2001/XMLSchema#> .\n" +
                        "\n" +
                        "res:RootID\n" +
                        "        res:hasDeleteAnonPropsTestNode    res:TestID .\n" +
                        "\n" +
                        "res:TestID\n" +
                        "\t\ta\t\t\t\t\t\tres:DeleteAnonPropsTestNode ;\n" +
                        "\t\trdfs:label              \"AnonNodeTest\" ;\n" +
                        "        rdfs:comment            \"test comment\"^^xs:string ;\n" +
                        "        res:hasAnonNode         [ res:hasAnonProperty   \"Anon property literal 1\"^^xs:string ;\n" +
                        "                                   res:hasAnonProperty   \"Anon property literal 2\"^^xs:string ];\n" +
                        "        res:hasAnonNode         [ res:hasAnonProperty   \"Anon property literal 3\"^^xs:string ;\n" +
                        "                                   res:hasAnonProperty   \"Anon property literal 4\"^^xs:string\n" +
                        "                                ] .\n"
        );

        final File testFile = this.testFileFolder.resolve("tmp.ttl").toFile();
        FileUtils.write(testFile, rdfFileContent);

        Model m = RdfFileServiceJena.openModelFromFile(testFile.getAbsolutePath());

        assertThat(m.size()).isEqualTo(10);

        m.listObjects().forEachRemaining(
                obj -> {
                    if (obj.isURIResource() && obj.asResource().listProperties().hasNext()) {
                        RdfUtilsJena.removeAnonProperties(obj.asResource().listProperties());
                    }
                });

        assertThat(m.size()).isEqualTo(6);
    }

    @Test
    public void testRemovePropertiesFromModel() throws Exception {
        // TODO implement test
        System.out.println("TODO implement testRemovePropertiesFromModel");
    }

}
