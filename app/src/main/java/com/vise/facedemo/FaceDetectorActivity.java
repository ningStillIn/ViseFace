package com.vise.facedemo;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import com.vise.face.CameraPreview;
import com.vise.face.DetectorData;
import com.vise.face.DetectorProxy;
import com.vise.face.FaceRectView;
import com.vise.face.ICameraCheckListener;
import com.vise.face.IDataListener;
import com.vise.face.IFaceDetector;
import com.vise.face.NormalFaceDetector;
import com.vise.log.ViseLog;

/**
 * @Description: 拍照测肤界面
 * @author: <a href="http://xiaoyaoyou1212.360doc.com">DAWI</a>
 * @date: 2017/8/15 11:12
 */
public class FaceDetectorActivity extends Activity {

    private CameraPreview mFace_detector_preview;
    private FaceRectView mFace_detector_face;
    private Button mFace_detector_take_photo;

    private DetectorProxy mDetectorProxy;
    private IFaceDetector mFaceDetector;

    private ICameraCheckListener mCameraCheckListener = new ICameraCheckListener() {
        @Override
        public void checkPermission(boolean isAllow) {
            ViseLog.i("checkPermission" + isAllow);
        }

        @Override
        public void checkPixels(long pixels, boolean isSupport) {
            ViseLog.i("checkPixels" + pixels);
        }
    };

    private IDataListener mDataListener = new IDataListener() {
        @Override
        public void onDetectorData(DetectorData detectorData) {
            ViseLog.i("识别数据:" + detectorData);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detector);
        init();
    }

    protected void init() {
        mFace_detector_preview = (CameraPreview) findViewById(R.id.face_detector_preview);
        mFace_detector_face = (FaceRectView) findViewById(R.id.face_detector_face);
        mFace_detector_face.setZOrderOnTop(true);
        mFace_detector_face.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mFace_detector_take_photo = (Button) findViewById(R.id.face_detector_take_photo);

        // 点击SurfaceView，切换摄相头
        mFace_detector_preview.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // 只有一个摄相头，不支持切换
                if (Camera.getNumberOfCameras() == 1) {
                    return;
                }
                if (mDetectorProxy == null) {
                    return;
                }
                mDetectorProxy.closeCamera();
                if (Camera.CameraInfo.CAMERA_FACING_FRONT == mDetectorProxy.getCameraId()) {
                    mDetectorProxy.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK);
                } else {
                    mDetectorProxy.setCameraId(Camera.CameraInfo.CAMERA_FACING_FRONT);
                }
                mDetectorProxy.openCamera();
            }
        });

        mFace_detector_take_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFace_detector_preview.getCamera().takePicture(new Camera.ShutterCallback() {
                    @Override
                    public void onShutter() {

                    }
                }, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] bytes, Camera camera) {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                });
            }
        });

        mFaceDetector = new NormalFaceDetector();
        mDetectorProxy = new DetectorProxy.Builder(mFace_detector_preview)
                .setCheckListener(mCameraCheckListener)
                .setFaceDetector(mFaceDetector)
                .setDataListener(mDataListener)
                .setFaceRectView(mFace_detector_face)
                .setDrawFaceRect(true)
                .setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
                .setMaxFacesCount(5)
                .setFaceIsRect(false)
                .setFaceRectColor(Color.rgb(255, 203, 15))
                .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDetectorProxy != null) {
            mDetectorProxy.detector();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDetectorProxy != null) {
            mDetectorProxy.release();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.gc();
    }

}
