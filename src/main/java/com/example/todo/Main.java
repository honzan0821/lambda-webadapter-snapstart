package com.example.todo;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

public class Main {

  private static final Logger logger = Logger.getLogger(Main.class.getSimpleName());

  public static volatile boolean isPrepared;

  private static Tomcat tomcat = new Tomcat();

  private static String BASE_DIR = System.getProperty("java.io.tmpdir");

  private static boolean isIDE = System.getenv("X_IDE") != null;

  private static int PORT = System.getenv("X_PORT") != null ?
      Integer.valueOf(System.getenv("X_PORT")) : 8080;

  private static String CONTEXT_PATH = System.getenv("X_CONTEXT_PATH") != null ?
      System.getenv("X_CONTEXT_PATH") : "/todo";

  private static String X_WARM_UP_TARGET_PATHS = System.getenv("X_WARM_UP_TARGET_PATHS") != null ?
      System.getenv("X_WARM_UP_TARGET_PATHS") : "/todo/list";

  // static initializerにtomcat起動を入れることでWebAdapterからのHealthcheckがされない挙動だったため切り出す
  static {

    try {
      logger.info("start static initializer.");

      tomcat.setBaseDir(BASE_DIR);
      tomcat.setPort(PORT);
      tomcat.getConnector();
      tomcat.getHost().setAppBase(BASE_DIR);

      Context ctx = null;
      if (isIDE) {
        // for IDE.
        ctx = tomcat.addWebapp(CONTEXT_PATH, new File("src/main/webapp/").getAbsolutePath());
      } else {
        ctx = tomcat.addWebapp(CONTEXT_PATH,
            Main.class.getProtectionDomain().getCodeSource().getLocation().getPath());
      }

      ctx.start();
      tomcat.start();

      warmUp();

      isPrepared = true;
      logger.info("end static initializer.");

    } catch (Throwable t) {
      logger.severe("raise error.");
      logger.severe(t.getMessage());
      t.printStackTrace(System.out);
      System.exit(-1);
    }

  }

  private static void warmUp() throws IOException, InterruptedException {
    logger.info("start warm up.");

    logger.info("send http request.");
    String[] targetUrls = X_WARM_UP_TARGET_PATHS.split(",");
    for (String ele : targetUrls) {
      HttpClient httpClient = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder(
          URI.create("http://localhost:8080" + CONTEXT_PATH + ele)).build();
      httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
    logger.info("end send http request.");

    logger.info("end warm up.");
  }

  public static boolean getIsPrepared() {
    return isPrepared;
  }

  public static void main(String[] args) {
    logger.info("main method start.");

    isPrepared = true;
    logger.info("Server is awaiting.");

    tomcat.getServer().await();
  }

}
