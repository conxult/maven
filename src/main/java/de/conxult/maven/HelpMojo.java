/*
 * Copyright by https://conxult.de
 */
package de.conxult.maven;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 *
 * @author joerg
 */

@Mojo(name = "help", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class HelpMojo
  extends BaseConxultMojo {

  @Override
  public void executeMojo() throws Exception {
      System.out.println("cx:help");
  }

}
