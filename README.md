[![Build Status](https://www.travis-ci.org/hellowin/kanca-api.svg?branch=master)](https://www.travis-ci.org/hellowin/kanca-api)
[![codecov](https://codecov.io/gh/hellowin/kanca-api/branch/master/graph/badge.svg)](https://codecov.io/gh/hellowin/kanca-api)

# Development

## Requirement

1. Java 8
1. Docker

## Start and Stop Dependencies

This project needs some dependencies like MySQL to manage data persistence,
so we need to start its container before developing on local environment.

### Start Dependencies

1. Run `$ sbt` to open `sbt shell`.
1. Inside `sbt shell` run `startDependencies`, it will automatically start all local dependencies needed.

Or simply run `$ sbt startDependencies` (but you will exited from `sbt shell`).

### Stop Dependencies

1. Run `stopDependencies` inside `sbt shell`, it will stop and destroy any dependencies remains.
