#!/bin/bash
set -e
keyid="${1}"
keyopt_dpkg=""
keyopt_gpg=""

if [ -n "${keyid}" ]; then
    keyopt_dpkg="-k ${keyid}"
    keyopt_gpg="--default-key ${keyid}"
fi
cd "$(dirname "${0}")"

cd repo
for deb in ./*.deb; do
    dpkg-sig -g "--pinentry-mode loopback" ${keyopt_dpkg} --sign builder "${deb}"
done
apt-ftparchive packages . > Packages

cd ..
apt-ftparchive release repo > repo/Release
gpg \
    --pinentry-mode loopback \
    ${keyopt_gpg} \
    --armor \
    --sign \
    --detach-sign \
    --output repo/Release.gpg \
    repo/Release