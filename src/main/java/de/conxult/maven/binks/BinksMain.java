/*
 * Copyright (c) 2018 iTAC Software AG, Germany. All Rights Reserved.
 *
 * This software is protected by copyright. Under no circumstances may any part
 * of this file in any form be copied, printed, edited or otherwise distributed,
 * be stored in a retrieval system, or be translated into another language
 * without the written permission of iTAC Software AG.
 */

package de.conxult.maven.binks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.jar.Manifest;

/**
 *
 * @author joergh
 * @since 13.02.2020
 */
public class BinksMain {

  static final String CACHE = "cache";

  static MessageDigest messageDigester;
  static List<String>  binksResources = new ArrayList<>();
  static String        binksMainClassName;
  static File          base, cache;

  public static void main(String... args) throws Exception {
    messageDigester = MessageDigest.getInstance("SHA-1");
    String binksHome = Arrays.asList(
      System.getProperty("BINKS_HOME"),
      System.getenv("BINKS_HOME"),
      System.getProperty("user.home")+"/.binks")
      .stream()
      .filter(bh -> bh != null)
      .findFirst().get();

    base = new File(binksHome);
    base.mkdirs();
    cache = new File(base, CACHE);
    cache.mkdirs();

    long started = System.currentTimeMillis();

    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    Collections.list(systemClassLoader.getResources("META-INF/MANIFEST.MF"))
      .forEach(BinksMain::checkManifest);

    BinksLog.info("Binks-Class-Path: {0}", binksResources);
    BinksLog.info("Binks-Main-Class: {0}", binksMainClassName);

    BinksClassLoader binksClassLoader = new BinksClassLoader(systemClassLoader);
    for (String binksResource : binksResources) {
      binksClassLoader.addJar(createFile(binksResource, systemClassLoader.getResourceAsStream(binksResource)));
    }

    long finished = System.currentTimeMillis();
    BinksLog.info("binks startup took {0} ms", finished-started);

    Class binksMainClass = binksClassLoader.loadClass(binksMainClassName);
    try {
      Method binksMainMethod = binksMainClass.getDeclaredMethod("main", String[].class);
      binksMainMethod.invoke(null, new Object[] { args });
    } catch (NoSuchMethodException noSuchMethodException) {
      try {
        Constructor binksConstructor = binksMainClass.getConstructor(String[].class);
        binksConstructor.newInstance(new Object[] { args });
      } catch (NoSuchMethodException noSuchConstructorException) {
        BinksLog.error("neither found in {0}", binksMainClass.getName());
        BinksLog.error("- public void main(String... args)");
        BinksLog.error("- public {0}(String... args)", binksMainClass.getSimpleName());
      }
    }
  }

  static File createFile(String name, InputStream is) throws Exception {
    messageDigester.reset();
    ByteArrayOutputStream binksBytes = new ByteArrayOutputStream(1024*1024);
    byte[] buffer = new byte[64*1024];
    for (int bytesRead = is.read(buffer); (bytesRead > 0); bytesRead = is.read(buffer)) {
      binksBytes.write(buffer, 0, bytesRead);
      messageDigester.update(buffer, 0, bytesRead);
    }
    String checkSum = toHex(messageDigester.digest());

    File cachedFile = new File(cache, checkSum + ".jar");
    if (!cachedFile.exists()) {
      BinksLog.debug("caching {0} [{1}]", cachedFile.getName(), name);
      try (
        FileOutputStream cachedFileStream = new FileOutputStream(cachedFile);
      ) {
        cachedFileStream.write(binksBytes.toByteArray());
      }
      try (
        PrintStream properties = new PrintStream(new FileOutputStream(new File(base, "binks.properties"), true));
      ) {
        properties.println(cachedFile.getName()+"="+name);
      }
    }
    return cachedFile;
  }

  static void checkManifest(URL manifestUrl) {
    try {
      checkManifest(new Manifest(manifestUrl.openStream()));
    } catch (IOException ioException) {
    }
  }

  static void checkManifest(Manifest manifest) {
    String manifestClassPath = manifest.getMainAttributes().getValue("Binks-Class-Path");
    if (manifestClassPath != null) {
      binksResources.addAll(Arrays.asList(manifestClassPath.split(" ")));
    }
    String binksMainClass = manifest.getMainAttributes().getValue("Binks-Main-Class");
    if (binksMainClass != null) {
      binksMainClassName = binksMainClass;
    }
  }

  static String toHex(byte... buffer) {
    StringBuilder buf = new StringBuilder(buffer.length * 2);
    for (byte b : buffer) {
      buf.append(String.format("%02x", b));
    }
    return buf.toString();
  }

}
