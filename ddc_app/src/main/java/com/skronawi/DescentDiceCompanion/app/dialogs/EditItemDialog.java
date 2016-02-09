package com.skronawi.DescentDiceCompanion.app.dialogs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.GridLayout;
import android.view.*;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.skronawi.DescentDiceCompanion.R;
import com.skronawi.DescentDiceCompanion.app.database.IconItem;
import com.skronawi.DescentDiceCompanion.app.dice.DiceImageResolver;
import com.skronawi.DescentDiceCompanion.app.icons.Icon;
import com.skronawi.DescentDiceCompanion.app.icons.IconSpinnerAdapter;
import com.skronawi.DescentDiceCompanion.app.items.CategoryList;
import com.skronawi.DescentDiceCompanion.lib.dice.Die;
import net.simonvt.numberpicker.NumberPicker;

import java.util.HashMap;
import java.util.List;

@SuppressLint("ValidFragment")
public class EditItemDialog extends DialogFragment {

    private EditText name;
    private IconItem item;
    private Spinner spinner;
    private final int titleId;
    private CategoryList list;
    private List<Die> dice;
    private List<Icon> icons;

    private HashMap<Die, NumberPicker> numberPickerMap;
    private static final int MAX_NUMBER_ITEMS = 8;

    public EditItemDialog(IconItem item, int titleId, CategoryList list, List<Die> dice, List<Icon> icons) {
        this.item = item;
        this.titleId = titleId;
        this.list = list;
        this.dice = dice;
        this.icons = icons;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.edititemdialog, container);
        getDialog().setTitle(titleId);

        view.findViewById(R.id.item_name).setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {

                    v.getContext();
                    InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        setupNumberPickers(view);

        setValues(view);
        setButtonListeners(view);

        return view;
    }

    private void setupNumberPickers(View view) {

        GridLayout layout = (GridLayout) view.findViewById(R.id.dice_chooser_layout);
        layout.removeAllViews();
        layout.setColumnCount(dice.size());

        for (Die die : dice) {
            ImageView iv = new ImageView(getActivity());
            iv.setImageResource(DiceImageResolver.resolveDieImageId(die.getType()));
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setGravity(Gravity.CENTER);
            iv.setLayoutParams(params);
            layout.addView(iv);
        }
        numberPickerMap = new HashMap<Die, NumberPicker>();
        for (Die die : dice) {
            NumberPicker np = new NumberPicker(getActivity());
            np.setMaxValue(5);
            np.setMinValue(0);
            numberPickerMap.put(die, np);
            layout.addView(np);
        }
    }


    @SuppressLint("WrongViewCast")
    private void setValues(View view) {

        name = (EditText) view.findViewById(R.id.item_name);
        if (item.getName() != null || item.getName().length() > 0) {
            name.setText(item.getName());
        }

        spinner = (Spinner) view.findViewById(R.id.item_icon_spinner);
        ArrayAdapter adapter = new IconSpinnerAdapter(list.getActivity(), android.R.layout.simple_spinner_item, icons);
        spinner.setAdapter(adapter);
        spinner.setSelection(findPosition(item.getIconName(), icons));

        for (Die die : dice) {
            NumberPicker np = numberPickerMap.get(die);
            np.setValue(item.getNumberOfDice(die));
        }
    }

    private int findPosition(String iconName, List<Icon> icons) {
        for (int i = 0; i < icons.size(); i++) {
            String name = list.getActivity().getResources().getResourceEntryName(icons.get(i).getImageResourceId());
            if (name.equals(iconName)) {
                return i;
            }
        }
        return 0;
    }

    private void setButtonListeners(final View view) {
        final Button okButton = (Button) view.findViewById(R.id.button_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (getNumberOfDiceToSet() > MAX_NUMBER_ITEMS) {
                    list.showToast(list.getResources().getString(
                            R.string.item_max_dice_number, MAX_NUMBER_ITEMS), Toast.LENGTH_SHORT);
                } else {
                    setValuesIntoItem();
                    list.setItem(item);
                    dismiss();
                }
            }
        });
        final Button cancelButton = (Button) view.findViewById(R.id.button_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                dismiss();
            }
        });
    }

    private int getNumberOfDiceToSet() {
        int numberOfDiceToSet = 0;
        for (Die die : dice) {
            NumberPicker np = numberPickerMap.get(die);
            numberOfDiceToSet += np.getValue();
        }
        return numberOfDiceToSet;
    }

    private void setValuesIntoItem() {
        item.setName(name.getText().toString());
        for (Die die : dice) {
            NumberPicker np = numberPickerMap.get(die);
            item.setNumberOfDice(die, np.getValue());
        }
        int iconResourceId = ((Icon) spinner.getSelectedItem()).getImageResourceId();
        String name = list.getActivity().getResources().getResourceEntryName(iconResourceId);
        item.setIconName(name);
    }
}