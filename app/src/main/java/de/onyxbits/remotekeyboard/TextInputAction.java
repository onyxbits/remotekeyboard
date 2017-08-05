package de.onyxbits.remotekeyboard;

import android.view.inputmethod.InputConnection;

/**
 * Commit text into the current editor.
 *
 * @author patrick
 */
class TextInputAction implements Runnable {

    protected String text;

    private RemoteKeyboardService myService;

    public TextInputAction(RemoteKeyboardService myservice) {
        this.myService = myservice;
    }

    @Override
    public void run() {
        InputConnection con = myService.getCurrentInputConnection();
        if (con != null) {
            con.commitText(text, text.length());
        }
    }

}
