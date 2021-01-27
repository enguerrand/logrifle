FROM docker.io/library/debian
RUN apt update && \
    apt -y install --no-install-recommends dpkg-dev dpkg-sig apt-utils gpg && \
    mkdir -p --mode=0700 /root/.gnupg
