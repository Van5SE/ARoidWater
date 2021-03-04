package com.van5se.ARoidWater.utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.van5se.ARoidWater.R;

/**
 * Created by dhl on 2017/2/21.
 * 一个展示弹广告图片的Dialog
 */

public class CustomImageDialog extends Dialog {



    private ImageView imageView_del ;
    private ImageView imageView_water;
    private int drawableID;

    public CustomImageDialog(Context context,int drawableID) {
        //super(context);
        super(context, R.style.Translucent_NoTitle);
        this.drawableID=drawableID;
    }

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.image_dialog_layout);
        imageView_water=findViewById(R.id.image_water);
        imageView_water.setImageResource(drawableID);

        imageView_del = (ImageView)findViewById(R.id.image_delete);
        imageView_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomImageDialog.this.dismiss();
            }
        });
        setCanceledOnTouchOutside(false);

    }

}
