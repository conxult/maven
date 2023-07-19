/*
 * Copyright by https://conxult.de
 */
package de.conxult.maven.binks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author joerg
 */
public class BinksClassLoader
  extends ClassLoader {

  List<BinksJarFile> classPath  = new ArrayList<>();
  Map<String, Class> classCache = new HashMap<>();

  public BinksClassLoader(ClassLoader parent) {
    super(parent);
  }

  public BinksClassLoader addJar(File jarFile) throws IOException {
    BinksLog.debug("addJar {0}", jarFile.getName());
    classPath.add(new BinksJarFile(jarFile));
    return this;
  }

  @Override
  public Class findClass(
    String className
  ) throws ClassNotFoundException {


    if (classCache.containsKey(className)) {
      return classCache.get(className);
    }

    for (BinksJarFile jarFile : classPath) {
      BinksLog.debug("findClass({0}) in {1}", className, jarFile.name);
      byte[] classBytes = jarFile.getClassBytes(className);

      if (classBytes != null) {
        classCache.put(className, defineClass(className, classBytes, 0, classBytes.length));
        break;
      }
    }

    return classCache.get(className);
  }
}
