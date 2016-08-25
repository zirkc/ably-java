#!/usr/bin/env bash

set -ex
export TERM=dumb

gradle java:testRestSuite --info
gradle java:testRealtimeSuite
