package org.folio.edgecommonspring.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.client.RestClient;

/**
 * Conditionally import the feign config. This class exists to combine the config condition with the component
 * scan from EdgeCommonSpringComponentScanConfiguration and the client bean from FeignClientConfiguration,
 * since these can no longer be combined into a single configuration class.
 */
@Configuration
@ConditionalOnMissingBean(value = RestClient.Builder.class)
@Import({EdgeServiceClientConfiguration.class, EdgeCommonSpringComponentScanConfiguration.class})
public class ConditionalEdgeCommonConfiguration {
}
