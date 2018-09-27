#!/usr/bin/env bash
docker pull snoopy2k18/a2si-facade
docker run -p 8189:8188/tcp -e fhir.restserver.serverBase='http4://data.developer.nhs.uk/ccri-fhir/STU3?throwExceptionOnFailure=false&bridgeEndpoint=true' snoopy2k18/a2si-facade &
