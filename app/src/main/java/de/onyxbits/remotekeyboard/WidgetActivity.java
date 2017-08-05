package de.onyxbits.remotekeyboard;

import android.app.Activity;
import android.content.Context;
import android.view.inputmethod.InputMethodManager;

public class WidgetActivity extends Activity {

    public WidgetActivity() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        final InputMethodManager service = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        service.showInputMethodPicker();
        finish();
    }

}
