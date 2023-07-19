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

@Mojo(name = "some-help", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class SomeHelpMojo
  extends BaseConxultMojo {

  @Override
  public void executeMojo() throws Exception {
      System.out.println("cx:some-help");

      Path javaSourceDir = Paths.get(GeneratorMojo.GENERATED_SOURCES, "help");
      Path javaPackageDir = javaSourceDir.resolve("de/conxult/maven/help");
      if (Files.notExists(javaPackageDir)) {
          Files.createDirectories(javaPackageDir);
      }
      Path javaSource = javaPackageDir.resolve("SomeHelp.java");
      try (
          PrintWriter java = new PrintWriter(Files.newOutputStream(javaSource));
      ) {
          java.println("package de.conxult.maven.help;");
          java.println();
          java.println("public class SomeHelp {");
          java.println("}");
      }

      project.addCompileSourceRoot(javaSourceDir.toString());
  }

}
