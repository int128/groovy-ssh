package org.hidetake.groovy.ssh.test.os

import org.hidetake.groovy.ssh.core.Service

class Fixture {

    static randomInt(int max = 10000) {
        (Math.random() * max) as int
    }

    static remoteTmpPath() {
        "/tmp/groovy-ssh.os-integration-test.${UUID.randomUUID()}"
    }

    static createRemotes(Service service) {
        service.remotes {
            Default {
                host = 'localhost'
                port = System.getenv('SSH_PORT') as int
                user = System.properties['user.name']
                identity = new File("${System.properties['user.home']}/.ssh/id_ecdsa")
                knownHosts = addHostKey(new File("build/known_hosts"))
            }
        }
        service.remotes {
            DefaultWithPassphrase {
                host = service.remotes.Default.host
                port = service.remotes.Default.port
                user = service.remotes.Default.user
                knownHosts = service.remotes.Default.knownHosts

                identity = new File("${System.properties['user.home']}/.ssh/id_ecdsa_pass")
                passphrase = 'gradle'
            }
            DefaultWithOpenSSHKnownHosts {
                host = service.remotes.Default.host
                port = service.remotes.Default.port
                user = service.remotes.Default.user
                identity = service.remotes.Default.identity

                knownHosts = new File("${System.properties['user.home']}/.ssh/known_hosts")
            }
            DefaultWithAgent {
                host = service.remotes.Default.host
                port = service.remotes.Default.port
                user = service.remotes.Default.user
                knownHosts = service.remotes.Default.knownHosts

                agent = true
            }
        }
    }

}
