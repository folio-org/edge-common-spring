package org.folio.edgecommonspring.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"org.folio.edgecommonspring.client", "org.folio.edgecommonspring.security",
  "org.folio.edgecommonspring.domain.entity", "org.folio.edgecommonspring.util", "org.folio.edgecommonspring.filter"})
public class EdgeSharedConfiguration {

}
