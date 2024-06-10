package org.folio;

import okhttp3.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;


public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static final int DEFAULT_CHUNK_SIZE = 1000;

    public static void main(String[] args) throws Exception {
        String okapiUrl = System.getProperty("okapiUrl");
        if (okapiUrl == null) {
            throw new IllegalArgumentException("Parameter missed: okapiUrl");
        }
        logger.info("Provided okapiUrl: {}", okapiUrl);

        String tenant = System.getProperty("tenant");
        if (tenant == null) {
            throw new IllegalArgumentException("Parameter missed: tenant");
        }
        logger.info("Provided tenant: {}", tenant);

        String username = System.getProperty("username");
        if (username == null) {
            throw new IllegalArgumentException("Parameter missed: username");
        }
        logger.info("Provided username: {}", username);

        String password = System.getProperty("password");
        if (password == null) {
            throw new IllegalArgumentException("Parameter missed: password");
        }
        logger.info("Provided password: ***");

        String chunkSizeStr = System.getProperty("chunkSize");
        int chunkSize;
        if (chunkSizeStr == null) {
            chunkSize = DEFAULT_CHUNK_SIZE;
            logger.info("Using default chunkSize: {}", DEFAULT_CHUNK_SIZE);
        } else {
            chunkSize = Integer.parseInt(chunkSizeStr);
            logger.info("Provided chunkSize: {}", chunkSize);
        }
        RestClient restClient = new RestClient(okapiUrl, tenant, username, password);
        Headers headers = restClient.login();
        JSONArray allInstances = restClient.getInstances(headers);
        logger.info("Retrieved instances, total: {}", allInstances.length());
        JSONArray[] jsonArrays = splitJSONArray(allInstances, chunkSize);
        for (JSONArray jsonArray: jsonArrays) {
            logger.info("Started processing chunk with {} elements", jsonArray.length());
            restClient.updateInstances(headers, jsonArray);
        }
    }

    private static JSONArray[] splitJSONArray(JSONArray jsonArray, int chunkSize) {
        // standard exited function logic of splitting json array into chunks
        int numOfChunks = (int) Math.ceil((double) jsonArray.length() / chunkSize);
        JSONArray[] jsonArrays = new JSONArray[numOfChunks];

        for (int i = 0; i < numOfChunks; ++i) {
            jsonArrays[i] = new JSONArray();
            for (int j = i * chunkSize; j < (i + 1) * chunkSize; j++) {
                if (j < jsonArray.length()) {
                    jsonArrays[i].put(jsonArray.get(j));
                } else {
                    break;
                }
            }
        }
        return jsonArrays;
    }
}
