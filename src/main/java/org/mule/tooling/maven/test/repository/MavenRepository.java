/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.maven.test.repository;

import java.io.File;
import java.util.Arrays;

import org.junit.rules.TemporaryFolder;

public class MavenRepository extends TemporaryFolder {

  private ArtifactCreator[] initialArtifacts;

  public MavenRepository() {
    this(new ArtifactCreator[] {});
  }

  public MavenRepository(ArtifactCreator... initialArtifacts) {
    this(null, initialArtifacts);
  }

  public MavenRepository(File repositoryFolder, ArtifactCreator... initialArtifacts) {
    super(repositoryFolder);
    this.initialArtifacts = initialArtifacts;
  }

  @Override
  protected void before() throws Throwable {
    super.before();
    addArtifacts(initialArtifacts);
  }

  public void addArtifacts(ArtifactCreator... artifactCreator) {
    Arrays.stream(artifactCreator).forEach(artifact -> artifact.create(getRoot()));
  }

}
