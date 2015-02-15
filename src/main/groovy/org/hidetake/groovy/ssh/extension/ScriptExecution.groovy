package org.hidetake.groovy.ssh.extension

import org.hidetake.groovy.ssh.session.SessionExtension

/**
 * An extension class to execute a script.
 *
 * @author hidetake.org
 */
trait ScriptExecution implements SessionExtension {

    /**
     * Execute the script.
     *
     * @param script script
     * @return output value of the command
     */
    String executeScript(String script) {
        withScript(script) { path ->
            execute(path)
        }
    }

    /**
     * Execute the script.
     *
     * @param script script
     * @param callback closure called with an output value of the command
     */
    void executeScript(String script, Closure callback) {
        withScript(script) { path ->
            execute(path, callback)
        }
    }

    /**
     * Execute the script.
     *
     * @param settings
     * @param script script
     * @return output value of the command
     */
    String executeScript(HashMap settings, String script) {
        withScript(script) { path ->
            execute(settings, path)
        }
    }

    /**
     * Execute the script.
     *
     * @param settings
     * @param script script
     * @param callback closure called with an output value of the command
     */
    void executeScript(HashMap settings, String script, Closure callback) {
        withScript(script) { path ->
            execute(settings, path, callback)
        }
    }

    private def withScript(String script, Closure executor) {
        def path = execute('mktemp || mktemp -t script', ignoreError: true, logging: 'none') ?: "/tmp/${UUID.randomUUID()}"
        sftp {
            putContent(new ByteArrayInputStream(), path)
            try {
                chmod(0700, path)
                putContent(new ByteArrayInputStream(script.bytes), path)
                executor.call(path)
            } finally {
                rm(path)
            }
        }
    }

}
