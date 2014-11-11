package de.k3b.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class EditTextPreferenceWithSummary extends EditTextPreference {
    private final static String TAG = EditTextPreferenceWithSummary.class
            .getName();

    public EditTextPreferenceWithSummary(final Context context,
                                         final AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    public EditTextPreferenceWithSummary(final Context context) {
        super(context);
        this.init();
    }

    private void init() {
        Log.e(EditTextPreferenceWithSummary.TAG, "init");
        final SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this.getContext());
        final String currentText = prefs.getString("test", this.getText());

        this.setSummary(currentText);

        this.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(final Preference preference,
                                              final Object newValue) {
                Log.w(EditTextPreferenceWithSummary.TAG,
                        "display score changed to " + newValue);
                preference.setSummary(newValue.toString()); // getSummary());
                return true;
            }
        });
    }

    @Override
    public CharSequence getSummary() {
        return super.getSummary();
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        this.setSummary(this.getText());
        return super.onCreateView(parent);
    }
}
