package com.game.sdk.view;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.game.sdk.HuosdkInnerManager;
import com.game.sdk.SdkConfig;
import com.game.sdk.SdkConstant;
import com.game.sdk.db.LoginControl;
import com.game.sdk.db.impl.UserLoginInfodao;
import com.game.sdk.domain.BaseRequestBean;
import com.game.sdk.domain.IndentifyBean;
import com.game.sdk.domain.IndentifyRespBean;
import com.game.sdk.domain.LoginRequestBean;
import com.game.sdk.domain.LoginResultBean;
import com.game.sdk.domain.LogincallBack;
import com.game.sdk.domain.Notice;
import com.game.sdk.domain.RealNameEvent;
import com.game.sdk.domain.UserInfo;
import com.game.sdk.domain.WebRequestBean;
import com.game.sdk.http.HttpCallbackDecode;
import com.game.sdk.http.HttpParamsBuild;
import com.game.sdk.http.SdkApi;
import com.game.sdk.listener.OnLoginListener;
import com.game.sdk.log.L;
import com.game.sdk.log.T;
import com.game.sdk.ui.FloatWebActivity;
import com.game.sdk.ui.HuoLoginActivity;
import com.game.sdk.util.DialogUtil;
import com.game.sdk.util.GsonUtil;
import com.game.sdk.util.MResource;
import com.game.sdk.util.RegExpUtil;
import com.kymjs.rxvolley.RxVolley;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Liuhongliangsdk on 2016/11/11.
 */
