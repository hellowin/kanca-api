[![Build Status](https://www.travis-ci.org/hellowin/kanca-api.svg?branch=master)](https://www.travis-ci.org/hellowin/kanca-api)
[![codecov](https://codecov.io/gh/hellowin/kanca-api/branch/master/graph/badge.svg)](https://codecov.io/gh/hellowin/kanca-api)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/a213c1b1dac049a18f0e089649606c0d)](https://www.codacy.com/app/hellowin/kanca-api?utm_source=github.com&utm_medium=referral&utm_content=hellowin/kanca-api&utm_campaign=badger)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/9e20f10f09ba4c21a73b17d1e7fccbe3)](https://www.codacy.com/app/hellowin/kanca-api?utm_source=github.com&utm_medium=referral&utm_content=hellowin/kanca-api&utm_campaign=Badge_Coverage)

# Development

## Requirement

1. Java 8
1. Docker

## Setup Configuration

1. Copy `.env.template` to `.env` on project root.
1. Setup necessary config for testing like FB Token, DB config, etc. Some of it needed for integration tests.

## Start and Stop Dependencies

This project needs some dependencies like MySQL to manage data persistence,
so we need to start its container before developing on local environment. 

### Start Dependencies

1. Run `$ sbt` to open `sbt shell`.
1. Inside `sbt shell` run `startDependencies`, it will automatically start all local dependencies needed.

Or simply run `$ sbt startDependencies` (but you will exited from `sbt shell`).

### Stop Dependencies

1. Run `stopDependencies` inside `sbt shell`, it will stop and destroy any dependencies remains.

### Notes

1. Error starting MySQL with code `Error starting userland proxy: ... input/output error.` see [this reference](https://github.com/docker/for-win/issues/573). TLDR disable `experiment` feature on Windows.
