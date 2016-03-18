package jp.co.cyberagent.android.gpuimage;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.Queue;

import static jp.co.cyberagent.android.gpuimage.util.TextureRotationUtil.TEXTURE_NO_ROTATION;

public class GPUImageRenderer implements Renderer, PreviewCallback {
    public static final int NO_IMAGE = -1;
    static final float CUBE[] = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f,};

    private GPUImageFilter mFilter;
    public final Object mSurfaceChangedWaiter = new Object();
    private int mGLTextureId = NO_IMAGE;
    private SurfaceTexture mSurfaceTexture = null;
    private final FloatBuffer mGLCubeBuffer;
    private final FloatBuffer mGLTextureBuffer;
    private IntBuffer mGLRgbBuffer;

    private int mOutputWidth;
    private int mOutputHeight;
    private int mImageWidth;
    private int mImageHeight;

    private final Queue<Runnable> mRunOnDraw;
    private Rotation mRotation;
    private boolean mFlipHorizontal;
    private boolean mFlipVertical;
    private GPUImage.ScaleType mScaleType = GPUImage.ScaleType.CENTER_CROP;

    public GPUImageRenderer(final GPUImageFilter filter) {
        mFilter = filter;
        mRunOnDraw = new LinkedList<Runnable>();

        //创建渲染区域的缓存
        mGLCubeBuffer = ByteBuffer.allocateDirect(CUBE.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mGLCubeBuffer.put(CUBE).position(0);

        mGLTextureBuffer = ByteBuffer.allocateDirect(TEXTURE_NO_ROTATION.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        setRotation(Rotation.NORMAL, false, false);
    }

    @Override
    public void onSurfaceCreated(final GL10 unused, final EGLConfig config) {
        //清除颜色到
        GLES20.glClearColor(0, 0, 0, 1);
        //禁用裁剪测试
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        mFilter.init();
    }

    @Override
    public void onSurfaceChanged(final GL10 gl, final int width, final int height) {
        mOutputWidth = width;
        mOutputHeight = height;
        //当surfaceview大小改变时适应
        GLES20.glViewport(0, 0, width, height);
        // 将program加入OpenGL ES环境中
        GLES20.glUseProgram(mFilter.getProgram());
        mFilter.onOutputSizeChanged(width, height);
        synchronized (mSurfaceChangedWaiter) {
            mSurfaceChangedWaiter.notifyAll();
        }
    }


    @Override
    public void onDrawFrame(final GL10 gl) {
        //先清除缓存再绘制每帧
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //线程阻塞机制
        synchronized (mRunOnDraw) {
            while (!mRunOnDraw.isEmpty()) {
                mRunOnDraw.poll().run();
            }
        }
        // 绘制surfaceview
        mFilter.onDraw(mGLTextureId, mGLCubeBuffer, mGLTextureBuffer);
        //SurfaceTexture方式渲染时用到
        if (mSurfaceTexture != null) {
            //更新纹理图像为从图像流中提取的最近一帧。
            mSurfaceTexture.updateTexImage();
        }
    }

    /**
     * 通过setPreviewCallback的回调来使用该重构方法来获取视频流，再将视频流渲染成纹理
     *
     * @param data  获取到的视频流数据
     * @param camera
     * @return void
     */
    @Override
    public void onPreviewFrame(final byte[] data, final Camera camera) {
        final Size previewSize = camera.getParameters().getPreviewSize();
        if (mGLRgbBuffer == null) {
            //视频帧缓存
            mGLRgbBuffer = IntBuffer.allocate(previewSize.width * previewSize.height);
        }
        if (mRunOnDraw.isEmpty()) {
            runOnDraw(new Runnable() {
                @Override
                public void run() {
                    //将YUV格式的data转换为RGBA格式的可渲染数据
                    GPUImageNativeLibrary.YUVtoRBGA(data, previewSize.width, previewSize.height, mGLRgbBuffer.array());
                    //生成纹理
                    mGLTextureId = OpenGlUtils.loadTexture(mGLRgbBuffer, previewSize, mGLTextureId);
                    camera.addCallbackBuffer(data);

                    if (mImageWidth != previewSize.width) {
                        mImageWidth = previewSize.width;
                        mImageHeight = previewSize.height;
                        adjustImageScaling();
                    }
                }
            });
        }
    }

    /**
     * 开启camera预览并通过SurfaceTexture将视频帧捕捉作为纹理
     *
     * @param camera
     * @return 空
     */
    public void setUpSurfaceTexture(final Camera camera) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                int[] textures = new int[1];
                GLES20.glGenTextures(1, textures, 0);//创建纹理数组
                //创建SurfaceTexture同时绑定一个纹理ID
                mSurfaceTexture = new SurfaceTexture(textures[0]);
                try {
                    //设置camera的预览帧渲染到mSurfaceTexture
                    camera.setPreviewTexture(mSurfaceTexture);
                    camera.setPreviewCallback(GPUImageRenderer.this);
                    camera.startPreview();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setFilter(final GPUImageFilter filter) {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                final GPUImageFilter oldFilter = mFilter;
                mFilter = filter;
                if (oldFilter != null) {
                    oldFilter.destroy();
                }
                mFilter.init();
                GLES20.glUseProgram(mFilter.getProgram());
                mFilter.onOutputSizeChanged(mOutputWidth, mOutputHeight);
            }
        });
    }

    //清除render 的纹理
    public void deleteImage() {
        runOnDraw(new Runnable() {

            @Override
            public void run() {
                GLES20.glDeleteTextures(1, new int[]{mGLTextureId}, 0);
                mGLTextureId = NO_IMAGE;
            }
        });
    }
    public void setImageBitmap(final Bitmap bitmap) {
        setImageBitmap(bitmap, true);
    }
    //设置处理后的图片作为新的纹理
    public void setImageBitmap(final Bitmap bitmap, final boolean recycle) {
        if (bitmap == null) {
            return;
        }

        runOnDraw(new Runnable() {

            @Override
            public void run() {
                Bitmap resizedBitmap = null;
                if (bitmap.getWidth() % 2 == 1) {
                    resizedBitmap = Bitmap.createBitmap(bitmap.getWidth() + 1, bitmap.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas can = new Canvas(resizedBitmap);
                    can.drawARGB(0x00, 0x00, 0x00, 0x00);
                    can.drawBitmap(bitmap, 0, 0, null);
                } else {
                }

                mGLTextureId = OpenGlUtils.loadTexture(resizedBitmap != null ? resizedBitmap : bitmap, mGLTextureId, recycle);
                if (resizedBitmap != null) {
                    resizedBitmap.recycle();
                }
                mImageWidth = bitmap.getWidth();
                mImageHeight = bitmap.getHeight();
                adjustImageScaling();
            }
        });
    }

    public void setScaleType(GPUImage.ScaleType scaleType) {
        mScaleType = scaleType;
    }

    protected int getFrameWidth() {
        return mOutputWidth;
    }

    protected int getFrameHeight() {
        return mOutputHeight;
    }

    /**
     * camera适应屏幕方向
     *
     * @param
     * @return
     */
    private void adjustImageScaling() {
        float outputWidth = mOutputWidth;
        float outputHeight = mOutputHeight;
        //当竖直拍摄时宽高互换
        if (mRotation == Rotation.ROTATION_270 || mRotation == Rotation.ROTATION_90) {
            outputWidth = mOutputHeight;
            outputHeight = mOutputWidth;
        }

        float ratio1 = outputWidth / mImageWidth;
        float ratio2 = outputHeight / mImageHeight;
        float ratioMin = Math.min(ratio1, ratio2);
        mImageWidth = Math.round(mImageWidth * ratioMin);
        mImageHeight = Math.round(mImageHeight * ratioMin);

        float ratioWidth = 1.0f;
        float ratioHeight = 1.0f;
        if (mImageWidth != outputWidth) {
            ratioWidth = mImageWidth / outputWidth;
        } else if (mImageHeight != outputHeight) {
            ratioHeight = mImageHeight / outputHeight;
        }

        float[] cube = CUBE;
        float[] textureCords = TextureRotationUtil.getRotation(mRotation, mFlipHorizontal, mFlipVertical);
        if (mScaleType == GPUImage.ScaleType.CENTER_CROP) {
            float distHorizontal = (1 / ratioWidth - 1) / 2;
            float distVertical = (1 / ratioHeight - 1) / 2;
            textureCords = new float[]{
                    addDistance(textureCords[0], distVertical),
                    addDistance(textureCords[1], distHorizontal),
                    addDistance(textureCords[2], distVertical),
                    addDistance(textureCords[3], distHorizontal),
                    addDistance(textureCords[4], distVertical),
                    addDistance(textureCords[5], distHorizontal),
                    addDistance(textureCords[6], distVertical),
                    addDistance(textureCords[7], distHorizontal),};
        } else {
            cube = new float[]{CUBE[0] * ratioWidth, CUBE[1] * ratioHeight,
                    CUBE[2] * ratioWidth, CUBE[3] * ratioHeight,
                    CUBE[4] * ratioWidth, CUBE[5] * ratioHeight,
                    CUBE[6] * ratioWidth, CUBE[7] * ratioHeight,};
        }

        mGLCubeBuffer.clear();
        mGLCubeBuffer.put(cube).position(0);
        mGLTextureBuffer.clear();
        mGLTextureBuffer.put(textureCords).position(0);
    }

    private float addDistance(float coordinate, float distance) {
        return coordinate == 0.0f ? distance : 1 - distance;
    }

    public void setRotationCamera(final Rotation rotation, final boolean flipHorizontal, final boolean flipVertical) {
        setRotation(rotation, flipVertical, flipHorizontal);
    }

    public void setRotation(final Rotation rotation, final boolean flipHorizontal, final boolean flipVertical) {
        mRotation = rotation;
        mFlipHorizontal = flipHorizontal;
        mFlipVertical = flipVertical;
        adjustImageScaling();
    }

    public boolean isFlippedHorizontally() {
        return mFlipHorizontal;
    }

    public boolean isFlippedVertically() {
        return mFlipVertical;
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.add(runnable);
        }
    }
}

