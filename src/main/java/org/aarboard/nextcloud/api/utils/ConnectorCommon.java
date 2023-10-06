package org.aarboard.nextcloud.api.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLContext;

import org.aarboard.nextcloud.api.ServerConfig;
import org.aarboard.nextcloud.api.exception.NextcloudApiException;
import org.aarboard.nextcloud.api.exception.NextcloudApiResultException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.TrustAllStrategy;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;

public class ConnectorCommon implements AutoCloseable
{
    private final ServerConfig serverConfig;
    private final CloseableHttpAsyncClient client;

    public ConnectorCommon(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.client = createClient(serverConfig);
    }

    public <R> CompletableFuture<R> executeGet(String part, ResponseParser<R> parser) {
        return executeGet(part, null, parser);
    }

    public <R> CompletableFuture<R> executeGet(String part, List<NameValuePair> queryParams, ResponseParser<R> parser) {
        try {
            URI url= buildUrl(part, queryParams, parser instanceof JsonAnswerParser);

            HttpRequestBase request = new HttpGet(url.toString());
            return executeRequest(parser, request);
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        }
    }

    public <R> CompletableFuture<R> executePost(String part, ResponseParser<R> parser) {
        return executePost(part, null, parser);
    }

    public <R> CompletableFuture<R> executePost(String part, List<NameValuePair> postParams, ResponseParser<R> parser) {
        return executePost(part, null, postParams, null, parser);
    }

    public <R> CompletableFuture<R> executePost(String part, Map<String, String> headers,
            List<NameValuePair> postParams, HttpEntity httpEntity,
            ResponseParser<R> parser) {
        try {
            URI url= buildUrl(part, postParams, parser instanceof JsonAnswerParser);

            HttpPost httpPost = new HttpPost(url.toString());
            if (httpEntity != null) {
                httpPost.setEntity(httpEntity);
            }

            if (headers != null) {
                headers.forEach(httpPost::addHeader);
            }

            return executeRequest(parser, httpPost, ContentType.APPLICATION_JSON.getMimeType());
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        }
    }

    public <R> CompletableFuture<R> executePut(String part1, String part2, ResponseParser<R> parser) {
        return executePut(part1, part2, null, parser);
    }

    public <R> CompletableFuture<R> executePut(String part1, String part2, List<NameValuePair> putParams, ResponseParser<R> parser) {
        try {
            URI url= buildUrl(part1 + "/" + part2, putParams, parser instanceof JsonAnswerParser);

            HttpRequestBase request = new HttpPut(url.toString());
            return executeRequest(parser, request);
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        }
    }

    public <R> CompletableFuture<R> executeDelete(String part1, String part2, ResponseParser<R> parser) {
        return executeDelete(part1, part2, null, parser);
    }

    public <R> CompletableFuture<R> executeDelete(String part1, String part2, List<NameValuePair> deleteParams, ResponseParser<R> parser) {
        try {
            URI url= buildUrl(part1 + "/" + part2, deleteParams, parser instanceof JsonAnswerParser);

            HttpRequestBase request = new HttpDelete(url.toString());
            return executeRequest(parser, request);
        } catch (IOException e) {
            throw new NextcloudApiException(e);
        }
    }

    private URI buildUrl(String subPath, List<NameValuePair> queryParams, boolean useJson) {
    	if(serverConfig.getSubPathPrefix()!=null) {
    		subPath = serverConfig.getSubPathPrefix()+"/"+subPath;
    	}

        if (useJson) {
            if (queryParams == null || queryParams.isEmpty()) {
                queryParams = new ArrayList<>();
            }
            queryParams.add(new BasicNameValuePair("format", "json"));
        }
    	
        URIBuilder uB= new URIBuilder()
        .setScheme(serverConfig.isUseHTTPS() ? "https" : "http")
        .setHost(serverConfig.getServerName())
        .setPort(serverConfig.getPort())
        .setPath(subPath);

        if (serverConfig.getAuthenticationConfig().usesBasicAuthentication()) {
            uB.setUserInfo(serverConfig.getAuthenticationConfig().getLoginName(),
                    serverConfig.getAuthenticationConfig().getPassword());
        }

        if (queryParams != null) {
            uB.addParameters(queryParams);
        }

        try {
            return uB.build();
        } catch (URISyntaxException e) {
            throw new NextcloudApiException(e);
        }
    }

    private <R> CompletableFuture<R> executeRequest(final ResponseParser<R> parser, HttpRequestBase request) throws IOException, ClientProtocolException {
        return executeRequest(parser, request, "application/x-www-form-urlencoded");
    }

