package de.k3b.add2goZip;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.net.URI;

import de.k3b.zip.CompressJob;

public class Add2ZipActivity extends ActionBarActivity {

    private static final String TAG = "Add2ZipActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add2zip);
        init();
    }

    private void init() {
        final Button saveButton = (Button) this
                .findViewById(R.id.ButtonSaveTimeSlice);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                addToZip();
                /*
                TimeSliceEditActivity.this.timeSlice
                        .setNotes(TimeSliceEditActivity.this.notesEditText
                                .getText().toString());
                if (TimeSliceEditActivity.this.validate()) {
                    final Intent intent = new Intent();
                    intent.putExtra(Global.EXTRA_TIMESLICE,
                            TimeSliceEditActivity.this.timeSlice);
                    TimeSliceEditActivity.this.setResult(Activity.RESULT_OK,
                            intent);
                }
                */
                finish();
            }
        });


        final Button cancelButton = (Button) this
                .findViewById(R.id.ButtonCancelTimeSlice);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.add2_go_zip, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private File getCurrentZipFile() {
        final File sdcard = Environment.getExternalStorageDirectory();
        String path = sdcard.getAbsolutePath() + File.separator + "copy"
                + File.separator + "2go.zip";

        return new File(path);
    }

    private void addToZip() {
        File fileToBeAdded = getFileToBeAdded();
        if (fileToBeAdded != null) {
            File currentZipFile = getCurrentZipFile();
            currentZipFile.getParentFile().mkdirs();
            CompressJob sut = new CompressJob(currentZipFile);
            sut.add("", fileToBeAdded.getAbsolutePath());
            sut.compress();
            Toast.makeText(this,"added " + fileToBeAdded, Toast.LENGTH_SHORT).show();
        }
    }

    private File getFileToBeAdded() {
        Intent intent = getIntent();

        Uri data = intent.getData();

        if (Global.debugEnabled) {
            Log.d(TAG, "getFileToBeAdded " + data);
        }

        if ((data != null)  && ("file".equalsIgnoreCase(data.getScheme()))) {
            return new File(data.getPath());
        }
        return null;
    }

}
