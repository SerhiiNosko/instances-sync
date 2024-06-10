package org.folio;

import okhttp3.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import java.nio.charset.StandardCharsets;

public class RestClient {
    private static final Logger logger = LogManager.getLogger(RestClient.class);

    private final OkHttpClient client;
    private final String okapiUrl;
    private final String tenant;
    private final String username;
    private final String password;

    public RestClient(String okapiUrl, String tenant, String username, String password) {
        this.okapiUrl = okapiUrl;
        this.tenant = tenant;
        this.username = username;
        this.password = password;
        this.client = new OkHttpClient();
    }

    public Headers login() throws IOException {
        Headers headers = Headers.of("x-okapi-tenant", tenant, "Content-Type", "application/json");
        RequestBody postBody = RequestBody.create(new JSONObject()
            .put("username", username)
            .put("password", password).toString().getBytes(StandardCharsets.UTF_8));
        Request request = new Request.Builder()
            .url(okapiUrl + "/authn/login")
            .headers(headers)
            .post(postBody)
            .build();
        try (Response response = client.newCall(request).execute()) {
            JSONObject res = new JSONObject(response.body().string());
            return Headers.of("x-okapi-tenant", tenant, "x-okapi-token", res.getString("okapiToken"), "Content-Type", "application/json");
        }
    }

    public JSONArray getInstances(Headers headers) throws Exception {
        HttpUrl url = HttpUrl.parse(okapiUrl + "/instance-storage/instances").newBuilder()
            .addQueryParameter("limit", String.valueOf(Integer.MAX_VALUE))
            .build();
        Request request = new Request.Builder()
            .url(url)
            .headers(headers)
            .get()
            .build();

        try (Response response = client.newCall(request).execute()) {
            JSONObject res = new JSONObject(response.body().string());
            return res.getJSONArray("instances");
        }
    }

    public void updateInstances(Headers headers, JSONArray instancesChunk) throws Exception {
        RequestBody postBody = RequestBody.create(new JSONObject()
            .put("instances", instancesChunk).toString().getBytes(StandardCharsets.UTF_8));
        HttpUrl url = HttpUrl.parse(okapiUrl + "/instance-storage/batch/synchronous").newBuilder()
            .addQueryParameter("upsert", Boolean.TRUE.toString())
            .build();
        Request request = new Request.Builder()
            .url(url)
            .headers(headers)
            .post(postBody)
            .build();

        try (Response response = client.newCall(request).execute()) {
            logger.info("Update {} instances operation completes with status code: {}", instancesChunk.length(), response.code());
            String errorResponseBody = response.body().string();
            if (!errorResponseBody.isEmpty()) {
                logger.error("Update instances operation failed with errors: {}", errorResponseBody);
            }
        }
    }
}
