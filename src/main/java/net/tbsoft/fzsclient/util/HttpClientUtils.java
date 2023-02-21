package net.tbsoft.fzsclient.util;

import lombok.extern.slf4j.Slf4j;
import net.tbsoft.fzsclient.fzsagent.Response;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;


import java.io.IOException;
import java.net.URI;

@Slf4j
public class HttpClientUtils {

    private static final CloseableHttpClient httpClient = HttpClients.createDefault();


    public static Response<byte[]> httpRequest(HttpHost httpHost, HttpRequest httpRequest) {
        CloseableHttpResponse httpResponse = null;
        log.info("http request={}{}", httpHost.toURI(), httpRequest.getRequestLine().getUri());
        try {
            httpResponse = httpClient.execute(httpHost, httpRequest);
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_OK) {
                byte[] result = EntityUtils.toByteArray(httpResponse.getEntity());
                return Response.success(0, httpRequest.getRequestLine().getUri(), result);
            }
            return Response.failure(-1, httpRequest.getRequestLine() + ",响应码:" + statusCode);
        } catch (IOException e) {
            return Response.failure(-1, httpRequest.getRequestLine() + ",错误信息:" + e.getMessage());
        } finally {
            if (httpResponse != null) {
                try {
                    httpResponse.close();
                } catch (IOException ignored) {
                }
            }
        }

    }

    public static Response<byte[]> httpRequestGetFzsFull(HttpHost httpHost, int mapId) {
        HttpGet httpRequest = new HttpGet();
        httpRequest.setURI(URI.create("/fzsfull?" + "mapno=" + mapId));
        return httpRequest(httpHost, httpRequest);
    }

    public static Response<byte[]> httpRequestGetFzsStop(HttpHost httpHost, int mapId) {
        HttpGet httpRequest = new HttpGet();
        httpRequest.setURI(URI.create("/mapstop?" + "map_no=" + mapId));
        return httpRequest(httpHost, httpRequest);
    }
}

