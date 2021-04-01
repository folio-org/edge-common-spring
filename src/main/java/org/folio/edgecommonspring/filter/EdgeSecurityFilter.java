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
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.folio.edgecommonspring.domain.entity.RequestWithHeaders;
import org.folio.edgecommonspring.exception.AuthorizationException;
import org.folio.edgecommonspring.security.SecurityManagerService;
import org.folio.edgecommonspring.util.ApiKeyHelperImpl;
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
@Log4j2
public class EdgeSecurityFilter extends GenericFilterBean {

  private final SecurityManagerService securityManagerService;
  private final ApiKeyHelperImpl apiKeyHelperImpl;
  @Value("${header.edge.validation.exclude:/admin/health,/admin/info,/_/tenant}")
  private String[] excludeBasePaths;

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
    throws IOException, ServletException {

    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    RequestWithHeaders wrapper = new RequestWithHeaders(httpRequest);

    if (isAuthorizationNeeded(wrapper)) {
      log.debug("Trying to get token while query: {}", ((HttpServletRequest) request).getRequestURI());
      var edgeApiKey = apiKeyHelperImpl.getEdgeApiKey(request, apiKeyHelperImpl.getSources());

      if (StringUtils.isEmpty(edgeApiKey)) {
        throw new AuthorizationException(
          "Edge API key not found in the request, while query " + ((HttpServletRequest) request).getRequestURI());
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
