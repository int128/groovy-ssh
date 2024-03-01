#!/bin/bash
# Generate key pairs and known_hosts.
# For GitHub Codespaces, set SSH_PORT=2222

set -ex
mkdir -m 700 -vp ~/.ssh

# user keys
ssh-keygen -t ecdsa -N '' -C '' -f ~/.ssh/id_ecdsa
ssh-keygen -t ecdsa -N gradle -C '' -f ~/.ssh/id_ecdsa_pass

# authorized_keys
cat ~/.ssh/id_*.pub > ~/.ssh/authorized_keys

# known_hosts
ssh -v \
  -o StrictHostKeyChecking=accept-new \
  -o HostKeyAlgorithms=ecdsa-sha2-nistp256 \
  -p "${SSH_PORT:-22}" \
  localhost true
