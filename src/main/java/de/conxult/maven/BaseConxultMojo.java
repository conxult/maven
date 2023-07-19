/*
 * Copyright by https://conxult.de
 */
package de.conxult.maven;

import de.conxult.log.Log;
import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 *
 * @author joerg
 */
abstract public class BaseConxultMojo extends AbstractMojo {

  protected Log log;

  @Parameter(required = true, defaultValue = "${project}")
  protected MavenProject project;

  @Parameter(required = true, defaultValue = "${project.basedir}")
  protected File baseDir;

  @Parameter(required = true, defaultValue = "${project.build.directory}")
  protected File targetDir;

  @Parameter(property = "debug", defaultValue = "false")
  boolean debug = false;

  protected BaseConxultMojo() {
    log = Log.instance(getClass().getSimpleName());
    Thread.currentThread().setName(getClass().getSimpleName());
    if (project != null) {
      log.info("project={0}:{1}:{2}", project.getGroupId(), project.getArtifactId(), project.getVersion());
    }
    if (baseDir != null) {
      log.info("baseDir={0}", baseDir.getAbsolutePath());
    }
    if (targetDir != null) {
      log.info("targetDir={0}", targetDir.getAbsolutePath());
    }
    log.info("debug={0}", debug);
  }

  abstract public void executeMojo() throws Exception;

  @Override
  public void execute() throws MojoExecutionException {
      try {
          executeMojo();
      } catch (Exception exception) {
          log.error(exception, "{0}.executeMojo() failed", getClass().getSimpleName());
          throw new MojoExecutionException(exception);
      }
  }


}
