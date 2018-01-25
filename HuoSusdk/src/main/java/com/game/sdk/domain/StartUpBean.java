package com.game.sdk.domain;

/**
 * Created by liu hong liang on 2016/11/10.
 */

public class StartUpBean extends BaseRequestBean {
    public static final String OPEN_CNT="open_cnt";
    public String open_cnt;//	是	INT	打开次数 默认为1
    private String ver_id;
    private String ver_code;
    public StartUpBean() {
        setUser_token(null);
    }

    public String getOpen_cnt() {
        return open_cnt;
    }

    public void setOpen_cnt(String open_cnt) {
        this.open_cnt = open_cnt;
    }

    public String getVer_id() {
        return ver_id;
    }

    public void setVer_id(String ver_id) {
        this.ver_id = ver_id;
    }

    public String getVer_code() {
        return ver_code;
    }

    public void setVer_code(String ver_code) {
        this.ver_code = ver_code;
    }
}
