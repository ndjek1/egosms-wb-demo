package org.pahappa.systems.egoSms.core.services.impl.whatsapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import org.springframework.stereotype.Component;

@Component
public class AiAssistantConfiguration {
  private final Properties fileValues = new Properties();
  private String loadedFile;

  public AiAssistantConfiguration() {
    String explicit =
        firstNonBlank(System.getProperty("egosms.env.file"), System.getenv("EGOSMS_ENV_FILE"));
    if (explicit != null && loadIfPresent(new File(explicit))) return;
    if (walkForEnv(new File(System.getProperty("user.dir", ".")))) return;
    try {
      URI location =
          AiAssistantConfiguration.class
              .getProtectionDomain()
              .getCodeSource()
              .getLocation()
              .toURI();
      walkForEnv(new File(location));
    } catch (Exception ignored) {
    }
  }

  public String apiKey() {
    return required("GEMINI_API_KEY");
  }

  public String model() {
    return value("GEMINI_MODEL", "gemini-3.5-flash-lite");
  }

  public String generateContentUrl() {
    String base = value("GEMINI_API_BASE_URL", "https://generativelanguage.googleapis.com/v1beta");
    while (base.endsWith("/")) base = base.substring(0, base.length() - 1);
    return base + "/models/" + model() + ":generateContent";
  }

  public int historyLimit() {
    try {
      return Math.max(
          4, Math.min(40, Integer.parseInt(value("GEMINI_CONVERSATION_HISTORY_LIMIT", "20"))));
    } catch (NumberFormatException ignored) {
      return 20;
    }
  }

  private boolean walkForEnv(File start) {
    File directory = start != null && start.isFile() ? start.getParentFile() : start;
    for (int i = 0; i < 14 && directory != null; i++, directory = directory.getParentFile()) {
      if (loadIfPresent(new File(directory, ".env"))) return true;
    }
    return false;
  }

  private boolean loadIfPresent(File file) {
    if (file == null || !file.isFile()) return false;
    load(file);
    loadedFile = file.getAbsolutePath();
    return true;
  }

  private String required(String name) {
    String configured = value(name, null);
    if (configured == null || configured.trim().isEmpty()) {
      String source = loadedFile == null ? "No .env file was found" : "Loaded " + loadedFile;
      throw new IllegalStateException(
          name
              + " is not configured. "
              + source
              + ". Add it to the server environment; never expose it in the browser.");
    }
    return configured;
  }

  private String value(String name, String fallback) {
    String configured = System.getenv(name);
    if (configured == null || configured.trim().isEmpty())
      configured = fileValues.getProperty(name);
    return configured == null || configured.trim().isEmpty() ? fallback : configured.trim();
  }

  private String firstNonBlank(String one, String two) {
    if (one != null && !one.trim().isEmpty()) return one.trim();
    return two == null || two.trim().isEmpty() ? null : two.trim();
  }

  private void load(File file) {
    try {
      BufferedReader reader = new BufferedReader(new FileReader(file));
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.isEmpty() || line.startsWith("#")) continue;
        if (line.startsWith("export ")) line = line.substring(7).trim();
        int split = line.indexOf('=');
        if (split < 1) continue;
        String key = line.substring(0, split).trim();
        String configured = line.substring(split + 1).trim();
        if (configured.length() > 1
            && ((configured.startsWith("\"") && configured.endsWith("\""))
                || (configured.startsWith("'") && configured.endsWith("'")))) {
          configured = configured.substring(1, configured.length() - 1);
        }
        fileValues.setProperty(key, configured);
      }
      reader.close();
    } catch (IOException error) {
      throw new IllegalStateException("Could not read " + file.getAbsolutePath(), error);
    }
  }
}
