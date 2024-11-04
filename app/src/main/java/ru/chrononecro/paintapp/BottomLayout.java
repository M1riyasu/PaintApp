package ru.chrononecro.paintapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class BottomLayout extends FrameLayout {
    private ImageButton color;
    private Button generate, erase, brush,
            pencil, instrumentalSquare, instrumentalCircle,
            instrumentalTriangle, instrumentalArrow;
    private SeekBar drawBar, pencilBar, eraseBar;
    private ColorPicker colorPicker;
    private LinearLayout instrumentalPage, drawPage, pencilPage, erasePage;
    public BottomLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.bottom_layout, this);
        init();
    }
    private void init() {
        color = findViewById(R.id.color);
        generate = findViewById(R.id.generate);
        erase = findViewById(R.id.erase);
        brush = findViewById(R.id.brush);
        pencil = findViewById(R.id.pencil);
        colorPicker = findViewById(R.id.colorPicker);
        drawBar = findViewById(R.id.drawBar);
        pencilBar = findViewById(R.id.pencilBar);
        eraseBar = findViewById(R.id.eraseBar);
        instrumentalSquare = findViewById(R.id.instrumentalSquare);
        instrumentalCircle = findViewById(R.id.instrumentalCircle);
        instrumentalTriangle = findViewById(R.id.instrumentalTriangle);
        instrumentalArrow = findViewById(R.id.instrumentalArrow);
        instrumentalPage = findViewById(R.id.instrumentsPage);
        drawPage = findViewById(R.id.drawPage);
        pencilPage = findViewById(R.id.pencilPage);
        erasePage = findViewById(R.id.erasePage);
        instrumentalSquare.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PaintView.setShape(true);
                PaintView.setDraw(false);
                PaintView.setErase(false);
                PaintView.setShapeName("square");
            }
        });
        instrumentalCircle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PaintView.setShape(true);
                PaintView.setDraw(false);
                PaintView.setErase(false);
                PaintView.setShapeName("circle");
            }
        });
        instrumentalTriangle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PaintView.setShape(true);
                PaintView.setDraw(false);
                PaintView.setErase(false);
                PaintView.setShapeName("triangle");
            }
        });
        instrumentalArrow.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                PaintView.setShape(true);
                PaintView.setDraw(false);
                PaintView.setErase(false);
                PaintView.setShapeName("arrow");
            }
        });
        brush.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                erasePage.setVisibility(GONE);
                pencilPage.setVisibility(GONE);
                drawPage.setVisibility(drawPage.getVisibility() == VISIBLE ? GONE : VISIBLE);
                return true;
            }
        });
        pencil.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                erasePage.setVisibility(GONE);
                drawPage.setVisibility(GONE);
                pencilPage.setVisibility(pencilPage.getVisibility() == VISIBLE ? GONE : VISIBLE);
                return true;
            }
        });
        erase.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                pencilPage.setVisibility(GONE);
                drawPage.setVisibility(GONE);
                erasePage.setVisibility(erasePage.getVisibility() == VISIBLE ? GONE : VISIBLE);
                return true;
            }
        });
        drawBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                PaintView.setDrawWidth(drawBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        pencilBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                PaintView.setPencilWidth(pencilBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        eraseBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                PaintView.setEraseWidth(eraseBar.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        setBottomClickListeners(color);
        setBottomClickListeners(generate);
        setBottomClickListeners(erase);
        setBottomClickListeners(brush);
        setBottomClickListeners(pencil);
        colorPicker.init(color);
    }
    private void setBottomClickListeners(View button) {
        button.setOnClickListener(new BottomClickListener(getContext(), button) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                getRunnable(button).run();
            }
        });
    }

    public ImageButton getColor() {
        color = findViewById(R.id.color);
        return color;
    }

    private Runnable getRunnable(View button) {
        Runnable[] runnable = {this::methodPencil};
        if (button.getId() == R.id.color) {
            runnable[0] = this::methodColor;
        } else if (button.getId() == R.id.generate) {
            runnable[0] = this::methodGenerate;
        } else if (button.getId() == R.id.erase) {
            runnable[0] = this::methodErase;
        } else if (button.getId() == R.id.brush) {
            runnable[0] = this::methodBrush;
        } else if (button.getId() == R.id.pencil) {
            runnable[0] = this::methodPencil;
        }
        return runnable[0];
    }
    private void methodColor() {
        if (colorPicker.getColorPickerMenu().getVisibility() == View.VISIBLE) {
            colorPicker.getColorPickerMenu().setVisibility(View.GONE);
            color.setBackgroundTintList(ColorStateList.valueOf(0xFFFFFFFF));
        } else {
            colorPicker.getColorPickerMenu().setVisibility(View.VISIBLE);
        }
    }
    private void methodGenerate() {
        PaintView.setDraw(false);
        PaintView.setErase(false);
        PaintView.setPencil(false);
        instrumentalPage.setVisibility(instrumentalPage.getVisibility() == VISIBLE ? GONE : VISIBLE);
    }
    private void methodErase() {
        PaintView.setErase(!PaintView.isErase());
        PaintView.setPencil(false);
        PaintView.setDraw(false);
        PaintView.setShape(false);
    }
    private void methodBrush() {
        PaintView.setDraw(!PaintView.isDraw());
        PaintView.setPencil(false);
        PaintView.setErase(false);
        PaintView.setShape(false);
    }
    private void methodPencil() {
        PaintView.setPencil(!PaintView.isPencil());
        PaintView.setErase(false);
        PaintView.setShape(false);
        PaintView.setDraw(false);
    }
}
