package com.matrix.hiper.lite.hiper;

import android.content.Context;

import com.google.gson.Gson;
import com.matrix.hiper.lite.utils.StringUtils;

import java.io.File;

public class Setting {

    private boolean autoUpdate;

    public Setting() {
        this(true);
    }

    public Setting(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void update(Context context, String name, boolean autoUpdate) {
        String settingPath = context.getFilesDir().getAbsolutePath() + "/" + name + "/setting.json";
        this.autoUpdate = autoUpdate;
        StringUtils.writeFile(settingPath, new Gson().toJson(this));
    }

    public static Setting getSetting(Context context, String name) {
        String settingPath = context.getFilesDir().getAbsolutePath() + "/" + name + "/setting.json";
        if (new File(settingPath).exists()) {
            String s = StringUtils.getStringFromFile(settingPath);
            return new Gson().fromJson(s, Setting.class);
        }
        else {
            Setting setting = new Setting();
            StringUtils.writeFile(settingPath, new Gson().toJson(setting));
            return setting;
        }
    }
}
