package xyz.leezoom.ssm.model.gson;

import com.google.gson.annotations.SerializedName;

/**
 * @Author lee
 * @Time 9/26/17.
 */

public class Upload {

    @SerializedName("code")
    private String status;
    @SerializedName("data")
    private SmData data;
    //display if upload failed
    @SerializedName("msg")
    private String msg;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public SmData getData() {
        return data;
    }

    public void setData(SmData data) {
        this.data = data;
    }
}
