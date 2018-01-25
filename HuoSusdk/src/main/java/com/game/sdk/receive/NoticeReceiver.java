package com.game.sdk.receive;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.SdkConstant;
import com.game.sdk.db.LoginControl;
import com.game.sdk.domain.BaseRequestBean;
import com.game.sdk.domain.IndentifyBean;
import com.game.sdk.domain.IndentifyRespBean;
import com.game.sdk.domain.LoginErrorMsg;
import com.game.sdk.domain.LogincallBack;
import com.game.sdk.domain.Notice;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.log.L;
import com.game.sdk.plugin.IHuoLogin;
import com.game.sdk.ui.RealNameAuthActivity;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.kymjs.rxvolley.RxVolley;

/**
 * Created by Administrator on 2018/1/4.
 */

public class NoticeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        L.e("sdkLogin", "==============onReceive=============");
        String userToken = intent.getStringExtra("usertoken");
        String memid = intent.getStringExtra("memid");
        int code = intent.getIntExtra("code", 0);
        if (code == 500) {
            LoginErrorMsg loginErrorMsg = new LoginErrorMsg(500, "用户登陆失败");
            OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
            if (onLoginListener != null) {
                L.e("SdkLogin", "onDestroy  CODE_LOGIN_CANCEL");
                onLoginListener.loginError(loginErrorMsg);
            }
        } else if (code == 200) {
            int from = intent.getIntExtra("from", -1);
            L.e("sdkLogin", "userToken =" + userToken + " ,from = " + from);
            if (from == IHuoLogin.LOGIN_QQ) {
                Toast.makeText(context, "QQ授权登录成功", Toast.LENGTH_SHORT).show();
            } else if (from == IHuoLogin.LOGIN_WX) {
                Toast.makeText(context, "微信授权登录成功", Toast.LENGTH_SHORT).show();
            }

            LoginControl.saveUserToken(userToken);
            OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
            if (onLoginListener != null) {
                onLoginListener.loginSuccess(new LogincallBack(memid, userToken));
                //登录成功后统一弹出弹框
//                getNotice(context);
                indentify(context, memid);
            }
        }
    }

    private void indentify(final Context context, final String memid) {
        IndentifyBean indentifyBean = new IndentifyBean();
        indentifyBean.setMem_id(memid);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(indentifyBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<IndentifyRespBean>(context, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(IndentifyRespBean data) {
                if (null != data) {
                    if (data.getType() == 1 && data.getStatus() == 0) {//拉起未鉴权
                        Intent intentAct = new Intent(context, RealNameAuthActivity.class);
                        intentAct.putExtra("isShow", data.getIs_show());
                        intentAct.putExtra("memId", memid);
                        intentAct.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentAct);
                    } else if (data.getType() == 1 && data.getStatus() == 1) {//拉起已鉴权
//                        Toast.makeText(context, "用户已实名验证", Toast.LENGTH_SHORT).show();
                        getNotice(context);
                    } else if (data.getType() == 0) {//不拉起
                        getNotice(context);
                    }
                }
            }

            @Override
            public void onFailure(String code, String msg) {
                Toast.makeText(context, "onFailure", Toast.LENGTH_SHORT).show();
                getNotice(context);
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);//对话框继续使用install接口，在startup联网结束后，自动结束等待loading
        RxVolley.post(SdkApi.indentify(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    private void getNotice(Context context) {
        BaseRequestBean baseRequestBean = new BaseRequestBean();
        baseRequestBean.setApp_id(SdkConstant.HS_APPID);
        L.e("sdkLogin", "baseRequestBean =" + baseRequestBean.toString());
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<Notice>(context, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(Notice data) {
                //登录成功后统一弹出弹框
                L.e("sdkLogin", "data =" + data.toString());
                DialogUtil.showNoticeDialog1(HuosdkInnerManager.getInstance().getContext(), data);
            }

            @Override
            public void onFailure(String code, String msg) {
                L.e("NoticeReceiver", "code =" + code + ", msg =" + msg);
                L.e("sdkLogin", "code =" + code + ", msg =" + msg);
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);//对话框继续使用install接口，在startup联网结束后，自动结束等待loading
        RxVolley.post(SdkApi.getNotice(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }
}