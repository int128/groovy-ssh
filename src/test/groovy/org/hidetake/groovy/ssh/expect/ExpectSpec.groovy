package org.hidetake.groovy.ssh.expect

import org.apache.sshd.SshServer
import org.apache.sshd.common.Factory
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.hidetake.groovy.ssh.operation.expect.Expect
import org.hidetake.groovy.ssh.server.ServerIntegrationTest
import org.hidetake.groovy.ssh.server.SshServerMock
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

@org.junit.experimental.categories.Category(ServerIntegrationTest)
class ExpectSpec extends Specification {

    SshServer server
    CommandExecutor commandExecutor=Mock(CommandExecutor)

    Service ssh
    def PROMPT='$'

    @Rule
    TemporaryFolder temporaryFolder


    def setup() {
        startServer()

        ssh = Ssh.newService()
        ssh.settings {
            knownHosts = allowAnyHosts
        }
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
            }

        }
    }

    private startServer() {
        server = SshServerMock.setUpLocalhostServer()
        server.with {
            commandFactory = Mock(CommandFactory)
            shellFactory = Mock(Factory) {
                create() >> new StubShell(commandExecutor, PROMPT)
            }
            passwordAuthenticator = Mock(PasswordAuthenticator) {
                (0.._) * authenticate('someuser', 'somepassword', _) >> true
            }
            start()
        }
    }

    def cleanup() {
        server.stop(true)
    }

    def "can expect for prompt"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shellExpect {
                    expectOrThrow 1, PROMPT
                }
            }
        }

        then:
        notThrown(Exception)

    }

    Closure sayHello = { String param ->
        shellExpect {
            send "hello $param"
            expectOrThrow 3,'please enter password:'
        }
    }

    def "can execute a simple extension"() {
        when:
            ssh.settings {
                extensions.add hello: sayHello
            }
            ssh.run {
                session(ssh.remotes.testServer) {
                    hello "server"
                }
            }
        then:

        1 * commandExecutor.processCommand("hello server") >> 'please enter password:'
        notThrown Exception
    }

    def "can send a command and expect a result"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shellExpect {
                    send 'hello server'
                    expectOrThrow 1,'please enter password:'
                    send 'Welcome1'
                    expectOrThrow 1, 'password OK'
                }
            }
        }

        then:
        1 * commandExecutor.processCommand("hello server") >> 'please enter password:'
        1 * commandExecutor.processCommand("Welcome1") >> 'password OK'
        notThrown Exception
    }

    def "throws exception when expected result is not found"() {
        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                shellExpect {
                    send 'hello server'
                    expectOrThrow 1,'please enter password:'
                    send 'Welcome2'
                    expectOrThrow 1, 'password OK'
                }
            }
        }

        then:
        1 * commandExecutor.processCommand("hello server") >> 'please enter password:'
        commandExecutor.processCommand("Welcome1") >> 'password OK'
        thrown Expect.TimeoutException
    }



}
