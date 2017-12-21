/*
 from https://github.com/kashifo/android-folder-picker-library

 Copyright 2017 Kashif Anwaar.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

 */
package lib.folderpicker;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.k3b.android.toGoZip.R;

public class simpleadapter extends ArrayAdapter<String> {

	Activity context;
	ArrayList<String> namesList;
	ArrayList<String> typesList;

	public simpleadapter(Activity context, ArrayList<String> namesList, ArrayList<String> typesList) {
		super(context, R.layout.filerow, namesList);

		this.context = context;
		this.namesList = namesList;
		this.typesList = typesList;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		convertView = inflater.inflate(R.layout.filerow, parent, false);

		ImageView imageView = (ImageView) convertView.findViewById(R.id.fp_iv_icon);
		TextView name = (TextView) convertView.findViewById(R.id.fp_tv_name);
		
		if( typesList.get(position).equals("folder") )
		{
			imageView.setImageResource( R.drawable.folder );
		}
		else
		{
			imageView.setImageResource( R.drawable.file );
		}

		name.setText( namesList.get(position) );

		return convertView;
	}

}