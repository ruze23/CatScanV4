package com.example.catscanv4;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.renderscript.Element;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Detector {
    private final Context context;
    private final String modelPath;
    private final String labelPath;

    private final List<String> labels = new ArrayList<String>();
    private final ImageProcessor imageProcessor;
    private Interpreter interpreter;

    private int tensorWidth;
    private int tensorHeight;
    private int numChannel;
    private int numElements;
    private Bitmap resizedBitmap;

    private static final float INPUT_MEAN = 0f;
    private static final float INPUT_STANDARD_DEVIATION = 255f;
    private static final DataType INPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static final DataType OUTPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static final float CONFIDENCE_THRESHOLD = 0.5F;
    private static final float IOU_THRESHOLD = 0.5F;

    private final DetectorListener detectorListener;
    private final OnModelSetupListener callBack;


    public Detector(Context context, String modelPath, String labelPath, DetectorListener detectorListener, OnModelSetupListener callBack){
        this.context = context;
        this.modelPath = modelPath;
        this.labelPath = labelPath;
        this.detectorListener = detectorListener;
        this.callBack = callBack;

        this.imageProcessor = new ImageProcessor.Builder()
                .add(new NormalizeOp(INPUT_MEAN,INPUT_STANDARD_DEVIATION))
                .add(new CastOp(INPUT_IMAGE_TYPE))
                .build();
    }
    private void setUp(){
        try{
            MappedByteBuffer model = FileUtil.loadMappedFile(context,modelPath);
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            interpreter = new Interpreter(model, options);

            int[] inputShape = interpreter.getInputTensor(0).shape();
            int[] outputShape = interpreter.getOutputTensor(0).shape();

            tensorWidth = inputShape[1];
            tensorHeight = inputShape[2];
            numChannel = outputShape[1];
            numElements = outputShape[2];

            try{
                InputStream inputStream = context.getAssets().open(labelPath);
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = reader.readLine();
                while(line != null && line != ""){
                    labels.add(line);
                    line = reader.readLine();
                }
                Log.i("Onload", "Model Loaded");
                reader.close();
                inputStream.close();

            }catch (Exception e){
                e.printStackTrace();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void SetupAsync(){
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() ->{
            try{
                Log.i("Onload", "Loading Model...");
                setUp();

                mainHandler.post(() ->{
                    if(callBack != null){
                        callBack.OnModelSetupSuccess();
                    }
                });
            }catch (Exception e){
                mainHandler.post(() ->{
                    if(callBack != null){
                        callBack.OnModelSetupFailed(e);
                    }
                });
            } finally {
                executor.shutdown();
            }
        });
    }

    public Bitmap detect(Bitmap frame){
        if(interpreter == null) return frame;
        if(tensorWidth == 0 || tensorHeight == 0 || numElements ==0 ||  numChannel == 0) return frame;

        Long inferenceTime = SystemClock.uptimeMillis();

        if(resizedBitmap == null){
            resizedBitmap = Bitmap.createBitmap(tensorWidth,tensorHeight,Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(resizedBitmap);
        Paint paint = new Paint();

        canvas.drawBitmap(frame,null, new Rect(0,0,tensorWidth,tensorHeight), paint);

        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(resizedBitmap);
        TensorImage processedImage = imageProcessor.process(tensorImage);
        ByteBuffer imageBuffer = processedImage.getBuffer();

        TensorBuffer output = TensorBuffer.createFixedSize(new int[]{1, numChannel, numElements},OUTPUT_IMAGE_TYPE);
        interpreter.run(imageBuffer,output.getBuffer());

        List<BoundingBox> bestBoxes = bestBox(output.getFloatArray());
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;

        if(bestBoxes == null){
            detectorListener.onEmptyDetect();
            return frame;
        }
        detectorListener.onDetect(bestBoxes);

        return frame;
    }

    private List<BoundingBox> bestBox(float[] floatArray){
        List<BoundingBox> boundingBoxes = new ArrayList<>();

        for(int i = 0; i < numElements; i++){
            float maxConf = -1.0f;
            int maxIdx = -1;
            int j = 4;
            int arrayIdx = i + numElements * j;

            while(j < numChannel){
                if(floatArray[arrayIdx] > maxConf){
                    maxConf = floatArray[arrayIdx];
                    maxIdx = j - 4;
                }
                j++;
                arrayIdx += numElements;
            }

            if(maxConf > CONFIDENCE_THRESHOLD){
                String clsName = labels.get(maxIdx);
                float cx = floatArray[i];
                float cy = floatArray[i + numElements];
                float w = floatArray[i + numElements * 2];
                float h = floatArray[i + numElements * 3];
                float x1 = cx - (w/2F);
                float y1 = cy - (w/2F);
                float x2 = cx + (w/2F);
                float y2 = cy + (w/2F);

                if (x1 < 0F || x1 > 1F) continue;
                if (y1 < 0F || y1 > 1F) continue;
                if (x2 < 0F || x2 > 1F) continue;
                if (y2 < 0F || y2 > 1F) continue;

                boundingBoxes.add
                        (new BoundingBox
                                (x1,y1,x2,y2,cx,cy,w,h,maxConf,maxIdx,clsName));
            }
        }
        if(boundingBoxes.isEmpty()) return null;
        return applyNMS(boundingBoxes);
    }

    private List<BoundingBox> applyNMS(List<BoundingBox> boxes){
        List<BoundingBox> sortedBoxes = new ArrayList<>(boxes);
        sortedBoxes.sort((b1, b2) -> Float.compare(b2.getCnf(), b1.getCnf()));

        List<BoundingBox> selectedBoxes = new ArrayList<>();

        while(!sortedBoxes.isEmpty()){
            BoundingBox first = sortedBoxes.get(0);
            selectedBoxes.add(first);

            Iterator<BoundingBox> iterator = sortedBoxes.listIterator(1);
            while(iterator.hasNext()){
                BoundingBox nextBox = iterator.next();
                float iou = calculateIOU(first, nextBox);
                if (iou > IOU_THRESHOLD){
                    iterator.remove();
                }
            }
            sortedBoxes.remove(0);
        }
        return selectedBoxes;
    }

    private float calculateIOU(BoundingBox b1, BoundingBox b2){
        float x1 = Math.max(b1.getX1(), b2.getX1());
        float y1 = Math.max(b1.getY1(), b2.getY1());
        float x2 = Math.min(b1.getX2(), b2.getX2());
        float y2 = Math.min(b1.getY2(), b2.getY2());

        float intersectionArea = Math.max(0F, x2-x1) * Math.max(0F, y2-y1);

        float box1Area = b1.getW() * b1.getH();
        float box2Area = b2.getW() * b2.getH();

        return intersectionArea / (box1Area + box2Area - intersectionArea);
    }

    public interface DetectorListener{
        public void onDetect(List<BoundingBox> boundingBoxes);
        public void onEmptyDetect();
    }
    public interface OnModelSetupListener{
        public void OnModelSetupSuccess();
        public void OnModelSetupFailed(Exception exception);
    }
}

