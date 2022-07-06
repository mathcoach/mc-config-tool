#! /bin/bash
# check OWASP security issues
# keep version of org.owasp:dependency-check-maven:7.1.1
# up-to-date
mvn org.owasp:dependency-check-maven:7.1.1:check
mvn org.owasp:dependency-check-maven:7.1.1:aggregate
