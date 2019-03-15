# loglib
基于asm插件自动埋点日志收集库。收集记录推送日志，广播日志，Exception日志，网络请求接口日志，activity生命周期日志，点击事件日志

# 日志库使用方式，在application加这段代码

Tracker.init(this.getApplicationContext(), new AppProxy() {

            @Override
            public String getUserToken() {
                String token = (String) SharePreferencesManager.getInstance().getBaseValue(SharePreferencesManager.SP_FILE_TOKEN, SharePreferencesManager.APP_TOKEN, "");
                return token;
            }

            @Override
            public String[] getStoreDeviceInfoJudgeUrl() {
                return new String[]{"cz_sign_in"};
            }

            @Override
            public NetInfoModel getNetInfo() {
                return NetInfoModel.Builder.newInstance()
                                   .startUploadUrl("https://dev01.letzgo.com.cn/dzcx_cz/m/file/startUpload")
                                   .uploadUrl("https://dev01.letzgo.com.cn/dzcx_cz/m/file/uploadCacheFile")
                                   .build();
            }
        });
        //是否打印日志
        Tracker.instance().setISDEBUG(true);

