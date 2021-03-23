package org.folio.edgecommonspring.filter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.apache.catalina.connector.RequestFacade;
import org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters;
import org.folio.edgecommonspring.domain.entity.RequestWithHeaders;
import org.folio.edgecommonspring.exception.AuthorizationException;
import org.folio.edgecommonspring.security.SecurityManagerService;
import org.folio.edgecommonspring.util.ApiKeyHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
class EdgeSecurityFilterTest {

  private static final String API_KEY = "eyJzIjoiZ0szc0RWZ3labCIsInQiOiJkaWt1IiwidSI6ImRpa3UifQ==";
  private static final String MOCK_TOKEN = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkaWt1X2FkbWluIiwidXNlcl9pZCI6ImQyNjUwOGJlLTJmMGItNTUyMC1iZTNkLWQwYjRkOWNkNmY2ZSIsImlhdCI6MTYxNjQ4NDc5NCwidGVuYW50IjoiZGlrdSJ9.VRYeogOD_0s18tM7lNdeIf4BehOgs7sbhn6rBNrKAl80";
  private static final String TENANT = "diku";
  private static RequestFacade request = Mockito.mock(RequestFacade.class);
  private static ServletResponse response = Mockito.mock(ServletResponse.class);
  private static FilterChain filterChain = Mockito.mock(FilterChain.class);
  @InjectMocks
  private EdgeSecurityFilter edgeSecurityFilter;
  @Mock
  private SecurityManagerService securityManagerService;
  @Mock
  private ApiKeyHelper apiKeyHelper;

  @BeforeAll
  static void beforeAll() {
    request = Mockito.mock(RequestFacade.class);
    response = Mockito.mock(ServletResponse.class);
    filterChain = Mockito.mock(FilterChain.class);
  }

  @Test
  void testDoFilter_shouldSetTokenToHeaders() throws IOException, ServletException {
    // given
    when((request).getServletPath()).thenReturn("/tests");
    when((request).getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    ConnectionSystemParameters connectionSystemParameters = new ConnectionSystemParameters()
      .withTenantId(TENANT)
      .withOkapiToken(MOCK_TOKEN);
    when(apiKeyHelper.getEdgeApiKey(request)).thenReturn(API_KEY);
    when(securityManagerService.getParamsWithToken(API_KEY)).thenReturn(connectionSystemParameters);
    ArgumentCaptor<RequestWithHeaders> requestCaptor = captureRequest();

    // when
    edgeSecurityFilter.doFilter(request, response, filterChain);

    // then
    Assertions.assertEquals(TENANT, requestCaptor.getValue().getHeader("x-okapi-tenant"));
    Assertions.assertEquals(MOCK_TOKEN, requestCaptor.getValue().getHeader("x-okapi-token"));
    Mockito.verify(filterChain).doFilter(requestCaptor.getValue(), response);
  }

  @Test
  void testDoFilter_shouldThrowAuthorizationException() throws IOException, ServletException {
    //given

    when((request).getServletPath()).thenReturn("/tests");
    when((request).getHeaderNames()).thenReturn(Collections.emptyEnumeration());

    // when & then
    AuthorizationException exception = Assertions.assertThrows(AuthorizationException.class,
      () -> edgeSecurityFilter.doFilter(request, response, filterChain));
    Assertions.assertEquals("Edge API key not found in the request", exception.getMessage());
  }

  @Test
  void testDoFilter_shouldNotCreateParams_whenHealthEndpoint() throws IOException, ServletException {
    //given

    when((request).getServletPath()).thenReturn(EdgeSecurityFilter.HEALTH_ENDPOINT);
    when((request).getHeaderNames()).thenReturn(Collections.emptyEnumeration());

    // when
    edgeSecurityFilter.doFilter(request, response, filterChain);

    // then
    Mockito.verify(apiKeyHelper, never()).getEdgeApiKey(any(ServletRequest.class));
    Mockito.verify(securityManagerService, never()).getParamsWithToken(anyString());
  }

  @Test
  void testDoFilter_shouldNotCreateParams_whenInfoEndpoint() throws IOException, ServletException {
    //given

    when((request).getServletPath()).thenReturn(EdgeSecurityFilter.INFO_ENDPOINT);
    when((request).getHeaderNames()).thenReturn(Collections.emptyEnumeration());

    // when
    edgeSecurityFilter.doFilter(request, response, filterChain);

    // then
    Mockito.verify(apiKeyHelper, never()).getEdgeApiKey(any(ServletRequest.class));
    Mockito.verify(securityManagerService, never()).getParamsWithToken(anyString());
  }

  private ArgumentCaptor<RequestWithHeaders> captureRequest() throws IOException, ServletException {
    ArgumentCaptor<RequestWithHeaders> requestCaptor = ArgumentCaptor.forClass(RequestWithHeaders.class);
    doNothing().when(filterChain).doFilter(requestCaptor.capture(), eq(response));
    return requestCaptor;
  }

}
