package com.skhu.capstone2020.Custom;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.skhu.capstone2020.R;

import java.util.Objects;

public class CustomDestinationDialog extends Dialog {
    private View.OnClickListener okListener;
    private View.OnClickListener cancelListener;

    public CustomDestinationDialog(@NonNull Context context, View.OnClickListener okListener, View.OnClickListener cancelListener) {
        super(context);
        this.okListener = okListener;
        this.cancelListener = cancelListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        layoutParams.dimAmount = 0.8f;
        Objects.requireNonNull(getWindow()).setAttributes(layoutParams);

        setContentView(R.layout.custom_destination_dialog);

        ImageButton btn_destination_dialog_ok = findViewById(R.id.btn_destination_dialog_ok);
        ImageButton btn_destination_dialog_cancel = findViewById(R.id.btn_destination_dialog_cancel);

        btn_destination_dialog_ok.setOnClickListener(okListener);
        btn_destination_dialog_cancel.setOnClickListener(cancelListener);
    }
}
