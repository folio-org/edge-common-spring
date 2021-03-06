package org.folio.edgecommonspring.client;

import static org.folio.spring.integration.XOkapiHeaders.TENANT;

import org.folio.edgecommonspring.domain.entity.ConnectionSystemParameters;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient("authn")
public interface AuthnClient {

  @PostMapping(value = "/login", consumes = MediaType.APPLICATION_JSON_VALUE)
  ResponseEntity<String> getApiKey(@RequestBody ConnectionSystemParameters connectionSystemParameters, @RequestHeader(TENANT) String tenantId);

}
