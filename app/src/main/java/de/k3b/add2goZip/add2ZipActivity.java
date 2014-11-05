package de.k3b.add2goZip;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class Add2ZipActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add2zip);

        final Button saveButton = (Button) this
                .findViewById(R.id.ButtonSaveTimeSlice);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
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
}
