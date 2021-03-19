package org.folio.edgecommonspring.util;

import static org.folio.edge.api.utils.Constants.HEADER_API_KEY;
import static org.folio.edge.api.utils.Constants.PARAM_API_KEY;
import static org.folio.edge.api.utils.Constants.PATH_API_KEY;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.servlet.ServletRequest;
import org.apache.catalina.connector.RequestFacade;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyHelper {

  public static final Pattern AUTH_TYPE = Pattern.compile("(?i).*apikey (\\w*).*");
  @Value("${api_key_sources}")
  private String apiKeySources;
  private List<ApiKeySource> sources;

  @PostConstruct
  public void init() {
    sources = new ArrayList<>();
    for (String source : Pattern.compile(",").split(apiKeySources)) {
      sources.add(ApiKeySource.valueOf(source));
    }
  }

  public String getEdgeApiKey(ServletRequest servletRequest) {
    for (ApiKeySource source : sources) {
      String apiKey = null;
      if (ApiKeySource.PARAM == source) {
        apiKey = getFromParam(servletRequest);
      } else if (ApiKeySource.HEADER == source) {
        apiKey = getFromHeader(servletRequest);
      } else if(ApiKeySource.PATH == source) {
        apiKey = getFromPath(servletRequest);
      }
      if (apiKey != null) {
        return apiKey;
      }
    }
    return null;
  }

  private String getFromParam(ServletRequest servletRequest) {
    return servletRequest.getParameter(PARAM_API_KEY);
  }

  private String getFromHeader(ServletRequest servletRequest) {

    String full = ((RequestFacade) servletRequest).getHeader(HEADER_API_KEY);

    if (full == null || full.isEmpty()) {
      return null;
    }

    Matcher matcher = AUTH_TYPE.matcher(full);
    if (matcher.matches()) {
      return matcher.group(1);
    } else {
      return full;
    }
  }

  private String getFromPath(ServletRequest servletRequest) {
    return servletRequest.getParameter(PATH_API_KEY);
  }

  public enum ApiKeySource {
    PARAM, HEADER, PATH
  }
}
