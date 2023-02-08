package org.folio.edgecommonspring.filter;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.edge.api.utils.exception.AuthorizationException;
import org.folio.edgecommonspring.domain.entity.RequestWithHeaders;
import org.folio.edgecommonspring.security.SecurityManagerService;
import org.folio.edgecommonspring.util.ApiKeyHelperImpl;
import org.folio.spring.integration.XOkapiHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component("defaultEdgeSecurityFilter")
@ConditionalOnMissingBean(name = "edgeSecurityFilter")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "edge.security.filter", name = "enabled", matchIfMissing = true)
@Log4j2
public class EdgeSecurityFilter extends GenericFilterBean {

  private final SecurityManagerService securityManagerService;
  private final ApiKeyHelperImpl apiKeyHelperImpl;
  @Value("${header.edge.validation.exclude:/admin/health,/admin/info,/swagger-resources,/v2/api-docs,/swagger-ui,/_/tenant}")
  private String[] excludeBasePaths;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
    throws IOException, ServletException {

    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    var wrapper = new RequestWithHeaders(httpRequest);
    try {
      if (isAuthorizationNeeded(wrapper)) {
        log.debug("Trying to get token while query: {}", ((HttpServletRequest) request).getRequestURI());
        var edgeApiKey = apiKeyHelperImpl.getEdgeApiKey(request, apiKeyHelperImpl.getSources());
        if (StringUtils.isEmpty(edgeApiKey)) {
          String errorMessage = String.format("Edge API key not found in the request, while query %s", ((HttpServletRequest) request).getRequestURI());
          throw new AuthorizationException(errorMessage);
        }
        var requiredOkapiHeaders = securityManagerService.getParamsWithToken(edgeApiKey);
        wrapper.putHeader(XOkapiHeaders.TOKEN, requiredOkapiHeaders.getOkapiToken());
        wrapper.putHeader(TENANT, requiredOkapiHeaders.getTenantId());
      }
    } catch (AuthorizationException e) {
      HttpServletResponse httpServletResponse = (HttpServletResponse) response;
      handleAuthorizationException(httpServletResponse, e);
      return;
    }
    filterChain.doFilter(wrapper, response);
  }

  private void handleAuthorizationException(HttpServletResponse response, AuthorizationException e) throws IOException {
    log.error(e.getMessage(), e);
    response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
  }

  private boolean isAuthorizationNeeded(RequestWithHeaders wrapper) {
    return Arrays.stream(excludeBasePaths)
      .noneMatch(wrapper.getRequestURI()::startsWith);
  }
}
