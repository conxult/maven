/*
 * Copyright by https://conxult.de
 */
package de.conxult.maven.binks;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 *
 * @author joerg
 */
public class BinksJarFile {

  String              name;
  Map<String, byte[]> content = new HashMap<>();

  public BinksJarFile(File jarFile) throws IOException {
    name = jarFile.getAbsolutePath();
    try (
      ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(jarFile));
    ) {
      for (ZipEntry zipEntry = zipInputStream.getNextEntry(); (zipEntry != null); zipEntry = zipInputStream.getNextEntry()) {
        if (!zipEntry.isDirectory()) {
          BinksLog.trace("{0}:{1}", jarFile.getName(), zipEntry.getName());
          ByteArrayOutputStream bytes = new ByteArrayOutputStream(Math.max(64*1024, (int)zipEntry.getSize()));
          byte[] buffer = new byte[64*1024];
          for (int len = zipInputStream.read(buffer); (len > 0); len = zipInputStream.read(buffer)) {
            bytes.write(buffer, 0, len);
          }
          content.put(zipEntry.getName(), bytes.toByteArray());
        }
      }
    }
  }

  public boolean containsClass(String className) {
    return containsResource(classResourceName(className));
  }

  public boolean containsResource(String resourceName) {
    return content.containsKey(resourceName);
  }

  public byte[] getClassBytes(String className) {
    return getResourceBytes(classResourceName(className));
  }

  public byte[] getResourceBytes(String resourceName) {
    return content.get(resourceName);
  }

  private String classResourceName(String className) {
    return className.replace('.', '/') + ".class";
  }

}
