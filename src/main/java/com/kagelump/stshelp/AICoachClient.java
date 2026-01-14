package com.kagelump.stshelp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client for communicating with the AI middleware/backend.
 * Sends game state and receives advice.
 */
public class AICoachClient {

    private static final String DEFAULT_ENDPOINT = "http://localhost:5000/advice";
    private String endpoint;
    private Gson gson;
    private ExecutorService executor;

    public interface AdviceCallback {
        void onAdviceReceived(String advice);
        void onError(String error);
    }

    public AICoachClient() {
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
        
        // Load endpoint from config or use default
        this.endpoint = loadEndpoint();
    }

    private String loadEndpoint() {
        // Try to load from config file
        File configFile = new File("stshelp_config.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject config = gson.fromJson(reader, JsonObject.class);
                if (config.has("endpoint")) {
                    return config.get("endpoint").getAsString();
                }
            } catch (Exception e) {
                STSHelpMod.logger.warn("Failed to load config, using default endpoint", e);
            }
        }
        return DEFAULT_ENDPOINT;
    }

    public void requestAdvice(String gameState, AdviceCallback callback) {
        // Run in background thread to avoid blocking the game
        executor.submit(() -> {
            try {
                String advice = sendRequest(gameState);
                callback.onAdviceReceived(advice);
            } catch (Exception e) {
                STSHelpMod.logger.error("Error requesting advice", e);
                callback.onError(e.getMessage());
            }
        });
    }

    private String sendRequest(String gameState) throws IOException {
        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        
        try {
            // Configure connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);

            // Send game state
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = gameState.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                return readResponse(conn.getInputStream());
            } else {
                String error = readResponse(conn.getErrorStream());
                throw new IOException("HTTP " + responseCode + ": " + error);
            }
        } finally {
            conn.disconnect();
        }
    }

    private String readResponse(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }
        
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line).append("\n");
            }
            return response.toString();
        }
    }

    public void shutdown() {
        if (executor != null) {
            executor.shutdown();
        }
    }
}
