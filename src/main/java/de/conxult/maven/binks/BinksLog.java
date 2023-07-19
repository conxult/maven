/*
 * Copyright (c) 2018 iTAC Software AG, Germany. All Rights Reserved.
 *
 * This software is protected by copyright. Under no circumstances may any part
 * of this file in any form be copied, printed, edited or otherwise distributed,
 * be stored in a retrieval system, or be translated into another language
 * without the written permission of iTAC Software AG.
 */

package de.conxult.maven.binks;

import java.text.MessageFormat;

/**
 *
 * @author joergh
 * @since 13.02.2020
 */
public class BinksLog {

  static int LEVEL = 0;
  static {
    String envLogLevel = System.getProperty("cx.log.binks.level", "0");
    try {
      LEVEL = Integer.parseInt(envLogLevel);
    } catch (NumberFormatException numberFormatException) {
      LEVEL = 99;
      error("illegal level (no int)" + envLogLevel);
    }
  }

  public static void error(String message, Object... arguments) {
    log(0, message, arguments);
  }

  public static void warn(String message, Object... arguments) {
    log(1, message, arguments);
  }

  public static void info(String message, Object... arguments) {
    log(2, message, arguments);
  }

  public static void debug(String message, Object... arguments) {
    log(3, message, arguments);
  }

  public static void trace(String message, Object... arguments) {
    log(4, message, arguments);
  }

  public static void log(int level, String message, Object... arguments) {
    if (level >= LEVEL) {
      // nada
    } else if (arguments == null ||arguments.length == 0) {
      System.out.println(message);
    } else {
      System.out.println(MessageFormat.format(message, arguments));
    }
  }


}
