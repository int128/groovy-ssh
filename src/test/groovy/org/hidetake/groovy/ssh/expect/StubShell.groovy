package org.hidetake.groovy.ssh.expect

import org.apache.sshd.server.Command
import org.apache.sshd.server.Environment
import org.apache.sshd.server.ExitCallback

public class StubShell implements Command, Runnable {

    private InputStream inp;
    private OutputStream out;
    private OutputStream err;
    private ExitCallback callback;
    private Environment environment;
    private Thread thread;

    CommandExecutor commandExecutor
    String prompt

    StubShell(CommandExecutor commandExecutor, String prompt){
        this.commandExecutor=commandExecutor
        this.prompt=prompt
    }

    public InputStream getIn() {
        return inp;
    }

    public OutputStream getOut() {
        return out;
    }

    public OutputStream getErr() {
        return err;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setInputStream(InputStream inp) {
        this.inp = inp;
    }

    public void setOutputStream(OutputStream out) {
        this.out = out;
    }

    public void setErrorStream(OutputStream err) {
        this.err = err;
    }

    public void setExitCallback(ExitCallback callback) {
        this.callback = callback;
    }

    public void start(Environment env) throws IOException {
        environment = env;
        thread = new Thread(this, "StubShell");
        thread.start();
    }

    public void destroy() {
        thread.interrupt();
    }

    public void run() {
        BufferedReader r = new BufferedReader(new InputStreamReader(inp));
        out.write(prompt.bytes)
        out.flush()
        try {
            for (;;) {
                String s = r.readLine();
                if (s == null) {
                    return;
                }
                if ("exit".equals(s)) {
                    return;
                }
                out.write((commandExecutor.processCommand(s) + "\n").getBytes());
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            callback.onExit(0);
        }
    }




}

