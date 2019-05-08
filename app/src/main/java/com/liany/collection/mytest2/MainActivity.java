package com.liany.collection.mytest2;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.huashi.otg.sdk.HSIDCardInfo;
import com.huashi.otg.sdk.HandlerMsg;
import com.huashi.otg.sdk.HsOtgApi;
import com.huashi.otg.sdk.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tv_connectCode,tv_msg;
    private Button btn_connect,btn_read,btn_read_auto,btn;
    boolean m_Auto = false;
    HsOtgApi api;
    String filepath="";
    SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");// 设置日期格式

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
            } else {
                tv_msg.setText("连接失败");
            }
        } else if (i == R.id.btn_read) {

        } else if (i == R.id.btn_read_auto) {

        } else if (i == R.id.button) {
            Intent intent = new Intent(MainActivity.this,SimpleScanActivity.class);
            startActivity(intent);
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

    Handler h = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 99 || msg.what == 100) {
                tv_msg.setText((String)msg.obj);
            }
            //第一次授权时候的判断是利用handler判断，授权过后就不用这个判断了
            if (msg.what ==HandlerMsg.CONNECT_SUCCESS) {
                tv_msg.setText("连接成功");
                tv_connectCode.setText(api.GetSAMID());
            }
            if (msg.what == HandlerMsg.CONNECT_ERROR) {
                tv_msg.setText("连接失败");
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

                if (fp[4] == (byte)0x01) {
                    m_FristPFInfo = String.format("指纹  信息：第一枚指纹注册成功。指位：%s。指纹质量：%d \n", GetFPcode(fp[5]), fp[6]);
                } else {
                    m_FristPFInfo = "身份证无指纹 \n";
                }
                if (fp[512 + 4] == (byte)0x01) {
                    m_SecondPFInfo = String.format("指纹  信息：第二枚指纹注册成功。指位：%s。指纹质量：%d \n", GetFPcode(fp[512 + 5]),
                            fp[512 + 6]);
                } else {
                    m_SecondPFInfo = "身份证无指纹 \n";
                }
                tv_connectCode.setText("姓名：" + ic.getPeopleName() + "\n" + "性别：" + ic.getSex() + "\n" + "民族：" + ic.getPeople()
                        + "\n" + "出生日期：" + df.format(ic.getBirthDay()) + "\n" + "地址：" + ic.getAddr() + "\n" + "身份号码："
                        + ic.getIDCard() + "\n" + "签发机关：" + ic.getDepartment() + "\n" + "有效期限：" + ic.getStrartDate()
                        + "-" + ic.getEndDate() + "\n"+m_FristPFInfo+"\n"+m_SecondPFInfo);
                Test.test("/mnt/sdcard/test.txt4", ic.toString());
                try {
                    int ret = api.Unpack(filepath, ic.getwltdata());// 照片解码
                    Test.test("/mnt/sdcard/test3.txt", "解码中");
                    if (ret != 0) {// 读卡失败
                        return;
                    }
                    FileInputStream fis = new FileInputStream(filepath + "/zp.bmp");
                    Bitmap bmp = BitmapFactory.decodeStream(fis);
                    fis.close();
//                    iv_photo.setImageBitmap(bmp);
                } catch (FileNotFoundException e) {
                    Toast.makeText(getApplicationContext(), "头像不存在！", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    // TODO 自动生成的 catch 块
                    Toast.makeText(getApplicationContext(), "头像读取错误", Toast.LENGTH_SHORT).show();
                }catch (Exception e)
                {
                    Toast.makeText(getApplicationContext(), "头像解码失败", Toast.LENGTH_SHORT).show();
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
