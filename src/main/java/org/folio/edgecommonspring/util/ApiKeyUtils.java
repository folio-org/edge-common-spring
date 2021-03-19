package org.folio.edgecommonspring.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import org.folio.edge.api.utils.model.ClientInfo;

public class ApiKeyUtils {

  private ApiKeyUtils() {
  }

  public static ClientInfo parseApiKey(String apiKey) throws ApiKeyUtils.MalformedApiKeyException {
    ClientInfo clientInfo;

    try {
      String decoded = new String(Base64.getUrlDecoder().decode(apiKey.getBytes()));
      ObjectMapper mapper = new ObjectMapper();
      clientInfo = mapper.readValue(decoded, ClientInfo.class);
    } catch (Exception var4) {
      throw new ApiKeyUtils.MalformedApiKeyException("Failed to parse", var4);
    }

    if (clientInfo.salt != null && !clientInfo.salt.isEmpty()) {
      if (clientInfo.tenantId != null && !clientInfo.tenantId.isEmpty()) {
        if (clientInfo.username != null && !clientInfo.username.isEmpty()) {
          return clientInfo;
        } else {
          throw new ApiKeyUtils.MalformedApiKeyException("Null/Empty Username");
        }
      } else {
        throw new ApiKeyUtils.MalformedApiKeyException("Null/Empty Tenant");
      }
    } else {
      throw new ApiKeyUtils.MalformedApiKeyException("Null/Empty Salt");
    }
  }

  public static class MalformedApiKeyException extends Exception {

    public static final String MSG = "Malformed API Key";
    private static final long serialVersionUID = 7852873967223950947L;

    public MalformedApiKeyException(String msg, Throwable t) {
      super(MSG + ": " + msg, t);
    }

    public MalformedApiKeyException(String msg) {
      super(MSG + ": " + msg);
    }
  }

}
