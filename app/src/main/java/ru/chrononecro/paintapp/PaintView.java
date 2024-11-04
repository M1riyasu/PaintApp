package ru.chrononecro.paintapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class PaintView extends AppCompatImageView {
    private static Paint paint = new Paint();
    private static Paint pencilPaint = new Paint();
    private static Paint erasePaint = new Paint();
    private static Path currentPath = new Path();
    private static List<Stack<DrawnPath>> drawnPaths = new ArrayList<>();
    private static List<Stack<DrawnPath>> undonePaths = new ArrayList<>();
    private static List<Bitmap[]> bitmaps = new ArrayList<>();
    private static List<Integer> colors = new ArrayList<>();
    private static String shapeName = "square";
    private static int canvasIndex = 0;
    private static int bitmapIndex = 0;
    private static int color;
    private static int delay = 1000;
    private static float drawWidth = 10f;
    private static float pencilWidth = 10f;
    private static float eraseWidth = 40f;
    private static boolean pencil = false;
    private static boolean draw = false;
    private static boolean erase = false;
    private static boolean shape = false;
    private static boolean animate = false;
    private static Handler animateHandler = new Handler();
    private Runnable animateRunnable = new Runnable() {
        @Override
        public void run() {
            if (canvasIndex < drawnPaths.size() - 1) {
                canvasIndex++;
                invalidate();
            } else {
                canvasIndex = 0;
                invalidate();
            }
            animateHandler.postDelayed(animateRunnable, delay);
        }
    };
    private static class DrawnPath {
        Path path;
        int color;
        float width;
        boolean isErase;
        boolean isPencil;

        DrawnPath(Path path) {
            this.path = path;
            this.width = PaintView.eraseWidth;
            this.isErase = true;
        }
        DrawnPath(Path path, int color, boolean isPencil) {
            this.path = path;
            this.width = isPencil ? PaintView.pencilWidth : PaintView.drawWidth;
            this.color = color;
            this.isErase = false;
            this.isPencil = isPencil;
        }
    }

    public PaintView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setBackground(new BitmapDrawable(getResources(), Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)));
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(10f);

        pencilPaint.setAntiAlias(true);
        pencilPaint.setColor(Color.BLACK);
        pencilPaint.setStyle(Paint.Style.STROKE);
        pencilPaint.setStrokeJoin(Paint.Join.ROUND);
        pencilPaint.setStrokeWidth(10f);

        erasePaint.setAntiAlias(true);
        erasePaint.setColor(Color.TRANSPARENT);
        erasePaint.setStyle(Paint.Style.STROKE);
        erasePaint.setStrokeJoin(Paint.Join.ROUND);
        erasePaint.setStrokeWidth(40f);
        erasePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        if (drawnPaths.isEmpty()) {
            drawnPaths.add(new Stack<DrawnPath>());
            undonePaths.add(new Stack<DrawnPath>());
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bitmaps.size() > bitmapIndex) {
            if (bitmaps.get(bitmapIndex).length > canvasIndex) {
                canvas.drawBitmap(bitmaps.get(bitmapIndex)[canvasIndex], 0, 0, null);
            }
        }
        for (DrawnPath drawnPath : drawnPaths.get(canvasIndex)) {
            if (drawnPath.isErase) {
                erasePaint.setStrokeWidth(drawnPath.width);
                canvas.drawPath(drawnPath.path, erasePaint);
            } else if (drawnPath.isPencil) {
                pencilPaint.setStrokeWidth(drawnPath.width);
                pencilPaint.setColor(drawnPath.color);
                canvas.drawPath(drawnPath.path, pencilPaint);
            } else {
                paint.setStrokeWidth(drawnPath.width);
                paint.setColor(drawnPath.color);
                canvas.drawPath(drawnPath.path, paint);
            }
        }
        if (erase) {
            erasePaint.setStrokeWidth(eraseWidth);
            canvas.drawPath(currentPath, erasePaint);
        } else if (draw) {
            paint.setStrokeWidth(drawWidth);
            paint.setColor(color);
            canvas.drawPath(currentPath, paint);
        } else {
            pencilPaint.setStrokeWidth(pencilWidth);
            pencilPaint.setColor(color);
            canvas.drawPath(currentPath, pencilPaint);
        }
    }

    public void onTouch(MotionEvent event) {
        if (!animate && (draw || pencil || erase)) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    currentPath.moveTo(x, y);
                    break;
                case MotionEvent.ACTION_MOVE:
                    currentPath.lineTo(x, y);
                    break;
                case MotionEvent.ACTION_UP:
                    if (erase) {
                        drawnPaths.get(canvasIndex).push(new DrawnPath(currentPath));
                    } else {
                        drawnPaths.get(canvasIndex).push(new DrawnPath(currentPath, color, pencil));
                    }
                    currentPath = new Path();
                    break;
                default:
            }
            invalidate();
        }
    }

    public byte[] generateGIF() {
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for (Stack<DrawnPath> pathStack : drawnPaths) {
            Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            for (DrawnPath drawnPath : pathStack) {
                if (drawnPath.isErase) {
                    canvas.drawPath(drawnPath.path, erasePaint);
                } else if (drawnPath.isPencil) {
                    canvas.drawPath(drawnPath.path, pencilPaint);
                } else {
                    canvas.drawPath(drawnPath.path, paint);
                }
            }
            bitmaps.add(bitmap);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GifEncoder encoder = new GifEncoder();
        encoder.setDelay(delay);
        encoder.start(bos);
        for (Bitmap bitmap : bitmaps) {
            encoder.addFrame(bitmap);
        }
        encoder.finish();
        return bos.toByteArray();
    }
    
    public void saveGif() {
        try {
            File file = File.createTempFile("temp", ".gif", getContext().getCacheDir());
            FileOutputStream fos = new FileOutputStream(file);
            byte[] data = generateGIF();
            fos.write(data);
            fos.close(); // Закрываем поток перед передачей файла
            shareGif(file); // Передаем файл для совместного использования
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shareGif(File gifFile) {
        Uri uri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".fileprovider", gifFile);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/gif");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // Разрешение на чтение URI
        getContext().startActivity(Intent.createChooser(shareIntent, "Поделиться GIF"));
    }

    public List<byte[][]> getBitmaps() {
        List<Bitmap[]> bitmapList = new ArrayList<>();
        for (Stack<DrawnPath> stack : drawnPaths) {
            Bitmap[] bitmapArray = new Bitmap[stack.size()];
            for (int i = 0; i < stack.size(); i++) {
                canvasIndex = 0;
                invalidate();
                Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                draw(canvas);
                bitmapArray[i] = bitmap;
            }
            bitmapList.add(bitmapArray);
        }

        List<byte[][]> byteArrayList = new ArrayList<>();
        for (Bitmap[] bitmapArray : bitmapList) {
            byte[][] byte2DArray = new byte[bitmapArray.length][];
            for (int i = 0; i < bitmapArray.length; i++) {
                Bitmap bitmap = bitmapArray[i];

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                byte2DArray[i] = byteArray;  // Просто присваиваем byte[]
            }
            byteArrayList.add(byte2DArray);
        }
        return byteArrayList;
    }
    public List<byte[][]> getOldBitmaps() {
        List<byte[][]> byteArrayList = new ArrayList<>(); // Создаем новый список для хранения массивов байтов

        for (Bitmap[] bitmapArray : bitmaps) {
            byte[][] byteArray = new byte[bitmapArray.length][]; // Создаем новый массив байтов для каждого массива битмапов

            for (int i = 0; i < bitmapArray.length; i++) {
                if (bitmapArray[i] != null) { // Проверяем, что битмап не null
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmapArray[i].compress(Bitmap.CompressFormat.PNG, 100, stream); // Сжимаем битмап в PNG
                    byteArray[i] = stream.toByteArray(); // Получаем массив байтов
                }
            }
            byteArrayList.add(byteArray); // Добавляем массив байтов в общий список
        }
        return byteArrayList;
    }

    public void startAnimate() {
        canvasIndex = -1;
        animate = true;
        animateHandler.removeCallbacks(animateRunnable);
        animateHandler.postDelayed(animateRunnable, delay);
    }

    public void stopAnimate() {
        canvasIndex = drawnPaths.size() - 1;
        animate = false;
        animateHandler.removeCallbacks(animateRunnable);
        invalidate();
    }

    public void addCanvas() {
        canvasIndex++;
        drawnPaths.add(canvasIndex, new Stack<DrawnPath>());
        undonePaths.add(canvasIndex, new Stack<DrawnPath>());
        invalidate();
    }

    public void removeCanvas() {
        if (drawnPaths.size() > 1) {
            drawnPaths.remove(canvasIndex);
            undonePaths.remove(canvasIndex);
            if (canvasIndex > drawnPaths.size() - 1) {
                canvasIndex -= 1;
                invalidate();
            }
        }
    }

    public static boolean isAnimate() {
        return animate;
    }

    public static boolean isDraw() {
        return draw;
    }

    public static boolean isPencil() {
        return pencil;
    }

    public static boolean isErase() {
        return erase;
    }

    public void setBitmaps(List<Bitmap[]> bitmaps) {
        PaintView.bitmaps = bitmaps;
        for (int i = 0; i < bitmaps.size(); i++) {
            drawnPaths.add(new Stack<DrawnPath>());
            undonePaths.add(new Stack<DrawnPath>());
        }
        invalidate();
    }

    public static void setColors(List<Integer> colors) {
        PaintView.colors = colors;
    }

    public static void setShape(boolean shape) {
        PaintView.shape = shape;
    }

    public static void setShapeName(String shapeName) {
        PaintView.shapeName = shapeName;
    }

    public static void setDrawWidth(float drawWidth) {
        PaintView.drawWidth = drawWidth;
    }

    public static void setPencilWidth(float pencilWidth) {
        PaintView.pencilWidth = pencilWidth;
    }

    public static void setEraseWidth(float eraseWidth) {
        PaintView.eraseWidth = eraseWidth;
    }

    public static void setColor(int color) {
        PaintView.color = color;
    }

    public static void setDelay(int delay) {
        PaintView.delay = delay;
    }

    public static void setDraw(boolean draw) {
        PaintView.draw = draw;
    }

    public static void setPencil(boolean pencil) {
        PaintView.pencil = pencil;
    }

    public static void setErase(boolean erase) {
        PaintView.erase = erase;
    }

    public void setCanvasIndex(int canvasIndex) {
        PaintView.canvasIndex = canvasIndex;
        invalidate();
    }

    public static List<Integer> getColors() {
        return PaintView.colors;
    }

    public static int getCanvasIndex() {
        return canvasIndex;
    }
    public static int getPathsLength() {
        return drawnPaths.size();
    }

    public void undo() {
        if (!drawnPaths.get(canvasIndex).isEmpty()) {
            undonePaths.get(canvasIndex).push(drawnPaths.get(canvasIndex).pop());
            invalidate();
        }
    }

    public void redo() {
        if (!undonePaths.get(canvasIndex).isEmpty()) {
            drawnPaths.get(canvasIndex).push(undonePaths.get(canvasIndex).pop());
            invalidate();
        }
    }
    public void addRandom(int count, boolean isRandom) {
        if (isRandom) {
            for (int i = 0; i < count; i++) {
                int j = (int) (Math.random() * 3);
                switch (j) {
                    case 0:
                        drawCircle((float) (this.getWidth() * Math.random()), (float) (this.getHeight() * Math.random()),
                                (float) ((Math.random() + 10) * 100), ((int)(Math.random() * 0x00FFFFFF)) | 0xFF000000);
                        break;
                    case 1:
                        drawSquare((float) (this.getWidth() * Math.random()), (float) (this.getHeight() * Math.random()),
                                (float) ((Math.random() + 10) * 100), ((int)(Math.random() * 0x00FFFFFF)) | 0xFF000000);
                        break;
                    case 2:
                        drawTriangle((float) (this.getWidth() * Math.random()), (float) (this.getHeight() * Math.random()),
                                (float) ((Math.random() * 100) + 10), ((int)(Math.random() * 0x00FFFFFF)) | 0xFF000000);
                }
            }
        } else {

            Stack<DrawnPath> previousPathStack = drawnPaths.get(canvasIndex - 1);
            Stack<DrawnPath> nextPathStack = drawnPaths.get(canvasIndex + 1);

            for (int i = 1; i <= count; i++) {
                float interpolationFactor = i / (float) (count + 1); // Интерполяция от предыдущего к следующему
                Stack<DrawnPath> interpolatedPathStack = interpolatePaths(previousPathStack, nextPathStack, interpolationFactor);

                // Добавляем интерполированный путь на нужную позицию
                drawnPaths.add(canvasIndex + i, interpolatedPathStack);
                undonePaths.add(canvasIndex + i, new Stack<DrawnPath>());
            }
        }
        invalidate();
    }
    public void drawCircle(float x, float y, float radius, int color) {
        drawShape("circle", x, y, radius, color);
    }

    public void drawSquare(float x, float y, float sideLength, int color) {
        drawShape("square", x, y, sideLength, color);
    }

    public void drawTriangle(float x, float y, float sideLength, int color) {
        drawShape("triangle", x, y, sideLength, color);
    }

    public void drawShape(String shapeType, float x, float y, float size, int color) {
        Path shapePath = new Path();
        switch (shapeType.toLowerCase()) {
            case "circle":
                shapePath.addCircle(x, y, size, Path.Direction.CW);
                break;
            case "square":
                shapePath.addRect(x - size / 2, y - size / 2, x + size / 2, y + size / 2, Path.Direction.CW);
                break;
            case "triangle":
                shapePath.moveTo(x, y - size / 2);
                shapePath.lineTo(x - size / 2, y + size / 2);
                shapePath.lineTo(x + size / 2, y + size / 2);
                shapePath.close();
                break;
            default:
                return; // Неверный тип фигуры, ничего не делаем
        }

        // Добавляем фигуру в список нарисованных путей
        DrawnPath shapePathObject = new DrawnPath(shapePath, color, pencil);
        drawnPaths.add(canvasIndex, new Stack<DrawnPath>());
        undonePaths.add(canvasIndex, new Stack<DrawnPath>());
        drawnPaths.get(canvasIndex).push(shapePathObject);
        invalidate();
    }
    // Метод интерполяции между двумя Path
    private Stack<DrawnPath> interpolatePaths(Stack<DrawnPath> pathStack1, Stack<DrawnPath> pathStack2, float interpolationFactor) {
        Stack<DrawnPath> interpolatedStack = new Stack<>();

        int size = Math.min(pathStack1.size(), pathStack2.size());
        for (int i = 0; i < size; i++) {
            DrawnPath dp1 = pathStack1.get(i);
            DrawnPath dp2 = pathStack2.get(i);

            Path interpolatedPath = interpolatePath(dp1.path, dp2.path, interpolationFactor);

            int interpolatedColor = interpolateColor(dp1.color, dp2.color, interpolationFactor);
            float interpolatedWidth = dp1.width + interpolationFactor * (dp2.width - dp1.width);
            boolean isErase = dp1.isErase && dp2.isErase;

            interpolatedStack.push(new DrawnPath(interpolatedPath, interpolatedColor, pencil));
            interpolatedStack.peek().isErase = isErase;
        }
        return interpolatedStack;
    }

    // Метод интерполяции для одного Path
    private Path interpolatePath(Path path1, Path path2, float interpolationFactor) {
        Path resultPath = new Path();
        PathMeasure measure1 = new PathMeasure(path1, false);
        PathMeasure measure2 = new PathMeasure(path2, false);

        float length1 = measure1.getLength();
        float length2 = measure2.getLength();
        float maxLength = Math.max(length1, length2);

        float[] pos1 = new float[2];
        float[] pos2 = new float[2];

        // Проверяем начальную позицию, чтобы избежать линии из угла
        if (length1 > 0) {
            measure1.getPosTan(0, pos1, null);
        }
        if (length2 > 0) {
            measure2.getPosTan(0, pos2, null);
        }

        resultPath.moveTo(pos1[0] + interpolationFactor * (pos2[0] - pos1[0]),
                pos1[1] + interpolationFactor * (pos2[1] - pos1[1]));

        for (float distance = 0; distance <= maxLength; distance += maxLength / 100) {
            if (distance <= length1) measure1.getPosTan(distance, pos1, null);
            if (distance <= length2) measure2.getPosTan(distance, pos2, null);

            float x = pos1[0] + interpolationFactor * (pos2[0] - pos1[0]);
            float y = pos1[1] + interpolationFactor * (pos2[1] - pos1[1]);
            resultPath.lineTo(x, y);
        }
        return resultPath;
    }

    // Метод интерполяции для цветов
    private int interpolateColor(int color1, int color2, float factor) {
        int alpha = (int) (Color.alpha(color1) + factor * (Color.alpha(color2) - Color.alpha(color1)));
        int red = (int) (Color.red(color1) + factor * (Color.red(color2) - Color.red(color1)));
        int green = (int) (Color.green(color1) + factor * (Color.green(color2) - Color.green(color1)));
        int blue = (int) (Color.blue(color1) + factor * (Color.blue(color2) - Color.blue(color1)));
        return Color.argb(alpha, red, green, blue);
    }

    public void deleteAll() {
        drawnPaths.clear();
        undonePaths.clear();
        canvasIndex = 0;
        drawnPaths.add(new Stack<DrawnPath>());
        undonePaths.add(new Stack<DrawnPath>());
        invalidate();
    }
    public void copy() {
        drawnPaths.add(drawnPaths.get(canvasIndex));
        undonePaths.add(undonePaths.get(canvasIndex));
        canvasIndex++;
        invalidate();
    }
    public void onDestroy() {
        animateHandler.removeCallbacks(animateRunnable);
    }
}
