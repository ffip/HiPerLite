package com.matrix.hiper.lite;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.matrix.hiper.lite.hiper.Sites;
import com.matrix.hiper.lite.utils.NetworkUtils;

import java.io.File;
import java.io.IOException;

public class AddInstanceDialog extends Dialog implements View.OnClickListener {

    private final AddInstanceCallback callback;
    private final Handler handler;

    private EditText editName;
    private EditText editToken;
    private TextView errorText;
    private Button positive;
    private Button negative;

    public AddInstanceDialog(@NonNull Context context, AddInstanceCallback callback) {
        super(context);
        setContentView(R.layout.dialog_add_instance);
        setCancelable(false);
        this.callback = callback;
        this.handler = new Handler();

        editName = findViewById(R.id.edit_name);
        editToken = findViewById(R.id.edit_token);
        errorText = findViewById(R.id.error_text);
        positive = findViewById(R.id.ok);
        negative = findViewById(R.id.cancel);
        positive.setOnClickListener(this);
        negative.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == positive) {
            String name = editName.getText().toString();
            String token = editToken.getText().toString();
            if (!name.equals("") && !token.equals("")) {
                String path = getContext().getFilesDir().getAbsolutePath() + "/" + name;
                if (!new File(path).exists()) {
                    getConfig(name, token);
                }
                else {
                    Toast.makeText(getContext(), getContext().getString(R.string.dialog_add_new_instance_warn), Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (view == negative) {
            dismiss();
        }
    }

    private void getConfig(String name, String token) {
        errorText.setVisibility(View.GONE);
        positive.setEnabled(false);
        negative.setEnabled(false);
        new Thread(() -> {
            try {
                String url;
                if (token.startsWith("https")) {
                    url = token;
                }
                else {
                    url = String.format("https://cert.mcer.cn/%s.yml", token);
                }
                String conf = NetworkUtils.doGet(NetworkUtils.toURL(url));
                if (conf.equals("")) {
                    handler.post(() -> {
                        Toast.makeText(getContext(), getContext().getString(R.string.dialog_add_new_instance_error_invalid), Toast.LENGTH_SHORT).show();
                    });
                }
                else {
                    Sites.IncomingSite incomingSite = Sites.IncomingSite.parse(name, token, conf);
                    incomingSite.save(getContext());
                    handler.post(() -> {
                        callback.onInstanceAdd();
                        dismiss();
                    });
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    errorText.setText(e.toString());
                    errorText.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), getContext().getString(R.string.dialog_add_new_instance_error_network), Toast.LENGTH_SHORT).show();
                });
            }
            handler.post(() -> {
                positive.setEnabled(true);
                negative.setEnabled(true);
            });
        }).start();
    }

    public interface AddInstanceCallback {
        void onInstanceAdd();
    }
}
