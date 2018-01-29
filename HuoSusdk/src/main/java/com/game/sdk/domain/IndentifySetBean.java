package com.game.sdk.domain;

/**
 * Created by Administrator on 2018/1/20.
 */

public class IndentifySetBean extends BaseRequestBean {
    private String mem_id;
    private String realname;
    private String idcard;

    public String getMem_id() {
        return mem_id;
    }

    public void setMem_id(String mem_id) {
        this.mem_id = mem_id;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getIdcard() {
        return idcard;
    }

    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }
}
