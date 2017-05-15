package com.microfocus.plugins.attribution.datamodel.services.impl;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.google.common.collect.Lists;
import com.microfocus.plugins.attribution.datamodel.beans.ProjectDependency;

public class ReportsServiceImplTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private ReportsServiceImpl reportsServiceImpl;

    @Before
    public void setUp() {
	reportsServiceImpl = new ReportsServiceImpl();
    }

    @Test
    public void testEnsureParentDirectoryExists() {
        // given a output file in a non-existing target directory
        final File targetDir = new File(folder.getRoot(), "target");
        final File outputFile = new File(targetDir, "attribution.xml");

        // given a dummy project dependency
        final ProjectDependency projectDependency = new ProjectDependency();
        projectDependency.setGroupId("groupId");
        projectDependency.setArtifactId("artifactId");
        projectDependency.setVersion("version");
        final List<ProjectDependency> projectDependencies = Lists.newArrayList(projectDependency);

        // when writing the attributions file
        reportsServiceImpl.createAttributionXmlFile(projectDependencies, outputFile);

        // then the file could be written
        assertTrue(outputFile.exists());
    }

    @Test
    public void testEnsureParentDirExistsDoesNotFailOnMissingParentDir() {
        // given a output file in a non-existing target directory
        final File outputFile = new File("attribution.xml");
        outputFile.deleteOnExit();

        // given a dummy project dependency
        final ProjectDependency projectDependency = new ProjectDependency();
        projectDependency.setGroupId("groupId");
        projectDependency.setArtifactId("artifactId");
        projectDependency.setVersion("version");
        final List<ProjectDependency> projectDependencies = Lists.newArrayList(projectDependency);

        // when writing the attributions file
        reportsServiceImpl.createAttributionXmlFile(projectDependencies, outputFile);

        // then the file could be written
        assertTrue(outputFile.exists());
    }
}
