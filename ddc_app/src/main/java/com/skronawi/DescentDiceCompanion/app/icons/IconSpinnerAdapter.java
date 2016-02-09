package com.skronawi.DescentDiceCompanion.app.icons;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.skronawi.DescentDiceCompanion.R;

import java.util.List;

public class IconSpinnerAdapter extends ArrayAdapter {

    private Context localContext;
    private List<Icon> icons;

    public IconSpinnerAdapter(Context context, int resourceId, List<Icon> icons) {
        super(context, resourceId, icons);
        localContext = context;
        this.icons = icons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (null == convertView) {

            LayoutInflater inflater = (LayoutInflater) localContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.icon_spinner_row, null);
        }

        TextView name = (TextView) convertView.findViewById(R.id.spinner_text);
        name.setText(localContext.getResources().getString(icons.get(position).getNameResourceId()));

        ImageView icon = (ImageView) convertView.findViewById(R.id.spinner_icon);
        icon.setImageDrawable(localContext.getResources().getDrawable(icons.get(position).getImageResourceId()));

        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent);
    }


//    private List<Icon> icons;
//
//    public IconSpinnerAdapter(Context context, int resourceId, List<Icon> icons) {
//        super(context, resourceId, icons);
//        this.icons = icons;
//    }
//
//    public View getView(int position, View convertView, ViewGroup parent) {
//        TextView textView = (TextView) super.getView(position, convertView, parent);
//        Icon icon = icons.get(position);
//        textView.setText(getContext().getResources().getString(icon.getNameResourceId()));
//        Drawable pic = getContext().getResources().getDrawable(icon.getImageResourceId());
//        textView.setCompoundDrawablesWithIntrinsicBounds(pic, null, null, null);
//        return textView;
//    }
//
//    public View getDropDownView(int position, View convertView, ViewGroup parent) {
//        return getView(position, convertView, parent);
//    }
}