public class HuoLoginViewNew extends FrameLayout implements View.OnClickListener {
    private static final String TAG = HuoLoginViewNew.class.getSimpleName();
    //登陆
    LinearLayout huoLlLoginRegister, huoLlOneKeyLogin;
    RelativeLayout huoRlLogin;
    private HuoLoginActivity loginActivity;
    private EditText huo_et_loginAccount;
    private EditText huo_et_loginPwd;
    private Button huo_btn_loginSubmit;
    private ImageView huo_img_show_pwd;
    private boolean showPwd = false;
    private Button huo_btn_loginSubmitForgetPwd;
    private ViewStackManager viewStackManager;
    private PopupWindow pw_select_user;
    private RecordUserAdapter pw_adapter;
    private List<UserInfo> userInfoList;
    private ImageView huo_iv_loginUserSelect;
    private ImageView img_login_qq, img_login_wx;
    private RelativeLayout huo_rl_loginAccount;
    private Context mContext;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            DialogUtil.dismissDialog2();
            loginActivity.callBackFinish();
        }
    };

    public HuoLoginViewNew(Context context) {
        super(context);
        mContext = context;
        setupUI();
    }

    public HuoLoginViewNew(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        setupUI();
    }

    public HuoLoginViewNew(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        setupUI();
    }

    private void setupUI() {
        loginActivity = (HuoLoginActivity) getContext();
        viewStackManager = ViewStackManager.getInstance(loginActivity);
        LayoutInflater.from(getContext()).inflate(MResource.getIdByName(getContext(), MResource.LAYOUT, "huo_sdk_include_login_new"), this);
        huoRlLogin = (RelativeLayout) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_rl_login"));
        huo_et_loginAccount = (EditText) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_et_loginAccount"));
        huo_et_loginPwd = (EditText) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_et_loginPwd"));
        huo_img_show_pwd = (ImageView) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_img_show_pwd"));
        huo_btn_loginSubmitForgetPwd = (Button) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_btn_loginSubmitForgetPwd"));
        huo_btn_loginSubmit = (Button) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_btn_loginSubmit"));
        huoLlOneKeyLogin = (LinearLayout) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_ll_onekeylogin"));
        huoLlLoginRegister = (LinearLayout) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_ll_loginRegister"));
        huo_iv_loginUserSelect = (ImageView) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_iv_loginUserSelect"));
        huo_rl_loginAccount = (RelativeLayout) findViewById(MResource.getIdByName(getContext(), "R.id.huo_sdk_rl_loginAccount"));
        img_login_qq = (ImageView) findViewById(MResource.getIdByName(getContext(), "R.id.img_login_qq"));
        img_login_wx = (ImageView) findViewById(MResource.getIdByName(getContext(), "R.id.img_login_wx"));

        huoLlOneKeyLogin.setOnClickListener(this);
        huoLlLoginRegister.setOnClickListener(this);
        huo_btn_loginSubmit.setOnClickListener(this);
        huo_img_show_pwd.setOnClickListener(this);
        huo_iv_loginUserSelect.setOnClickListener(this);
        huo_btn_loginSubmitForgetPwd.setOnClickListener(this);
        img_login_qq.setOnClickListener(this);
        img_login_wx.setOnClickListener(this);
        UserInfo userInfoLast = UserLoginInfodao.getInstance(loginActivity).getUserInfoLast();
        if (userInfoLast != null) {
            huo_et_loginAccount.setText(userInfoLast.username);
            huo_et_loginPwd.setText(userInfoLast.password);
        }
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
        if (view.getId() == huoLlLoginRegister.getId()) {
            viewStackManager.addView(loginActivity.getHuoRegisterView());
        } else if (view.getId() == huo_btn_loginSubmit.getId()) {
            submitLogin();
        } else if (view.getId() == huo_img_show_pwd.getId()) {
            if (showPwd) {
                huo_et_loginPwd.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                showPwd = false;
            } else {
                huo_et_loginPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                showPwd = true;
            }
        } else if (view.getId() == huo_btn_loginSubmitForgetPwd.getId()) {
            HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(new WebRequestBean()));
            FloatWebActivity.start(loginActivity, SdkApi.getWebForgetpwd(), "忘记密码",
                    httpParamsBuild.getHttpParams().getUrlParams().toString(), httpParamsBuild.getAuthkey());
        } else if (view.getId() == huo_iv_loginUserSelect.getId()) {
            userselect(huo_et_loginAccount, huo_rl_loginAccount.getWidth());
        } else if (view.getId() == huoLlOneKeyLogin.getId()) {//一键注册
            HuoUserNameRegisterViewNew huoUserNameRegisterView = (HuoUserNameRegisterViewNew) viewStackManager.getViewByClass(HuoUserNameRegisterViewNew.class);
            if (huoUserNameRegisterView != null) {
                huoUserNameRegisterView.switchUI(true);
                viewStackManager.addView(huoUserNameRegisterView);
            }
        } else if (view.getId() == img_login_qq.getId()) {//QQ授权登陆
            L.e("sdkLogin", "启动QQ授权登陆。。");
            if (isAppInstalled(mContext, "com.qf.sdklogin")) {
                int localLoginVerCode = SdkConfig.LOGIN_APK_VERSION;//apk插件版本
                int currLoginVerCode = getVersionCode(mContext, "com.qf.sdklogin");;//sdk版本
                if (currLoginVerCode <= 0 || localLoginVerCode <= 0) {
                    installLoginApk(mContext);
                    return;
                }

                if (currLoginVerCode < localLoginVerCode) {
                    installLoginApk(mContext);
                } else {
                    DialogUtil.showDialog2(mContext, false, "启动qq登录...");
                    //使用第三方登陆插件
                    Intent intent = new Intent("com.qf.sdklogin");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("isLoginQQ", true);

                    intent.putExtra("appid", SdkConstant.HS_APPID);
                    intent.putExtra("agent", SdkConstant.HS_AGENT);
                    intent.putExtra("from", SdkConstant.FROM);
                    intent.putExtra("usertoken", SdkConstant.userToken);
                    intent.putExtra("packagename", SdkConstant.packageName);
                    intent.putExtra("rsapublickey", SdkConstant.RSA_PUBLIC_KEY);
                    intent.putExtra("clientid", SdkConstant.HS_CLIENTID);
                    intent.putExtra("clientkey", SdkConstant.HS_CLIENTKEY);
                    intent.putExtra("servertimeinterval", SdkConstant.SERVER_TIME_INTERVAL);

                    intent.putExtra("device_id", SdkConstant.deviceBean.getDevice_id());
                    intent.putExtra("userua", SdkConstant.deviceBean.getUserua());
                    intent.putExtra("ipaddrid", SdkConstant.deviceBean.getIpaddrid());
                    intent.putExtra("deviceinfo", SdkConstant.deviceBean.getDeviceinfo());
                    intent.putExtra("idfv", SdkConstant.deviceBean.getIdfv());
                    intent.putExtra("idfa", SdkConstant.deviceBean.getIdfa());
                    intent.putExtra("local_ip", SdkConstant.deviceBean.getLocal_ip());
                    intent.putExtra("mac", SdkConstant.deviceBean.getMac());
                    ComponentName comp = new ComponentName("com.qf.sdklogin", "com.qf.sdklogin.QQAuthActivity");
                    intent.setComponent(comp);
                    mContext.startActivity(intent);

                    Message msg = mHandler.obtainMessage();
                    mHandler.sendMessageDelayed(msg, 3000);
                }
            } else {
                installLoginApk(mContext);
            }
        } else if (view.getId() == img_login_wx.getId()) {//微信授权登陆
            L.e("sdkLogin", "启动微信授权登陆。。");
            if (isAppInstalled(mContext, "com.qf.sdklogin")) {
                int localLoginVerCode = SdkConfig.LOGIN_APK_VERSION;//apk插件版本
                int currLoginVerCode = getVersionCode(mContext, "com.qf.sdklogin");;//sdk版本
                if (currLoginVerCode <= 0 || localLoginVerCode <= 0) {
                    installLoginApk(mContext);
                    return;
                }

                if (currLoginVerCode < localLoginVerCode) {
                    installLoginApk(mContext);
                } else {
                    DialogUtil.showDialog2(mContext, false, "启动微信登录...");
                    Intent intent = new Intent("com.qf.sdklogin");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("isLoginWX", true);

                    intent.putExtra("appid", SdkConstant.HS_APPID);
                    intent.putExtra("agent", SdkConstant.HS_AGENT);
                    intent.putExtra("from", SdkConstant.FROM);
                    intent.putExtra("usertoken", SdkConstant.userToken);
                    intent.putExtra("packagename", SdkConstant.packageName);
                    intent.putExtra("rsapublickey", SdkConstant.RSA_PUBLIC_KEY);
                    intent.putExtra("clientid", SdkConstant.HS_CLIENTID);
                    intent.putExtra("clientkey", SdkConstant.HS_CLIENTKEY);
                    intent.putExtra("servertimeinterval", SdkConstant.SERVER_TIME_INTERVAL);

                    intent.putExtra("device_id", SdkConstant.deviceBean.getDevice_id());
                    intent.putExtra("userua", SdkConstant.deviceBean.getUserua());
                    intent.putExtra("ipaddrid", SdkConstant.deviceBean.getIpaddrid());
                    intent.putExtra("deviceinfo", SdkConstant.deviceBean.getDeviceinfo());
                    intent.putExtra("idfv", SdkConstant.deviceBean.getIdfv());
                    intent.putExtra("idfa", SdkConstant.deviceBean.getIdfa());
                    intent.putExtra("local_ip", SdkConstant.deviceBean.getLocal_ip());
                    intent.putExtra("mac", SdkConstant.deviceBean.getMac());
                    ComponentName comp = new ComponentName("com.qf.sdklogin", "com.qf.sdklogin.wxapi.WXEntryActivity");
                    intent.setComponent(comp);
                    mContext.startActivity(intent);

                    Message msg = mHandler.obtainMessage();
                    mHandler.sendMessageDelayed(msg, 3000);
                }
            } else {
                installLoginApk(mContext);
            }
        }
    }

    private void installLoginApk(Context context) {
        if (copyApkFromAssets(context, "sdklogin.apk",
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdklogin.apk")) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdklogin.apk"),
                    "application/vnd.android.package-archive");
            context.startActivity(intent);
            Toast.makeText(context, "请先安装游戏应用第三方授权登录助手", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean copyApkFromAssets(Context context, String fileName, String path) {
        boolean copyIsFinish = false;
        try {
            InputStream is = context.getAssets().open(fileName);
            File file = new File(path);
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            byte[] temp = new byte[1024];
            int i = 0;
            while ((i = is.read(temp)) > 0) {
                fos.write(temp, 0, i);
            }
            fos.close();
            is.close();
            copyIsFinish = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return copyIsFinish;
    }

    public boolean isAppInstalled(Context context, String packageName) {
        final PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return pName.contains(packageName);
    }

    private int getVersionCode(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            int versionCode = packageInfo.versionCode;
            return versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getCurrPackName(Context context) {
        try {
            String pkName = context.getPackageName();
            return pkName;
        } catch (Exception e) {
        }
        return null;
    }

    private void submitLogin() {
        final String account = huo_et_loginAccount.getText().toString().trim();
        final String password = huo_et_loginPwd.getText().toString().trim();
        if (!RegExpUtil.isMatchAccount(account)) {
            T.s(loginActivity, "账号只能由6至16位英文或数字组成");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            T.s(loginActivity, "密码不能为空");
            return;
        }
        final LoginRequestBean loginRequestBean = new LoginRequestBean();
        loginRequestBean.setUsername(account);
        loginRequestBean.setPassword(password);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(loginRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<LoginResultBean>(loginActivity, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(LoginResultBean data) {
                if (data != null) {
                    LoginControl.saveUserToken(data.getCp_user_token());
                    HuosdkInnerManager.notice = data.getNotice(); //发送通知内容
                    OnLoginListener onLoginListener = HuosdkInnerManager.getInstance().getOnLoginListener();
                    if (onLoginListener != null) {
                        onLoginListener.loginSuccess(new LogincallBack(data.getMem_id(), data.getCp_user_token()));
                        //登录成功后统一弹出弹框
//                        getNotice();
                        indentify(data.getMem_id());
                    }
                    //保存账号到数据库
                    if (!UserLoginInfodao.getInstance(loginActivity).findUserLoginInfoByName(account)) {
                        UserLoginInfodao.getInstance(loginActivity).saveUserLoginInfo(account, password);
                    } else {
                        UserLoginInfodao.getInstance(loginActivity).deleteUserLoginByName(account);
                        UserLoginInfodao.getInstance(loginActivity).saveUserLoginInfo(account, password);
                    }
                }
            }
        };
        httpCallbackDecode.setShowTs(true);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(true);
        httpCallbackDecode.setLoadMsg("正在登录...");
        RxVolley.post(SdkApi.getLogin(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    private void indentify(final String memid) {
        IndentifyBean indentifyBean = new IndentifyBean();
        indentifyBean.setMem_id(memid);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(indentifyBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<IndentifyRespBean>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(IndentifyRespBean data) {
                if (null != data) {
                    if (data.getType() == 1 && data.getStatus() == 0) {//拉起未鉴权
                        EventBus.getDefault().post(new RealNameEvent(data.getIs_show()));

                        RealNameAuthView realNameAuthView = loginActivity.getRealNameAuthView();
                        realNameAuthView.setMemId(memid);
                        viewStackManager.addView(realNameAuthView);
                        viewStackManager.removeView(HuoLoginViewNew.this);
                    } else if (data.getType() == 1 && data.getStatus() == 1) {//拉起已鉴权
                        Toast.makeText(mContext, "用户已实名验证", Toast.LENGTH_SHORT).show();
                        getNotice();
                        loginActivity.callBackFinish();
                    } else if (data.getType() == 0) {//不拉起
                        getNotice();
                        loginActivity.callBackFinish();
                    }
                }
            }

            @Override
            public void onFailure(String code, String msg) {
                L.e(TAG, "code =" + code + ", msg =" + msg);
                getNotice();
                loginActivity.callBackFinish();
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);//对话框继续使用install接口，在startup联网结束后，自动结束等待loading
        RxVolley.post(SdkApi.indentify(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    private void getNotice() {
        BaseRequestBean baseRequestBean = new BaseRequestBean();
        baseRequestBean.setApp_id(SdkConstant.HS_APPID);
        HttpParamsBuild httpParamsBuild = new HttpParamsBuild(GsonUtil.getGson().toJson(baseRequestBean));
        HttpCallbackDecode httpCallbackDecode = new HttpCallbackDecode<Notice>(mContext, httpParamsBuild.getAuthkey()) {
            @Override
            public void onDataSuccess(Notice data) {
                L.e(TAG, "content =" + data.getContent() + ", title =" + data.getTitle());
                //登录成功后统一弹出弹框
                DialogUtil.showNoticeDialog1(HuosdkInnerManager.getInstance().getContext(), data);
            }

            @Override
            public void onFailure(String code, String msg) {
                L.e(TAG, "code =" + code + ", msg =" + msg);
            }
        };
        httpCallbackDecode.setShowTs(false);
        httpCallbackDecode.setLoadingCancel(false);
        httpCallbackDecode.setShowLoading(false);//对话框继续使用install接口，在startup联网结束后，自动结束等待loading
        RxVolley.post(SdkApi.getNotice(), httpParamsBuild.getHttpParams(), httpCallbackDecode);
    }

    private void userselect(View v, int width) {
        L.e(TAG, "width=" + width);
        if (pw_select_user != null && pw_select_user.isShowing()) {
            pw_select_user.dismiss();
        } else {
            userInfoList = UserLoginInfodao.getInstance(loginActivity)
                    .getUserLoginInfo();
            if (null == userInfoList || userInfoList.isEmpty()) {
                return;
            }
            if (null == pw_adapter) {
                pw_adapter = new RecordUserAdapter();
            }
            if (pw_select_user == null) {
                // View
                // view=getLayoutInflater().inflate(R.layout.tiantianwan_pw_list,null);
                View view = LayoutInflater.from(loginActivity).inflate(MResource.getIdByName(loginActivity, "R.layout.huo_sdk_pop_record_account"), null);
                // ListView lv_pw=(ListView) view.findViewById(R.id.lv_pw);
                ListView lv_pw = (ListView) view.findViewById(MResource
                        .getIdByName(loginActivity, "R.id.huo_sdk_lv_pw"));
                // LinearLayout.LayoutParams lp=new
                // LinearLayout.LayoutParams(200,-2 );
                // lv_pw.setLayoutParams(lp);
                lv_pw.setCacheColorHint(0x00000000);
                lv_pw.setAdapter(pw_adapter);
                lv_pw.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> adapterview,
                                            View view, int position, long row) {
                        pw_select_user.dismiss();
                        UserInfo userInfo = userInfoList.get(position);
                        huo_et_loginAccount.setText(userInfo.username);
                        huo_et_loginPwd.setText(userInfo.password);
                    }
                });
                pw_select_user = new PopupWindow(view, width,
                        LinearLayout.LayoutParams.WRAP_CONTENT, true);
                pw_select_user.setBackgroundDrawable(new ColorDrawable(
                        0x00000000));
                pw_select_user.setContentView(view);
            } else {
                pw_adapter.notifyDataSetChanged();
            }
            pw_select_user.showAsDropDown(v, 0, 0);
        }
    }

    /**
     * popupwindow显示已经登录用户的设配器
     *
     * @author Administrator
     */
    private class RecordUserAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return userInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return userInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            if (null == convertView) {
                View view = LayoutInflater.from(loginActivity).inflate(MResource.getIdByName(loginActivity,
                        "R.layout.huo_sdk_pop_record_account_list_item"), null);

                convertView = view;
            }
            TextView tv_username = (TextView) convertView.findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_tv_username"));
            ImageView iv_delete = (ImageView) convertView.findViewById(MResource.getIdByName(loginActivity, "R.id.huo_sdk_iv_delete"));
            iv_delete.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 如果删除的用户名与输入框中的用户名一致将删除输入框中的用户名与密码
                    if (huo_et_loginAccount.getText().toString().trim()
                            .equals(userInfoList.get(position).username)) {
                        huo_et_loginAccount.setText("");
                        huo_et_loginPwd.setText("");
                    }
                    UserLoginInfodao.getInstance(loginActivity)
                            .deleteUserLoginByName(
                                    userInfoList.get(position).username);
                    userInfoList.remove(position);
                    if (null != pw_adapter) {
                        if (userInfoList.isEmpty()) {
                            pw_select_user.dismiss();
                        }
                        notifyDataSetChanged();
                    }
                }
            });
            tv_username.setText(userInfoList.get(position).username);
            return convertView;
        }
    }
}
