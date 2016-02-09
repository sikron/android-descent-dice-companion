package com.skronawi.DescentDiceCompanion.app.probability;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import com.skronawi.DescentDiceCompanion.R;


public class DamagePerRangeProbabilityAdapter extends ProbabilityAdapter {

    private int markedRow;
    private int markedColumn;


    public DamagePerRangeProbabilityAdapter(Double[][] matrix, LayoutInflater inflater, float density) {

        super(matrix, inflater, density);

        markedColumn = -1;
        markedRow = -1;
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

            case 4:
                view = getColumnHeaderMarked(row, column, convertView, parent);
                break;
            case 5:
                view = getLineHeaderMarked(row, column, convertView, parent);
                break;
            case 6:
                view = getCellMarked(row, column, convertView, parent);
                break;

            default:
                throw new RuntimeException("Unknown view type : " + getItemViewType(row, column));
        }

        return view;
    }

    @Override
    public int getItemViewType(int row, int column) {

        final int itemViewType;

        if (row == -1 && column == -1) {
            //corner
            itemViewType = 0;

        } else if (row == -1) {
            //lineheader
            if (column == markedColumn) {
                return 4;
            } else {
                return 1;
            }

        } else if (column == -1) {
            //columnheader
            if (row == markedRow) {
                return 5;
            } else {
                return 2;
            }

        } else {
            //cell
            if (row == markedRow || column == markedColumn) {
                return 6;
            } else {
                return 3;
            }
        }

        return itemViewType;
    }

    private View getCorner(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_corner, parent, false);
        }
        return convertView;
    }

    private View getColumnHeaderMarked(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_columnheader_marked, parent, false);
            ((TextView) convertView.findViewById(android.R.id.text1)).setTypeface(null, Typeface.BOLD);
        }

        setupColumnHeaderView(row, column, convertView);

        return convertView;
    }

    private void setupColumnHeaderView(int row, int column, View convertView) {

        ((TextView) convertView.findViewById(android.R.id.text1)).setText(columnHeaders[column]);
        convertView.setTag(new int[]{row, column});
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                markedColumn = ((int[]) v.getTag())[1];
//				markedRow = -1;
                notifyDataSetChanged();
            }
        });
    }

    private View getColumnHeader(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_columnheader, parent, false);
        }

        setupColumnHeaderView(row, column, convertView);
        return convertView;
    }

    private View getLineHeaderMarked(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_lineheader_marked, parent, false);
            ((TextView) convertView.findViewById(android.R.id.text1)).setTypeface(null, Typeface.BOLD);
        }

        setupLineHeaderView(row, column, convertView);
        return convertView;
    }

    private View getLineHeader(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_lineheader, parent, false);
        }

        setupLineHeaderView(row, column, convertView);
        return convertView;
    }

    private void setupLineHeaderView(int row, int column, View convertView) {

        ((TextView) convertView.findViewById(android.R.id.text1)).setText(lineHeaders[row]);
        convertView.setTag(new int[]{row, column});
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
//				markedColumn = -1;
                markedRow = ((int[]) v.getTag())[0];
                notifyDataSetChanged();
            }
        });
    }

    private View getCellMarked(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_cell_marked, parent, false);
            if (row == markedRow && column == markedColumn) {
                ((TextView) convertView.findViewById(android.R.id.text1)).setTypeface(null, Typeface.BOLD);
            }
        }

        setupCellView(row, column, convertView);
        return convertView;
    }

    private View getCell(int row, int column, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.probability_table_cell, parent, false);
        }

        convertView.setBackgroundResource(row % 2 == 0 ? R.drawable.probability_table_bg_cell1 :
                R.drawable.probability_table_bg_cell2);

        setupCellView(row, column, convertView);
        return convertView;
    }

    private void setupCellView(int row, int column, View convertView) {

        TextView textView = (TextView) convertView.findViewById(android.R.id.text1);
        textView.setText(format(matrix[row][column]));
        colorize(textView, matrix[row][column]);

        convertView.setTag(new int[]{row, column});
        convertView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                markedColumn = ((int[]) v.getTag())[1];
                markedRow = ((int[]) v.getTag())[0];
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getViewTypeCount() {
        return 7;
    }
}