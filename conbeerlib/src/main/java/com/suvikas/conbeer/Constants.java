package com.suvikas.conbeer;

public class Constants {

    public static String[] PARALLELSPACE_SERVICES = {
            "com.lbe.parallel.service.KeyguardService",
            "cn.thinkingdata.android.TDQuitSafelyService$TDKeepAliveService",
            "com.lbe.parallel.install.AppInstallService",
            "com.lbe.doubleagent.service.proxy.KeepAliveService"
            };

    public static String [] BLACKLISTED_ENV_VARIABLES = {
            "V_REPLACE_ITEM","V_KEEP_ITEM","V_SO_PATH",
            "REPLACE_ITEM_ORIG","REPLACE_ITEM_DST","ZM_ENV_TPN","ENV_IOR_RULES",
            "REDIRECT_SRC","WHITELIST_SRC"
    };

    public static String[] BLACKLISTED_STACKTRACE_CLASSES = {
       "com.doubleagent", //Parallel Space
       "com.lody", //Dual Space, Virtual App
       "org.nl", // Super Clone
       "io.tt", // Multi-Parallel App
       "com.trendmicro.tmmssandbox", //DrClone
       "com.estrongs.vbox", //Clone App
       "com.excelliance", //2Accounts
       "com.prism.gaia"
    };
}


