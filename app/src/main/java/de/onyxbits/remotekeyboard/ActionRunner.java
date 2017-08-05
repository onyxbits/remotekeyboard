package de.onyxbits.remotekeyboard;

/**
 * Wrapper for InputAction. We cannot post InputActions directly to the
 * messagequeue because we use commitText() and sendKeyEvent(). The later
 * executes asynchronously and hence fast commits (e.g. via copy&paste) result
 * in linebreaks being out of order.
 *
 * @author patrick
 */
class ActionRunner implements Runnable {

    private Runnable action;
    private boolean finished;

    protected void setAction(Runnable action) {
        this.action = action;
        this.finished = false;
    }

    public void run() {
        action.run();
        synchronized (this) {
            finished = true;
            notify();
        }
    }

    public synchronized void waitResult() {
        while (!finished) {
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }
}