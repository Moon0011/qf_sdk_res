package com.game.sdk.plugin.hbfpay;

import android.app.Activity;
import android.content.Intent;

import com.game.sdk.domain.NotProguard;
import com.game.sdk.domain.PayResultBean;
import com.game.sdk.pay.IPayListener;
import com.game.sdk.plugin.IHuoPay;
import com.game.sdk.plugin.hbfpay.http.Constant;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Administrator on 2017/11/29.
 */

public class HbfpayImpl extends IHuoPay {

    @Override
    @NotProguard
    public void startPay(final Activity activity, final IPayListener listener, final float money, final PayResultBean payResultBean) {
        try {
            JSONObject jsonObject = new JSONObject(payResultBean.getToken());
            String payurl = jsonObject.getString("payUrl");//支付URL
            String orderSn = jsonObject.getString("orderSn");//海贝付平台订单号
            Intent intent = new Intent(activity, PayInterfaceActivity.class);
            intent.putExtra("payUrl", payurl);
            intent.putExtra("orderSn", orderSn);
            activity.startActivityForResult(intent, Constant.REQUESTCODE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
