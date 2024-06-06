## 06/06/2024 v2.4.4 - Released
This release contains security bug fixes.

* [EDGCMNSPR-53](https://folio-org.atlassian.net/browse/EDGCMNSPR-53) Spring Boot 3.2.6, bcprov-jdk18on 1.78 fixing vulns

## 05/20/2024 v2.4.3 - Released
This release contains adjustments for handling of the deprecated okapi\_url in the EnrichUrlClient.

## 03/14/2024 v2.4.2 - Released
This release contains TLS support.

### Bug Fixes
* [EDGCMNSPR-51](https://folio-org.atlassian.net/browse/EDGCMNSPR-51) - Update EnrichUrlClient to use new property and add TLS

## 03/14/2024 v2.4.1 - Released
This release fixes a bug.

### Bug Fixes
* [EDGCMNSPR-49](https://folio-org.atlassian.net/browse/EDGCMNSPR-49) - edge-api-utils 1.4.1 fixing vault and circular dependency issues

## 03/13/2024 v2.4.0 - Released
This release upgrades dependencies for Quesnelia.

### Tasks
* [EDGCMNSPR-47](https://folio-org.atlassian.net/browse/EDGCMNSPR-47) - Spring Boot 3.2, folio-spring 8.1, edge-api-utils 1.4

## 11/10/2023 v2.3.0 - Released
This release includes dependencies upgrade, implementation of refresh token rotation.

### Tasks
* [EDGCMNSPR-40](https://issues.folio.org/browse/EDGCMNSPR-40) - Upgrade Folio spring base and Folio spring system user dependency version
* [EDGCMNSPR-38](https://issues.folio.org/browse/EDGCMNSPR-38) - Upgrade dependencies for Poppy (Spring Boot 3.1)
* [EDGCMNSPR-35](https://issues.folio.org/browse/EDGCMNSPR-35) - Migrate to folio-spring-support v7.0.0

### Stories
* [EDGCMNSPR-39](https://issues.folio.org/browse/EDGCMNSPR-39) - Implement Expired token from cache of edge-common-spring
* [EDGCMNSPR-29](https://issues.folio.org/browse/EDGCMNSPR-29) - Implement refresh token rotation

### Bug Fixes
* [EDGCMNSPR-34](https://issues.folio.org/browse/EDGCMNSPR-34) - Spring Boot 3.0.5

## 22/02/2023 v2.2.0 - Released
This release includes Spring boot upgrade and logging improvements configuration.

### Tasks
* [EDGCMNSPR-31](https://issues.folio.org/browse/EDGCMNSPR-31) - Update the module to Spring boot v3.0.0 and identify issues.
* [EDGCMNSPR-30](https://issues.folio.org/browse/EDGCMNSPR-30) - Update to Java 17.
* [EDGCMNSPR-28](https://issues.folio.org/browse/EDGCMNSPR-28) - Logging improvement - Configuration

## 17/10/2022 v2.1.0 - Released
This release includes version upgrade of spring and folio-spring-base library.

### Tasks
* [EDGCMNSPR-26](https://issues.folio.org/browse/EDGCMNSPR-26) - edge-common-spring: spring upgrade

## 04/08/2022 v2.0.2 - Released
This release includes update of headers processing.

### Stories
* [EDGCMNSPR-22](https://issues.folio.org/browse/EDGCMNSPR-22) - Updating of headers processing

## 05/07/2022 v2.0.1 - Released
This release includes minor technical changes and dependencies updating.

### Stories
* [EDGCMNSPR-21](https://issues.folio.org/browse/EDGCMNSPR-21) - edge-api-utils dependency updating to 1.1.2
* [EDGCMNSPR-18](https://issues.folio.org/browse/EDGCMNSPR-18) - Publish javadoc and sources to maven repository
* [EDGCMNSPR-16](https://issues.folio.org/browse/EDGCMNSPR-16) - edge-common-spring Spring upgrade Morning Glory 2022 R2
* [EDGCMNSPR-11](https://issues.folio.org/browse/EDGCMNSPR-11) - edge-common-spring: folio-spring-base update

## 08/04/2022 v2.0.0 - Released
* [EDGCMNSPR-14](https://issues.folio.org/browse/EDGCMNSPR-14) Upgrade folio-spring-base to 4.0.0

## 22/02/2022 v1.2.0 - Released
* [EDGCMNSPR-9](https://issues.folio.org/browse/EDGCMNSPR-9) Upgrade to the folio spring base v.3.0.1

## 05/01/2022 v1.1.1 - Released
 Upgrade log4j version

## 29/09/2021 v1.1.0 - Released
The primary focus of this release was to update edge-api-util version and handle api key parameters

### Stories
* [EDGCMNSPR-6](https://issues.folio.org/browse/EDGCMNSPR-6) - Incorrect api key param name
* [EDGCMNSPR-5](https://issues.folio.org/browse/EDGCMNSPR-5) - Allow log level to be specified as system property or program argument for edge modules

### Bug Fixes
* [EDGCMNSPR-7](https://issues.folio.org/browse/EDGCMNSPR-7) - Fix wrong throwing of Authorization exception

## 27/05/2021 v1.0.0 - Released
Initial release of `edge-common-spring` library

### Stories
* [EDGCMNSPR-3](https://issues.folio.org/browse/EDGCMNSPR-3) - Exclude swagger endpoints from security filter
* [EDGCMNSPR-2](https://issues.folio.org/browse/EDGCMNSPR-2) - Port edge-common to spring-way
* [EDGCMNSPR-1](https://issues.folio.org/browse/EDGCMNSPR-1) - Create Project Skeleton
