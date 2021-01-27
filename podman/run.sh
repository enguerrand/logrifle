#!/bin/bash
set -e

function ask(){
    local _confirm="initial"
    local _prefix=""
    local _suffix=" proceed? [yN]: "
    while ! [ -z "$_confirm" ] && ! [[ "$_confirm" =~ ^[YyNn]$ ]]; do
        read -p "${_prefix}${@}${_suffix}" _confirm
        _prefix="Invalid input! "
        [[ "$_confirm" =~ [yY] ]] && return
    done
    exit 0
}

cd "$(dirname "$0")"
source ./config.inc.sh
rm -rf ./repo/*
deb=$(ls ../target/logrifle*.deb | tail -n 1)
ask "Will publish ${deb}..."
cp ${deb} ./repo/
podman run -v .:/mnt/ -v ${HOME}/.gnupg/private-keys-v1.d/:/root/.gnupg/private-keys-v1.d/ -v ${HOME}/.gnupg/pubring.kbx:/root/.gnupg/pubring.kbx -it --rm debian:pkg /mnt/update-repo.sh ${keyid}
echo "New repo content:"
ls -halt ./repo/
ask "Will now sync to public repo..."
rsync -av --delete ./repo/ ${host}:${webroot}