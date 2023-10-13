package org.aarboard.nextcloud.api.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.aarboard.nextcloud.api.exception.NextcloudOperationFailedException;
import org.apache.http.HttpStatus;

public class NextcloudResponseHelper
{
    public static final int NC_OK= 100; // Nextcloud OK message

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

    public static  boolean isStatusCodeOkay(NextcloudResponse answer)
    {
        return answer.getStatusCode() == NC_OK || answer.getStatusCode() == HttpStatus.SC_OK;
    }

    public static <A> A getAndWrapException(CompletableFuture<A> answer)
    {
        try {
            return answer.get();
        } catch (ExecutionException | InterruptedException e) {
            throw new NextcloudApiException(e);
        }
    }
}
