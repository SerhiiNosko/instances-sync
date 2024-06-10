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
        int totalInstances = restClient.getTotalInstances(headers);
        if (totalInstances == 0) {
            logger.info("No instances matched conditions found, returning...");
        }
        logger.info("Retrieved total number of instances matched date filter: {}", totalInstances);
        int totalPages = (int) Math.ceil((double) totalInstances / chunkSize);
        logger.info("Calculated total pages: {} with chunk size: {}", totalPages, chunkSize);
        for (int i = 0; i < totalPages; i++) {
            int offset = i * chunkSize;
            logger.info("Started processing {} chunk with chunk size: {}", i + 1, chunkSize);
            JSONArray instances = restClient.getInstances(headers, offset, chunkSize);
            restClient.updateInstancesInBulk(headers, instances);
            logger.info("Chunk {} has been processed", i + 1);
        }
    }
}
