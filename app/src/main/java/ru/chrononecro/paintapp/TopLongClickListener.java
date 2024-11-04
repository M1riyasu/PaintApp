package ru.chrononecro.paintapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;

public class TopLongClickListener implements View.OnLongClickListener {
    private static int white, gray;
    public TopLongClickListener(Context context) {
        white = context.getResources().getColor(R.color.white);
        gray = context.getResources().getColor(R.color.gray);
    }

    @Override
    public boolean onLongClick(View view) {
        view.setBackgroundTintList(ColorStateList.valueOf(white));
        view.postDelayed(() -> view.setBackgroundTintList(ColorStateList.valueOf(gray)), 500);
        return true;
    }
}
