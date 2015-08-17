package org.hidetake.groovy.ssh.expect

interface CommandExecutor {
    String processCommand(String command)
}