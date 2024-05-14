package org.folio.edgecommonspring.client;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Value;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import static org.apache.commons.lang3.StringUtils.isBlank;

@Log4j2
public class EnrichUrlClient extends Client.Default {
  private final EdgeFeignClientProperties properties;

  @Deprecated
  @Value("${okapi_url:NO_VALUE}")
  private String okapiUrl; // okapi_url is deprecated. Please use folio.client.okapiUrl instead

  public EnrichUrlClient(EdgeFeignClientProperties properties,  SSLSocketFactory sslContextFactory, HostnameVerifier hostnameVerifier) {
    super(sslContextFactory, hostnameVerifier);
    this.properties = properties;
  }

  @Override
  @SneakyThrows
  public Response execute(Request request, Options options) {
    String okapiUrlToUse = properties.getOkapiUrl();
    if (isBlank(okapiUrlToUse)) {
      log.warn("deprecated property okapi_url is used. Please use folio.client.okapiUrl instead.");
      okapiUrlToUse = okapiUrl;
    }
    FieldUtils.writeDeclaredField(request, "url", request.url().replace("http://", okapiUrlToUse + "/"), true);

    return super.execute(request, options);
  }

}
