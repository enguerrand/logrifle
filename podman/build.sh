#!/bin/bash
cd "$(dirname "$0")"
podman build --tag debian:pkg .
