/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.tooling.maven.test.repository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.eclipse.aether.util.version.GenericVersionScheme;
import org.eclipse.aether.version.InvalidVersionSpecificationException;

public class ArtifactCreator {

  private static final String MAVEN_MODEL_VERSION = "4.0.0";
  private static final String TYPE_POM = "pom";
  private static final String TYPE_JAR = "jar";

  private final String groupId;
  private final String artifactId;
  private final String version;
  private final String type;
  private final String classifier;
  private final List<DependencyBuilder> dependencies;
  private GenericVersionScheme versionScheme = new GenericVersionScheme();

  public static Builder<Builder> artifact(String groupId, String artifactId, String version) {
    return new Builder<>().groupId(groupId).artifactId(artifactId).version(version);
  }

  public static DependencyBuilder dependency(String groupId, String artifactId, String version) {
    return new DependencyBuilder().groupId(groupId).artifactId(artifactId).version(version);
  }

  private ArtifactCreator(String groupId, String artifactId, String version, String type, String classifier,
                          List<DependencyBuilder> dependencies) {
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.type = type;
    this.classifier = classifier;
    this.dependencies = dependencies;
  }

  public void create(File repositoryFolder) {
    Model model = new Model();
    model.setModelVersion(MAVEN_MODEL_VERSION);
    model.setGroupId(groupId);
    model.setArtifactId(artifactId);
    model.setVersion(version);
    model.setPackaging(type);

    List<Dependency> mavenDependencies = new ArrayList<>(dependencies.size());
    for (DependencyBuilder dependency : dependencies) {
      ArtifactCreator dependencyArtifact = dependency.build();
      if (!isVersionRange(dependency.version)) {
        dependencyArtifact.create(repositoryFolder);
      }
      mavenDependencies.add(dependency.buildDependency());
    }

    if (!mavenDependencies.isEmpty()) {
      model.setDependencies(mavenDependencies);
    }
    File pomFile = getFile(repositoryFolder, TYPE_POM);
    pomFile.getParentFile().mkdirs();
    try {
      if (!TYPE_POM.equals(type)) {
        File artifactFile = getFile(repositoryFolder, type);
        artifactFile.createNewFile();
      }
      new MavenXpp3Writer().write(new FileWriter(pomFile), model);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private boolean isVersionRange(String version) {
    try {
      versionScheme.parseVersionRange(version);
      return true;
    } catch (InvalidVersionSpecificationException e) {
      return false;
    }
  }

  private File getFile(File repositoryFolder, String type) {
    StringBuilder file = new StringBuilder(repositoryFolder.getAbsolutePath());
    for (String groupSegment : groupId.split("\\.")) {
      file.append(File.separator);
      file.append(groupSegment);
    }
    file.append(File.separator);
    file.append(artifactId);
    file.append(File.separator);
    file.append(version);
    file.append(File.separator);
    file.append(artifactId).append("-").append(version);
    if (!TYPE_POM.equals(type) && classifier != null) {
      file.append("-").append(classifier);
    }
    file.append(".").append(type);
    return new File(file.toString());
  }

  public static class Builder<T extends Builder> {

    protected String groupId;
    protected String artifactId;
    protected String version;
    protected String type = TYPE_JAR;
    protected String classifier;
    protected String optional;
    private List<DependencyBuilder> dependencies = Collections.emptyList();

    public T groupId(String groupId) {
      this.groupId = groupId;
      return (T) this;
    }

    public T artifactId(String artifactId) {
      this.artifactId = artifactId;
      return (T) this;
    }

    public T version(String version) {
      this.version = version;
      return (T) this;
    }

    public T type(String type) {
      this.type = type;
      return (T) this;
    }

    public T classifier(String classifier) {
      this.classifier = classifier;
      return (T) this;
    }

    public T optional(String optional) {
      this.optional = optional;
      return (T) this;
    }

    public T dependencies(DependencyBuilder... dependencyBuilders) {
      dependencies = Arrays.asList(dependencyBuilders);
      return (T) this;
    }

    public ArtifactCreator build() {
      return new ArtifactCreator(groupId, artifactId, version, type, classifier, dependencies);
    }
  }

  public static class DependencyBuilder extends Builder<DependencyBuilder> {

    private String scope;

    public DependencyBuilder scope(String scope) {
      this.scope = scope;
      return this;
    }

    public Dependency buildDependency() {
      Dependency mavenDependency = new Dependency();
      mavenDependency.setGroupId(groupId);
      mavenDependency.setArtifactId(artifactId);
      mavenDependency.setVersion(version);
      mavenDependency.setOptional(optional);
      if (!TYPE_JAR.equals(type)) {
        mavenDependency.setType(type);
      }
      if (classifier != null) {
        mavenDependency.setClassifier(classifier);
      }
      if (scope != null) {
        mavenDependency.setScope(scope);
      }
      return mavenDependency;
    }
  }
}
