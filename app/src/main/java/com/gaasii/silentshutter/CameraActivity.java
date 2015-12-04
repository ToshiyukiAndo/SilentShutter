package com.gaasii.silentshutter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.graphics.BitmapCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.graphics.*;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.List;
import mylibs.libs;

public class CameraActivity extends Activity implements Camera.AutoFocusCallback {
    private Camera myCamera;//カメラ本体
    public boolean inPregress_;//オートフォーカスが起動しているかどうか
    static private final int PREVIEW_WIDTH = 640;
    static private final int PREVIEW_HEIGHT = 480;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //魔法の呪文
        super.onCreate(savedInstanceState);
        //activity_cameraをレイアウトにセットしています。
        setContentView(R.layout.activity_camera);
        //SurfaceView（さっきxmlで宣言したもの）をセットできるようにしています。
        SurfaceView mySurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        SurfaceHolder holder = mySurfaceView.getHolder();
        holder.addCallback(mSurfaceListener);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }

    private SurfaceHolder.Callback mSurfaceListener =
            new SurfaceHolder.Callback() {
                public void surfaceCreated(SurfaceHolder holder) {
                    // TODO Auto-generated method stub
                    myCamera = Camera.open();
                    try {
                        myCamera.setPreviewDisplay(holder);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                public void surfaceDestroyed(SurfaceHolder holder) {
                    // TODO Auto-generated method stub
                    myCamera.release();
                    myCamera = null;
                }

                public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                           int height) {
                    // TODO Auto-generated method stub
                    setPreviewSize(width, height);
                    myCamera.startPreview();

                    //setCameraAutoFocus();

                }

                public void setPreviewSize(int width, int height) {
                    Camera.Parameters params = myCamera.getParameters();
                    List<Camera.Size> supported = params.getSupportedPreviewSizes();
                    if (supported != null) {
                        for (Camera.Size size : supported) {
                            if (size.width <= width && size.height <= height) {
                                params.setPreviewSize(size.width, size.height);
                                myCamera.setParameters(params);
                                break;
                            }
                        }
                    }
                }
            };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (myCamera != null) {
                //タッチでシャッターがきれないように
                //myCamera.takePicture(shutterListener_, null, pictureListener_);
                myCamera.autoFocus(autoFocusListener_);
            }
        }
        return true;
    }

    private Camera.ShutterCallback shutterListener_ = new Camera.ShutterCallback() {
        public void onShutter() {

        }
    };

    //画像が生成された時のリスナー
    private Camera.PictureCallback pictureListener_ = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {

            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;

            BitmapFactory.decodeByteArray(data, 0, data.length, options);

            if (options.outWidth > 200) {
                int scale = options.outWidth / 200 + 1;
                options.inSampleSize = scale;
            }

            options.inJustDecodeBounds = false;

            Bitmap bit = BitmapFactory.decodeByteArray(data, 0, data.length, options);

            libs.saveBitmapToSd(bit);

            camera.startPreview();
            inPregress_ = false;
        }
    };

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        myCamera.takePicture(shutterListener_, null, pictureListener_);
    }

    // AF完了時のコールバック
    private Camera.AutoFocusCallback autoFocusListener_ = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            inPregress_ = true; // 処理中フラグ
            camera.autoFocus(null);
            //myCamera.takePicture(shutterListener_, null, pictureListener_);
            myCamera.setPreviewCallback(previewCallback_);
        }
    };

    private final Camera.PreviewCallback previewCallback_ = new Camera.PreviewCallback() {

        public void onPreviewFrame(byte[] data, Camera camera) {
            // 読み込む範囲

            myCamera.setPreviewCallback(null);

            int previewWidth = camera.getParameters().getPreviewSize().width;
            int previewHeight = camera.getParameters().getPreviewSize().height;

            // プレビューデータから Bitmap を生成
            Bitmap bmp = getBitmapImageFromYUV(data, previewWidth, previewHeight);
            // あとはBitmapを好きに使う。

            libs.saveBitmapToSd(bmp);

            camera.startPreview();
            inPregress_ = false;

        }

        public Bitmap getBitmapImageFromYUV(byte[] data, int width, int height) {
            YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            yuvimage.compressToJpeg(new Rect(0, 0, width, height), 80, baos);
            byte[] jdata = baos.toByteArray();
            BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
            bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);
            return bmp;
        }
    };
}
