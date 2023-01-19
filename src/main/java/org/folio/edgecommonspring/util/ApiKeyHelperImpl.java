package org.folio.edgecommonspring.util;

import static org.folio.edge.api.utils.Constants.HEADER_API_KEY;
import static org.folio.edge.api.utils.Constants.LEGACY_PARAM_API_KEY;
import static org.folio.edge.api.utils.Constants.PARAM_API_KEY;
import static org.folio.edge.api.utils.Constants.PATH_API_KEY;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.folio.edge.api.utils.util.ApiKeyHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyHelperImpl implements ApiKeyHelper {

  public static final Pattern AUTH_TYPE = Pattern.compile("(?i).*apikey (\\w*).*");
  @Value("${api_key_sources:PARAM,HEADER,PATH}")
  private String apiKeySources;
  private List<ApiKeySource> sources;

  @PostConstruct
  public void init() {
    sources = new ArrayList<>();
    for (String source : Pattern.compile(",").split(apiKeySources)) {
      sources.add(ApiKeySource.valueOf(source));
    }
  }

  @Override
  public String getFromParam(Object request) {
    ServletRequest servletRequest = (ServletRequest) request;
    if(servletRequest.getParameterMap().containsKey(PARAM_API_KEY)) {
      return servletRequest.getParameter(PARAM_API_KEY);
    }
    return servletRequest.getParameter(LEGACY_PARAM_API_KEY);
  }

  @Override
  public String getFromHeader(Object servletRequest) {
    String full = ((HttpServletRequest) servletRequest).getHeader(HEADER_API_KEY);

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

  @Override
  public String getFromPath(Object servletRequest) {
    return ((ServletRequest) servletRequest).getParameter(PATH_API_KEY);
  }

  public List<ApiKeySource> getSources() {
    return sources;
  }

}
