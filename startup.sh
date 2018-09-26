#!/usr/bin/env bash
docker pull snoopy2k18/a2si-facade
docker run -p 8189:8188/tcp snoopy2k18/a2si-facade &
