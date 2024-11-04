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

public class ColorPicker extends FrameLayout {
    private ImageButton color;
    private Button closeColor, applyColor;
    private LinearLayout colorPickerMenu, colorPickerActive, colorPick;
    private GridLayout colorPickerExpanded;
    private SeekBar alphaBar, redBar, greenBar, blueBar;
    private View colorIndicator;
    private int barColor = 0xFF996633;

    public ColorPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.color_picker, this);
    }
    public void init(ImageButton color) {
        this.color = color;
        colorPick = findViewById(R.id.colorPick);
        colorPickerMenu = findViewById(R.id.colorPickerMenu);
        colorPickerActive = findViewById(R.id.colorPickerActive);
        colorPickerExpanded = findViewById(R.id.colorPickerExpanded);
        colorIndicator = findViewById(R.id.colorIndicator);
        closeColor = findViewById(R.id.closeColor);
        applyColor = findViewById(R.id.applyColor);
        alphaBar = findViewById(R.id.alphaBar);
        redBar = findViewById(R.id.redBar);
        greenBar = findViewById(R.id.greenBar);
        blueBar = findViewById(R.id.blueBar);

        setExpandClick((Button)colorPickerActive.getChildAt(0));
        setActiveClick(colorPickerActive);
        setColorPickerExpanded(colorPickerExpanded);
        setBar(alphaBar);
        setBar(redBar);
        setBar(greenBar);
        setBar(blueBar);
        setApplyColorClick(applyColor);
        setCloseColorClick(closeColor);
    }
    private void setCloseColorClick(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                colorPick.setVisibility(View.GONE);
            }
        });
    }
    private void setApplyColorClick(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> colors = new ArrayList<>();
                colors.addAll(PaintView.getColors());
                if (colors.contains(barColor)) {
                    for (int i = 0; i < colors.size(); i++) {
                        if (colors.get(i) == barColor) colors.remove(i);
                    }
                    colors.add(0, barColor);
                } else {
                    colors.add(0, barColor);
                    if (colors.size() > colorPickerExpanded.getChildCount() - 1) {
                        colors.remove(colors.size() - 1);
                    }
                }
                PaintView.setColors(colors);
                sortColors();
            }
        });
    }
    private void setBar(SeekBar bar) {
        ((FrameLayout) bar.getParent()).setClipToOutline(true);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                barColor = (alphaBar.getProgress() << 24) |
                        (redBar.getProgress() << 16) |
                        (greenBar.getProgress() << 8) |
                        blueBar.getProgress();
                colorIndicator.setBackgroundTintList(ColorStateList.valueOf(barColor));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
    private void sortColors() {
        List<Integer> colors = PaintView.getColors();
        for (int i = 0; i < colorPickerExpanded.getRowCount(); i++) {
            for (int j = 0; j < colorPickerExpanded.getColumnCount(); j++) {
                if (i + j != 8) {
                    ((Button) colorPickerExpanded.getChildAt(colorPickerExpanded.getChildCount() - 1 - (i * colorPickerExpanded.getColumnCount() + j)))
                            .setBackgroundTintList(ColorStateList.valueOf(colors.get(i * colorPickerExpanded.getColumnCount() + j)));
                }
            }
        }
        int j = 3;
        for (int i = 1; i < colorPickerActive.getChildCount(); i++) {
            if (colorPickerActive.getChildAt(i) instanceof Button)
                colorPickerActive.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(colors.get(j--)));
        }
        color.setImageTintList(ColorStateList.valueOf(colors.get(0)));
        PaintView.setColor(colors.get(0));
    }
    private void setColorPickerExpanded(GridLayout grid) {
        List<Integer> colors = new ArrayList<>();
        colors.addAll(PaintView.getColors());
        for (int i = 0; i < grid.getRowCount(); i++) {
            for (int j = 0; j < grid.getColumnCount(); j++) {
                SquareButton button = new SquareButton(grid.getContext());

                int size = getResources().getDimensionPixelSize(R.dimen.dp40);
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = 0;
                params.height = size;
                params.bottomMargin = size / 5;
                params.topMargin = size / 5;
                params.rowSpec = GridLayout.spec(i, 1f);
                params.columnSpec = GridLayout.spec(j, 1f);
                button.setLayoutParams(params);
                if (i == 0 && j == 0) {
                    button.setBackground(ContextCompat.getDrawable(grid.getContext(), R.drawable.color_circle));
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (colorPick.getVisibility() == View.VISIBLE) {
                                colorPick.setVisibility(View.GONE);
                            } else {
                                colorPick.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                } else {
                    button.setBackground(ContextCompat.getDrawable(grid.getContext(), R.drawable.shape_circle));
                    setColorClickListeners(button);
                    int color;
                    try {
                        color = colors.get(i * grid.getColumnCount() + j - 1);
                    } catch (IndexOutOfBoundsException e) {
                        color = 0xFF000000 | ((int) (Math.random() * 0x00FFFFFF));
                        colors.add(color);
                    }

                    button.setBackgroundTintList(ColorStateList.valueOf(color));
                }
                grid.addView(button);
                button.postDelayed(() -> button.setWidth(size), 250);
            }
        }
        PaintView.setColors(colors);
    }
    private void setColorClickListeners(Button button) {
        button.setOnClickListener(new ColorClickListener(getContext(), button) {
            @Override
            public void onClick(View v) {
                setColors(PaintView.getColors());
                super.onClick(v);
                PaintView.setColors(getColors());
                sortColors();
            }
        });

    }
    private void setActiveClick(LinearLayout layout) {
        int j = 3;
        List<Integer> colors = PaintView.getColors();
        if (colors.size() > 0) {
            color.setImageTintList(ColorStateList.valueOf(colors.get(0)));
            PaintView.setColor(colors.get(0));
        }
        for (int i = 1; i < layout.getChildCount(); i++) {
            if (layout.getChildAt(i) instanceof Button) {
                if (colors.size() > 3) {
                    layout.getChildAt(i).setBackgroundTintList(ColorStateList.valueOf(colors.get(j--)));
                }
                layout.getChildAt(i).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        List<Integer> colors = new ArrayList<>();
                        colors.addAll(PaintView.getColors());
                        int chosenColor = view.getBackgroundTintList().getDefaultColor();
                        for (int i = 0; i < 4; i++) {
                            if (colors.get(i) == chosenColor)
                                colors.remove(i);
                        }
                        colors.add(0, chosenColor);
                        PaintView.setColors(colors);
                        sortColors();
                    }
                });
            }
        }
    }
    private void setExpandClick(Button button) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (colorPickerExpanded.getVisibility() == View.VISIBLE) {
                    colorPickerExpanded.setVisibility(View.GONE);
                    view.setBackgroundTintList(ColorStateList.valueOf(0xFFFFFFFF));
                } else {
                    colorPickerExpanded.setVisibility(View.VISIBLE);
                    view.setBackgroundTintList(ColorStateList.valueOf(0xFFA8DB10));
                }
            }
        });
    }
    public LinearLayout getColorPickerMenu() {
        return colorPickerMenu;
    }
}
