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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.net.URL;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.assertThat;
import org.g_node.micro.commons.RDFService;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the {@link ModelUtils} class.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class ModelUtilsTest {

    private Model baseModel;

    @Before
    public void setUp() {
        this.baseModel = ModelFactory.createDefaultModel();
    }

    @Test
    public void testRemoveAnonProperties() throws Exception {

        final URL testFileNameURL = this.getClass().getResource("/testFiles/RemoveAnonNodeTest.ttl");
        final String testFileName = Paths.get(testFileNameURL.toURI()).toFile().toString();

        this.baseModel = RDFService.openModelFromFile(testFileName);

        assertThat(this.baseModel.size()).isEqualTo(7);

        this.baseModel.listObjects().forEachRemaining(
                obj -> {
                    if (obj.isURIResource() && obj.asResource().listProperties().hasNext()) {
                       ModelUtils.removeAnonProperties(obj.asResource().listProperties());
                    }
                });

        assertThat(this.baseModel.size()).isEqualTo(5);
    }

    @Test
    public void testRemovePropertiesFromModel() throws Exception {
        System.out.println(
                String.join("", "[TEST DEBUG] Remove properties; Model size: ",
                        Long.toString(this.baseModel.size()))
        );
    }

}
