package org.aarboard.nextcloud.api.utils;

import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.aarboard.nextcloud.api.exception.NextcloudOperationFailedException;

import java.util.concurrent.CompletableFuture;

public class NextcloudResponseHelper
{
    public static final int NC_OK= 100; // Nextcloud OK message
    public static final int NC_CREATED = 201; // Nextcloud OK message

    private NextcloudResponseHelper() {
    }

    public static <A extends NextcloudResponse> A getAndCheckStatus(CompletableFuture<A> answer)
    {
        A wrappedAnswer = getAndWrapException(answer);
        if(isStatusCodeOkay(wrappedAnswer))
        {
            return wrappedAnswer;
        }
        throw new NextcloudOperationFailedException(wrappedAnswer.getStatusCode(), wrappedAnswer.getMessage());
    }

    public static <A extends NextcloudResponse> boolean isStatusCodeOkay(CompletableFuture<A> answer)
    {
        return isStatusCodeOkay(getAndWrapException(answer));
    }

    public static <A extends NextcloudResponse> boolean isStatusCodeOkayOrCreated(CompletableFuture<A> answer) {
        A wrapped = getAndWrapException(answer);
        return isStatusCodeOkay(wrapped) || isStatusCodeCreated(wrapped);
    }

    public static  boolean isStatusCodeOkay(NextcloudResponse answer)
    {
        return answer.getStatusCode() == NC_OK;
    }

    public static boolean isStatusCodeCreated(NextcloudResponse answer) {
        return answer.getStatusCode() == NC_CREATED;
    }

    public static <A> A getAndWrapException(CompletableFuture<A> answer)
    {
        try {
            return answer.get();
        } catch (Exception e) {
            throw new NextcloudApiException(e);
        }
    }
}
