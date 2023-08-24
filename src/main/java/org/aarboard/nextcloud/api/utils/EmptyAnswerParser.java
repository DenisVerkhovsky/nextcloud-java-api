package org.aarboard.nextcloud.api.utils;

import java.io.Reader;

public class EmptyAnswerParser implements ConnectorCommon.ResponseParser<Void> {
    private static volatile EmptyAnswerParser instance;

    @SuppressWarnings("unchecked")
    public static EmptyAnswerParser getInstance() {
        if (instance == null) {
            synchronized (EmptyAnswerParser.class) {
                if (instance == null) {
                    instance = new EmptyAnswerParser();
                }
            }
        }
        return instance;
    }

    @Override
    public Void parseResponse(Reader reader) {
        return null;
    }
}
