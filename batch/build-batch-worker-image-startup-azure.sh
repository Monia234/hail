#!/bin/bash

set -ex

apt-get update

apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    jq \
    lsb-release \
    software-properties-common

# Install Docker

curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -

add-apt-repository \
   "deb [arch=amd64] https://download.docker.com/linux/ubuntu \
   $(lsb_release -cs) \
   stable"

apt-get install -y docker-ce

rm -rf /var/lib/apt/lists/*

[ -f /etc/docker/daemon.json ] || echo "{}" > /etc/docker/daemon.json

# Install Azure CLI
curl -sL https://packages.microsoft.com/keys/microsoft.asc |
    gpg --dearmor |
    tee /etc/apt/trusted.gpg.d/microsoft.gpg > /dev/null

AZ_REPO=$(lsb_release -cs)
echo "deb [arch=amd64] https://packages.microsoft.com/repos/azure-cli/ $AZ_REPO main" |
    tee /etc/apt/sources.list.d/azure-cli.list

apt-get update
apt-get install azure-cli

az login --identity
az acr login --name {{ global.container_registry_name }}

# avoid "unable to get current user home directory: os/user lookup failed"
export HOME=/root

docker pull {{ global.docker_root_image }}

# add docker daemon debug logging
jq '.debug = true' /etc/docker/daemon.json > daemon.json.tmp
mv daemon.json.tmp /etc/docker/daemon.json