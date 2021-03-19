package org.folio.edgecommonspring.client;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;
import feign.okhttp.OkHttpClient;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Value;

@Log4j2
public class EnrichUrlClient implements Client {

  private final OkHttpClient delegate;
  @Value("${okapi_url}")
  private String okapiUrl;

  public EnrichUrlClient(okhttp3.OkHttpClient okHttpClient) {
    this.delegate = new OkHttpClient(okHttpClient);
  }

  @Override
  @SneakyThrows
  public Response execute(Request request, Options options) {

    FieldUtils.writeDeclaredField(request, "url", request.url().replace("http://", okapiUrl + "/"), true);

    return delegate.execute(request, options);
  }

}
