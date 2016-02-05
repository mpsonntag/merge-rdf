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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Class to provide utility methods to the application.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class AppUtils {

    /**
     * Return time stamp formatted corresponding to input format pattern.
     * @param format Input format pattern.
     * @return Formatted timestamp.
     */
    public static String getTimeStamp(final String format) {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(format));
    }

}
