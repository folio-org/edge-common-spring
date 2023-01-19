package org.folio.edgecommonspring.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import org.apache.catalina.connector.RequestFacade;
import org.apache.commons.lang3.ArrayUtils;
import org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters;
import org.folio.edgecommonspring.domain.entity.RequestWithHeaders;
import org.folio.edgecommonspring.security.SecurityManagerService;
import org.folio.edgecommonspring.util.ApiKeyHelperImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EdgeSecurityFilterTest {

  private static final String API_KEY = "eyJzIjoiZ0szc0RWZ3labCIsInQiOiJkaWt1IiwidSI6ImRpa3UifQ==";
  private static final String MOCK_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWt1X2FkbWluIiwidXNlcl9pZCI6ImQyNjUwOGJlLTJmMGItNTUyMC1iZTNkLWQwYjRkOWNkNmY2ZSIsImlhdCI6MTYxNjQ4NDc5NCwidGVuYW50IjoiZGlrdSJ9.VRYeogOD_0s18tM7lNdeIf4BehOgs7sbhn6rBNrKAl80";
  private static final String TENANT = "diku";
  private static final String HEALTH_ENDPOINT = "/admin/health";
  private static final String INFO_ENDPOINT = "/admin/info";
  private static final String TENANT_ENDPOINTS = "/_/tenant";
  private static final String SWAGGER_RESOURCES_ENDPOINT = "/swagger-resources";
  private static final String SWAGGER_DOCS_ENDPOINT = "/v2/api-docs";
  private static final String SWAGGER_UI_ENDPOINT = "/swagger-ui";
  private static RequestFacade request = Mockito.mock(RequestFacade.class);
  private static HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
  private static FilterChain filterChain = Mockito.mock(FilterChain.class);
  @InjectMocks
  private EdgeSecurityFilter edgeSecurityFilter;
  @Mock
  private SecurityManagerService securityManagerService;
  @Mock
  private ApiKeyHelperImpl apiKeyHelperImpl;

  @BeforeAll
  static void beforeAll() {
    request = Mockito.mock(RequestFacade.class);
    response = Mockito.mock(HttpServletResponse.class);
    filterChain = Mockito.mock(FilterChain.class);
  }

  @Test
  void testDoFilter_shouldSetTokenToHeaders() throws IOException, ServletException {
    // given
    ReflectionTestUtils
      .setField(edgeSecurityFilter, "excludeBasePaths", ArrayUtils.EMPTY_STRING_ARRAY);
    when((request).getServletPath()).thenReturn("/tests");
    when((request).getHeaderNames()).thenReturn(Collections.enumeration(Set.of("Accept", "Accept-Encoding")));
    when((request).getRequestURI()).thenReturn("/tests");
    ConnectionSystemParameters connectionSystemParameters = new ConnectionSystemParameters()
      .withTenantId(TENANT)
      .withOkapiToken(MOCK_TOKEN);
    when(apiKeyHelperImpl.getEdgeApiKey(request, apiKeyHelperImpl.getSources())).thenReturn(API_KEY);
    when(securityManagerService.getParamsWithToken(API_KEY)).thenReturn(connectionSystemParameters);
    ArgumentCaptor<RequestWithHeaders> requestCaptor = captureRequest();

    // when
    edgeSecurityFilter.doFilter(request, response, filterChain);

    // then
    Assertions.assertEquals(TENANT, requestCaptor.getValue().getHeader("x-okapi-tenant"));
    Assertions.assertEquals(MOCK_TOKEN, requestCaptor.getValue().getHeader("x-okapi-token"));
    Assertions.assertEquals(2, Collections.list(requestCaptor.getValue().getHeaderNames()).size());
    verify(filterChain).doFilter(requestCaptor.getValue(), response);
  }

  @Test
  void testDoFilter_shouldSetStatus401ToResponse_whenAuthorizationExceptionThrown() throws IOException, ServletException {
    //given
    ReflectionTestUtils
      .setField(edgeSecurityFilter, "excludeBasePaths", new String[]{"/admin"});
    when((request).getServletPath()).thenReturn("/tests");
    when((request).getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when((request).getRequestURI()).thenReturn("/tests");

    // when
    edgeSecurityFilter.doFilter(request, response, filterChain);

    //then
    verify(response).sendError(HttpStatus.UNAUTHORIZED.value(), "Edge API key not found in the request, while query /tests");
  }

  @ParameterizedTest
  @ValueSource(strings = {HEALTH_ENDPOINT, TENANT_ENDPOINTS, INFO_ENDPOINT, SWAGGER_RESOURCES_ENDPOINT, SWAGGER_DOCS_ENDPOINT, SWAGGER_UI_ENDPOINT})
  void testDoFilter_shouldNotCreateParams_whenAuthorizationNotNeeded(String endpoint)
    throws IOException, ServletException {
    //given
    ReflectionTestUtils
      .setField(edgeSecurityFilter, "excludeBasePaths",
        new String[]{"/admin/health", "/admin/info", "/_/tenant"});
    when((request).getServletPath()).thenReturn(endpoint);
    when((request).getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    when((request).getRequestURI()).thenReturn("/_/tenant");

    // when
    edgeSecurityFilter.doFilter(request, response, filterChain);

    // then
    verify(apiKeyHelperImpl, never()).getEdgeApiKey(any(ServletRequest.class), anyList());
    verify(securityManagerService, never()).getParamsWithToken(anyString());
  }

  private ArgumentCaptor<RequestWithHeaders> captureRequest() throws IOException, ServletException {
    ArgumentCaptor<RequestWithHeaders> requestCaptor = ArgumentCaptor.forClass(RequestWithHeaders.class);
    doNothing().when(filterChain).doFilter(requestCaptor.capture(), eq(response));
    return requestCaptor;
  }

}
