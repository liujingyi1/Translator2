package com.rgk.android.translator.mpush;

import android.content.Context;

public class MPushApi {

    private static IMPushApi mPushApi = null;
    private MPushApi(){
        
    }

    public synchronized static IMPushApi get(Context context) {
        if (mPushApi == null)
            mPushApi = new MPushApiProxy2(context);
        return mPushApi;
    }

}
