package com.fig.camerademo;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.hardware.Camera.open;

public class MainActivity extends Activity implements Camera.PreviewCallback, Camera.FaceDetectionListener, Camera.AutoFocusCallback {
    private static final String TAG = "MainActivity";
    private static final int ROTATE_DEGREE = 90;

    @BindView(R.id.surfaceview)
    SurfaceView mSurfaceview;
    @BindView(R.id.video_frame)
    FrameLayout mVideoFrame;
    @BindView(R.id.detectView)
    DetectView mDetectView;
    @BindView(R.id.btn_change_camera)
    ImageView mBtnChangeCamera;
    @BindView(R.id.btn_snapshot)
    ImageView mBtnSnapshot;
    @BindView(R.id.iv_snapshot)
    ImageView mIvSnapshot;
    @BindView(R.id.iv_delete)
    ImageView mIvDelete;
    @BindView(R.id.view_snapshot)
    ConstraintLayout mViewSnapshot;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private byte[] mShapShotData;//单帧Yuv数据
    public static final int PREVIEW_WIDTH = 1280;
    public static final int PREVIEW_HEIGHT = 720;
    /**
     * 是否是前置摄像头
     */
    private boolean mIsFront = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        surfaceHolder = mSurfaceview.getHolder();
        mSurfaceview.setKeepScreenOn(true);
        initListener();
    }

    /**
     * 相机的一些设置
     */
    private void initCamParams() {
        try {
            Camera.Parameters parameters = camera.getParameters();
            //Camera Preview Callback的YUV420常用数据格式有两种：一个是NV21，一个是YV12。Android一般默认使用YUV_420_SP的格式（NV21）
            parameters.setPreviewFormat(ImageFormat.NV21);//设置回调数据的格式
            parameters.setPreviewSize(PREVIEW_WIDTH, PREVIEW_HEIGHT); //对应手机的height和width
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);//设置持续对焦模式

            camera.setDisplayOrientation(ROTATE_DEGREE);//旋转90度
            camera.setParameters(parameters);//传入参数
            camera.setPreviewDisplay(surfaceHolder);
            camera.setPreviewCallback(this);
            camera.setFaceDetectionListener(this);
            camera.cancelAutoFocus();
            camera.autoFocus(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Camera openSingleCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            return Camera.open(i);
        }
        return null;
    }

    private void initListener() {
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                //surface创建了，绑定holder并开启预览
                try {
                    //打开摄像头，并且旋转90度
                    if (camera != null) {
                        camera.release();
                    }
                    if (Camera.getNumberOfCameras() > 1) {
                        camera = open(Camera.CameraInfo.CAMERA_FACING_FRONT);
//                    mIsFront = false;
                    } else {
                        camera = openSingleCamera();
                    }
                    initCamParams();
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                    camera.startFaceDetection();//一定要在startPreview()后调用
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                camera.release();
            }
        });
    }


    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        //在此获取相机采集的单帧Yuv数据
        mShapShotData = bytes;
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        List<RectF> rects = new ArrayList<>();
        List<Point> leftEyes = new ArrayList<>();
        List<Point> rightEyes = new ArrayList<>();

        if (faces.length == 0) {
            Toast.makeText(this, "没有检测到人脸", Toast.LENGTH_SHORT).show();
            rects.add(new RectF(0f, 0f, 0f, 0f));
            mDetectView.onDetectFace(rects);
        } else {
            //返回的face.rect坐标需做转换
            Matrix matrix = new Matrix();
            boolean mirror = mIsFront;//前置摄像头需做镜像翻转
            matrix.setScale(mirror ? -1 : 1, 1);
            matrix.postRotate(ROTATE_DEGREE);
            matrix.postScale(mSurfaceview.getWidth() / 2000f, mSurfaceview.getHeight() / 2000f);
            matrix.postTranslate(mSurfaceview.getWidth() / 2f, mSurfaceview.getHeight() / 2f);

            for (Camera.Face face : faces) {
                RectF srcRect = new RectF(face.rect);
                RectF dstRect = new RectF(0f, 0f, 0f, 0f);
                matrix.mapRect(dstRect, srcRect);
                rects.add(dstRect);

                leftEyes.add(face.leftEye);
                rightEyes.add(face.rightEye);
            }
            mDetectView.onDetectFace(rects);
            mDetectView.onDetectEyes(leftEyes, rightEyes);
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        Log.i(TAG, "onAutoFocus: " + success);
    }

    @OnClick({R.id.btn_change_camera, R.id.btn_snapshot, R.id.iv_delete})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_change_camera:
                //切换摄像头
                changeCamera();
                break;
            case R.id.btn_snapshot:
                //抓拍
                takePicture();
                break;
            case R.id.iv_delete:
                //取消抓拍图查看
                mViewSnapshot.setVisibility(View.GONE);
                break;
        }
    }

    /**
     * 切换摄像头
     */
    private void changeCamera() {
        try {
            if (Camera.getNumberOfCameras() > 1) {
                drawEmptyFace();
                if (camera != null) {
                    //切换摄像头要释放资源，否则会报错
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.lock();
                    camera.release();
                    camera = null;
                }

                if (!mIsFront) {
                    camera = open(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else {
                    camera = open(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
                mIsFront = !mIsFront;

                initCamParams();
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                camera.startFaceDetection();//一定要在startPreview()后调用
            } else {
                Toast.makeText(this, "没有可切换的摄像头", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void drawEmptyFace() {
        List<RectF> rects = new ArrayList<>();
        rects.add(new RectF(0f, 0f, 0f, 0f));
        mDetectView.onDetectFace(rects);
    }

    /**
     * 抓拍
     */
    private void takePicture() {
        if (mShapShotData != null) {
            Bitmap bitmap = BitmapUtil.convertYuv2Bitmap(mShapShotData, PREVIEW_WIDTH, PREVIEW_HEIGHT);
            if (mIsFront) {
                bitmap = BitmapUtil.rotateBitmap(bitmap, 360 - ROTATE_DEGREE);
                bitmap = BitmapUtil.horMirrorBitmap(bitmap);
            } else {
                bitmap = BitmapUtil.rotateBitmap(bitmap, ROTATE_DEGREE);
            }
            mIvSnapshot.setImageBitmap(bitmap);
            if (mViewSnapshot.getVisibility() != View.VISIBLE) {
                mViewSnapshot.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.stopFaceDetection();
            camera.release();
        }
    }
}
