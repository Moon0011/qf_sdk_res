package com.game.sdk.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.SdkConstant;
import com.game.sdk.domain.BaseRequestBean;
import com.game.sdk.domain.IndentifyRespBean;
import com.game.sdk.domain.IndentifySetBean;
import com.game.sdk.domain.LoginErrorMsg;
import com.game.sdk.domain.Notice;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.log.L;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.IDCardUtil;
import com.game.sdk.util.MResource;
import com.kymjs.rxvolley.RxVolley;

/**
 * Created by Administrator on 2018/1/22.
 */

public class RealNameAuthActivity extends BaseActivity implements View.OnClickListener {
    private final static int CODE_LOGIN_CANCEL = -2;//用户取消登陆
    private EditText etAccount;
    private EditText etIdcard;
    private Button btnSure;
    private ImageView imgClose;
    private Context mContext;
    private String memId;
    private int iSshow;
    private boolean callBacked;//是否已经回调过了

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(MResource.getIdByName(this, "R.layout.real_name_auth_activity_layout"));
        mContext = this;
        if (getIntent() != null) {
            iSshow = getIntent().getIntExtra("isShow", -1);
            memId = getIntent().getStringExtra("memId");
        }
        setupUI();
    }


    private void setupUI() {
        callBacked = false;
        LayoutInflater.from(this).inflate(MResource.getIdByName(mContext, MResource.LAYOUT, "real_name_auth_layout"), null);
        etAccount = (EditText) findViewById(MResource.getIdByName(mContext, "R.id.et_account"));
        etIdcard = (EditText) findViewById(MResource.getIdByName(mContext, "R.id.et_idcard"));
        btnSure = (Button) findViewById(MResource.getIdByName(mContext, "R.id.btn_sure"));
        imgClose = (ImageView) findViewById(MResource.getIdByName(mContext, "R.id.img_close"));
        btnSure.setOnClickListener(this);
        imgClose.setOnClickListener(this);

        if (iSshow == 1) {
            imgClose.setVisibility(View.VISIBLE);
        } else if (iSshow == 0) {
            imgClose.setVisibility(View.GONE);
        }
    }

//    @Override
//    protected void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        //自动设置相应的布局尺寸
//        if (getChildCount() > 0) {
//            View childAt = getChildAt(0);
//            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) childAt.getLayoutParams();
//            layoutParams.leftMargin = (int) (getResources().getDimension(MResource.getIdByName(loginActivity, "R.dimen.huo_sdk_activity_horizontal_margin")));
//            layoutParams.rightMargin = layoutParams.leftMargin;
//        }
//    }

    @Override
    public void onClick(View view) {
        if (view.getId() == btnSure.getId()) {
            String account = etAccount.getText().toString();
            String idcard = etIdcard.getText().toString();
            if (TextUtils.isEmpty(idcard)
                    || TextUtils.isEmpty(account)) {
                Toast.makeText(mContext, "姓名或身份证不能为空!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!IDCardUtil.isIDCard(idcard)) {
                Toast.makeText(mContext, "身份证格式不对!", Toast.LENGTH_SHORT).show();
                return;
            }
            indentifySet(memId, account, idcard);
        } else if (view.getId() == imgClose.getId()) {
            getNotice();
        }
    }

    private void indentifySet(String memid, String account, String idcard) {
        IndentifySetBean indentifySetBean = new IndentifySetBean();
        indentifySetBean.setMem_id(memid);
        indentifySetBean.setRealname(account);
        indentifySetBean.setIdcard(idcard);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(indentifySetBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<IndentifyRespBean>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(IndentifyRespBean data) {
                Toast.makeText(mContext, "鉴权成功!", Toast.LENGTH_SHORT).show();
                getNotice();
            }

            @Override
            public void onFailure(String code, String msg) {
                Toast.makeText(mContext, "鉴权失败!", Toast.LENGTH_SHORT).show();
                getNotice();
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);//对话框继续使用install接口，在startup联网结束后，自动结束等待loading
        RxVolley.post(SdkApi.indentifyset(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    private void getNotice() {
        BaseRequestBean baseRequestBean = new BaseRequestBean();
        baseRequestBean.setApp_id(SdkConstant.HS_APPID);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<Notice>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(Notice data) {
                //登录成功后统一弹出弹框
                DialogUtil.showNoticeDialog(mContext, data);
            }

            @Override
            public void onFailure(String code, String msg) {
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);//对话框继续使用install接口，在startup联网结束后，自动结束等待loading
        RxVolley.post(SdkApi.getNotice(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!callBacked) {//还没有回调过，是用户取消登陆
            LoginErrorMsg loginErrorMsg = new LoginErrorMsg(CODE_LOGIN_CANCEL, "用户取消登陆");
            OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
            if (onLoginListener != null) {
                L.e("SdkLogin", "onDestroy  CODE_LOGIN_CANCEL");
                onLoginListener.loginError(loginErrorMsg);
            }
        }
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

    public static void start(Context context) {
        Intent starter = new Intent(context, RealNameAuthActivity.class);
        starter.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
        context.startActivity(starter);
    }

    public void callBackFinish() {
        this.callBacked = true;
        finish();
    }

}
