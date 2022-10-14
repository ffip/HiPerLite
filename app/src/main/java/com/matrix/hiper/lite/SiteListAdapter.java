package com.matrix.hiper.lite;

import static android.app.Activity.RESULT_OK;

import static com.matrix.hiper.lite.MainActivity.START_HIPER_CODE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.VpnService;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;
import com.matrix.hiper.lite.hiper.HiPerVpnService;
import com.matrix.hiper.lite.hiper.Setting;
import com.matrix.hiper.lite.hiper.Sites;
import com.matrix.hiper.lite.utils.FileUtils;
import com.matrix.hiper.lite.utils.NetworkUtils;
import com.matrix.hiper.lite.utils.StringUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SiteListAdapter extends BaseAdapter {

    private final Context context;
    private final MainActivity activity;
    private final ArrayList<Sites.Site> list;

    public SiteListAdapter(Context context, MainActivity activity, ArrayList<Sites.Site> list) {
        this.context = context;
        this.activity = activity;
        this.list = list;
    }

    private static class ViewHolder {
        ConstraintLayout parent;
        TextView name;
        TextView date;
        TextView ip;
        ImageButton run;
        ImageButton cancel;
        ImageButton delete;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder viewHolder;
        if (view == null){
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(R.layout.item_instance, null);
            viewHolder.parent = view.findViewById(R.id.parent);
            viewHolder.name = view.findViewById(R.id.name);
            viewHolder.date = view.findViewById(R.id.date);
            viewHolder.ip = view.findViewById(R.id.ip);
            viewHolder.run = view.findViewById(R.id.run);
            viewHolder.cancel = view.findViewById(R.id.stop);
            viewHolder.delete = view.findViewById(R.id.delete);
            view.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) view.getTag();
        }
        Sites.Site site = list.get(i);
        viewHolder.name.setText(site.getName());
        Date date = null;
        try {
            date = stringToDate(site.getCert().getCert().getDetails().getNotAfter());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        DateFormat dateFormat = new SimpleDateFormat(context.getString(R.string.time_pattern), Locale.getDefault());
        viewHolder.date.setText(date == null ? site.getCert().getCert().getDetails().getNotAfter() : dateFormat.format(date));
        viewHolder.ip.setText(site.getCert().getCert().getDetails().getIps().get(0).split("/")[0]);
        if (HiPerVpnService.isRunning(site.getName())) {
            viewHolder.name.setTextColor(Color.GREEN);
            viewHolder.date.setTextColor(Color.GREEN);
            viewHolder.ip.setTextColor(Color.GREEN);
            viewHolder.run.setVisibility(View.GONE);
            viewHolder.cancel.setVisibility(View.VISIBLE);
            viewHolder.delete.setEnabled(false);
        }
        else {
            viewHolder.name.setTextColor(Color.GRAY);
            viewHolder.date.setTextColor(Color.GRAY);
            viewHolder.ip.setTextColor(Color.GRAY);
            viewHolder.run.setVisibility(View.VISIBLE);
            viewHolder.cancel.setVisibility(View.GONE);
            viewHolder.delete.setEnabled(true);
        }
        viewHolder.run.setOnClickListener(view1 -> {
            view1.setEnabled(false);
            viewHolder.delete.setEnabled(false);
            new Thread(() -> {
                try {
                    if (Setting.getSetting(context, site.getName()).isAutoUpdate()) {
                        String url = "https://cert.mcer.cn/point.yml";
                        String conf = NetworkUtils.doGet(NetworkUtils.toURL(url));
                        String path = context.getFilesDir().getAbsolutePath() + "/" + site.getName() + "/hiper_config.json";
                        String s = StringUtils.getStringFromFile(path);
                        Sites.IncomingSite incomingSite = new Gson().fromJson(s, Sites.IncomingSite.class);
                        incomingSite.update(conf);
                        incomingSite.save(context);
                    }
                    activity.runOnUiThread(() -> {
                        for (Sites.Site si : list) {
                            if (HiPerVpnService.isRunning(si.getName())) {
                                Intent intent = new Intent(context, HiPerVpnService.class);
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("stop", true);
                                intent.putExtras(bundle);
                                activity.startService(intent);
                                activity.refreshList();
                                break;
                            }
                        }
                        activity.setName(site.getName());
                        Intent vpnPrepareIntent = VpnService.prepare(context);
                        if (vpnPrepareIntent != null) {
                            activity.startActivityForResult(vpnPrepareIntent, START_HIPER_CODE);
                        }
                        else {
                            activity.onActivityResult(START_HIPER_CODE, RESULT_OK, null);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                    activity.runOnUiThread(() -> {
                        Toast.makeText(context, context.getString(R.string.dialog_add_new_instance_error_network), Toast.LENGTH_SHORT).show();
                    });
                }
                activity.runOnUiThread(() -> {
                    view1.setEnabled(true);
                    viewHolder.delete.setEnabled(true);
                });
            }).start();
        });
        viewHolder.cancel.setOnClickListener(view1 -> {
            Intent intent = new Intent(context, HiPerVpnService.class);
            Bundle bundle = new Bundle();
            bundle.putBoolean("stop", true);
            intent.putExtras(bundle);
            activity.startService(intent);
            activity.refreshList();
        });
        viewHolder.delete.setOnClickListener(view1 -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.dialog_delete_title));
            builder.setMessage(context.getString(R.string.dialog_delete_msg));
            builder.setPositiveButton(context.getString(R.string.dialog_delete_positive), (dialogInterface, i1) -> {
                String path = context.getFilesDir().getAbsolutePath() + "/" + site.getName();
                if (FileUtils.deleteDirectory(path)) {
                    list.remove(i);
                    notifyDataSetChanged();
                }
                else {
                    Toast.makeText(context, context.getString(R.string.item_delete_fail), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(context.getString(R.string.dialog_delete_negative), null);
            builder.create().show();
        });
        viewHolder.parent.setOnClickListener(view12 -> {
            Intent intent = new Intent(context, LogActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString("name", site.getName());
            intent.putExtras(bundle);
            activity.startActivity(intent);
        });
        return view;
    }

    private Date stringToDate(String date) throws ParseException {
        SimpleDateFormat simpledateformat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault());
        return simpledateformat.parse(date);
    }
}
