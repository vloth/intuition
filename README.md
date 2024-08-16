# Intuition

This is a CLI script to pull data from various sources and upsert them into a 
local database. Built with Clojurescript, runs on Nodejs and saves data to a 
Duckdb instance.  

Supported sources:
* [X] Jenkins builds
* [X] Jira issues 
* [X] Git commits
* [X] Git tags
* [x] Bitbucket pull requests
* [ ] Github issues

## Getting started

Required dependencies:
* [Duckdb v1.0.0 1f98600c2c](https://duckdb.org/)
* [Nodejs v22.6.0](https://nodejs.org/en)
* [babashka v1.3.191](https://babashka.org/)

```bash
# Set up environment variables
$ cp .env.sample .env

# Start nrepl
$ npm run nrepl

# Or run tests
$ npm test
```
