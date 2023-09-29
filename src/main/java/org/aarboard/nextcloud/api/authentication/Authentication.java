package org.aarboard.nextcloud.api.authentication;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.aarboard.nextcloud.api.ServerConfig;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.aarboard.nextcloud.api.utils.ConnectorCommon;
import org.aarboard.nextcloud.api.utils.NextcloudResponseHelper;
import org.aarboard.nextcloud.api.utils.XMLAnswer;
import org.aarboard.nextcloud.api.utils.XMLAnswerParser;

public class Authentication implements AutoCloseable {
    private final static String CORE_AUTH_URL = "ocs/v2.php/core";
    private final static String APP_PASSWORD = CORE_AUTH_URL + "/getapppassword";
    private final static String REMOVE_APP_PASSWORD = "apppassword";

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

    /**
     * Invalidates and removes issued application token
     */
    public boolean logout() {
        return NextcloudResponseHelper.isStatusCodeOkay(logoutAsync());
    }

    public CompletableFuture<XMLAnswer> logoutAsync() {
        return connectorCommon.executeDelete(CORE_AUTH_URL, REMOVE_APP_PASSWORD,
                XMLAnswerParser.getInstance(XMLAnswer.class));
    }

    @Override
    public void close() throws IOException {
        connectorCommon.close();
    }
}
