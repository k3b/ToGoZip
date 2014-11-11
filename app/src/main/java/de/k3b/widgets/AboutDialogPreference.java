package de.k3b.widgets;

import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import de.k3b.add2GoZip.R;
import de.k3b.android.GuiUtil;

/**
 * html/Webview about preference entry showing
 * R.string.about_content with placeholders
 * $versionName$ and
 * $about$ (R.string.about_content_about)<br/>
 * Created by k3b on 11.11.2014.
 */
public class AboutDialogPreference extends DialogPreference {
    private Context context;

    public AboutDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogIcon(R.drawable.ic_launcher);
        setDialogTitle(R.string.about_summary);
        setDialogLayoutResource(R.layout.about_dialog);
        this.context = context;

    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        WebView wv = (WebView) view.findViewById(R.id.content);

        String html = this.context.getResources().getString(R.string.about_content); // "<html><body>some <b>html</b> here</body></html>";

        final String versionName = GuiUtil.getAppVersionName(context);
        if (versionName != null) {
            html = html.replace("$versionName$", versionName);
        }

        html = html.replace("$about$",
                this.context.getText(R.string.about_content_about));

        wv.loadData(html, "text/html", "UTF-8");
        wv.setVerticalScrollBarEnabled(true);

        final WebSettings mWebSettings = wv.getSettings();
        mWebSettings.setBuiltInZoomControls(true);
        wv.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_OVERLAY);
        wv.setScrollbarFadingEnabled(false);
    }
}
