package de.onyxbits.remotekeyboard;

import net.wimpi.telnetd.net.Connection;
import net.wimpi.telnetd.net.ConnectionEvent;
import net.wimpi.telnetd.shell.Shell;

/**
 * Workaround class for something being broken within the telnetd lib: limiting
 * maxcon to 1 may arbitrarily result in the telnet server getting stuck upon
 * disconnecting and not accepting any more connections till the service restarts.
 * This looks like some kind of a race condition on improperly closed sockets. I
 * can't pinpoint the problem, but having a backlog >1 seems to remedy the
 * situation.
 * <p>
 * Since TelnetEditorShell is designed to be a singleton, a shell that
 * immediately finishes after starting is the easiest way to deal with multiple
 * connections if there is already a valid one.
 *
 * @author patrick
 */
public class DummyShell implements Shell {

    public DummyShell() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public void connectionIdle(ConnectionEvent ce) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connectionTimedOut(ConnectionEvent ce) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connectionLogoutRequest(ConnectionEvent ce) {
        // TODO Auto-generated method stub

    }

    @Override
    public void connectionSentBreak(ConnectionEvent ce) {
        // TODO Auto-generated method stub

    }

    @Override
    public void run(Connection con) {
        // TODO Auto-generated method stub

    }

}
