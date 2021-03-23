package org.folio.edgecommonspring.util;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j2;
import org.folio.edgecommonspring.exception.AuthorizationException;

@Log4j2
public class PropertiesUtil {

  private static final Pattern isURL = Pattern.compile("(?i)^http[s]?://.*");

  private PropertiesUtil() {
  }

  public static Properties getProperties(String secureStorePropFile) {
    Properties secureStoreProps = new Properties();

    log.info("Attempt to load properties from: " + secureStorePropFile);

    if (secureStorePropFile != null) {
      URL url = null;
      try {
        if (isURL.matcher(secureStorePropFile).matches()) {
          url = new URL(secureStorePropFile);
        }

        try (
          InputStream in = url == null ? new FileInputStream(secureStorePropFile) : url.openStream()) {
          secureStoreProps.load(in);
          log.info("Successfully loaded properties from: " + secureStorePropFile);
        }
      } catch (Exception e) {
        throw new AuthorizationException("Failed to load secure store properties");
      }
    } else {
      log.warn("No secure store properties file specified. Using defaults");
    }
    return secureStoreProps;
  }

}
