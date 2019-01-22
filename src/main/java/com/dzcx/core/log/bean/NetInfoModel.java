package com.dzcx.core.log.bean;

/**
 * Created by chen3 on 2017/10/9.
 */
public class NetInfoModel {

    private String startUploadUrl;

    private String uploadUrl;

    private NetInfoModel(String startUploadUrl,String uploadUrl){
        this.startUploadUrl = startUploadUrl;
        this.uploadUrl = uploadUrl;
    }

    public String getStartUploadUrl() {
        return startUploadUrl;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }

    public static class Builder{

        private String uploadUrl;

        private String startUploadUrl;

        private Builder(){

        }

        public Builder uploadUrl(String uploadUrl){
            this.uploadUrl=uploadUrl;
            return this;
        }

        public Builder startUploadUrl(String startUploadUrl){
            this.startUploadUrl=startUploadUrl;
            return this;
        }

        public NetInfoModel build(){
            return new NetInfoModel(startUploadUrl,uploadUrl);
        }

        public static Builder newInstance(){
            return new Builder();
        }
    }
}
