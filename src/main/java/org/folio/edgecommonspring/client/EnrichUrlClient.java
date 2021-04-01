package org.folio.edgecommonspring.client;

import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.beans.factory.annotation.Value;

@Log4j2
public class EnrichUrlClient extends Client.Default {

  @Value("${okapi_url}")
  private String okapiUrl;

  public EnrichUrlClient() {
    super(null, null);
  }

  @Override
  @SneakyThrows
  public Response execute(Request request, Options options) {

    FieldUtils.writeDeclaredField(request, "url", request.url().replace("http://", okapiUrl + "/"), true);

    return super.execute(request, options);
  }

}
