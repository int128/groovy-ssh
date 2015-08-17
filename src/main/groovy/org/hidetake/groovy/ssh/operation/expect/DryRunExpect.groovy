package org.hidetake.groovy.ssh.operation.expect

import groovy.util.logging.Slf4j

/**
 * Dry-run implementation of {@link Expect}.
 *
 * @author Romano Zabini
 */
@Slf4j
class DryRunExpect {

    def expectOrThrow(int timeout, Object... patterns){
        log.info("waiting {} seconds for: {}", timeout, patterns)
    }

    def send(String command){
        log.info("sending command: {}", command)
    }
}
