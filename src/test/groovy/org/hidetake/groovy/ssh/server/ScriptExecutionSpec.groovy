package org.hidetake.groovy.ssh.server

import org.apache.sshd.SshServer
import org.apache.sshd.server.CommandFactory
import org.apache.sshd.server.PasswordAuthenticator
import org.apache.sshd.server.sftp.SftpSubsystem
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.hidetake.groovy.ssh.server.SshServerMock.commandWithExit

class ScriptExecutionSpec extends Specification {

    SshServer server

    Service ssh

    @Rule
    TemporaryFolder temporaryFolder

    File temporaryFile

    def setup() {
        server = SshServerMock.setUpLocalhostServer()
        server.subsystemFactories = [new SftpSubsystem.Factory()]
        server.passwordAuthenticator = Mock(PasswordAuthenticator) {
            (1.._) * authenticate('someuser', 'somepassword', _) >> true
        }

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

        temporaryFile = temporaryFolder.newFile()
    }

    def cleanup() {
        server.stop(true)
    }


    def "executeScript should put and execute a script"() {
        given:
        server.commandFactory = Mock(CommandFactory)
        server.start()

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                executeScript '''#!/bin/sh
date
'''
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String command ->
            assert command.startsWith('mktemp')
            commandWithExit(0, temporaryFile.path)
        }

        then:
        1 * server.commandFactory.createCommand(temporaryFile.path) >> {
            assert temporaryFile.text == '''#!/bin/sh
date
'''
            commandWithExit(0, 'something result')
        }

        then:
        result == 'something result'
        !temporaryFile.exists()
    }

    def "executeScript should work with settings"() {
        given:
        server.commandFactory = Mock(CommandFactory)
        server.start()

        when:
        def result = ssh.run {
            session(ssh.remotes.testServer) {
                executeScript 'something', ignoreError: true
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String command ->
            assert command.startsWith('mktemp')
            commandWithExit(0, temporaryFile.path)
        }

        then:
        1 * server.commandFactory.createCommand(temporaryFile.path) >> {
            assert temporaryFile.text == 'something'
            commandWithExit(1, 'something result')
        }

        then:
        result == 'something result'
        !temporaryFile.exists()
    }

    def "executeScript should work with callback"() {
        given:
        server.commandFactory = Mock(CommandFactory)
        server.start()

        def result

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeScript('something') { result = it }
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String command ->
            assert command.startsWith('mktemp')
            commandWithExit(0, temporaryFile.path)
        }

        then:
        1 * server.commandFactory.createCommand(temporaryFile.path) >> {
            assert temporaryFile.text == 'something'
            commandWithExit(0, 'something result')
        }

        then:
        result == 'something result'
        !temporaryFile.exists()
    }

    def "executeScript should work with callback and settings"() {
        given:
        server.commandFactory = Mock(CommandFactory)
        server.start()

        def result

        when:
        ssh.run {
            session(ssh.remotes.testServer) {
                executeScript('something', ignoreError: true) { result = it }
            }
        }

        then:
        1 * server.commandFactory.createCommand(_) >> { String command ->
            assert command.startsWith('mktemp')
            commandWithExit(0, temporaryFile.path)
        }

        then:
        1 * server.commandFactory.createCommand(temporaryFile.path) >> {
            assert temporaryFile.text == 'something'
            commandWithExit(1, 'something result')
        }

        then:
        result == 'something result'
        !temporaryFile.exists()
    }

}
