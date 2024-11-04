package ru.chrononecro.paintapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.List;

public class BottomClickListener implements OnClickListener {
    private static List<View> buttons = new ArrayList<>();
    private static int white, lime;
    public BottomClickListener(Context context, View button) {
        white = context.getResources().getColor(R.color.white);
        lime = context.getResources().getColor(R.color.lime);
        buttons.add(button);
    }

    @Override
    public void onClick(View v) {
        int buttonColor = v.getBackgroundTintList().getDefaultColor();
        for (int i = 0; i < buttons.size(); i++) {
            buttons.get(i).setBackgroundTintList(ColorStateList.valueOf(white));
        }
        v.setBackgroundTintList(buttonColor == lime ?
                ColorStateList.valueOf(white) : ColorStateList.valueOf(lime));
    }
}