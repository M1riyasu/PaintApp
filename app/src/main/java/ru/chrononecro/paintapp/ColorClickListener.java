package ru.chrononecro.paintapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.ArrayList;
import java.util.List;

public class ColorClickListener implements OnClickListener {
    private static List<View> buttons = new ArrayList<>();
    private static List<Integer> colors;
    public ColorClickListener(Context context, View button) {
        buttons.add(button);
    }
    public static void setColors(List<Integer> colors) {
        ColorClickListener.colors = colors;
    }
    public static List<Integer> getColors() {
        return colors;
    }
    @Override
    public void onClick(View v) {
        if (colors.contains(v.getBackgroundTintList().getDefaultColor())) {
            for (int i = 0; i < colors.size(); i++) {
                if (colors.get(i) == v.getBackgroundTintList().getDefaultColor()) {
                    colors.remove(i);
                }
            }
            colors.add(0, v.getBackgroundTintList().getDefaultColor());
        } else {
            colors.add(0, v.getBackgroundTintList().getDefaultColor());
            if (colors.size() > buttons.size()) {
                colors.remove(colors.size() - 1);
            }
        }

        for (int i = 0; i < buttons.size(); i++) {
            try {
                buttons.get(buttons.size() - 1 - i).setBackgroundTintList(ColorStateList.valueOf(colors.get(i)));
            } catch (IndexOutOfBoundsException e) {}
        }
    }
}