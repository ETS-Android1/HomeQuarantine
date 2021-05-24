package com.unitbv.quarantineapp.rtcgenerator;
import android.content.res.Resources;

import com.unitbv.quarantineapp.R;
import com.unitbv.quarantineapp.rtcgenerator.RtcTokenBuilder;
import com.unitbv.quarantineapp.rtcgenerator.RtcTokenBuilder.Role;

public class RtcTokenBuilderSample {
    private final String appId = "74eacb428c12400094da582170a8b66a";
    private final String appCertificate = "8a7c2635143847a2ae51fbe3b74395c1";

    static int expirationTimeInSeconds = 3600; 

    public String getRtcToken(String userAccount, String channelName) {
        RtcTokenBuilder token = new RtcTokenBuilder();
        int timestamp = (int)(System.currentTimeMillis() / 1000 + expirationTimeInSeconds);
        String result = token.buildTokenWithUserAccount(appId, appCertificate,
        		 channelName, userAccount, Role.Role_Publisher, timestamp);

        return result;
    }
}
