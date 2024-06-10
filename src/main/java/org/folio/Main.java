package org.folio;

import okhttp3.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;

import java.util.LinkedHashMap;
import java.util.Map;


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
        int totalInstances = restClient.getTotalInstances(headers);
        if (totalInstances == 0) {
            logger.info("No instances matched conditions found, returning...");
        }
        logger.info("Retrieved total number of instances matched date filter: {}", totalInstances);
        // we need to retrieve all instances and keep in memory before updating,
        // because query filter uses 'updatedDate' field that is modifying during update operation
        Map<Integer, JSONArray> instanceChunks = getAllInstanceChunks(headers, restClient, totalInstances, chunkSize);

        for (Map.Entry<Integer, JSONArray> entry : instanceChunks.entrySet()) {
            int i = entry.getKey();
            JSONArray instancesChunk = entry.getValue();
            restClient.updateInstancesInBulk(headers, instancesChunk);
            logger.info("Chunk {} has been processed", i + 1);
        }
    }

    private static Map<Integer, JSONArray> getAllInstanceChunks(Headers headers, RestClient restClient,
                                                                int totalInstances, int chunkSize) throws Exception {
        Map<Integer, JSONArray> result = new LinkedHashMap<>();
        int totalPages = (int) Math.ceil((double) totalInstances / chunkSize);
        logger.info("Calculated total chunks: {} with chunk size: {}", totalPages, chunkSize);
        for (int i = 0; i < totalPages; i++) {
            int offset = i * chunkSize;
            logger.info("Started retrieving data for chunk: {}", i + 1);
            JSONArray instances = restClient.getInstances(headers, offset, chunkSize);
            result.put(i, instances);
        }
        logger.info("Total retrieved instances: {} into {} chunks", result.values().stream().mapToInt(JSONArray::length).sum(), totalPages);
        return result;
    }
}
