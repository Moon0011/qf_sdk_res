package com.game.sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.R;
import com.game.sdk.domain.LoginErrorMsg;
import com.game.sdk.domain.RealNameEvent;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.log.L;
import com.game.sdk.util.MResource;
import com.game.sdk.view.HuoFastLoginViewNew;
import com.game.sdk.view.HuoLoginViewNew;
import com.game.sdk.view.HuoRegisterViewNew;
import com.game.sdk.view.HuoUserNameRegisterViewNew;
import com.game.sdk.view.RealNameAuthView;
import com.game.sdk.view.SelectAccountView;
import com.game.sdk.view.ViewStackManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class HuoLoginActivity extends BaseActivity {
    private static final String TAG = HuoLoginActivity.class.getSimpleName();
    public final static int TYPE_FAST_LOGIN = 0;
    public final static int TYPE_LOGIN = 1;
    //    public final static int TYPE_REGISTER_LOGIN=2;
    private final static int CODE_LOGIN_FAIL = -1;//登陆失败
    private final static int CODE_LOGIN_CANCEL = -2;//用户取消登陆
    HuoLoginViewNew huoLoginView;
    HuoRegisterViewNew huoRegisterView;
    RealNameAuthView realNameAuthView;
    private HuoFastLoginViewNew huoFastLoginView;
    private HuoUserNameRegisterViewNew huoUserNameRegisterView;
    private ViewStackManager viewStackManager;
    private boolean callBacked;//是否已经回调过了
    private SelectAccountView huoSdkSelectAccountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(MResource.getIdByName(this, "R.layout.huo_sdk_activity_huo_login_new"));
        setupUI();
    }

    private void setupUI() {
        callBacked = false;
        viewStackManager = ViewStackManager.getInstance(this);
        int type = getIntent().getIntExtra("type", 1);
        huoLoginView = (HuoLoginViewNew) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_loginView_new"));
        huoFastLoginView = (HuoFastLoginViewNew) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_fastLoginView_new"));
        huoRegisterView = (HuoRegisterViewNew) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_registerView"));
        huoUserNameRegisterView = (HuoUserNameRegisterViewNew) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_userNameRegisterView"));
        huoSdkSelectAccountView = (SelectAccountView) findViewById(MResource.getIdByName(this, "R.id.huo_sdk_selectAccountView"));
        realNameAuthView = (RealNameAuthView) findViewById(MResource.getIdByName(this, "R.id.real_name_auth_view"));
        viewStackManager.addBackupView(huoLoginView);
        viewStackManager.addBackupView(huoFastLoginView);
        viewStackManager.addBackupView(huoRegisterView);
        viewStackManager.addBackupView(huoUserNameRegisterView);
        viewStackManager.addBackupView(huoSdkSelectAccountView);
        viewStackManager.addBackupView(realNameAuthView);
        switchUI(type);
    }

    public void switchUI(int type) {
        if (type == TYPE_FAST_LOGIN) {
            viewStackManager.addView(huoFastLoginView);
        } else if (type == TYPE_LOGIN) {
            viewStackManager.addView(huoLoginView);
        } else {
            viewStackManager.addView(huoRegisterView);
        }
    }

    @Override
    public void onBackPressed() {
        if (realNameAuthView.getVisibility() == View.VISIBLE) {
            return;
        }
        if (viewStackManager.isLastView()) {
            if (huoFastLoginView.getVisibility() == View.VISIBLE) {//当前最后一个view是快速登陆view，不允许返回
                return;
            }
            super.onBackPressed();
        } else {
            viewStackManager.removeTopView();
        }
    }

    public HuoRegisterViewNew getHuoRegisterView() {
        return huoRegisterView;
    }

    public HuoLoginViewNew getHuoLoginView() {
        return huoLoginView;
    }

    public RealNameAuthView getRealNameAuthView() {
        return realNameAuthView;
    }

    @Subscribe
    public void onMessageEvent(RealNameEvent event) {
        if (event.isShow == 1) {
            realNameAuthView.findViewById(R.id.img_close).setVisibility(View.VISIBLE);
        }else{
            realNameAuthView.findViewById(R.id.img_close).setVisibility(View.GONE);
        }
    }

    @Override
    protected void onStart() {
        EventBus.getDefault().register(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewStackManager.clear();
        if (!callBacked) {//还没有回调过，是用户取消登陆
            LoginErrorMsg loginErrorMsg = new LoginErrorMsg(CODE_LOGIN_CANCEL, "用户取消登陆");
            OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
            if (onLoginListener != null) {
                L.e("SdkLogin", "onDestroy  CODE_LOGIN_CANCEL");
                onLoginListener.loginError(loginErrorMsg);
            }
        }
    }

    /**
     * 通知回调成功并关闭activity
     */
    public void callBackFinish() {
        this.callBacked = true;
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    public static void start(Context context, int type) {
        Intent starter = new Intent(context, HuoLoginActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
        starter.putExtra("type", type);
        context.startActivity(starter);
    }
}
