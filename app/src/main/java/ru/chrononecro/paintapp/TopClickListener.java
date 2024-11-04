package ru.chrononecro.paintapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.View;
import android.view.View.OnClickListener;

public class TopClickListener implements OnClickListener {
    private static int white, gray;
    public TopClickListener(Context context) {
        white = context.getResources().getColor(R.color.white);
        gray = context.getResources().getColor(R.color.gray);
    }

    @Override
    public void onClick(View v) {
       v.setBackgroundTintList(ColorStateList.valueOf(white));
       v.postDelayed(() -> v.setBackgroundTintList(ColorStateList.valueOf(gray)), 100);
    }
}