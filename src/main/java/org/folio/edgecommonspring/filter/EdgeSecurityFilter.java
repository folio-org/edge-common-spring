package org.folio.edgecommonspring.filter;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;

import java.io.IOException;
import java.util.Arrays;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.folio.edgecommonspring.domain.entity.RequestWithHeaders;
import org.folio.edgecommonspring.exception.AuthorizationException;
import org.folio.edgecommonspring.security.SecurityManagerService;
import org.folio.edgecommonspring.util.ApiKeyHelper;
import org.folio.spring.integration.XOkapiHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component("defaultEdgeSecurityFilter")
@ConditionalOnMissingBean(name = "edgeSecurityFilter")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "edge.security.filter", name = "enabled", matchIfMissing = true)
public class EdgeSecurityFilter extends GenericFilterBean {

  public static final String HEALTH_ENDPOINT = "/admin/health";
  public static final String INFO_ENDPOINT = "/admin/info";
  public static final String TENANT_ENDPOINTS = "/_/tenant";
  public static final String PROXY_ENDPOINTS = "/_/proxy";
  private final SecurityManagerService securityManagerService;
  private final ApiKeyHelper apiKeyHelper;
  @Value("${header.edge.validation.exclude:/admin/health,/admin/info,/_/tenant,/_/proxy}")
  private String[] excludeBasePaths;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
    throws IOException, ServletException {

    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    RequestWithHeaders wrapper = new RequestWithHeaders(httpRequest);

    if (isAuthorizationNeeded(wrapper)) {
      var edgeApiKey = apiKeyHelper.getEdgeApiKey(request);

      if (StringUtils.isEmpty(edgeApiKey)) {
        throw new AuthorizationException("Edge API key not found in the request");
      }

      var requiredOkapiHeaders = securityManagerService.getParamsWithToken(edgeApiKey);

      wrapper.putHeader(XOkapiHeaders.TOKEN, requiredOkapiHeaders.getOkapiToken());
      wrapper.putHeader(TENANT, requiredOkapiHeaders.getTenantId());

    }
    filterChain.doFilter(wrapper, response);
  }

  private boolean isAuthorizationNeeded(RequestWithHeaders wrapper) {
    return Arrays.stream(excludeBasePaths)
      .noneMatch(wrapper.getRequestURI()::startsWith);
  }
}