    private <R> CompletableFuture<R> executeRequest(final ResponseParser<R> parser, HttpRequestBase request,
            String contentType) throws IOException, ClientProtocolException {
        // https://docs.nextcloud.com/server/14/developer_manual/core/ocs-share-api.html
        request.addHeader("OCS-APIRequest", "true");
        request.addHeader(HttpHeaders.CONTENT_TYPE, contentType);
        request.setProtocolVersion(HttpVersion.HTTP_1_1);

        if (serverConfig.getAuthenticationConfig().usesBearerTokenAuthentication()) {
            String credentials = String.format("%s:%s", serverConfig.getAuthenticationConfig().getLoginName(),
                    serverConfig.getAuthenticationConfig().getBearerToken());
            String authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
            request.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }

        HttpClientContext context = prepareContext();

        CompletableFuture<R> futureResponse = new CompletableFuture<>();
        client.execute(request, context, new ResponseCallback<>(parser, futureResponse));
        return futureResponse;
    }

    private HttpClientContext prepareContext() {
        if (serverConfig.getAuthenticationConfig().usesBasicAuthentication()) {
            HttpHost targetHost = new HttpHost(serverConfig.getServerName(), serverConfig.getPort(), serverConfig.isUseHTTPS() ? "https" : "http");
            AuthCache authCache = new BasicAuthCache();
            authCache.put(targetHost, new BasicScheme());

            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            UsernamePasswordCredentials credentials
            = new UsernamePasswordCredentials(serverConfig.getAuthenticationConfig().getLoginName(), serverConfig.getAuthenticationConfig().getPassword());
            credsProvider.setCredentials(AuthScope.ANY, credentials);

            // Add AuthCache to the execution context
            HttpClientContext context = HttpClientContext.create();
            context.setCredentialsProvider(credsProvider);
            context.setAuthCache(authCache);
            return context;
        }
        return HttpClientContext.create();
    }

    private final class ResponseCallback<R> implements FutureCallback<HttpResponse> {
        private final ResponseParser<R> parser;
        private final CompletableFuture<R> futureResponse;

        private ResponseCallback(ResponseParser<R> parser, CompletableFuture<R> futureResponse) {
            this.parser = parser;
            this.futureResponse = futureResponse;
        }

        @Override
        public void completed(HttpResponse response) {
            try {
                R result = handleResponse(parser, response);
                futureResponse.complete(result);
            } catch(Exception ex) {
                futureResponse.completeExceptionally(ex);
            }
        }

        private R handleResponse(ResponseParser<R> parser, HttpResponse response) throws IOException {
            StatusLine statusLine= response.getStatusLine();
            if (statusLine.getStatusCode() == HttpStatus.SC_OK || statusLine.getStatusCode() == HttpStatus.SC_CREATED) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    Charset charset = ContentType.getOrDefault(entity).getCharset();
                    Reader reader = new InputStreamReader(entity.getContent(), charset);
                    return parser.parseResponse(reader);
                }
                throw new NextcloudApiResultException("Empty response received", statusLine.getStatusCode());
            } else if (statusLine.getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                return null;
            }
            throw new NextcloudApiResultException(String.format("Request failed with %d %s", statusLine.getStatusCode(),
                    statusLine.getReasonPhrase()), statusLine.getStatusCode());
        }

        @Override
        public void failed(Exception ex) {
            futureResponse.completeExceptionally(ex);
        }

        @Override
        public void cancelled() {
            futureResponse.cancel(true);
        }
    }

    public CloseableHttpAsyncClient createClient(ServerConfig serverConfig) {
        try {
            CloseableHttpAsyncClient client;
            if (serverConfig.isTrustAllCertificates()) {

                SSLContext sslContext = SSLContexts.custom()
                        .loadTrustMaterial(null, TrustAllStrategy.INSTANCE).build();
                client = HttpAsyncClients.custom()
                        .setSSLHostnameVerifier((NoopHostnameVerifier.INSTANCE))
                        .setSSLContext(sslContext)
                        .build();
            } else {
                client = HttpAsyncClients.createDefault();
            }

            client.start();
            return client;
        } catch (KeyManagementException | NoSuchAlgorithmException
                | KeyStoreException e) {
            throw new NextcloudApiException(e);
        }
    }

    public interface ResponseParser<R> {
        R parseResponse(Reader reader);
    }

    @Override
    public void close() throws IOException {
        client.close();
    }
}
