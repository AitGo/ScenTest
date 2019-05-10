package com.liany.collection.mytest2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huashi.otg.sdk.HSIDCardInfo;
import com.huashi.otg.sdk.HandlerMsg;
import com.huashi.otg.sdk.HsOtgApi;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_connectCode,tv_msg;
    private Button btn_connect,btn_read,btn_read_auto,btn;
    private ImageView iv_photo;
    boolean m_Auto = false;
    HsOtgApi api;
    SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");// 设置日期格式
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "easycollect" + File.separator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        tv_msg = findViewById(R.id.tv_msg);
        tv_connectCode = findViewById(R.id.tv_connectCode);
        btn_connect = findViewById(R.id.btn_connect);
        btn_read = findViewById(R.id.btn_read);
        btn_read_auto = findViewById(R.id.btn_read_auto);
        btn = findViewById(R.id.button);
        iv_photo = findViewById(R.id.iv_photo);

        btn.setOnClickListener(this);
        btn_connect.setOnClickListener(this);
        btn_read.setOnClickListener(this);
        btn_read_auto.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_connect) {
            api = new HsOtgApi(h, MainActivity.this);
            int ret = api.init();// 因为第一次需要点击授权，所以第一次点击时候的返回是-1所以我利用了广播接受到授权后用handler发送消息
            if (ret == 1) {
                tv_msg.setText("连接成功");
                tv_connectCode.setText(api.GetSAMID());
                btn_read.setEnabled(true);
                btn_read_auto.setEnabled(true);
            } else {
                tv_msg.setText("连接失败");
                btn_read.setEnabled(false);
                btn_read_auto.setEnabled(false);
            }
        } else if (i == R.id.btn_read) {
            if(api != null) {
                tv_msg.setText("");
                iv_photo.setImageBitmap(null);
                if (api.Authenticate(200, 200) != 1) {
                    tv_msg.setText("卡认证失败");
                    return;
                }
                HSIDCardInfo ici = new HSIDCardInfo();
                if (api.ReadCard(ici, 200, 1300) == 1) {
                    Message msg = Message.obtain();
                    msg.obj = ici;
                    msg.what = HandlerMsg.READ_SUCCESS;
                    h.sendMessage(msg);
                }
            }

        } else if (i == R.id.btn_read_auto) {
            if(api != null) {
                if (m_Auto) {
                    m_Auto = false;
                    btn_read_auto.setText("自动读卡");
                }
                else{
                    m_Auto = true;
                    new Thread(new CPUThread()).start();
                    btn_read_auto.setText("停止读卡");
                }
            }
        } else if (i == R.id.button) {
            Map<Integer,String> result = new HashMap<>();
//            result.put(11,"111");
//            result.put(14,"");
//            result.put(20,"");
            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "easycollect11111";
            Intent intent = new Intent(MainActivity.this,SimpleScanActivity.class);
            intent.putExtra("com.liany.easycollect.filePath",filePath);
            intent.putExtra("com.liany.easycollect.resultMap", (Serializable) result);
            startActivityForResult(intent,1003);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if (requestCode == 1003) {
                Map<Integer,String> result = (Map<Integer, String>) data.getSerializableExtra("resultMap");
                String s = result.get(11);
                s.toString();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (api == null) {
            return;
        }
        api.unInit();
    }

    public class CPUThread extends Thread {
        public CPUThread() {
            super();
        }
        @Override
        public void run() {
            super.run();
            HSIDCardInfo ici;
            Message msg;
            while (m_Auto) {
                if (api.Authenticate(200, 200) != 1) {
                    msg = Message.obtain();
                    msg.what = HandlerMsg.READ_ERROR;
                    h.sendMessage(msg);
                } else {
                    ici = new HSIDCardInfo();
                    if (api.ReadCard(ici, 200, 1300) == 1) {
                        msg = Message.obtain();
                        msg.obj = ici;
                        msg.what = HandlerMsg.READ_SUCCESS;
                        h.sendMessage(msg);
                    }
                }
                SystemClock.sleep(300);
            }

        }
    }

    Handler h = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 99 || msg.what == 100) {
                tv_msg.setText((String) msg.obj);
                btn_read.setEnabled(false);
                btn_read_auto.setEnabled(false);
            }
            //第一次授权时候的判断是利用handler判断，授权过后就不用这个判断了
            if (msg.what == HandlerMsg.CONNECT_SUCCESS) {
                tv_msg.setText("连接成功");
                tv_msg.setText(api.GetSAMID());
                btn_read.setEnabled(true);
                btn_read_auto.setEnabled(true);
            }
            if (msg.what == HandlerMsg.CONNECT_ERROR) {
                tv_msg.setText("连接失败");
                btn_read.setEnabled(false);
                btn_read_auto.setEnabled(false);
            }
            if (msg.what == HandlerMsg.READ_ERROR) {
                //cz();
                tv_msg.setText("卡认证失败");
            }
            if (msg.what == HandlerMsg.READ_SUCCESS) {
                tv_msg.setText("读卡成功");
                HSIDCardInfo ic = (HSIDCardInfo) msg.obj;
                byte[] fp = new byte[1024];
                fp = ic.getFpDate();
                String m_FristPFInfo = "";
                String m_SecondPFInfo = "";

                if (fp[4] == (byte) 0x01) {
                    m_FristPFInfo = String.format("指纹  信息：第一枚指纹注册成功。指位：%s。指纹质量：%d \n", GetFPcode(fp[5]), fp[6]);
                } else {
                    m_FristPFInfo = "身份证无指纹 \n";
                }
                if (fp[512 + 4] == (byte) 0x01) {
                    m_SecondPFInfo = String.format("指纹  信息：第二枚指纹注册成功。指位：%s。指纹质量：%d \n", GetFPcode(fp[512 + 5]),
                            fp[512 + 6]);
                } else {
                    m_SecondPFInfo = "身份证无指纹 \n";
                }
                tv_connectCode.setText("姓名：" + ic.getPeopleName() + "\n" + "性别：" + ic.getSex() + "\n" + "民族：" + ic.getPeople()
                        + "\n" + "出生日期：" + df.format(ic.getBirthDay()) + "\n" + "地址：" + ic.getAddr() + "\n" + "身份号码："
                        + ic.getIDCard() + "\n" + "签发机关：" + ic.getDepartment() + "\n" + "有效期限：" + ic.getStrartDate()
                        + "-" + ic.getEndDate() + "\n" + m_FristPFInfo + "\n" + m_SecondPFInfo);

                File file = new File(filePath + "zp" + ".png");
                FileInputStream fis = null;
                try {
                    if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
                    int ret = api.Unpack(filePath, ic.getwltdata());// 照片解码
                    if (ret != 0) {// 读卡失败
                        return;
                    }
                    fis = new FileInputStream(filePath + "zp" + ".bmp");
                    Bitmap bmp = BitmapFactory.decodeStream(fis);
                    iv_photo.setImageBitmap(bmp);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "头像不存在！", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    // TODO 自动生成的 catch 块
                    Toast.makeText(getApplicationContext(), "头像读取错误", Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "头像解码失败", Toast.LENGTH_SHORT).show();
                } finally {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
    };

    /**
     * 指纹 指位代码
     *
     * @param FPcode
     * @return
     */
    String GetFPcode(int FPcode) {
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

