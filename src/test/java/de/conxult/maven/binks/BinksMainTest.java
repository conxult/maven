/*
 * Copyright by https://conxult.de
 */
package de.conxult.maven.binks;

/**
 *
 * @author joerg
 */
public class BinksMainTest {

//  @Test
  public void showClassLoaderInfo() throws Exception {
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    System.out.println(systemClassLoader.getClass().getName());
    for (Class i : systemClassLoader.getClass().getInterfaces()) {
      System.out.println("  " + i.getName());
    }
  }
}
