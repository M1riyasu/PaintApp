package ru.chrononecro.paintapp;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

public class SquareButton extends AppCompatButton {
    public SquareButton(Context context) {
        super(context);
    }

    public SquareButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        int height = getHeight();
        if (getBackground() != null) {
            getBackground().setBounds((height - getWidth()) / 2 * -1, 0, height - (height - getWidth()) / 2, height);
        }
        super.onDraw(canvas);
    }
}