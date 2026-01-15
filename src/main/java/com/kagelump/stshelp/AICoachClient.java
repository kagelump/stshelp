package com.kagelump.stshelp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Client for communicating with LLM APIs directly.
 * Processes game state and receives advice from AI.
 */
public class AICoachClient {

    private static final String DEFAULT_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    
    private String apiKey;
    private String endpoint;
    private String model;
    private Gson gson;
    private ExecutorService executor;
    private LLMClient llmClient;

    public interface AdviceCallback {
        void onAdviceReceived(String advice);
        void onError(String error);
    }

    public AICoachClient() {
        this.gson = new Gson();
        this.executor = Executors.newSingleThreadExecutor();
        
        // Load configuration
        loadConfiguration();
        
        // Initialize LLM client
        this.llmClient = new LLMClient(apiKey, endpoint, model);
    }

    private void loadConfiguration() {
        // Default values
        this.apiKey = System.getenv("OPENAI_API_KEY");
        this.endpoint = System.getenv("OPENAI_ENDPOINT");
        this.model = System.getenv("OPENAI_MODEL");
        
        // Try to load from config file
        File configFile = new File("stshelp_config.json");
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                JsonObject config = gson.fromJson(reader, JsonObject.class);
                
                // Override with config file values if present
                if (config.has("openai_api_key") && (apiKey == null || apiKey.isEmpty())) {
                    apiKey = config.get("openai_api_key").getAsString();
                }
                if (config.has("openai_endpoint") && (endpoint == null || endpoint.isEmpty())) {
                    endpoint = config.get("openai_endpoint").getAsString();
                }
                if (config.has("model") && (model == null || model.isEmpty())) {
                    model = config.get("model").getAsString();
                }
            } catch (Exception e) {
                STSHelpMod.logger.warn("Failed to load config file", e);
            }
        }
        
        // Set defaults if still null
        if (endpoint == null || endpoint.isEmpty()) {
            endpoint = DEFAULT_ENDPOINT;
        }
        if (model == null || model.isEmpty()) {
            model = DEFAULT_MODEL;
        }
        
        // Log configuration (without sensitive data)
        STSHelpMod.logger.info("LLM Configuration - Model: " + model);
    }

    public void requestAdvice(String gameStateJson, AdviceCallback callback) {
        // Run in background thread to avoid blocking the game
        executor.submit(() -> {
            try {
                // Parse game state
                JsonObject gameState = gson.fromJson(gameStateJson, JsonObject.class);
                
                // Check for error in game state
                if (gameState.has("error")) {
                    callback.onAdviceReceived(gameState.get("error").getAsString());
                    return;
                }
                
                // Create prompt from game state
                String prompt = llmClient.createPrompt(gameState);
                STSHelpMod.logger.info("Created prompt for LLM");
                
                // Get advice from LLM
                String advice = llmClient.getAdvice(prompt);
                callback.onAdviceReceived(advice);
            } catch (Exception e) {
                STSHelpMod.logger.error("Error requesting advice", e);
                callback.onError("Error: " + e.getMessage());
            }
        });
    }

    public void shutdown() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
