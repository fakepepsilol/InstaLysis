#!/usr/bin/env bash
cd api
./gradlew publishToMavenLocal
cd ../service
./gradlew publishToMavenLocal
