# edge-common-spring

Copyright (C) 2021 The Open Library Foundation

This software is distributed under the terms of the Apache License, Version 2.0. See the file "LICENSE" for more information.

## Introduction

This is a shared library/framework for edge APIs for spring way modules.
This module is a port of [edge-common](https://github.com/folio-org/edge-common), and main goal is feature parity with that framework. 
From a hosting/SysOps perspective, edge modules developed with this framework should be indistinguishable from those developed using edge-common.

## Overview
The intent of edge-common-spring is to simplify the implementation of edge APIs by providing much of the boilerplate code shared among these APIs.

### How to use this framework
All spring way edge modules, that will use this library, should add it as a dependency. 
After that, all requests that will call the edge module will go through the request filter from the library. 
As a result, outgoing request will contain ***x-okapi-token*** in the header, and the edge module will be able to invoke folio modules.

By default, filter will make authorization and receive the ***x-okapi-token*** header only if the requested URL does not start with: /admin/health, admin/info, /swagger-resources, /v2/api-docs, /swagger-ui, /_/tenant.
The array of endpoints that need to be excluded can be overriden, for example:
 -D***header.edge.validation.exclude***="/firstRequiredEndpointToExclude, /secondRequiredEndpointToExclude".

### Issue tracker

See project [EDGCMNSPR](https://issues.folio.org/browse/EDGCMNSPR)
at the [FOLIO issue tracker](https://dev.folio.org/guidelines/issue-tracker).

### Other documentation

Other [modules](https://dev.folio.org/source-code/#server-side) are described,
with further FOLIO Developer documentation at [dev.folio.org](https://dev.folio.org/)
