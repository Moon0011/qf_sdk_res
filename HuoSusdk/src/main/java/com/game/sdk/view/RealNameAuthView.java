package com.game.sdk.view;

import android.content.Context;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.SdkConstant;
import com.game.sdk.domain.BaseRequestBean;
import com.game.sdk.domain.IndentifyRespBean;
import com.game.sdk.domain.IndentifySetBean;
import com.game.sdk.domain.Notice;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.ui.HuoLoginActivity;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.IDCardUtil;
import com.game.sdk.util.MResource;
import com.kymjs.rxvolley.RxVolley;

public class RealNameAuthView extends FrameLayout implements View.OnClickListener {
    private HuoLoginActivity loginActivity;
    private EditText etAccount;
    private EditText etIdcard;
    private Button btnSure;
    private ImageView imgClose;
    private ViewStackManager viewStackManager;
    private Context mContext;
    private String memId;
    private int iSshow;

    public void setMemId(String memid) {
        this.memId = memid;
    }

    public void setISshow(int iSshow) {
        this.iSshow = iSshow;
    }

    public RealNameAuthView(Context context) {
        super(context);
        mContext = context;
        setupUI();
    }

    public RealNameAuthView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setupUI();
    }

    public RealNameAuthView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setupUI();
    }

    private void setupUI() {
        loginActivity = (HuoLoginActivity) getContext();
        viewStackManager = ViewStackManager.getInstance(loginActivity);
        LayoutInflater.from(getContext()).inflate(MResource.getIdByName(getContext(), MResource.LAYOUT, "real_name_auth_layout"), this);
        etAccount = (EditText) findViewById(MResource.getIdByName(getContext(), "R.id.et_account"));
        etIdcard = (EditText) findViewById(MResource.getIdByName(getContext(), "R.id.et_idcard"));
        btnSure = (Button) findViewById(MResource.getIdByName(getContext(), "R.id.btn_sure"));
        imgClose = (ImageView) findViewById(MResource.getIdByName(getContext(), "R.id.img_close"));
        btnSure.setOnClickListener(this);
        imgClose.setOnClickListener(this);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //自动设置相应的布局尺寸
        if (getChildCount() > 0) {
            View childAt = getChildAt(0);
            LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
            layoutParams.leftMargin = (int) (getResources().getDimension(MResource.getIdByName(loginActivity, "R.dimen.huo_sdk_activity_horizontal_margin")));
            layoutParams.rightMargin = layoutParams.leftMargin;
        }
    }

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
            if (!IDCardUtil.isIdCard(idcard)) {
                Toast.makeText(mContext, "身份证格式不对!", Toast.LENGTH_SHORT).show();
                return;
            }
            indentifySet(memId, account, idcard);
        } else if (view.getId() == imgClose.getId()) {
            getNotice();
            viewStackManager.removeView(this);
            loginActivity.callBackFinish();
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
                loginActivity.callBackFinish();
                getNotice();
            }

            @Override
            public void onFailure(String code, String msg) {
                Toast.makeText(mContext, "鉴权失败!", Toast.LENGTH_SHORT).show();
//                loginActivity.callBackFinish();
//                getNotice();
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
                DialogUtil.showNoticeDialog1(HuosdkInnerManager.getInstance().getContext(), data);
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
}
