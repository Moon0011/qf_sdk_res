package com.game.sdk.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.game.sdk.R;
import com.game.sdk.util.MResource;
import com.kymjs.rxvolley.RxVolley;
import com.kymjs.rxvolley.client.HttpCallback;
import com.kymjs.rxvolley.client.ProgressListener;

import java.io.File;
import java.util.Map;

/**
 * Created by Administrator on 2017/9/19.
 */

public class UpdateDailog extends Dialog {
    private Activity mContext;
    private String downUrl;
    private ProgressDialog pd;

    public UpdateDailog(@NonNull Context context) {
        super(context);
    }

    public UpdateDailog(@NonNull Activity context, @StyleRes int themeResId, String downUrl) {
        super(context, themeResId);
        mContext = context;
        this.downUrl = downUrl;
    }

    protected UpdateDailog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(MResource.getIdByName(mContext, "R.layout.update_dialog_layout"), null);
        setContentView(view);
        ImageView imageView = (ImageView) view.findViewById(MResource.getIdByName(mContext, "R.id.img_close"));
        Button btnWait = (Button) view.findViewById(MResource.getIdByName(mContext, "R.id.btn_wait"));
        Button btnUpdate = (Button) view.findViewById(MResource.getIdByName(mContext, "R.id.btn_update"));
        btnWait.setOnClickListener(new clickListener());
        btnUpdate.setOnClickListener(new clickListener());
        imageView.setOnClickListener(new clickListener());
    }

    class clickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            if (id == R.id.img_close) {
                dismiss();
                mContext.finish();
            } else if (id == R.id.btn_wait) {
                dismiss();
                mContext.finish();
            } else if (id == R.id.btn_update) {
                update(downUrl);
                UpdateDailog.this.dismiss();
            }
        }
    }

    private void update(String downLoadApkUrl) {
        String fileName = "defaultgame.apk";
        String saveFilePath = new StringBuffer(getSDPath() + "/qfgame").append(File.separator).append(fileName).toString();
        if (downLoadApkUrl.lastIndexOf("/") >= 0) {
            fileName = downLoadApkUrl.substring(downLoadApkUrl.lastIndexOf("/"));
            saveFilePath = new StringBuffer(getSDPath() + "/qfgame").append(fileName).toString();
        }
        final String finalSaveFilePath = saveFilePath;
        File file = new File(saveFilePath);
        if (file.exists()) {
            install(mContext, file);
            AlertDialog.Builder normalDialog =
                    new AlertDialog.Builder(mContext);
            normalDialog.setCancelable(false);
            normalDialog.setTitle("更新");
            normalDialog.setMessage("安装包已下载好,请及时更新版本！");
            normalDialog.show();
            return;
        }
        RxVolley.download(saveFilePath,
                downLoadApkUrl,
                new ProgressListener() {
                    @Override
                    public void onProgress(long transferredBytes, long totalSize) {
                        pd.setProgress((int) (transferredBytes * 100 / totalSize));
                        pd.show();
                    }
                }, new HttpCallback() {
                    @Override
                    public void onPreStart() {
                        super.onPreStart();
                        Log.e("", "onPreStart()");
                    }

                    @Override
                    public void onPreHttp() {
                        super.onPreHttp();
                        Toast.makeText(mContext, "游戏版本有更新，正在下载最新版本", Toast.LENGTH_SHORT).show();
                        pd = new ProgressDialog(mContext);
                        pd.setCanceledOnTouchOutside(false);
                        pd.setCancelable(false);
                        pd.setTitle("正在下载新版本安装包:");
                        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        pd.setMax(100);
                    }

                    @Override
                    public void onSuccessInAsync(byte[] t) {
                        super.onSuccessInAsync(t);
                        Log.e("", "onSuccessInAsync()");
                    }

                    @Override
                    public void onSuccess(String t) {
                        super.onSuccess(t);
                        Log.e("", "onPreStart()");
                    }

                    @Override
                    public void onFailure(int errorNo, String strMsg, String completionInfo) {
                        super.onFailure(errorNo, strMsg, completionInfo);
                        Log.e("", "onFailure()");
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        Log.e("", "onFinish()");
                        install(mContext, new File(finalSaveFilePath));
                        pd.cancel();
                        AlertDialog.Builder normalDialog =
                                new AlertDialog.Builder(mContext);
                        normalDialog.setCancelable(false);
                        normalDialog.setTitle("更新");
                        normalDialog.setMessage("安装包已下载好,请及时更新版本！");
                        normalDialog.show();
                    }

                    @Override
                    public void onSuccess(Map<String, String> headers, Bitmap bitmap) {
                        super.onSuccess(headers, bitmap);
                        Log.e("", "onSuccess()");
                    }
                });
    }

    private static void install(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri =
                    FileProvider.getUriForFile(context, "com.game.sdk.installapk", file);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
        }
        context.startActivity(intent);
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if (sdCardExist) {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }
}
