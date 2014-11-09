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

    private final int ACTIVITY_CHOOSE_FILE = 4711;

    //############## state ############

    private File[] fileToBeAdded = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.fileToBeAdded = getFileToBeAdded();

        if (this.fileToBeAdded == null) {
            Toast.makeText(this, getString(R.string.WARN_ADD_NO_FILES), Toast.LENGTH_LONG).show();
            this.finish();
        }

        if (isGuiEnabled()) {
            setContentView(R.layout.activity_add2zip);
            initGui();
        } else {
            addToZip();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String filePath = uri.getPath();
                }
            }
        }
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

    private void initGui() {
        final Button saveButton = (Button) this
                .findViewById(R.id.ButtonSaveTimeSlice);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                addToZip();
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

        final Button fileButton = (Button) this
                .findViewById(R.id.ButtonChooseFile);
        fileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                chooseFile();
            }
        });
    }

    //############ state ##########

    private boolean isGuiEnabled() {
        return false; // todo implement gui
    }

    private File getCurrentZipFile() {
        final File sdcard = Environment.getExternalStorageDirectory();

        return new File(sdcard.getAbsolutePath(), getString(R.string.default_zip_path));
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

    //############ processing ########

    private void addToZip() {
        File currentZipFile = getCurrentZipFile();

        if (this.fileToBeAdded != null) {
            currentZipFile.getParentFile().mkdirs();
            CompressJob job = new CompressJob(currentZipFile);
            job.add("", this.fileToBeAdded);
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

    private void addResult(ArrayList<File> result, Uri data) {
        if ((data != null) && ("file".equalsIgnoreCase(data.getScheme()))) {
            result.add(new File(data.getPath()));
        }
    }

    private void chooseFile() {
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("folder/*");
        intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
    }

}
