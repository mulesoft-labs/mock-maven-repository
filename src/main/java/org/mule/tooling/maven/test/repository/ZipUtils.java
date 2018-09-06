/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.maven.test.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Provides utility methods to work with ZIP files
 */
public class ZipUtils {

  private ZipUtils() {}

  /**
   * Describes a resource that can be compressed in a ZIP file
   */
  public static class ZipResource {

    private final byte[] content;
    private final String alias;

    public ZipResource(byte[] content, String alias) {
      this.content = content;
      this.alias = alias;
    }
  }

  /**
   * Compress a set of resource files into a ZIP file
   *
   * @param targetFile file that will contain the zipped files
   * @param resources resources to compress
   * @throws UncheckedIOException in case of any error processing the files
   */
  public static void compress(File targetFile, ZipResource[] resources) {
    try (ZipOutputStream out = new ZipOutputStream(new FileOutputStream(targetFile))) {
      for (ZipResource zipResource : resources) {
        out.putNextEntry(new ZipEntry(zipResource.alias));
        out.write(zipResource.content);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
