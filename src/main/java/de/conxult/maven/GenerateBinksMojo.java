/*
 * Copyright by https://conxult.de
 */
package de.conxult.maven;

import de.conxult.maven.binks.BinksClassLoader;
import de.conxult.maven.binks.BinksJarFile;
import de.conxult.maven.binks.BinksLog;
import de.conxult.maven.binks.BinksMain;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.maven.RepositoryUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;

/**
 *
 * @author joerg
 */
@Mojo(name = "generate-binks", defaultPhase = LifecyclePhase.PACKAGE)
public class GenerateBinksMojo
    extends BaseConxultMojo {

    @Parameter(required = true)
    String binksMainClass;

    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}.jar")
    File sourceJar;

    @Parameter(defaultValue = "${project.build.directory}/${project.artifactId}-${project.version}-binks.jar")
    File binksJar;

    @Parameter(defaultValue = "${repositorySystemSession}")
    RepositorySystemSession repositorySession;

    @Parameter(defaultValue = "${localRepository}")
    ArtifactRepository localRepository;

    @Component
    ProjectDependenciesResolver projectDependenciesResolver;

    @Override
    public void executeMojo() throws Exception {
        try (
            ZipOutputStream binksZipStream = new ZipOutputStream(new FileOutputStream(binksJar));) {

            List<String> binksResources = new ArrayList<>();

            Set<String> existingSourceJarEntryNames = new HashSet<>();

            // copy current artifact into binks jar
            String binksResource = "binks/" + sourceJar.getName();
            binksResources.add(binksResource);
            binksZipStream.putNextEntry(new ZipEntry(binksResource));

            copyStream(new FileInputStream(sourceJar), binksZipStream);

            // copy dependent artifacts into binks jar
            File m2RepositoryDir = new File(localRepository.getBasedir());

            List<Artifact> allNeededArtifacts = getAllNeededArtifacts();
            for (Artifact artifact : allNeededArtifacts) {
                if (artifact.getScope().equals("compile")) {
                    String artifactResource = String.format("%s-%s-%s.jar", artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
                    binksResource = "binks/" + artifactResource;
                    log.info("add {0}", binksResource);
                    binksResources.add(binksResource);
                    binksZipStream.putNextEntry(new ZipEntry(binksResource));

                    File artifactFile = new File(m2RepositoryDir, localRepository.pathOf(artifact));
                    FileInputStream artifactStream = new FileInputStream(artifactFile);
                    copyStream(artifactStream, binksZipStream);
                }
            }

            // add Binks* .classes
            addClassToZip(existingSourceJarEntryNames, binksZipStream, BinksClassLoader.class);
            addClassToZip(existingSourceJarEntryNames, binksZipStream, BinksJarFile.class);
            addClassToZip(existingSourceJarEntryNames, binksZipStream, BinksMain.class);
            addClassToZip(existingSourceJarEntryNames, binksZipStream, BinksLog.class);

            // create new manifest
            Manifest sourceJarManifest = new JarFile(sourceJar).getManifest();
            sourceJarManifest.getMainAttributes().put(new Attributes.Name("Created-By"), "CX Binks");
            sourceJarManifest.getMainAttributes().put(new Attributes.Name("Main-Class"), BinksMain.class.getName());
            log.config("Binks-Main-Class: {0}", binksMainClass);
            sourceJarManifest.getMainAttributes().put(new Attributes.Name("Binks-Main-Class"), binksMainClass);
            log.config("Binks-Class-Path: {0}", String.join(" ", binksResources.toArray(new String[0])));
            sourceJarManifest.getMainAttributes().put(new Attributes.Name("Binks-Class-Path"), String.join(" ", binksResources.toArray(new String[0])));

            binksZipStream.putNextEntry(new ZipEntry(JarFile.MANIFEST_NAME));
            sourceJarManifest.write(binksZipStream);
        }
    }

    void addClassToZip(Set<String> sourceJarEntryNames, ZipOutputStream binksZipStream, Class clazz) throws IOException {
        String entryName = clazz.getName().replace('.', '/').concat(".class");
        if (!sourceJarEntryNames.contains(entryName.toLowerCase())) {
            InputStream classStream = getClass().getResourceAsStream("/" + entryName);
            binksZipStream.putNextEntry(new ZipEntry(entryName));
            copyStream(classStream, binksZipStream);
        }
    }

    void copyStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[64 * 1024];
        for (int binksBytesRead = is.read(buffer); (binksBytesRead > 0); binksBytesRead = is.read(buffer)) {
            os.write(buffer, 0, binksBytesRead);
        }
    }

    List<Artifact> getAllNeededArtifacts() throws DependencyResolutionException {
        DefaultDependencyResolutionRequest dependencyResolutionRequest = new DefaultDependencyResolutionRequest(project, repositorySession);
        DependencyResolutionResult dependencyResolutionResult = projectDependenciesResolver.resolve(dependencyResolutionRequest);
        Set<Artifact> artifacts = new HashSet<>();
        if (dependencyResolutionResult.getDependencyGraph() != null
            && !dependencyResolutionResult.getDependencyGraph().getChildren().isEmpty()) {
            RepositoryUtils.toArtifacts(artifacts, dependencyResolutionResult.getDependencyGraph().getChildren(),
                Collections.singletonList(project.getArtifact().getId()), new DependencyFilter() {
                @Override
                public boolean accept(DependencyNode dn, List<DependencyNode> list) {
                    return true;
                }
            });
        }
        return new ArrayList<>(artifacts);
    }

}
