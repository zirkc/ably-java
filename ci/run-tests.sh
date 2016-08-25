#!/usr/bin/env bash

set -ex
export TERM=dumb

gradle clean java:testRestSuite --stacktrace
gradle java:testRealtimeSuite
