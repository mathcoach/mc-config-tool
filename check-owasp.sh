#! /bin/bash
# check OWASP security issues
# keep version of org.owasp:dependency-check-maven:12.0.0
# up-to-date
mvn org.owasp:dependency-check-maven:12.0.0:check
mvn org.owasp:dependency-check-maven:12.0.0:aggregate

