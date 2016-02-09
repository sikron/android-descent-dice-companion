package com.skronawi.DescentDiceCompanion.app.probability;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.TextView;
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;

public abstract class ProbabilityAdapter extends BaseTableAdapter {

    protected Double[][] matrix;
    protected String[] columnHeaders;
    protected String[] lineHeaders;
    protected float density;
    protected LayoutInflater inflater;

    public ProbabilityAdapter(Double[][] matrix, LayoutInflater inflater, float density) {
        this.matrix = matrix;
        columnHeaders = new String[matrix[0].length];
        for (int i = 0; i < matrix[0].length; i++) {
            columnHeaders[i] = "" + i;
        }
        lineHeaders = new String[matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            lineHeaders[i] = "" + i;
        }
        this.density = density;
        this.inflater = inflater;
    }

    @Override
    public int getRowCount() {
        return matrix.length;
    }

    @Override
    public int getColumnCount() {
        return matrix[0].length;
    }


    @Override
    public int getWidth(int column) {
        return Math.round(60 * density);
    }

    @Override
    public int getHeight(int row) {
        return Math.round(45 * density);
    }

    protected String format(Double value) {
        //value is a percentage and in [0,1]
        return String.valueOf(Math.round(value * 10000) / 100.0);
    }

    protected void colorize(TextView textView, Double percentage){
        double v = Math.round(percentage * 10000) / 100.0;
        int red = (255 * (100 - (int) v)) / 100;
        int green = (255 * (int) v) / 100;
        textView.setTextColor(Color.rgb(red, green, 0));
//        Log.i(getClass().getSimpleName(), "v="+v+", red="+red+", green="+green);
    }
}
