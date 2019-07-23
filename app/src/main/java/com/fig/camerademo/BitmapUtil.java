package com.fig.camerademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;

import java.io.ByteArrayOutputStream;

public class BitmapUtil {

    /**
     * Yuv数据转比他们bitmap
     *
     * @param yuv    yuv数据
     * @param width  预览宽
     * @param height 预览高
     * @return
     */
    public static Bitmap convertYuv2Bitmap(byte[] yuv, int width, int height) {
        //yuv数据转YuvImage
        YuvImage yuvImage = new YuvImage(yuv, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //YuvImage 转换成jpg格式
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);// 80--JPG图片的质量[0-100],100最高
        byte[] imageBytes = baos.toByteArray();

        //将mImageBytes转换成bitmap
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length, options);
    }

    /**
     * 顺时针旋转bitmap
     */
    public static Bitmap rotateBitmap(Bitmap bitmap, int degress) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postRotate(degress);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }

    /**
     * 水平翻转bitmap
     *
     * @param bitmap
     * @return
     */
    public static Bitmap horMirrorBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postScale(-1, 1);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }

    /**
     * 垂直翻转bitmap
     *
     * @param bitmap
     * @return
     */
    public static Bitmap verMirrorBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            Matrix m = new Matrix();
            m.postScale(1, -1);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
            return bitmap;
        }
        return bitmap;
    }
}
