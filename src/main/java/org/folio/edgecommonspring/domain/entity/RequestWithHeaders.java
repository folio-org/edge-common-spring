package org.folio.edgecommonspring.domain.entity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public final class RequestWithHeaders extends HttpServletRequestWrapper {

  private final Map<String, String> headers;

  public RequestWithHeaders(HttpServletRequest request) {
    super(request);
    headers = new HashMap<>();
  }

  public void putHeader(String name, String value) {
    headers.put(name, value);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    return Collections.enumeration(Collections.singletonList(headers.get(name)));
  }

  @Override
  public String getHeader(String name) {
    return this.headers.get(name);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    return Collections.enumeration(new HashSet<>(headers.keySet()));
  }

}
