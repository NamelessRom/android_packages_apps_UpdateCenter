/*
 * Copyright (C) 2014 Alexander "Evisceration" Martinz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses
 */

package org.namelessrom.updatecenter.utils.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.namelessrom.updatecenter.R;
import org.namelessrom.updatecenter.utils.items.UpdateListItem;

import java.util.List;

/**
 * Created by alex on 06.01.14.
 */
public class UpdateListAdapter extends ArrayAdapter<UpdateListItem> {

    private final Activity mContext;
    private final List<UpdateListItem> mUpdateListItems;

    public UpdateListAdapter(Activity context, List<UpdateListItem> updateListItems) {
        super(context, R.layout.list_item_updates, updateListItems);
        mContext = context;
        mUpdateListItems = updateListItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = mContext.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.list_item_updates, null, true);

        TextView txtUpdateChannel =
                (TextView) rowView.findViewById(R.id.list_item_updates_channel);
        TextView txtUpdateName = (TextView) rowView.findViewById(R.id.list_item_updates_title);
        TextView txtUpdateInfo = (TextView) rowView.findViewById(R.id.list_item_updates_info);

        txtUpdateChannel.setText(mUpdateListItems.get(position).getUpdateChannel());
        txtUpdateName.setText(mUpdateListItems.get(position).getUpdateName());
        txtUpdateInfo.setText(mUpdateListItems.get(position).getUpdateInfo());

        if (txtUpdateInfo.getText().toString().isEmpty()) {
            txtUpdateInfo.setVisibility(View.GONE);
        }

        return rowView;
    }
}
