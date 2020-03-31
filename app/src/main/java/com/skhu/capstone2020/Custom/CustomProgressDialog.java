package com.skhu.capstone2020.Custom;

import android.app.Dialog;
import android.content.Context;
import android.view.Window;

import androidx.annotation.NonNull;

import com.skhu.capstone2020.R;

public class CustomProgressDialog extends Dialog {

    public CustomProgressDialog(@NonNull Context context) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_dialog);
    }
}
