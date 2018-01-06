package com.game.sdk.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.SdkConstant;
import com.game.sdk.db.LoginControl;
import com.game.sdk.domain.BaseRequestBean;
import com.game.sdk.domain.Notice;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.log.L;
import com.game.sdk.plugin.IHuoLogin;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.kymjs.rxvolley.RxVolley;

/**
 * Created by Administrator on 2018/1/4.
 */

public class NoticeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        DialogUtil.dismissDialog();
        String userToken = intent.getStringExtra("usertoken");
        int from = intent.getIntExtra("from", -1);
        if (from == IHuoLogin.LOGIN_QQ) {
            Toast.makeText(context, "QQ授权登录成功", Toast.LENGTH_SHORT).show();
        } else if (from == IHuoLogin.LOGIN_WX) {
            Toast.makeText(context, "微信授权登录成功", Toast.LENGTH_SHORT).show();
        }

        LoginControl.saveUserToken(userToken);
        getNotice(context);
    }

    private void getNotice(Context context) {
        BaseRequestBean baseRequestBean = new BaseRequestBean();
        baseRequestBean.setApp_id(SdkConstant.HS_APPID);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<Notice>(context, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(Notice data) {
                //登录成功后统一弹出弹框
                DialogUtil.showNoticeDialog(HuosdkInnerManager.getInstance().getContext(), data);
            }

            @Override
            public void onFailure(String code, String msg) {
                L.e("NoticeReceiver", "code =" + code + ", msg =" + msg);
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);//对话框继续使用install接口，在startup联网结束后，自动结束等待loading
        RxVolley.post(SdkApi.getNotice(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }
}