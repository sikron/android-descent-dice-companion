package com.skronawi.DescentDiceCompanion.app.probability;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.inqbarna.tablefixheaders.TableFixHeaders;
import com.inqbarna.tablefixheaders.adapters.BaseTableAdapter;
import com.skronawi.DescentDiceCompanion.R;

@SuppressLint("ValidFragment")
public class ProbabilityMatrixDialog extends DialogFragment {

    private final int icon;
    private Double[][] probabilities;
    private int titleId;

    public ProbabilityMatrixDialog(Double[][] probabilities, int titleId, int icon) {
        this.probabilities = probabilities;
        this.titleId = titleId;
        this.icon = icon;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.probability_table_dialog, container);
        getDialog().setTitle(titleId);

        view.findViewById(R.id.button_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                dismiss();
            }
        });

        TableFixHeaders tableFixHeaders = (TableFixHeaders) view.findViewById(R.id.probability_table);

        DimensionType dimensionType = determineDimensionType(probabilities);

        BaseTableAdapter baseTableAdapter;
        if (dimensionType == DimensionType.MATRIX) {
            baseTableAdapter = new DamagePerRangeProbabilityAdapter(probabilities,
                    inflater, getResources().getDisplayMetrics().density);
        } else {
            if (dimensionType == DimensionType.ROW) {
                probabilities = mirror(probabilities);
            }
            baseTableAdapter = new OneDimensionProbabilityAdapter(probabilities, inflater,
                    getResources().getDisplayMetrics().density, icon);
        }
        tableFixHeaders.setAdapter(baseTableAdapter);

        return view;
    }

    private Double[][] mirror(Double[][] probabilities) {
        //diagonal spiegeln
        Double[][] mirror = new Double[probabilities[0].length][probabilities.length];
        for (int i = 0; i < probabilities.length; i++) {
            for (int j = 0; j < probabilities[0].length; j++) {
                mirror[j][i] = probabilities[i][j];
            }
        }
        return mirror;
    }

    private DimensionType determineDimensionType(Double[][] probabilities) {
        if (probabilities.length == 1 && probabilities[0].length >= 1) {
            return DimensionType.ROW;
        } else if (probabilities.length > 1 && probabilities[0].length == 1) {
            return DimensionType.COLUMN;
        } else {
            return DimensionType.MATRIX;
        }
    }
}
