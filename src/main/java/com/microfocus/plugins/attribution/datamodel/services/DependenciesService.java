package com.microfocus.plugins.attribution.datamodel.services;

import java.util.List;

import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;

import com.microfocus.plugins.attribution.datamodel.beans.DependencyOverride;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;

public interface DependenciesService {
    List<ProjectDependency> getProjectDependencies(MavenProject project, Settings settings, ArtifactRepository localRepository, DependencyOverride[] dependencyOverrides);
}
