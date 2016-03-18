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
import java.net.URL;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link RdfUtilsJena} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class RdfUtilsJenaTest {

    private Model baseModel;

    @Before
    public void setUp() {
        this.baseModel = ModelFactory.createDefaultModel();
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

    @Test
    public void testRemoveAnonProperties() throws Exception {
        // TODO replace test file with a dynamically created file
        final URL testFileNameURL = this.getClass().getResource("/testFiles/RemoveAnonNodeTest.ttl");
        final String testFileName = Paths.get(testFileNameURL.toURI()).toFile().toString();

        this.baseModel = RdfFileServiceJena.openModelFromFile(testFileName);

        assertThat(this.baseModel.size()).isEqualTo(7);

        this.baseModel.listObjects().forEachRemaining(
                obj -> {
                    if (obj.isURIResource() && obj.asResource().listProperties().hasNext()) {
                        RdfUtilsJena.removeAnonProperties(obj.asResource().listProperties());
                    }
                });

        assertThat(this.baseModel.size()).isEqualTo(5);
    }

    @Test
    public void testRemovePropertiesFromModel() throws Exception {
        // TODO implement test
        System.out.println("TODO implement testRemovePropertiesFromModel");
        System.out.println(
                String.join("", "[TEST DEBUG] Remove properties; Model size: ",
                        Long.toString(this.baseModel.size()))
        );
    }

}
