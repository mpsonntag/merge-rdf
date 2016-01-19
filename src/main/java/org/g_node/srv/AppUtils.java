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

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * Class to provide utility methods to the application.
 *
 * @author Michael Sonntag (sonntag@bio.lmu.de)
 */
public class AppUtils {
    /**
     * Prototype method - test various hashing mechanisms.
     */
    public static void testHashing() {
        final String hashMe = "HASH ME";
        final String hashMeResult = "41bc1af4a1782d40a4d3c0fe5396f57d4201f02b1e232e330ea430a642f3d4d0";

        // test hashing - java built in
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] byteDigest = md.digest(hashMe.getBytes("UTF-8"));

            final String sha256hexMD = DatatypeConverter.printHexBinary(byteDigest)
                    .toLowerCase(Locale.ENGLISH);
            final Boolean corrHashMD = hashMeResult.equals(sha256hexMD);

            System.out.println(
                    String.join("", "MessageDigest sha256: ", sha256hexMD,
                            " (correct hash: ", corrHashMD.toString(), ")")
            );

            md = MessageDigest.getInstance("SHA");
            byteDigest = md.digest(hashMe.getBytes("UTF-8"));

            final String sha1hexMD = DatatypeConverter.printHexBinary(byteDigest).toLowerCase(Locale.ENGLISH);

            System.out.println(
                    String.join("", "MessageDigest sha-1: ", sha1hexMD)
            );

            md = MessageDigest.getInstance("SHA-224");
            byteDigest = md.digest(hashMe.getBytes("UTF-8"));

            final String sha224hexMD = DatatypeConverter.printHexBinary(byteDigest)
                    .toLowerCase(Locale.ENGLISH);

            System.out.println(
                    String.join("", "MessageDigest sha-224: ", sha224hexMD)
            );

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        // hashing apache common codec
        final String sha256hex = DigestUtils.sha256Hex(hashMe);
        final Boolean corrHash = hashMeResult.equals(sha256hex);
        System.out.println(
                String.join("", "Apache common sha256: ", sha256hex,
                        " (correct hash: ", corrHash.toString(), ")"));

        final String sha1hex = DigestUtils.shaHex(hashMe);
        System.out.println(String.join("", "Apache common sha-1: ", sha1hex));

        final String sha512hex = DigestUtils.sha512Hex(hashMe);
        System.out.println(String.join("", "Apache common sha-512: ", sha512hex));
    }

}
