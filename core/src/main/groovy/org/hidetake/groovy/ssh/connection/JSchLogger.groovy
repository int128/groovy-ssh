package org.hidetake.groovy.ssh.connection

import com.jcraft.jsch.Logger
import groovy.util.logging.Slf4j

/**
 * A logger which bridges JSch and SLF4J.
 * It does not redirect DEBUG log because it is too detail and verbose.
 *
 * @author Hidetake Iwata
 */
@Singleton
@Slf4j
class JSchLogger implements Logger {

    final ThreadLocal<Boolean> jschLoggerSetting = new ThreadLocal<>()

    @Override
    boolean isEnabled(int logLevel) {
        switch (logLevel) {
            case INFO:
                return log.isDebugEnabled() && jschLoggerSetting.get()
            case WARN:
                return log.isInfoEnabled()
            case ERROR:
                return log.isWarnEnabled()
            case FATAL:
                return log.isErrorEnabled()
            default:
                return false
        }
    }

    @Override
    void log(int logLevel, String message) {
        switch (logLevel) {
            case INFO:
                log.debug("[jsch] $message")
                break
            case WARN:
                log.info(message)
                break
            case ERROR:
                log.warn(message)
                break
            case FATAL:
                log.error(message)
                break
        }
    }
}
