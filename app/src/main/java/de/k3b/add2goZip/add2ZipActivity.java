package de.k3b.add2goZip;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import de.k3b.zip.CompressJob;

public class Add2ZipActivity extends ActionBarActivity {

    private static final String TAG = "Add2ZipActivity";

    private File[] fileToBeAdded = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.fileToBeAdded = getFileToBeAdded();
        if (this.fileToBeAdded == null) {
            Toast.makeText(this, getString(R.string.WARN_ADD_NO_FILES), Toast.LENGTH_LONG).show();
            this.finish();
        }
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

        return new File(sdcard.getAbsolutePath(), getString(R.string.default_zip_path));
    }

    private void addToZip() {
        File currentZipFile = getCurrentZipFile();

        if (fileToBeAdded != null) {
            currentZipFile.getParentFile().mkdirs();
            CompressJob job = new CompressJob(currentZipFile);
            job.add("", fileToBeAdded);
            int result = job.compress();
            if (result == CompressJob.RESULT_ERROR_ABOART) {
                Toast.makeText(this,
                        String.format(getString(R.string.ERR_ADD),
                                currentZipFile.getAbsolutePath(), job.getLastError()), Toast.LENGTH_LONG).show();
            }
            if (result == CompressJob.RESULT_NO_CHANGES) {
                Toast.makeText(this, String.format(getString(R.string.WARN_ADD_NO_CHANGES), currentZipFile.getAbsolutePath()), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, String.format(getString(R.string.SUCCESS_ADD), currentZipFile.getAbsolutePath(), job.getAddCount()), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private File[] getFileToBeAdded() {
        ArrayList<File> result = new ArrayList<File>();
        Intent intent = getIntent();

        if (Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction())) {
            ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
            if (uris != null) {
                for (Uri item : uris) {
                    addResult(result, item);
                }
            }
        } else {
            addResult(result, (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM));
        }

        addResult(result, intent.getData());

        int len = result.size();
        if (Global.debugEnabled) {
            Log.d(TAG, "getFileToBeAdded " + len + ":" + result);
        }

        if (len == 0) return null;
        return result.toArray(new File[len]);
    }

    private void addResult(ArrayList<File> result, Uri data) {
        if ((data != null) && ("file".equalsIgnoreCase(data.getScheme()))) {
            result.add(new File(data.getPath()));
        }
    }

}
