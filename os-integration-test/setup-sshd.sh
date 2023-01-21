#!/bin/bash
set -eux

sudo systemctl status sshd

install_host_key () {
  sudo cp "os-integration-test/etc/ssh/$1" "/etc/ssh/$1"
  sudo chmod 600 "/etc/ssh/$1"
}

install_host_key ssh_host_ecdsa_key
install_host_key ssh_host_ed25519_key

mkdir -p ~/.ssh
cat os-integration-test/etc/ssh/*.pub > ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

sudo systemctl restart sshd
