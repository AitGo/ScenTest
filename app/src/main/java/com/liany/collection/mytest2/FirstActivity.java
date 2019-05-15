package com.liany.collection.mytest2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.huashi.otg.sdk.HSIDCardInfo;
import com.huashi.otg.sdk.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @创建者 ly
 * @创建时间 2019/5/14
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class FirstActivity extends Activity {
    private Button btn;
    private TextView tv_info;
    private ImageView iv_photo;

    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "wltlib";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        initView();
    }

    private void initView() {
        btn = findViewById(R.id.button2);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstActivity.this,MainActivity.class);
                startActivityForResult(intent,1001);
            }
        });

        tv_info = findViewById(R.id.tv_info);
        iv_photo = findViewById(R.id.iv_photo);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            if(requestCode == 1001) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy年MM月dd日");
                List<HashMap<String,String>> cardInfos = (List<HashMap<String,String>>) data.getSerializableExtra("com.liany.easycollect.idCardInfos");
                for(HashMap ic : cardInfos) {
                    tv_info.setText("姓名：" + ic.get("name") + "\n" + "性别：" + ic.get("sex") + "\n" + "民族：" + ic.get("nation")
                            + "\n" + "出生日期：" + ic.get("birthday") + "\n" + "地址：" + ic.get("address") + "\n" + "身份号码："
                            + ic.get("idCard") + "\n" + "签发机关：" + ic.get("department") + "\n" + "有效期限：" + ic.get("startDate")
                            + "-" + ic.get("endDate") + "\n");

                    String photoName = (String) ic.get("photoName");
                    iv_photo.setImageBitmap(BitmapFactory.decodeFile(filePath + File.separator + photoName ));

                }
            }
        }
    }
}
