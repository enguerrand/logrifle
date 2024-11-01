FROM docker.io/library/debian
RUN apt update && \
    apt -y install --no-install-recommends dpkg-dev apt-utils gpg gpg-agent && \
    mkdir -p --mode=0700 /root/.gnupg
