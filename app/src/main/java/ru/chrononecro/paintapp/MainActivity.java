package ru.chrononecro.paintapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PaintView paintView;
    private Button play, pause, layers,
                    add, bin, undo, redo,
                    frameBack, frameForward,
                    deleteAll, addRandom, addClone,
                    pauseSave;
    private EditText frameIndex, playText;
    private BottomLayout bottomLayout;
    private LinearLayout framePage, binPage, addPage, playPage, pausePage;
    private FrameLayout canvas;
    private ScaleGestureDetector scaleGestureDetector;
    private SharedPreferences sharedPreferences;
    private final String nightModeKey = "NIGHT_MODE_KEY";
    private final String colorsFile = "colors";
    private final String bitmapsFile = "bitmaps";
    private List<byte[][]> bitmaps = new ArrayList<>();
    private float scaleFactor = 1.0f;
    private Handler handlerSave = new Handler();
    private Runnable runnableSave = new Runnable() {
        @Override
        public void run() {
            saveBitmaps();
            handlerSave.postDelayed(this, 30000);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PaintView.setColors(loadColors());
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("My_Prefs", MODE_PRIVATE);
        init();
        ((FrameLayout) paintView.getParent()).setClipToOutline(true);
    }
    private void init() {
        paintView = findViewById(R.id.paintView);
        play = findViewById(R.id.play);
        pause = findViewById(R.id.pause);
        layers = findViewById(R.id.layers);
        add = findViewById(R.id.add);
        bin = findViewById(R.id.bin);
        undo = findViewById(R.id.undo);
        redo = findViewById(R.id.redo);
        frameIndex = findViewById(R.id.frameIndex);
        frameBack = findViewById(R.id.frameBack);
        frameForward = findViewById(R.id.frameForward);
        deleteAll = findViewById(R.id.deleteAll);
        addRandom = findViewById(R.id.addRandom);
        addClone = findViewById(R.id.addClone);
        playText = findViewById(R.id.playText);
        pauseSave = findViewById(R.id.pauseSave);
        framePage = findViewById(R.id.framePage);
        binPage = findViewById(R.id.binPage);
        addPage = findViewById(R.id.addPage);
        playPage = findViewById(R.id.playPage);
        pausePage = findViewById(R.id.pausePage);
        bottomLayout = findViewById(R.id.bottomLayout);
        canvas = findViewById(R.id.canvas);
        ((FrameLayout) canvas.getParent()).setClipToOutline(true);
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        setClickListeners();
        loadList(this);
        handlerSave.postDelayed(runnableSave, 30000);
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setClickListeners() {
        setTopClickListeners(play);
        setTopClickListeners(pause);
        setTopClickListeners(layers);
        setTopClickListeners(add);
        setTopClickListeners(bin);
        setTopClickListeners(undo);
        setTopClickListeners(redo);
        frameBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frameBack();
            }
        });
        frameForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                frameForward();
            }
        });
        frameIndex.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String input = frameIndex.getText().toString();
                    if (!input.isEmpty()) {
                        int index = Integer.parseInt(input);
                        if (index > 0 && index - 1 < PaintView.getPathsLength() && index < Integer.MAX_VALUE) {
                            paintView.setCanvasIndex(index - 1);
                        }
                        frameIndex.setText(String.valueOf(paintView.getCanvasIndex() + 1));
                    }
                    return true;
                }
                return false;
            }
        });
        bin.setOnLongClickListener(new TopLongClickListener(this) {
            @Override
            public boolean onLongClick(View view) {
                super.onLongClick(view);
                methodLongBin();
                return true;
            }
        });
        deleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                methodAllBin();
            }
        });
        add.setOnLongClickListener(new TopLongClickListener(this) {
            @Override
            public boolean onLongClick(View view) {
                super.onLongClick(view);
                methodLongAdd();
                return true;
            }
        });
        addClone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.copy();
            }
        });
        addRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LinearLayout layout = new LinearLayout(MainActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                LinearLayout checkLayout = new LinearLayout(MainActivity.this);
                checkLayout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setPadding(16, 16, 16, 16);
                EditText editText = new EditText(MainActivity.this);
                CheckBox checkBox = new CheckBox(MainActivity.this);
                TextView textView = new TextView(MainActivity.this);
                textView.setText("Сгенерировать случайные фигуры");
                checkLayout.addView(checkBox);
                checkLayout.addView(textView);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                layout.addView(editText);
                layout.addView(checkLayout);
                builder.setView(layout);
                builder.setTitle("Введите количество элементов")
                        .setPositiveButton("Создать", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (0 < Integer.parseInt(editText.getText().toString()) && Integer.parseInt(editText.getText().toString()) < Integer.MAX_VALUE) {
                                    paintView.addRandom(Integer.parseInt(editText.getText().toString()), checkBox.isChecked());
                                } else {
                                    Toast.makeText(MainActivity.this, "Неверное число", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .show();
            }
        });
        play.setOnLongClickListener(new TopLongClickListener(this) {
            @Override
            public boolean onLongClick(View view) {
                super.onLongClick(view);
                methodLongPlay();
                return true;
            }
        });
        playText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    String input = playText.getText().toString();
                    if (!input.isEmpty()) {
                        int index = Integer.parseInt(input);
                        if (index > 0 && index < Integer.MAX_VALUE) {
                            PaintView.setDelay(index);
                        } else {
                            Toast.makeText(MainActivity.this, "Неверное число", Toast.LENGTH_SHORT).show();
                        }
                    }
                    return true;
                }
                return false;
            }
        });
        pause.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                pausePage.setVisibility(pausePage.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
                return true;
            }
        });
        pauseSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                paintView.saveGif();
            }
        });
        canvas.setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            private float startY;
            private static final int SWIPE_THRESHOLD = 150;
            private static final int SWIPE_VELOCITY_THRESHOLD = 150;
            private static final long DOUBLE_TAP_TIMEOUT = 200;
            private long lastTapTime = 0;
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (PaintView.isDraw() || PaintView.isPencil() || PaintView.isErase()) {
                    paintView.onTouch(motionEvent);
                    return true;
                } else {
                    scaleGestureDetector.onTouchEvent(motionEvent);
                    switch (motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - lastTapTime < DOUBLE_TAP_TIMEOUT) {
                                scaleFactor = 1f;
                                canvas.setScaleX(1f);
                                canvas.setScaleY(1f);
                                lastTapTime = 0;
                                return true;
                            }
                            startX = motionEvent.getX();
                            startY = motionEvent.getY();
                            lastTapTime = currentTime;
                            return true;

                        case MotionEvent.ACTION_UP:
                            float endX = motionEvent.getX();
                            float endY = motionEvent.getY();

                            float deltaX = endX - startX;
                            float deltaY = endY - startY;

                            float absDeltaX = Math.abs(deltaX);
                            float absDeltaY = Math.abs(deltaY);
                            long duration = motionEvent.getEventTime() - motionEvent.getDownTime();

                            if (absDeltaX > SWIPE_THRESHOLD && absDeltaX > absDeltaY) {
                                if (deltaX > 0) {
                                    frameBack();
                                } else {
                                    frameForward();
                                }
                                return true;
                            }
                            break;
                        case MotionEvent.ACTION_MOVE:
                            canvas.setPivotX(Math.max(0, Math.min(canvas.getPivotX() + (startX - motionEvent.getX()), canvas.getWidth())));
                            canvas.setPivotY(Math.max(0, Math.min(canvas.getPivotY() + (startY - motionEvent.getY()), canvas.getHeight())));
                            break;
                    }
                }
                return false;
            }
        });
    }
    private void frameBack() {
        if (PaintView.getCanvasIndex() > 0) {
            paintView.setCanvasIndex(PaintView.getCanvasIndex() - 1);
            frameIndex.setText(PaintView.getCanvasIndex() + 1 + "");
        }
    }
    private void frameForward() {
        if (PaintView.getCanvasIndex() < PaintView.getPathsLength() - 1) {
            paintView.setCanvasIndex(PaintView.getCanvasIndex() + 1);
            frameIndex.setText(PaintView.getCanvasIndex() + 1 + "");
        }
    }
    private void methodPlay() {
        paintView.startAnimate();
    }
    private void methodPause() {
        paintView.stopAnimate();
    }
    private void methodLayers() {
        framePage.setVisibility(framePage.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        frameIndex.setText(PaintView.getCanvasIndex() + 1 + "");
    }
    private void methodAdd() {
        if (!PaintView.isAnimate()) paintView.addCanvas();
        frameIndex.setText(PaintView.getCanvasIndex() + 1 + "");
    }
    private void methodBin() {
        if (!PaintView.isAnimate()) paintView.removeCanvas();
        frameIndex.setText(PaintView.getCanvasIndex() + 1 + "");
    }
    private void methodUndo() {
        if (!PaintView.isAnimate()) paintView.undo();
    }
    private void methodRedo() {
        if (!PaintView.isAnimate()) paintView.redo();
    }
    private void methodLongBin() {
        binPage.setVisibility(binPage.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
    private void methodAllBin() {
        Context context = this;
        binPage.setVisibility(View.GONE);
        new AlertDialog.Builder(context)
                .setMessage("Вы уверены, что хотите продолжить? Все кадры будут безвозратно удалены")
                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                })
                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new AlertDialog.Builder(context)
                                .setMessage("Вы точно уверены? Вы не сможете вернуть удаленные кадры")
                                .setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                    }
                                })
                                .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        paintView.deleteAll();
                                    }
                                }).show();
                    }
                }).show();
    }
    private void methodLongAdd() {
        addPage.setVisibility(addPage.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
    private void methodLongLayers() {

    }
    private void methodLongPlay() {
        playPage.setVisibility(playPage.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }
    private void setTopClickListeners(View button) {
        button.setOnClickListener(new TopClickListener(this) {
            @Override
            public void onClick(View v) {
                super.onClick(v);
                getRunnable(button).run();
            }
        });
    }

    private Runnable getRunnable(View button) {
        Runnable[] runnable = new Runnable[1];
        if (button.getId() == R.id.play) {
            runnable[0] = this::methodPlay;
        } else if (button.getId() == R.id.pause) {
            runnable[0] = this::methodPause;
        } else if (button.getId() == R.id.layers) {
            runnable[0] = this::methodLayers;
        } else if (button.getId() == R.id.add) {
            runnable[0] = this::methodAdd;
        } else if (button.getId() == R.id.bin) {
            runnable[0] = this::methodBin;
        } else if (button.getId() == R.id.undo) {
            runnable[0] = this::methodUndo;
        } else if (button.getId() == R.id.redo) {
            runnable[0] = this::methodRedo;
        }
        return runnable[0];
    }
    private void saveBitmaps() {
        try (FileOutputStream fos = openFileOutput(bitmapsFile, Context.MODE_PRIVATE);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            List<byte[][]> oldBitmaps = paintView.getOldBitmaps();
            List<byte[][]> newBitmaps = paintView.getBitmaps();
            int maxLength = Math.max(oldBitmaps.size(), newBitmaps.size());

            // Список для хранения объединённых битмапов в виде byte[][]
            List<byte[][]> mergedBitmaps = new ArrayList<>();

            for (int i = 0; i < maxLength; i++) {
                Bitmap combinedBitmap = null;

                // Объединяем старые битмапы
                if (i < oldBitmaps.size()) {
                    Bitmap[] oldBitmapsArray = convertToBitmapArray(oldBitmaps.get(i));
                    combinedBitmap = overlayBitmaps(oldBitmapsArray);
                }

                // Объединяем новые битмапы
                if (i < newBitmaps.size()) {
                    Bitmap[] newBitmapsArray = convertToBitmapArray(newBitmaps.get(i));
                    if (combinedBitmap != null) {
                        combinedBitmap = overlayBitmaps(new Bitmap[]{combinedBitmap, newBitmapsArray[0]});
                    } else {
                        combinedBitmap = overlayBitmaps(newBitmapsArray);
                    }
                }

                // Преобразуем объединённый битмап обратно в byte[][]
                if (combinedBitmap != null) {
                    byte[][] combinedByteArray = convertToByteArray(new Bitmap[]{combinedBitmap});
                    mergedBitmaps.add(combinedByteArray);
                }
            }

            // Сохраняем объединённые массивы байтов
            oos.writeObject(mergedBitmaps);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Метод для накладывания битмапов друг на друга
    private Bitmap overlayBitmaps(Bitmap[] bitmaps) {
        if (bitmaps == null || bitmaps.length == 0) return null;

        // Определяем размеры для итогового битмапа
        int width = bitmaps[0].getWidth();
        int height = bitmaps[0].getHeight();
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(resultBitmap);

        // Рисуем все битмапы на одном канвасе
        for (Bitmap bitmap : bitmaps) {
            canvas.drawBitmap(bitmap, 0, 0, null);
        }

        return resultBitmap;
    }

    // Метод для конвертации byte[][] в Bitmap[]
    private Bitmap[] convertToBitmapArray(byte[][] byteArray) {
        Bitmap[] bitmaps = new Bitmap[byteArray.length];
        for (int i = 0; i < byteArray.length; i++) {
            bitmaps[i] = BitmapFactory.decodeByteArray(byteArray[i], 0, byteArray[i].length);
        }
        return bitmaps;
    }

    // Метод для конвертации Bitmap[] в byte[][]
    private byte[][] convertToByteArray(Bitmap[] bitmapArray) {
        byte[][] byteArrays = new byte[bitmapArray.length][];
        for (int i = 0; i < bitmapArray.length; i++) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmapArray[i].compress(Bitmap.CompressFormat.PNG, 100, stream);
            byteArrays[i] = stream.toByteArray();
        }
        return byteArrays;
    }


    public void loadList(Context context) {
        try (FileInputStream fis = context.openFileInput(bitmapsFile);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            bitmaps = (List<byte[][]>) ois.readObject();  // Изменено на byte[][]
            List<Bitmap[]> bitmapList = new ArrayList<>();

            for (byte[][] byteArrayArray : bitmaps) {
                Bitmap[] bitmapArray = new Bitmap[byteArrayArray.length];
                for (int i = 0; i < byteArrayArray.length; i++) {
                    byte[] primitiveByteArray = byteArrayArray[i];  // Теперь просто byte[]

                    // Создаем Bitmap из byte[]
                    bitmapArray[i] = BitmapFactory.decodeByteArray(primitiveByteArray, 0, primitiveByteArray.length);
                }
                // Добавляем массив Bitmap в список
                bitmapList.add(bitmapArray);
            }
            paintView.setBitmaps(bitmapList);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            bitmaps = new ArrayList<>();
        }
    }
    private void saveColors() {
        try (FileOutputStream fos = openFileOutput(colorsFile, Context.MODE_PRIVATE)) {
            List<Integer> colors = paintView.getColors();
            byte[] byteArray = new byte[colors.size() * 4];

            for (int i = 0; i < colors.size(); i++) {
                int color = colors.get(i);
                byteArray[i * 4] = (byte) (color >> 24);
                byteArray[i * 4 + 1] = (byte) (color >> 16);
                byteArray[i * 4 + 2] = (byte) (color >> 8);
                byteArray[i * 4 + 3] = (byte) color;
            }
            fos.write(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private List<Integer> loadColors() {
        try (FileInputStream fis = openFileInput(colorsFile)) {
            List<Integer> colors = new ArrayList<>();
                byte[] byteArray = new byte[fis.available()];
                fis.read(byteArray);
                for (int i = 0; i < byteArray.length; i += 4) {
                    int a = byteArray[i] & 0xFF;
                    int r = byteArray[i + 1] & 0xFF;
                    int g = byteArray[i + 2] & 0xFF;
                    int b = byteArray[i + 3] & 0xFF;
                    int color = (a << 24) | (r << 16) | (g << 8) | b;
                    colors.add(color);
                }

            return colors;
        } catch (IOException e) {
            return Arrays.asList(0xFF000000);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveColors();
        bitmaps = paintView.getBitmaps();
        saveBitmaps();
        paintView.onDestroy();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveColors();
        bitmaps = paintView.getBitmaps();
        saveBitmaps();
        paintView.onDestroy();
        handlerSave.removeCallbacks(runnableSave);
    }
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float factor = detector.getScaleFactor() - 1;
            scaleFactor += factor * Math.max(1, scaleFactor / 3f);
            scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 10.0f));

            canvas.setScaleX(scaleFactor);
            canvas.setScaleY(scaleFactor);

            return true;
        }

        @Override
        public boolean onScaleBegin(@NonNull ScaleGestureDetector detector) {
            float focusX = detector.getFocusX();
            float focusY = detector.getFocusY();

            float imageX = focusX - canvas.getLeft() * 0.5f;
            float imageY = focusY - canvas.getTop() * 0.5f;

            canvas.setPivotX(imageX);
            canvas.setPivotY(imageY);

            return super.onScaleBegin(detector);
        }
    }
}