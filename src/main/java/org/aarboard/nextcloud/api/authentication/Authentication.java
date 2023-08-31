package org.aarboard.nextcloud.api.authentication;

import org.aarboard.nextcloud.api.ServerConfig;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.aarboard.nextcloud.api.utils.ConnectorCommon;
import org.aarboard.nextcloud.api.utils.XMLAnswerParser;

import java.util.concurrent.CompletableFuture;

public class Authentication {
    private final static String APP_PASSWORD = "ocs/v2.php/core/getapppassword";
    private final static String AUTH = "ocs/v2.php/cloud/user?format=json";

    private final ConnectorCommon connectorCommon;

    public Authentication(ServerConfig serverConfig) {
        this.connectorCommon = new ConnectorCommon(serverConfig);
    }

    /**
     * Obtaining application token with provided credentials
     *
     * @return bearer token
     */
    public String login() {
        try {
            return loginAsync().get().getToken();
        } catch (Exception e) {
            throw new NextcloudApiException(e);
        }
    }

    /**
     * Obtaining application token with provided credentials
     *
     * @return bearer token
     */
    public CompletableFuture<LoginAnswer> loginAsync() {
        return connectorCommon.executeGet(APP_PASSWORD, XMLAnswerParser.getInstance(LoginAnswer.class));
    }
}
