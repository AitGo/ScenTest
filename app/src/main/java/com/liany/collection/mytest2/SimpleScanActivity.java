package com.liany.collection.mytest2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;
import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.integratedbiometrics.ibscanultimate.IBScan;
import com.integratedbiometrics.ibscanultimate.IBScanDevice;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerCountState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.FingerQualityState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageData;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.ImageType;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.LedState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.PlatenState;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.RollingData;
import com.integratedbiometrics.ibscanultimate.IBScanDevice.SegmentPosition;
import com.integratedbiometrics.ibscanultimate.IBScanDeviceListener;
import com.integratedbiometrics.ibscanultimate.IBScanException;
import com.integratedbiometrics.ibscanultimate.IBScanListener;


/**
 * Main activity for SimpleScan application. Capture on a single connected
 * scanner can be started and stopped. After an acquisition is complete,
 * long-clicking on the small preview window will allow the image to be e-mailed
 * or show a larger view of the image.
 */
public class SimpleScanActivity extends Activity implements IBScanListener,
        IBScanDeviceListener {
    /* *********************************************************************************************
     * PRIVATE CONSTANTS
     * *********************************************************
     * **********************************
     */
    public String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "easycollect" + File.separator;
    public static final int RESULT_CODE_STARTCAMERA = 1001;
    public int fingerCode = 11;
    public Map<Integer,String> result = new HashMap<>();

    /* The tag used for Android log messages from this app. */
    private static final String TAG = "Simple Scan";

    protected static final int __INVALID_POS__ = -1;

    /* The default value of the status TextView. */
    protected static final String __NFIQ_DEFAULT__ = "0-0-0-0";

    /* The default value of the frame time TextView. */
    protected static final String __NA_DEFAULT__ = "n/a";


    /* The background color of the preview image ImageView. */
    protected static final int PREVIEW_IMAGE_BACKGROUND = Color.LTGRAY;

    /*
     * The background color of a finger quality TextView when the finger is not
     * present.
     */
    protected static final int FINGER_QUALITY_NOT_PRESENT_COLOR = Color.LTGRAY;

    protected final int __TIMER_STATUS_DELAY__ = 500;

    // Capture sequence definitions
    protected final String CAPTURE_SEQ_FLAT_SINGLE_FINGER = "单指平放";
    protected final String CAPTURE_SEQ_ROLL_SINGLE_FINGER = "单指滚动";
//    protected final String CAPTURE_SEQ_2_FLAT_FINGERS = "2 flat fingers";
//    protected final String CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS = "10 single flat fingers";
//    protected final String CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS = "10 single rolled fingers";
//    protected final String CAPTURE_SEQ_4_FLAT_FINGERS = "4 flat fingers";
//    protected final String CAPTURE_SEQ_10_FLAT_WITH_4_FINGER_SCANNER = "10 flat fingers with 4-finger scanner";

    // Beep definitions
    protected final int __BEEP_FAIL__ = 0;
    protected final int __BEEP_SUCCESS__ = 1;
    protected final int __BEEP_OK__ = 2;
    protected final int __BEEP_DEVICE_COMMUNICATION_BREAK__ = 3;

    // LED color definitions
    protected final int __LED_COLOR_NONE__ = 0;
    protected final int __LED_COLOR_GREEN__ = 1;
    protected final int __LED_COLOR_RED__ = 2;
    protected final int __LED_COLOR_YELLOW__ = 3;

    // Key button definitions
    protected final int __LEFT_KEY_BUTTON__ = 1;
    protected final int __RIGHT_KEY_BUTTON__ = 2;

    /* The number of finger segments set in the result image. */
    protected static final int FINGER_SEGMENT_COUNT = 4;

    /* *********************************************************************************************
     * PRIVATE CLASSES
     * ***********************************************************
     * ********************************
     */

    /*
     * This class wraps the data saved by the app for configuration changes.
     */
    protected class AppData {
        /* The usb device currently selected. */
        public int usbDevices = __INVALID_POS__;

        /* The sequence of capture currently selected. */
        public int captureSeq = __INVALID_POS__;

        /* The current contents of the nfiq TextView. */
        public String nfiq = __NFIQ_DEFAULT__;

        /* The current contents of the frame time TextView. */
        public String frameTime = __NA_DEFAULT__;

        /* The current image displayed in the image preview ImageView. */
        public Bitmap imageBitmap = null;

        /* The current background colors of the finger quality TextViews. */
        public int[] fingerQualityColors = new int[] {
                FINGER_QUALITY_NOT_PRESENT_COLOR,
                FINGER_QUALITY_NOT_PRESENT_COLOR,
                FINGER_QUALITY_NOT_PRESENT_COLOR,
                FINGER_QUALITY_NOT_PRESENT_COLOR };

        /* Indicates whether the image preview ImageView can be long-clicked. */
        public boolean imagePreviewImageClickable = false;

        /* The current contents of the overlayText TextView. */
        public String overlayText = "";

        /* The current contents of the overlay color for overlayText TextView. */
        public int overlayColor = PREVIEW_IMAGE_BACKGROUND;

        /* The current contents of the status message TextView. */
        public String statusMessage = __NA_DEFAULT__;
    }

    protected class CaptureInfo {
        String PreCaptureMessage; // to display on fingerprint window
        String PostCaptuerMessage; // to display on fingerprint window
        ImageType ImageType; // capture mode
        int NumberOfFinger; // number of finger count
        String fingerName; // finger name (e.g left thumbs, left index ... )
    };

    /* *********************************************************************************************
     * PRIVATE FIELDS (UI COMPONENTS)
     * ********************************************
     * ***********************************************
     */

    private ImageView iv_easycollect_left_11,iv_easycollect_left_12,iv_easycollect_left_13,iv_easycollect_left_14,iv_easycollect_left_15,
            iv_easycollect_right_16,iv_easycollect_right_17,iv_easycollect_right_18,iv_easycollect_right_19,iv_easycollect_right_20;
    private TextView m_txtStatusMessage;
    private TextView m_tvFigureType;
    private ImageView m_imgPreview;
    private Spinner m_cboUsbDevices;
    private Spinner m_cboCaptureSeq;
    private Button m_btnCaptureStart;
    private Button m_btnCaptureStop;
    private Button m_btnCaptureNext;
    private Dialog m_enlargedDialog;
    private Bitmap m_BitmapImage;

    /* *********************************************************************************************
     * PRIVATE FIELDS
     * ************************************************************
     * *******************************
     */

    /*
     * A handle to the single instance of the IBScan class that will be the
     * primary interface to the library, for operations like getting the number
     * of scanners (getDeviceCount()) and opening scanners (openDeviceAsync()).
     */
    private IBScan m_ibScan;

    /*
     * A handle to the open IBScanDevice (if any) that will be the interface for
     * getting data from the open scanner, including capturing the image
     * (beginCaptureImage(), cancelCaptureImage()), and the type of image being
     * captured.
     */
    private IBScanDevice m_ibScanDevice;

    /*
     * An object that will play a sound when the image capture has completed.
     */
//    private PlaySound m_beeper = new PlaySound();

    /*
     * Information retained to show view.
     */
    private ImageData m_lastResultImage;
    private ImageData[] m_lastSegmentImages = new ImageData[FINGER_SEGMENT_COUNT];

    /*
     * Information retained for orientation changes.
     */
    private AppData m_savedData = new AppData();

    protected int m_nSelectedDevIndex = -1; // /< Index of selected device
    protected boolean m_bInitializing = false; // /< Device initialization is in
    // progress
    protected String m_ImgSaveFolderName = "";
    String m_ImgSaveFolder = ""; // /< Base folder for image saving
    String m_ImgSubFolder = ""; // /< Sub Folder for image sequence
    protected String m_strImageMessage = "";
    protected boolean m_bNeedClearPlaten = false;
    protected boolean m_bBlank = false;

    protected Vector<CaptureInfo> m_vecCaptureSeq = new Vector<CaptureInfo>(); // /<
    // Sequence
    // of
    // capture
    // steps
    protected int m_nCurrentCaptureStep = -1; // /< Current capture step

    protected IBScanDevice.LedState m_LedState;
    protected FingerQualityState[] m_FingerQuality = {
            FingerQualityState.FINGER_NOT_PRESENT,
            FingerQualityState.FINGER_NOT_PRESENT,
            FingerQualityState.FINGER_NOT_PRESENT,
            FingerQualityState.FINGER_NOT_PRESENT };
    protected ImageType m_ImageType;
    protected int m_nSegmentImageArrayCount = 0;
    protected SegmentPosition[] m_SegmentPositionArray;

    protected ArrayList<String> m_arrUsbDevices;
    protected ArrayList<String> m_arrCaptureSeq;

    protected byte[] m_drawBuffer;
    protected double m_scaleFactor;
    protected int m_leftMargin;
    protected int m_topMargin;

    // ////////////////////////////////////////////////////////////////////////////////////////////////////
    // GLobal Varies Definitions
    // ////////////////////////////////////////////////////////////////////////////////////////////////////

    /* *********************************************************************************************
     * INHERITED INTERFACE (Activity OVERRIDES)
     * **********************************
     * *********************************************************
     */

    /*
     * Called when the activity is started.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        m_ibScan = IBScan.getInstance(this.getApplicationContext());
        m_ibScan.setScanListener(this);

        checkSavePermission();

        setContentView(R.layout.activity_easycollect_scan);

        /* Initialize UI fields. */
        _InitUIFields();

        _InitData();
        /*
         * Make sure there are no USB devices attached that are IB scanners for
         * which permission has not been granted. For any that are found,
         * request permission; we should receive a callback when permission is
         * granted or denied and then when IBScan recognizes that new devices
         * are connected, which will result in another refresh.
         */
        final UsbManager manager = (UsbManager) this.getApplicationContext()
                .getSystemService(Context.USB_SERVICE);
        final HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        final Iterator<UsbDevice> deviceIterator = deviceList.values()
                .iterator();
        while (deviceIterator.hasNext()) {
            final UsbDevice device = deviceIterator.next();
            final boolean isScanDevice = IBScan.isScanDevice(device);
            if (isScanDevice) {
                final boolean hasPermission = manager.hasPermission(device);
                if (!hasPermission) {
                    this.m_ibScan.requestPermission(device.getDeviceId());
                }
            }
        }

        OnMsg_UpdateDeviceList(false);

        /* Initialize UI with data. */
        _PopulateUI();

        _TimerTaskThreadCallback thread = new _TimerTaskThreadCallback(
                __TIMER_STATUS_DELAY__);
        thread.start();
    }

    private void _InitData() {
        if(getIntent().getStringExtra("com.liany.easycollect.filePath") != null && !getIntent().getStringExtra("com.liany.easycollect.filePath").equals("")) {
            filePath = getIntent().getStringExtra("com.liany.easycollect.filePath");
        }
        if(getIntent().getSerializableExtra("com.liany.easycollect.resultMap") != null ) {
            result = (Map<Integer, String>) getIntent().getSerializableExtra("com.liany.easycollect.resultMap");
        }
        for (Integer key : result.keySet()) {
            if(key == 11) {
                iv_easycollect_left_11.setVisibility(View.VISIBLE);
            } else if(key == 12) {
                iv_easycollect_left_12.setVisibility(View.VISIBLE);
            } else if(key == 13) {
                iv_easycollect_left_13.setVisibility(View.VISIBLE);
            } else if(key == 14) {
                iv_easycollect_left_14.setVisibility(View.VISIBLE);
            } else if(key == 15) {
                iv_easycollect_left_15.setVisibility(View.VISIBLE);
            } else if(key == 16) {
                iv_easycollect_right_16.setVisibility(View.VISIBLE);
            } else if(key == 17) {
                iv_easycollect_right_17.setVisibility(View.VISIBLE);
            } else if(key == 18) {
                iv_easycollect_right_18.setVisibility(View.VISIBLE);
            } else if(key == 19) {
                iv_easycollect_right_19.setVisibility(View.VISIBLE);
            } else if(key == 20) {
                iv_easycollect_right_20.setVisibility(View.VISIBLE);
            }
        }
    }

    private void checkSavePermission() {
        //是否授权
        if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                || PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            //提示用户打开权限
            String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, perms, RESULT_CODE_STARTCAMERA);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == RESULT_CODE_STARTCAMERA) {
            boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (!cameraAccepted) {
                //用户授权拒绝
                Toast.makeText(this,"请打开存储权限",Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            setContentView(R.layout.activity_easycollect_scan);
        } else {
            setContentView(R.layout.activity_easycollect_scan);
        }

        /* Initialize UI fields for new orientation. */
        _InitUIFields();

        OnMsg_UpdateDeviceList(true);

        /* Populate UI with data from old orientation. */
        _PopulateUI();

    }

    /*
     * Release driver resources.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        for (int i = 0; i < 10; i++) {
            try {
                _ReleaseDevice();
                break;
            } catch (IBScanException ibse) {
                if (ibse.getType().equals(IBScanException.Type.RESOURCE_LOCKED)) {
                } else {
                    break;
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
//        exitApp(this);
        super.onBackPressed();
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
        return null;
    }

    /* *********************************************************************************************
     * PRIVATE METHODS
     * ***********************************************************
     * ********************************
     */

    /*
     * Initialize UI fields for new orientation.
     */
    private void _InitUIFields() {
        m_txtStatusMessage = (TextView) findViewById(R.id.txtStatusMessage);
        m_tvFigureType = findViewById(R.id.txtFigureType);

        m_imgPreview = (ImageView) findViewById(R.id.imgPreview);
        m_imgPreview.setBackgroundColor(PREVIEW_IMAGE_BACKGROUND);

        m_btnCaptureStop = (Button) findViewById(R.id.stop_capture_btn);
        m_btnCaptureStop.setOnClickListener(this.m_btnCaptureStopClickListener);

        m_btnCaptureNext = findViewById(R.id.next_capture_btn);
        m_btnCaptureNext.setOnClickListener(this.m_btnCaptureNextClickListener);

        m_btnCaptureStart = (Button) findViewById(R.id.start_capture_btn);
        m_btnCaptureStart
                .setOnClickListener(this.m_btnCaptureStartClickListener);

        m_cboUsbDevices = (Spinner) findViewById(R.id.spinUsbDevices);
        m_cboCaptureSeq = (Spinner) findViewById(R.id.spinCaptureSeq);

        iv_easycollect_left_11 = findViewById(R.id.iv_easycollect_left_11);
        iv_easycollect_left_12 = findViewById(R.id.iv_easycollect_left_12);
        iv_easycollect_left_13 = findViewById(R.id.iv_easycollect_left_13);
        iv_easycollect_left_14 = findViewById(R.id.iv_easycollect_left_14);
        iv_easycollect_left_15 = findViewById(R.id.iv_easycollect_left_15);
        iv_easycollect_right_16 = findViewById(R.id.iv_easycollect_right_16);
        iv_easycollect_right_17 = findViewById(R.id.iv_easycollect_right_17);
        iv_easycollect_right_18 = findViewById(R.id.iv_easycollect_right_18);
        iv_easycollect_right_19 = findViewById(R.id.iv_easycollect_right_19);
        iv_easycollect_right_20 = findViewById(R.id.iv_easycollect_right_20);

        iv_easycollect_left_11.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                fingerCode = 11;
                m_tvFigureType.setText(GetFPcode(fingerCode));
            }
        });

        m_tvFigureType.setText(GetFPcode(fingerCode));
    }

    /*
     * Populate UI with data from old orientation.
     */
    private void _PopulateUI() {

        if (m_savedData.usbDevices != __INVALID_POS__) {
            m_cboUsbDevices.setSelection(m_savedData.usbDevices);
        }

        if (m_savedData.captureSeq != __INVALID_POS__) {
            m_cboCaptureSeq.setSelection(m_savedData.captureSeq);
        }

        if (m_savedData.imageBitmap != null) {
            m_imgPreview.setImageBitmap(m_savedData.imageBitmap);
        }

        if (m_BitmapImage != null) {
            m_BitmapImage.isRecycled();
        }

        m_imgPreview.setLongClickable(m_savedData.imagePreviewImageClickable);
    }

    // Get IBScan.
    protected IBScan getIBScan() {
        return (this.m_ibScan);
    }

    // Get opened or null IBScanDevice.
    protected IBScanDevice getIBScanDevice() {
        return (this.m_ibScanDevice);
    }

    // Set IBScanDevice.
    protected void setIBScanDevice(IBScanDevice ibScanDevice) {
        m_ibScanDevice = ibScanDevice;
        if (ibScanDevice != null) {
            ibScanDevice.setScanDeviceListener(this);
        }
    }

    /*
     * Set status message text box.
     */
    protected void _SetStatusBarMessage(final String s) {
        /* Make sure this occurs on the UI thread. */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                m_txtStatusMessage.setText(s);
            }
        });
    }

    /*
     * Timer task with using Thread
     */
    class _TimerTaskThreadCallback extends Thread {
        private int timeInterval;

        _TimerTaskThreadCallback(int timeInterval) {
            this.timeInterval = timeInterval;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                if (getIBScanDevice() != null) {
                    OnMsg_DrawFingerQuality();

                    if (m_bNeedClearPlaten)
                        m_bBlank = !m_bBlank;
                }

                _Sleep(timeInterval);

                try {
                    Thread.sleep(timeInterval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * Initialize Device with using Thread
     */
    class _InitializeDeviceThreadCallback extends Thread {
        private int devIndex;

        _InitializeDeviceThreadCallback(int devIndex) {
            this.devIndex = devIndex;
        }

        @Override
        public void run() {
            try {
                m_bInitializing = true;
                IBScanDevice ibScanDeviceNew = getIBScan().openDevice(
                        this.devIndex);
                setIBScanDevice(ibScanDeviceNew);
                m_bInitializing = false;

                if (ibScanDeviceNew != null) {

                    int outWidth = m_imgPreview.getWidth() - 20;
                    int outHeight = m_imgPreview.getHeight() - 20;
                    m_BitmapImage = Bitmap.createBitmap(outWidth, outHeight,
                            Bitmap.Config.ARGB_8888);
                    m_drawBuffer = new byte[outWidth * outHeight * 4];

                    m_LedState = getIBScanDevice().getOperableLEDs();

                    OnMsg_CaptureSeqStart();
                }
            } catch (IBScanException ibse) {
                m_bInitializing = false;

                if (ibse.getType().equals(IBScanException.Type.DEVICE_ACTIVE)) {
                    _SetStatusBarMessage("[Error Code =-203] 设备初始化失败，正在被另一个线程使用.");
                } else if (ibse.getType().equals(
                        IBScanException.Type.USB20_REQUIRED)) {
                    _SetStatusBarMessage("[Error Code =-209] 设备初始化失败，请使用usb2.0.");
                } else {
                    _SetStatusBarMessage("设备初始化失败.");
                }

                OnMsg_UpdateDisplayResources();
            }
        }
    }

    protected Bitmap _CreateBitmap(int width, int height) {
        final Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        if (bitmap != null) {
            final byte[] imageBuffer = new byte[width * height * 4];
            /*
             * The image in the buffer is flipped vertically from what the
             * Bitmap class expects; we will flip it to compensate while moving
             * it into the buffer.
             */
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    imageBuffer[(y * width + x) * 4] = imageBuffer[(y * width + x) * 4 + 1] = imageBuffer[(y
                            * width + x) * 4 + 2] = (byte) 128;
                    imageBuffer[(y * width + x) * 4 + 3] = (byte) 255;
                }
            }
            bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(imageBuffer));
        }
        return (bitmap);
    }

    protected void _CalculateScaleFactors(ImageData image, int outWidth,
                                          int outHeight) {
        int left = 0, top = 0;
        int tmp_width = outWidth;
        int tmp_height = outHeight;
        int imgWidth = image.width;
        int imgHeight = image.height;
        int dispWidth, dispHeight, dispImgX, dispImgY;

        if (outWidth > imgWidth) {
            tmp_width = imgWidth;
            left = (outWidth - imgWidth) / 2;
        }
        if (outHeight > imgHeight) {
            tmp_height = imgHeight;
            top = (outHeight - imgHeight) / 2;
        }

        float ratio_width = (float) tmp_width / (float) imgWidth;
        float ratio_height = (float) tmp_height / (float) imgHeight;

        dispWidth = outWidth;
        dispHeight = outHeight;

        if (ratio_width >= ratio_height) {
            dispWidth = tmp_height * imgWidth / imgHeight;
            dispWidth -= (dispWidth % 4);
            dispHeight = tmp_height;
            dispImgX = (tmp_width - dispWidth) / 2 + left;
            dispImgY = top;
        } else {
            dispWidth = tmp_width;
            dispWidth -= (dispWidth % 4);
            dispHeight = tmp_width * imgHeight / imgWidth;
            dispImgX = left;
            dispImgY = (tmp_height - dispHeight) / 2 + top;
        }

        if (dispImgX < 0) {
            dispImgX = 0;
        }
        if (dispImgY < 0) {
            dispImgY = 0;
        }

        // /////////////////////////////////////////////////////////////////////////////////
        m_scaleFactor = (double) dispWidth / image.width;
        m_leftMargin = dispImgX;
        m_topMargin = dispImgY;
        // /////////////////////////////////////////////////////////////////////////////////
    }

    protected void _DrawOverlay_WarningOfClearPlaten(Canvas canvas, int left,
                                                     int top, int width, int height) {
        if (getIBScanDevice() == null)
            return;

        boolean idle = !m_bInitializing && (m_nCurrentCaptureStep == -1);

        if (!idle && m_bNeedClearPlaten && m_bBlank) {
            Paint g = new Paint();
            g.setStyle(Paint.Style.STROKE);
            g.setColor(Color.RED);
            // g.setStrokeWidth(10);
            g.setStrokeWidth(20);
            g.setAntiAlias(true);
            canvas.drawRect(left, top, width - 1, height - 1, g);
        }
    }

    protected void _DrawOverlay_ResultSegmentImage(Canvas canvas,
                                                   ImageData image, int outWidth, int outHeight) {
        if (image.isFinal) {
            // if (m_chkDrawSegmentImage.isSelected())
            {
                // Draw quadrangle for the segment image

                _CalculateScaleFactors(image, outWidth, outHeight);
                Paint g = new Paint();
                g.setColor(Color.rgb(0, 128, 0));
                // g.setStrokeWidth(1);
                g.setStrokeWidth(4);
                g.setAntiAlias(true);
                for (int i = 0; i < m_nSegmentImageArrayCount; i++) {
                    int x1, x2, x3, x4, y1, y2, y3, y4;
                    x1 = m_leftMargin
                            + (int) (m_SegmentPositionArray[i].x1 * m_scaleFactor);
                    x2 = m_leftMargin
                            + (int) (m_SegmentPositionArray[i].x2 * m_scaleFactor);
                    x3 = m_leftMargin
                            + (int) (m_SegmentPositionArray[i].x3 * m_scaleFactor);
                    x4 = m_leftMargin
                            + (int) (m_SegmentPositionArray[i].x4 * m_scaleFactor);
                    y1 = m_topMargin
                            + (int) (m_SegmentPositionArray[i].y1 * m_scaleFactor);
                    y2 = m_topMargin
                            + (int) (m_SegmentPositionArray[i].y2 * m_scaleFactor);
                    y3 = m_topMargin
                            + (int) (m_SegmentPositionArray[i].y3 * m_scaleFactor);
                    y4 = m_topMargin
                            + (int) (m_SegmentPositionArray[i].y4 * m_scaleFactor);

                    canvas.drawLine(x1, y1, x2, y2, g);
                    canvas.drawLine(x2, y2, x3, y3, g);
                    canvas.drawLine(x3, y3, x4, y4, g);
                    canvas.drawLine(x4, y4, x1, y1, g);
                }
            }
        }
    }

    protected void _DrawOverlay_RollGuideLine(Canvas canvas, ImageData image,
                                              int width, int height) {
        if (getIBScanDevice() == null || m_nCurrentCaptureStep == -1)
            return;

        if (m_ImageType == IBScanDevice.ImageType.ROLL_SINGLE_FINGER) {
            Paint g = new Paint();
            RollingData rollingdata;
            g.setAntiAlias(true);
            try {
                rollingdata = getIBScanDevice().getRollingInfo();

            } catch (IBScanException e) {
                rollingdata = null;
            }

            if ((rollingdata != null)
                    && rollingdata.rollingLineX > 0
                    && (rollingdata.rollingState
                    .equals(IBScanDevice.RollingState.TAKE_ACQUISITION) || rollingdata.rollingState
                    .equals(IBScanDevice.RollingState.COMPLETE_ACQUISITION))) {
                _CalculateScaleFactors(image, width, height);
                int LineX = m_leftMargin
                        + (int) (rollingdata.rollingLineX * m_scaleFactor);

                // Guide line for rolling
                if (rollingdata.rollingState
                        .equals(IBScanDevice.RollingState.TAKE_ACQUISITION))
                    g.setColor(Color.RED);
                else if (rollingdata.rollingState
                        .equals(IBScanDevice.RollingState.COMPLETE_ACQUISITION))
                    g.setColor(Color.GREEN);

                if (rollingdata.rollingLineX > -1) {
                    // g.setStrokeWidth(2);
                    g.setStrokeWidth(4);
                    canvas.drawLine(LineX, 0, LineX, height, g);
                }
            }
        }
    }

    protected void _BeepFail() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice()
                    .getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(
                        IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC,
                        2/* Sol */, 12/* 300ms = 12*25ms */, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(
                        IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC,
                        2/* Sol */, 6/* 150ms = 6*25ms */, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(
                        IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC,
                        2/* Sol */, 6/* 150ms = 6*25ms */, 0, 0);
                _Sleep(150);
                getIBScanDevice().setBeeper(
                        IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC,
                        2/* Sol */, 6/* 150ms = 6*25ms */, 0, 0);
            }
        } catch (IBScanException ibse) {
            // devices for without beep chip
            // send the tone to the "alarm" stream (classic beeps go there) with
            // 30% volume
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM,
                    30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300); // 300
            // is
            // duration
            // in
            // ms
            _Sleep(300 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150
            // is
            // duration
            // in
            // ms
            _Sleep(150 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150
            // is
            // duration
            // in
            // ms
            _Sleep(150 + 150);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 150); // 150
            // is
            // duration
            // in
            // ms
        }
    }

    protected void _BeepSuccess() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice()
                    .getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(
                        IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC,
                        2/* Sol */, 4/* 100ms = 4*25ms */, 0, 0);
                _Sleep(50);
                getIBScanDevice().setBeeper(
                        IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC,
                        2/* Sol */, 4/* 100ms = 4*25ms */, 0, 0);
            }
        } catch (IBScanException ibse) {
            // devices for without beep chip
            // send the tone to the "alarm" stream (classic beeps go there) with
            // 30% volume
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM,
                    30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100
            // is
            // duration
            // in
            // ms
            _Sleep(100 + 50);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100
            // is
            // duration
            // in
            // ms
        }
    }

    protected void _BeepOk() {
        try {
            IBScanDevice.BeeperType beeperType = getIBScanDevice()
                    .getOperableBeeper();
            if (beeperType != IBScanDevice.BeeperType.BEEPER_TYPE_NONE) {
                getIBScanDevice().setBeeper(
                        IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC,
                        2/* Sol */, 4/* 100ms = 4*25ms */, 0, 0);
            }
        } catch (IBScanException ibse) {
            // devices for without beep chip
            // send the tone to the "alarm" stream (classic beeps go there) with
            // 30% volume
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM,
                    30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100
            // is
            // duration
            // in
            // ms
        }
    }

    protected void _BeepDeviceCommunicationBreak() {
        for (int i = 0; i < 8; i++) {
            // send the tone to the "alarm" stream (classic beeps go there) with
            // 30% volume
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM,
                    30);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100); // 100
            // is
            // duration
            // in
            // ms
            _Sleep(100 + 100);
        }
    }

    protected void _Sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
        }
    }

    protected void _SetImageMessage(String s) {
        m_strImageMessage = s;
    }

    protected void _AddCaptureSeqVector(String PreCaptureMessage,
                                        String PostCaptuerMessage, IBScanDevice.ImageType imageType,
                                        int NumberOfFinger, String fingerName) {
        CaptureInfo info = new CaptureInfo();
        info.PreCaptureMessage = PreCaptureMessage;
        info.PostCaptuerMessage = PostCaptuerMessage;
        info.ImageType = imageType;
        info.NumberOfFinger = NumberOfFinger;
        info.fingerName = fingerName;
        m_vecCaptureSeq.addElement(info);
    }

    protected void _UpdateCaptureSequences() {
        try {
            // store currently selected device
            String strSelectedText = "";
            int selectedSeq = m_cboCaptureSeq.getSelectedItemPosition();
            if (selectedSeq > -1)
                strSelectedText = m_cboCaptureSeq.getSelectedItem().toString();

            // populate combo box
            m_arrCaptureSeq = new ArrayList<String>();

            m_arrCaptureSeq.add("- 请选择 -");
            final int devIndex = this.m_cboUsbDevices.getSelectedItemPosition() - 1;
            if (devIndex > -1) {
                IBScan.DeviceDesc devDesc = getIBScan().getDeviceDescription(
                        devIndex);
                if ((devDesc.productName.equals("WATSON"))
                        || (devDesc.productName.equals("WATSON MINI"))
                        || (devDesc.productName.equals("SHERLOCK_ROIC"))
                        || (devDesc.productName.equals("SHERLOCK"))) {
                    m_arrCaptureSeq.add(CAPTURE_SEQ_FLAT_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_ROLL_SINGLE_FINGER);
                } else if ((devDesc.productName.equals("COLUMBO"))
                        || (devDesc.productName.equals("CURVE"))) {
                    m_arrCaptureSeq.add(CAPTURE_SEQ_FLAT_SINGLE_FINGER);
//                    m_arrCaptureSeq.add(CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS);
                } else if ((devDesc.productName.equals("HOLMES"))
                        || (devDesc.productName.equals("KOJAK"))
                        || (devDesc.productName.equals("FIVE-0"))) {
                    m_arrCaptureSeq.add(CAPTURE_SEQ_FLAT_SINGLE_FINGER);
                    m_arrCaptureSeq.add(CAPTURE_SEQ_ROLL_SINGLE_FINGER);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                    R.layout.spinner_text_layout, m_arrCaptureSeq);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            m_cboCaptureSeq.setAdapter(adapter);
            m_cboCaptureSeq
                    .setOnItemSelectedListener(m_captureTypeItemSelectedListener);

            // if (selectedSeq > -1)
            // this.m_cboCaptureSeq.setse(strSelectedText);

            OnMsg_UpdateDisplayResources();
        } catch (IBScanException e) {
            e.printStackTrace();
        }
    }

    protected void _ReleaseDevice() throws IBScanException {
        if (getIBScanDevice() != null) {
            if (getIBScanDevice().isOpened() == true) {
                getIBScanDevice().close();
                setIBScanDevice(null);
            }
        }

        m_nCurrentCaptureStep = -1;
        m_bInitializing = false;
    }

    protected void _SaveBitmapImage(ImageData image, String fingerName) {
        /*
         * String filename = m_ImgSaveFolderName + ".bmp";
         *
         * try { image.saveToFile(filename, "BMP"); } catch(IOException e) {
         * e.printStackTrace(); }
         */}

    protected void _SaveWsqImage(ImageData image, String fingerName) {
        String filename = m_ImgSaveFolderName + ".wsq";

        try {
            getIBScanDevice().wsqEncodeToFile(filename, image.buffer,
                    image.width, image.height, image.pitch, image.bitsPerPixel,
                    500, 0.75, "");
        } catch (IBScanException e) {
            e.printStackTrace();
        }
    }

    protected void _SavePngImage(ImageData image, String fingerName) {
//        String filename = m_ImgSaveFolderName + ".png";
        String filename = fingerName + ".png";
        File file = new File(filename);
        FileOutputStream filestream = null;
        try {
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            filestream = new FileOutputStream(file);
            final Bitmap bitmap = image.toBitmap();
            bitmap.compress(CompressFormat.PNG, 100, filestream);
            result.put(fingerCode,filename);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(result.get(fingerCode) != null && !result.get(fingerCode).equals("")) {
                        if(fingerCode == 11) {
                            iv_easycollect_left_11.setVisibility(View.VISIBLE);
                        } else if(fingerCode == 12) {
                            iv_easycollect_left_12.setVisibility(View.VISIBLE);
                        } else if(fingerCode == 13) {
                            iv_easycollect_left_13.setVisibility(View.VISIBLE);
                        } else if(fingerCode == 14) {
                            iv_easycollect_left_14.setVisibility(View.VISIBLE);
                        } else if(fingerCode == 15) {
                            iv_easycollect_left_15.setVisibility(View.VISIBLE);
                        } else if(fingerCode == 16) {
                            iv_easycollect_right_16.setVisibility(View.VISIBLE);
                        } else if(fingerCode == 17) {
                            iv_easycollect_right_17.setVisibility(View.VISIBLE);
                        } else if(fingerCode == 18) {
                            iv_easycollect_right_18.setVisibility(View.VISIBLE);
                        } else if(fingerCode == 19) {
                            iv_easycollect_right_19.setVisibility(View.VISIBLE);
                        } else if(fingerCode == 20) {
                            iv_easycollect_right_20.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                filestream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected void _SaveJP2Image(ImageData image, String fingerName) {
        /*
         * String filename = m_ImgSaveFolderName + ".jp2";
         *
         * try { getIBScanDevice().SaveJP2Image(filename, image.buffer,
         * image.width, image.height, image.pitch, image.resolutionX,
         * image.resolutionY , 80); } catch (IBScanException e) {
         * e.printStackTrace(); } catch( StackOverflowError e) {
         * System.out.println("Exception :"+ e); e.printStackTrace(); }
         */
    }

    public void _SetLEDs(CaptureInfo info, int ledColor, boolean bBlink) {
        try {
            LedState ledState = getIBScanDevice().getOperableLEDs();
            if (ledState.ledCount == 0) {
                return;
            }
        } catch (IBScanException ibse) {
            ibse.printStackTrace();
        }

        int setLEDs = 0;

        if (ledColor == __LED_COLOR_NONE__) {
            try {
                getIBScanDevice().setLEDs(setLEDs);
            } catch (IBScanException ibse) {
                ibse.printStackTrace();
            }

            return;
        }

        if (m_LedState.ledType == IBScanDevice.LedType.FSCAN) {
            if (bBlink) {
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_BLINK_RED;
                }
            }

            if (info.ImageType == IBScanDevice.ImageType.ROLL_SINGLE_FINGER) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_ROLL;
            }

            if ((info.fingerName.equals("SFF_Right_Thumb"))
                    || (info.fingerName.equals("SRF_Right_Thumb"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_TWO_THUMB;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Thumb"))
                    || (info.fingerName.equals("SRF_Left_Thumb"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_TWO_THUMB;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                }
            } else if ((info.fingerName.equals("TFF_2_Thumbs"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_TWO_THUMB;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_THUMB_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_THUMB_RED;
                }
            }
            // /////////////////LEFT HAND////////////////////
            else if ((info.fingerName.equals("SFF_Left_Index"))
                    || (info.fingerName.equals("SRF_Left_Index"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Middle"))
                    || (info.fingerName.equals("SRF_Left_Middle"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Ring"))
                    || (info.fingerName.equals("SRF_Left_Ring"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                }
            } else if ((info.fingerName.equals("SFF_Left_Little"))
                    || (info.fingerName.equals("SRF_Left_Little"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                }
            } else if ((info.fingerName.equals("4FF_Left_4_Fingers"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_LEFT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_LEFT_LITTLE_RED;
                }
            }
            // /////////RIGHT HAND /////////////////////////
            else if ((info.fingerName.equals("SFF_Right_Index"))
                    || (info.fingerName.equals("SRF_Right_Index"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                }
            } else if ((info.fingerName.equals("SFF_Right_Middle"))
                    || (info.fingerName.equals("SRF_Right_Middle"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                }
            } else if ((info.fingerName.equals("SFF_Right_Ring"))
                    || (info.fingerName.equals("SRF_Right_Ring"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                }
            } else if ((info.fingerName.equals("SFF_Right_Little"))
                    || (info.fingerName.equals("SRF_Right_Little"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                }
            } else if ((info.fingerName.equals("4FF_Right_4_Fingers"))) {
                setLEDs |= IBScanDevice.IBSU_LED_F_PROGRESS_RIGHT_HAND;
                if (ledColor == __LED_COLOR_GREEN__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                } else if (ledColor == __LED_COLOR_RED__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                } else if (ledColor == __LED_COLOR_YELLOW__) {
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_GREEN;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_INDEX_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_MIDDLE_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_RING_RED;
                    setLEDs |= IBScanDevice.IBSU_LED_F_RIGHT_LITTLE_RED;
                }
            }

            if (ledColor == __LED_COLOR_NONE__) {
                setLEDs = 0;
            }

            try {
                getIBScanDevice().setLEDs(setLEDs);
            } catch (IBScanException ibse) {
                ibse.printStackTrace();
            }
        }
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    // Event-dispatch threads
    private void OnMsg_SetStatusBarMessage(final String s) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                _SetStatusBarMessage(s);
            }
        });
    }



    private void OnMsg_Beep(final int beepType) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (beepType == __BEEP_FAIL__)
                    _BeepFail();
                else if (beepType == __BEEP_SUCCESS__)
                    _BeepSuccess();
                else if (beepType == __BEEP_OK__)
                    _BeepOk();
                else if (beepType == __BEEP_DEVICE_COMMUNICATION_BREAK__)
                    _BeepDeviceCommunicationBreak();
            }
        });
    }

    private void OnMsg_CaptureSeqStart() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (getIBScanDevice() == null) {
                    OnMsg_UpdateDisplayResources();
                    return;
                }

                String strCaptureSeq = "";
                int nSelectedSeq = m_cboCaptureSeq.getSelectedItemPosition();
                if (nSelectedSeq > -1)
                    strCaptureSeq = m_cboCaptureSeq.getSelectedItem()
                            .toString();

                m_vecCaptureSeq.clear();

                /**
                 * Please refer to definition below protected final String
                 * CAPTURE_SEQ_FLAT_SINGLE_FINGER = "Single flat finger";
                 * protected final String CAPTURE_SEQ_ROLL_SINGLE_FINGER =
                 * "Single rolled finger"; protected final String
                 * CAPTURE_SEQ_2_FLAT_FINGERS = "2 flat fingers"; protected
                 * final String CAPTURE_SEQ_10_SINGLE_FLAT_FINGERS =
                 * "10 single flat fingers"; protected final String
                 * CAPTURE_SEQ_10_SINGLE_ROLLED_FINGERS =
                 * "10 single rolled fingers"; protected final String
                 * CAPTURE_SEQ_4_FLAT_FINGERS = "4 flat fingers"; protected
                 * final String CAPTURE_SEQ_10_FLAT_WITH_4_FINGER_SCANNER =
                 * "10 flat fingers with 4-finger scanner";
                 */
                if (strCaptureSeq.equals(CAPTURE_SEQ_FLAT_SINGLE_FINGER)) {
                    _AddCaptureSeqVector(
                            "开始采集",
                            "Keep finger on the sensor!",
                            IBScanDevice.ImageType.FLAT_SINGLE_FINGER, 1,
                            "SFF_Unknown");
                }

                if (strCaptureSeq.equals(CAPTURE_SEQ_ROLL_SINGLE_FINGER)) {
                    _AddCaptureSeqVector(
                            "开始采集",
                            "Roll finger!",
                            IBScanDevice.ImageType.ROLL_SINGLE_FINGER, 1,
                            "SRF_Unknown");
                }

                OnMsg_CaptureSeqNext();
            }
        });
    }

    private void OnMsg_CaptureSeqNext() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (getIBScanDevice() == null)
                    return;

                m_bBlank = false;
                for (int i = 0; i < 4; i++)
                    m_FingerQuality[i] = FingerQualityState.FINGER_NOT_PRESENT;

                m_nCurrentCaptureStep++;
                if (m_nCurrentCaptureStep >= m_vecCaptureSeq.size()) {
                    // All of capture sequence completely
                    CaptureInfo tmpInfo = new CaptureInfo();
                    _SetLEDs(tmpInfo, __LED_COLOR_NONE__, false);
                    m_nCurrentCaptureStep = -1;

                    OnMsg_UpdateDisplayResources();
                    return;
                }

                try {
                    /*
                     * if (m_chkDetectSmear.isSelected()) {
                     * getIBScanDevice().setProperty
                     * (IBScanDevice.PropertyId.ROLL_MODE, "1"); String strValue
                     * = String.valueOf(m_cboSmearLevel.getSelectedIndex());
                     * getIBScanDevice
                     * ().setProperty(IBScanDevice.PropertyId.ROLL_LEVEL,
                     * strValue); } else {
                     * getIBScanDevice().setProperty(IBScanDevice
                     * .PropertyId.ROLL_MODE, "0"); }
                     */
                    // Make capture delay for display result image on multi
                    // capture mode (500 ms)
                    if (m_nCurrentCaptureStep > 0) {
                        _Sleep(500);
                        m_strImageMessage = "";
                    }

                    CaptureInfo info = m_vecCaptureSeq
                            .elementAt(m_nCurrentCaptureStep);

                    IBScanDevice.ImageResolution imgRes = IBScanDevice.ImageResolution.RESOLUTION_500;
                    boolean bAvailable = getIBScanDevice().isCaptureAvailable(
                            info.ImageType, imgRes);
                    if (!bAvailable) {
                        _SetStatusBarMessage("The capture mode ("
                                + info.ImageType + ") is not available");
                        m_nCurrentCaptureStep = -1;
                        OnMsg_UpdateDisplayResources();
                        return;
                    }

                    // Start capture
                    int captureOptions = 0;
                    // if (m_chkAutoContrast.isSelected())
                    captureOptions |= IBScanDevice.OPTION_AUTO_CONTRAST;
                    // if (m_chkAutoCapture.isSelected())
                    captureOptions |= IBScanDevice.OPTION_AUTO_CAPTURE;
                    // if (m_chkIgnoreFingerCount.isSelected())
                    captureOptions |= IBScanDevice.OPTION_IGNORE_FINGER_COUNT;

                    getIBScanDevice().beginCaptureImage(info.ImageType, imgRes,
                            captureOptions);

                    String strMessage = info.PreCaptureMessage;
                    _SetStatusBarMessage(strMessage);
                    // if (!m_chkAutoCapture.isSelected())
                    // strMessage +=
                    // "\r\nPress button 'Take Result Image' when image is good!";

                    _SetImageMessage(strMessage);
                    m_strImageMessage = strMessage;

                    m_ImageType = info.ImageType;

                    _SetLEDs(info, __LED_COLOR_RED__, true);

                    OnMsg_UpdateDisplayResources();
                } catch (IBScanException ibse) {
                    ibse.printStackTrace();
                    _SetStatusBarMessage("Failed to execute beginCaptureImage()");
                    m_nCurrentCaptureStep = -1;
                }
            }
        });
    }

    private void OnMsg_cboUsbDevice_Changed() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (m_nSelectedDevIndex == m_cboUsbDevices
                        .getSelectedItemPosition())
                    return;

                m_nSelectedDevIndex = m_cboUsbDevices.getSelectedItemPosition();
                if (getIBScanDevice() != null) {
                    try {
                        _ReleaseDevice();
                    } catch (IBScanException ibse) {
                        ibse.printStackTrace();
                    }
                }

                _UpdateCaptureSequences();
            }
        });
    }

    private void OnMsg_UpdateDeviceList(final boolean bConfigurationChanged) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                try {
                    boolean idle = (!m_bInitializing && (m_nCurrentCaptureStep == -1))
                            || (bConfigurationChanged);

                    if (idle) {
                        m_btnCaptureStop.setEnabled(false);
                        m_btnCaptureStart.setEnabled(false);
                    }

                    // store currently selected device
                    String strSelectedText = "";
                    int selectedDev = m_cboUsbDevices.getSelectedItemPosition();
                    if (selectedDev > -1)
                        strSelectedText = m_cboUsbDevices.getSelectedItem()
                                .toString();

                    m_arrUsbDevices = new ArrayList<String>();

                    m_arrUsbDevices.add("- 请选择 -");
                    // populate combo box
                    int devices = getIBScan().getDeviceCount();
                    // m_cboUsbDevices.setMaximumRowCount(devices + 1);

                    selectedDev = 0;
                    for (int i = 0; i < devices; i++) {
                        IBScan.DeviceDesc devDesc = getIBScan()
                                .getDeviceDescription(i);
                        String strDevice;
                        strDevice = devDesc.productName + "_v"
                                + devDesc.fwVersion + "("
                                + devDesc.serialNumber + ")";

                        m_arrUsbDevices.add(strDevice);
                        if (strDevice == strSelectedText)
                            selectedDev = i + 1;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                            SimpleScanActivity.this,
                            R.layout.spinner_text_layout, m_arrUsbDevices);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    m_cboUsbDevices.setAdapter(adapter);
                    m_cboUsbDevices
                            .setOnItemSelectedListener(m_cboUsbDevicesItemSelectedListener);

                    if ((selectedDev == 0 && (m_cboUsbDevices.getCount() == 2)))
                        selectedDev = 1;

                    m_cboUsbDevices.setSelection(selectedDev);

                    if (idle) {
                        OnMsg_cboUsbDevice_Changed();
                        _UpdateCaptureSequences();
                    }
                } catch (IBScanException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void OnMsg_UpdateDisplayResources() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                boolean selectedDev = m_cboUsbDevices.getSelectedItemPosition() > 0;
                boolean captureSeq = m_cboCaptureSeq.getSelectedItemPosition() > 0;
                boolean idle = !m_bInitializing
                        && (m_nCurrentCaptureStep == -1);
                boolean active = !m_bInitializing
                        && (m_nCurrentCaptureStep != -1);

                m_cboUsbDevices.setEnabled(idle);
                m_cboCaptureSeq.setEnabled(selectedDev && idle);

                m_btnCaptureStart.setEnabled(captureSeq);
                m_btnCaptureStop.setEnabled(captureSeq);

                if (active) {
                    m_btnCaptureStart.setText("采集");
                } else {
                    m_btnCaptureStart.setText("采集");
                }
            }
        });
    }

    private void OnMsg_AskRecapture(final IBScanException imageStatus) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String askMsg;

                askMsg = "[Warning = " + imageStatus.getType().toString()
                        + "] Do you want a recapture?";

                AlertDialog.Builder dlgAskRecapture = new AlertDialog.Builder(
                        SimpleScanActivity.this);
                dlgAskRecapture.setMessage(askMsg);
                dlgAskRecapture.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                // To recapture current finger position
                                m_nCurrentCaptureStep--;
                                OnMsg_CaptureSeqNext();
                            }
                        });
                dlgAskRecapture.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                OnMsg_CaptureSeqNext();
                            }
                        });

                dlgAskRecapture.show();
            }
        });
    }

    private void OnMsg_DeviceCommunicationBreak() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (getIBScanDevice() == null)
                    return;

                _SetStatusBarMessage("Device communication was broken");

                try {
                    _ReleaseDevice();

                    OnMsg_Beep(__BEEP_DEVICE_COMMUNICATION_BREAK__);
                    OnMsg_UpdateDeviceList(false);
                } catch (IBScanException ibse) {
                    if (ibse.getType().equals(
                            IBScanException.Type.RESOURCE_LOCKED)) {
                        OnMsg_DeviceCommunicationBreak();
                    }
                }
            }
        });
    }

    private void OnMsg_DrawImage(final IBScanDevice device,
                                 final ImageData image) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int destWidth = m_imgPreview.getWidth() - 20;
                int destHeight = m_imgPreview.getHeight() - 20;
                // int outImageSize = destWidth * destHeight;

                try {
                    if (destHeight <= 0 || destWidth <= 0)
                        return;

                    if (destWidth != m_BitmapImage.getWidth()
                            || destHeight != m_BitmapImage.getHeight()) {
                        // if image size is changed (e.g changed capture type
                        // from flat to rolled finger)
                        // Create bitmap again
                        m_BitmapImage = Bitmap.createBitmap(destWidth,
                                destHeight, Bitmap.Config.ARGB_8888);
                        m_drawBuffer = new byte[destWidth * destHeight * 4];
                    }

                    if (image.isFinal) {
                        getIBScanDevice().generateDisplayImage(image.buffer,
                                image.width, image.height, m_drawBuffer,
                                destWidth, destHeight, (byte) 255,
                                2 /* IBSU_IMG_FORMAT_RGB32 */,
                                2 /* HIGH QUALITY */, true);
                        /*
                         * for (int i=0; i<destWidth*destHeight; i++) { if
                         * (m_drawBuffer[i] != -1) { OnMsg_Beep(__BEEP_OK__);
                         * break; } }
                         */} else {
                        getIBScanDevice().generateDisplayImage(image.buffer,
                                image.width, image.height, m_drawBuffer,
                                destWidth, destHeight, (byte) 255,
                                2 /* IBSU_IMG_FORMAT_RGB32 */,
                                0 /* LOW QUALITY */, true);
                    }
                } catch (IBScanException e) {
                    e.printStackTrace();
                }

                m_BitmapImage.copyPixelsFromBuffer(ByteBuffer
                        .wrap(m_drawBuffer));
                // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                Canvas canvas = new Canvas(m_BitmapImage);

                _DrawOverlay_WarningOfClearPlaten(canvas, 0, 0, destWidth,
                        destHeight);
                _DrawOverlay_ResultSegmentImage(canvas, image, destWidth,
                        destHeight);
                _DrawOverlay_RollGuideLine(canvas, image, destWidth, destHeight);
                /*
                 * _DrawOverlay_WarningOfClearPlaten(canvas, 0, 0, image.width,
                 * image.height); _DrawOverlay_ResultSegmentImage(canvas, image,
                 * image.width, image.height);
                 * _DrawOverlay_RollGuideLine(canvas, image, image.width,
                 * image.height);
                 */
                m_savedData.imageBitmap = m_BitmapImage;
                m_imgPreview.setImageBitmap(m_BitmapImage);
            }
        });
    }

    private void OnMsg_DrawFingerQuality() {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // Update value in fingerQuality field and flash button.
                for (int i = 0; i < 4; i++) {
                    int color;
                    if (m_FingerQuality[i] == IBScanDevice.FingerQualityState.GOOD)
                        color = Color.rgb(0, 128, 0);
                    else if (m_FingerQuality[i] == IBScanDevice.FingerQualityState.FAIR)
                        color = Color.rgb(255, 128, 0);
                    else if (m_FingerQuality[i] == IBScanDevice.FingerQualityState.POOR
                            || m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_TOP
                            || m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_BOTTOM
                            || m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_LEFT
                            || m_FingerQuality[i] == IBScanDevice.FingerQualityState.INVALID_AREA_RIGHT)
                        color = Color.rgb(255, 0, 0);
                    else
                        color = Color.LTGRAY;

                    m_savedData.fingerQualityColors[i] = color;
                }
            }
        });
    }

    /*
     * Show Toast message on UI thread.
     */
    private void showToastOnUiThread(final String message, final int duration) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), message,
                        duration);
                toast.show();
            }
        });
    }

    /*
     * Exit application.
     */
    private static void exitApp(Activity ac) {
        ac.moveTaskToBack(true);
        ac.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /* *********************************************************************************************
     * EVENT HANDLERS
     * ************************************************************
     * *******************************
     */

    /*
     * Handle click on "Start capture" button.
     */
    private OnClickListener m_btnCaptureStartClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (m_bInitializing)
                return;

            int devIndex = m_cboUsbDevices.getSelectedItemPosition() - 1;
            if (devIndex < 0)
                return;

            if (m_nCurrentCaptureStep != -1) {
                try {
                    boolean IsActive = getIBScanDevice().isCaptureActive();
                    if (IsActive) {
                        // Capture image manually for active device
                        getIBScanDevice().captureImageManually();
                        return;
                    }
                } catch (IBScanException ibse) {
                    _SetStatusBarMessage("IBScanDevice.takeResultImageManually() returned exception "
                            + ibse.getType().toString() + ".");
                }
            }

            if (getIBScanDevice() == null) {
                m_bInitializing = true;
                _InitializeDeviceThreadCallback thread = new _InitializeDeviceThreadCallback(
                        m_nSelectedDevIndex - 1);
                thread.start();
            } else {
                OnMsg_CaptureSeqStart();
            }

            OnMsg_UpdateDisplayResources();
        }
    };

    private OnClickListener m_btnCaptureNextClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if(fingerCode < 20 ) {
                fingerCode++;
                m_tvFigureType.setText(GetFPcode(fingerCode));
            } else {
                //退出界面，得到数据
                Intent intent = getIntent();
                intent.putExtra("resultMap", (Serializable) result);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }
    };
    /*
     * Handle click on "Stop capture" button.
     */
    private OnClickListener m_btnCaptureStopClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (getIBScanDevice() == null)
                return;

            // Cancel capturing image for active device.
            try {
                // Cancel capturing image for active device.
                getIBScanDevice().cancelCaptureImage();
                CaptureInfo tmpInfo = new CaptureInfo();
                _SetLEDs(tmpInfo, __LED_COLOR_NONE__, false);
                m_nCurrentCaptureStep = -1;
                m_bNeedClearPlaten = false;
                m_bBlank = false;

                _SetStatusBarMessage("下一个");
                m_strImageMessage = "";
                _SetImageMessage("");
                if(fingerCode < 20 ) {
                    fingerCode++;
                } else {
                    //退出界面，得到数据
                    Intent intent = getIntent();
                    intent.putExtra("resultMap", (Serializable) result);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                }
                OnMsg_UpdateDisplayResources();

            } catch (IBScanException ibse) {
                _SetStatusBarMessage("cancel returned exception "
                        + ibse.getType().toString() + ".");
            }
        }
    };

    /*
     * Handle click on the spinner that determine the Usb Devices.
     */
    private OnItemSelectedListener m_cboUsbDevicesItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> parent,
                                   final View view, final int pos, final long id) {
            OnMsg_cboUsbDevice_Changed();
            m_savedData.usbDevices = pos;
        }

        @Override
        public void onNothingSelected(final AdapterView<?> parent) {
            m_savedData.usbDevices = __INVALID_POS__;
        }
    };

    /*
     * Handle click on the spinner that determine the Fingerprint capture.
     */
    private OnItemSelectedListener m_captureTypeItemSelectedListener = new OnItemSelectedListener() {
        @Override
        public void onItemSelected(final AdapterView<?> parent,
                                   final View view, final int pos, final long id) {
            if (pos == 0) {
                m_btnCaptureStart.setEnabled(false);
                m_btnCaptureNext.setEnabled(false);
            } else {
                m_btnCaptureStart.setEnabled(true);
                m_btnCaptureNext.setEnabled(true);
            }

            m_savedData.captureSeq = pos;
        }

        @Override
        public void onNothingSelected(final AdapterView<?> parent) {
            m_savedData.captureSeq = __INVALID_POS__;
        }
    };

    /*
     * Hide the enlarged dialog, if it exists.
     */
    private OnClickListener m_btnCloseEnlargedDialogClickListener = new OnClickListener() {
        @Override
        public void onClick(final View v) {
            if (m_enlargedDialog != null) {
                m_enlargedDialog.cancel();
                m_enlargedDialog = null;
            }
        }
    };

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC INTERFACE: IBScanListener METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void scanDeviceAttached(final int deviceId) {
        showToastOnUiThread("Device " + deviceId + " attached",
                Toast.LENGTH_SHORT);

        /*
         * Check whether we have permission to access this device. Request
         * permission so it will appear as an IB scanner.
         */
        final boolean hasPermission = m_ibScan.hasPermission(deviceId);
        if (!hasPermission) {
            m_ibScan.requestPermission(deviceId);
        }
    }

    @Override
    public void scanDeviceDetached(final int deviceId) {
        /*
         * A device has been detached. We should also receive a
         * scanDeviceCountChanged() callback, whereupon we can refresh the
         * display. If our device has detached while scanning, we should receive
         * a deviceCommunicationBreak() callback as well.
         */
        showToastOnUiThread("Device " + deviceId + " detached",
                Toast.LENGTH_SHORT);
    }

    @Override
    public void scanDevicePermissionGranted(final int deviceId,
                                            final boolean granted) {
        if (granted) {
            /*
             * This device should appear as an IB scanner. We can wait for the
             * scanDeviceCountChanged() callback to refresh the display.
             */
            showToastOnUiThread("Permission granted to device " + deviceId,
                    Toast.LENGTH_SHORT);
        } else {
            showToastOnUiThread("Permission denied to device " + deviceId,
                    Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void scanDeviceCountChanged(final int deviceCount) {
        OnMsg_UpdateDeviceList(false);
    }

    @Override
    public void scanDeviceInitProgress(final int deviceIndex,
                                       final int progressValue) {
        OnMsg_SetStatusBarMessage("初始化设备中..." + progressValue
                + "%");
    }

    @Override
    public void scanDeviceOpenComplete(final int deviceIndex,
                                       final IBScanDevice device, final IBScanException exception) {
    }

    // //////////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC INTERFACE: IBScanDeviceListener METHODS
    // //////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void deviceCommunicationBroken(final IBScanDevice device) {
        OnMsg_DeviceCommunicationBreak();
    }

    @Override
    public void deviceImagePreviewAvailable(final IBScanDevice device,
                                            final ImageData image) {
        OnMsg_DrawImage(device, image);
    }

    @Override
    public void deviceFingerCountChanged(final IBScanDevice device,
                                         final FingerCountState fingerState) {
        if (m_nCurrentCaptureStep >= 0) {
            CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);
            if (fingerState == IBScanDevice.FingerCountState.NON_FINGER) {
                _SetLEDs(info, __LED_COLOR_RED__, true);
            } else {
                _SetLEDs(info, __LED_COLOR_YELLOW__, true);
            }
        }
    }

    @Override
    public void deviceFingerQualityChanged(final IBScanDevice device,
                                           final FingerQualityState[] fingerQualities) {
        for (int i = 0; i < fingerQualities.length; i++) {
            m_FingerQuality[i] = fingerQualities[i];
        }

        OnMsg_DrawFingerQuality();
    }

    @Override
    public void deviceAcquisitionBegun(final IBScanDevice device,
                                       final ImageType imageType) {
        if (imageType.equals(IBScanDevice.ImageType.ROLL_SINGLE_FINGER)) {
            OnMsg_Beep(__BEEP_OK__);
            m_strImageMessage = "When done remove finger from sensor";
            _SetImageMessage(m_strImageMessage);
            _SetStatusBarMessage(m_strImageMessage);
        }
    }

    @Override
    public void deviceAcquisitionCompleted(final IBScanDevice device,
                                           final ImageType imageType) {
        if (imageType.equals(IBScanDevice.ImageType.ROLL_SINGLE_FINGER)) {
            OnMsg_Beep(__BEEP_OK__);
        } else {
            OnMsg_Beep(__BEEP_SUCCESS__);
            _SetImageMessage("Remove fingers from sensor");
            _SetStatusBarMessage("Acquisition completed, postprocessing..");
        }
    }

    @Override
    public void deviceImageResultAvailable(final IBScanDevice device,
                                           final ImageData image, final ImageType imageType,
                                           final ImageData[] splitImageArray) {
        /* TODO: ALTERNATIVELY, USE RESULTS IN THIS FUNCTION */
    }

    @Override
    public void deviceImageResultExtendedAvailable(IBScanDevice device,
                                                   IBScanException imageStatus, final ImageData image,
                                                   final ImageType imageType, final int detectedFingerCount,
                                                   final ImageData[] segmentImageArray,
                                                   final SegmentPosition[] segmentPositionArray) {
        m_savedData.imagePreviewImageClickable = true;
        m_lastResultImage = image;
        m_lastSegmentImages = segmentImageArray;
//        File file = new File(filePath + File.separator + "22222.png");
//        try {
//            image.saveToFile(file,"png");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        _SavePngImage(image,filePath + UUID.randomUUID().toString().replace("-", ""));
        // imageStatus value is greater than "STATUS_OK", Image acquisition
        // successful.
        if (imageStatus == null /* STATUS_OK */
                || imageStatus.getType().compareTo(
                IBScanException.Type.INVALID_PARAM_VALUE) > 0) {
            if (imageType.equals(IBScanDevice.ImageType.ROLL_SINGLE_FINGER)) {
                OnMsg_Beep(__BEEP_SUCCESS__);
            }
        }

        if (m_bNeedClearPlaten) {
            m_bNeedClearPlaten = false;
            OnMsg_DrawFingerQuality();
        }

        // imageStatus value is greater than "STATUS_OK", Image acquisition
        // successful.
        if (imageStatus == null /* STATUS_OK */
                || imageStatus.getType().compareTo(
                IBScanException.Type.INVALID_PARAM_VALUE) > 0) {
            // Image acquisition successful
            CaptureInfo info = m_vecCaptureSeq.elementAt(m_nCurrentCaptureStep);
            _SetLEDs(info, __LED_COLOR_GREEN__, false);

            {
                m_nSegmentImageArrayCount = detectedFingerCount;
                m_SegmentPositionArray = segmentPositionArray;
            }

            // NFIQ
            // if (m_chkNFIQScore.isSelected())
            {
                byte[] nfiq_score = { 0, 0, 0, 0 };
                try {
                    for (int i = 0, segment_pos = 0; i < 4; i++) {
                        if (m_FingerQuality[i].ordinal() != IBScanDevice.FingerQualityState.FINGER_NOT_PRESENT
                                .ordinal()) {
                            nfiq_score[i] = (byte) getIBScanDevice()
                                    .calculateNfiqScore(
                                            segmentImageArray[segment_pos++]);
                        }
                    }
                } catch (IBScanException ibse) {
                    ibse.printStackTrace();
                }

            }

            if (imageStatus == null /* STATUS_OK */) {
                m_strImageMessage = "采集完成";
                _SetImageMessage(m_strImageMessage);
                _SetStatusBarMessage(m_strImageMessage);
            } else {
                // > IBSU_STATUS_OK
                m_strImageMessage = "采集警告 (警告码 = "
                        + imageStatus.getType().toString() + ")";
                _SetImageMessage(m_strImageMessage);
                _SetStatusBarMessage(m_strImageMessage);

                OnMsg_DrawImage(device, image);
                OnMsg_AskRecapture(imageStatus);
                return;
            }
        } else {
            // < IBSU_STATUS_OK
            m_strImageMessage = "采集失败 (错误码 = "
                    + imageStatus.getType().toString() + ")";
            _SetImageMessage(m_strImageMessage);
            _SetStatusBarMessage(m_strImageMessage);

            // Stop all of acquisition
            m_nCurrentCaptureStep = (int) m_vecCaptureSeq.size();
        }

        OnMsg_DrawImage(device, image);

        OnMsg_CaptureSeqNext();
    }

    @Override
    public void devicePlatenStateChanged(final IBScanDevice device,
                                         final PlatenState platenState) {
        if (platenState.equals(IBScanDevice.PlatenState.HAS_FINGERS))
            m_bNeedClearPlaten = true;
        else
            m_bNeedClearPlaten = false;

        if (platenState.equals(IBScanDevice.PlatenState.HAS_FINGERS)) {
            m_strImageMessage = "";
            _SetImageMessage(m_strImageMessage);
            _SetStatusBarMessage(m_strImageMessage);
        } else {
            if (m_nCurrentCaptureStep >= 0) {
                CaptureInfo info = m_vecCaptureSeq
                        .elementAt(m_nCurrentCaptureStep);

                // Display message for image acuisition again
                String strMessage = info.PreCaptureMessage;

                _SetStatusBarMessage(strMessage);
                // if (!m_chkAutoCapture.isSelected())
                // strMessage +=
                // "\r\nPress button 'Take Result Image' when image is good!";

                _SetImageMessage(strMessage);
                m_strImageMessage = strMessage;
            }
        }

        OnMsg_DrawFingerQuality();
    }

    @Override
    public void deviceWarningReceived(final IBScanDevice device,
                                      final IBScanException warning) {
        _SetStatusBarMessage("Warning received " + warning.getType().toString());
    }

    @Override
    public void devicePressedKeyButtons(IBScanDevice device,
                                        int pressedKeyButtons) {
        _SetStatusBarMessage("PressedKeyButtons " + pressedKeyButtons);

        boolean selectedDev = m_cboUsbDevices.getSelectedItemPosition() > 0;
        boolean idle = m_bInitializing && (m_nCurrentCaptureStep == -1);
        boolean active = m_bInitializing && (m_nCurrentCaptureStep != -1);
        try {
            if (pressedKeyButtons == __LEFT_KEY_BUTTON__) {
                if (selectedDev && idle) {
                    device.setBeeper(
                            IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC,
                            2/* Sol */, 4/* 100ms = 4*25ms */, 0, 0);
                    this.m_btnCaptureStart.performClick();
                }
            } else if (pressedKeyButtons == __RIGHT_KEY_BUTTON__) {
                if ((active)) {
                    device.setBeeper(
                            IBScanDevice.BeepPattern.BEEP_PATTERN_GENERIC,
                            2/* Sol */, 4/* 100ms = 4*25ms */, 0, 0);
                    this.m_btnCaptureStop.performClick();
                }
            }
        } catch (IBScanException e) {
            e.printStackTrace();
        }
    }

    /**
     * 指纹 指位代码
     *
     * @param FPcode
     * @return
     */
    public String GetFPcode(int FPcode) {
        switch (FPcode) {
            case 11:
                return "右手拇指";
            case 12:
                return "右手食指";
            case 13:
                return "右手中指";
            case 14:
                return "右手环指";
            case 15:
                return "右手小指";
            case 16:
                return "左手拇指";
            case 17:
                return "左手食指";
            case 18:
                return "左手中指";
            case 19:
                return "左手环指";
            case 20:
                return "左手小指";
            case 97:
                return "右手不确定指位";
            case 98:
                return "左手不确定指位";
            case 99:
                return "其他不确定指位";
            default:
                return "未知";
        }
    }
}
