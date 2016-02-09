package com.skronawi.DescentDiceCompanion.app.probability;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.skronawi.DescentDiceCompanion.R;

public class OneDimensionProbabilityAdapter extends ProbabilityAdapter {

    private final int iconId;

    public OneDimensionProbabilityAdapter(Double[][] matrix, LayoutInflater inflater, float density, int iconId) {

        super(matrix, inflater, density);
        this.iconId = iconId;
    }

    @Override
    public View getView(int row, int column, View convertView, ViewGroup parent) {

        final View view;
        switch (getItemViewType(row, column)) {

            case 0:
                view = getCorner(row, column, convertView, parent);
                break;

            case 1:
                view = getColumnHeader(row, column, convertView, parent);
                break;
            case 2:
                view = getLineHeader(row, column, convertView, parent);
                break;

            case 3:
                view = getCell(row, column, convertView, parent);
                break;

            default:
                throw new RuntimeException("Unknown view type : " + getItemViewType(row, column));
        }

        return view;
    }

    private View getCell(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_cell, parent, false);
        }

        convertView.setBackgroundResource(R.drawable.probability_table_bg_cell1);

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(format(matrix[row][column]));
        colorize(textView, matrix[row][column]);

        convertView.setTag(new int[]{row, column});

        return convertView;
    }

    private View getLineHeader(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_lineheader, parent, false);
        }

        ((TextView) convertView.findViewById(android.R.id.text1)).setText(lineHeaders[row]);
        convertView.setTag(new int[]{row, column});

        return convertView;
    }

    private View getColumnHeader(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_columnheader_icon, parent, false);
        }

        ImageView imageView = (ImageView) convertView.findViewById(R.id.column_icon);
        imageView.setImageDrawable(inflater.getContext().getResources().getDrawable(iconId));

        convertView.setTag(new int[]{row, column});

        return convertView;
    }

    private View getCorner(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_corner_blank, parent, false);
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int row, int column) {

        final int itemViewType;

        if (row == -1 && column == -1) {
            //corner
            itemViewType = 0;

        } else if (row == -1) {
            //lineheader
            return 1;

        } else if (column == -1) {
            //columnheader
            return 2;

        } else {
            //cell
            return 3;
        }

        return itemViewType;
    }

    @Override
    public int getViewTypeCount() {
        return 4;
    }
}
