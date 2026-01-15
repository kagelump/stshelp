package com.kagelump.stshelp;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Client for communicating directly with OpenAI-compatible LLM APIs.
 * Handles prompt creation and API communication.
 */
public class LLMClient {

    private static final String DEFAULT_ENDPOINT = "https://api.openai.com/v1/chat/completions";
    private static final String DEFAULT_MODEL = "gpt-3.5-turbo";
    private static final int MAX_TOKENS = 200;
    private static final double TEMPERATURE = 0.7;

    private String endpoint;
    private String apiKey;
    private String model;
    private Gson gson;

    public LLMClient(String apiKey, String endpoint, String model) {
        this.apiKey = apiKey;
        this.endpoint = endpoint != null ? endpoint : DEFAULT_ENDPOINT;
        this.model = model != null ? model : DEFAULT_MODEL;
        this.gson = new Gson();
    }

    /**
     * Create a prompt for the LLM based on game state.
     */
    public String createPrompt(JsonObject gameState) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert Slay the Spire coach. Analyze the current game state and provide concise, actionable advice.\n\n");
        prompt.append("Game State:\n");

        // Player info
        if (gameState.has("player")) {
            JsonObject player = gameState.getAsJsonObject("player");
            if (player.has("character")) {
                prompt.append("Character: ").append(player.get("character").getAsString()).append("\n");
            }
            if (player.has("current_hp") && player.has("max_hp")) {
                prompt.append("HP: ").append(player.get("current_hp").getAsInt())
                      .append("/").append(player.get("max_hp").getAsInt()).append("\n");
            }
            if (player.has("current_energy")) {
                prompt.append("Energy: ").append(player.get("current_energy").getAsInt()).append("\n");
            }
            if (player.has("gold")) {
                prompt.append("Gold: ").append(player.get("gold").getAsInt()).append("\n");
            }
        }

        // Floor and act
        if (gameState.has("floor") && gameState.has("act")) {
            prompt.append("Floor: ").append(gameState.get("floor").getAsInt())
                  .append(" (Act ").append(gameState.get("act").getAsInt()).append(")\n");
        }

        // Deck
        if (gameState.has("deck")) {
            JsonArray deck = gameState.getAsJsonArray("deck");
            prompt.append("\nDeck (").append(deck.size()).append(" cards):\n");
            StringBuilder deckList = new StringBuilder();
            for (int i = 0; i < deck.size(); i++) {
                if (i > 0) deckList.append(", ");
                deckList.append(deck.get(i).getAsString());
            }
            prompt.append(deckList.toString()).append("\n");
        }

        // Relics
        if (gameState.has("relics")) {
            JsonArray relics = gameState.getAsJsonArray("relics");
            prompt.append("\nRelics:\n");
            StringBuilder relicList = new StringBuilder();
            for (int i = 0; i < relics.size(); i++) {
                if (i > 0) relicList.append(", ");
                relicList.append(relics.get(i).getAsString());
            }
            prompt.append(relicList.toString()).append("\n");
        }

        // Combat info
        if (gameState.has("combat")) {
            JsonObject combat = gameState.getAsJsonObject("combat");
            prompt.append("\n=== COMBAT ===\n");

            if (combat.has("hand")) {
                JsonArray hand = combat.getAsJsonArray("hand");
                prompt.append("Hand: ");
                StringBuilder handList = new StringBuilder();
                for (int i = 0; i < hand.size(); i++) {
                    if (i > 0) handList.append(", ");
                    handList.append(hand.get(i).getAsString());
                }
                prompt.append(handList.toString()).append("\n");
            }

            if (combat.has("enemies")) {
                JsonArray enemies = combat.getAsJsonArray("enemies");
                prompt.append("\nEnemies:\n");
                for (int i = 0; i < enemies.size(); i++) {
                    JsonObject enemy = enemies.get(i).getAsJsonObject();
                    prompt.append("  - ").append(enemy.get("name").getAsString())
                          .append(": HP ").append(enemy.get("current_hp").getAsInt())
                          .append("/").append(enemy.get("max_hp").getAsInt());
                    
                    if (enemy.has("intent")) {
                        prompt.append(", Intent: ").append(enemy.get("intent").getAsString());
                        if (enemy.has("intent_damage") && enemy.get("intent_damage").getAsInt() > 0) {
                            prompt.append(" (Damage: ").append(enemy.get("intent_damage").getAsInt()).append(")");
                        }
                    }
                    prompt.append("\n");
                }
            }

            prompt.append("\nProvide specific advice for this combat turn. What cards should be played and in what order?\n");
        } else {
            prompt.append("\nNot currently in combat. Provide general strategy advice for the current run.\n");
        }

        return prompt.toString();
    }

    /**
     * Send prompt to LLM API and get advice.
     */
    public String getAdvice(String prompt) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            return "Error: API key not configured. Please configure your API credentials.";
        }

        URL url = new URL(endpoint);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            // Configure connection
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "Bearer " + apiKey);
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);

            // Create request payload
            JsonObject payload = new JsonObject();
            payload.addProperty("model", model);
            payload.addProperty("max_tokens", MAX_TOKENS);
            payload.addProperty("temperature", TEMPERATURE);

            // Create messages array
            JsonArray messages = new JsonArray();
            
            // System message
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            systemMessage.addProperty("content", "You are an expert Slay the Spire coach. Provide concise, actionable advice in 2-3 sentences.");
            messages.add(systemMessage);

            // User message
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            userMessage.addProperty("content", prompt);
            messages.add(userMessage);

            payload.add("messages", messages);

            // Send request
            String jsonPayload = gson.toJson(payload);
            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // Read response
            int responseCode = conn.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String responseText = readResponse(conn.getInputStream());
                return parseAdviceFromResponse(responseText);
            } else {
                String error = readResponse(conn.getErrorStream());
                throw new IOException("HTTP " + responseCode + ": " + error);
            }
        } finally {
            conn.disconnect();
        }
    }

    private String parseAdviceFromResponse(String responseText) {
        try {
            JsonObject response = gson.fromJson(responseText, JsonObject.class);
            JsonArray choices = response.getAsJsonArray("choices");
            if (choices != null && choices.size() > 0) {
                JsonObject firstChoice = choices.get(0).getAsJsonObject();
                JsonObject message = firstChoice.getAsJsonObject("message");
                if (message != null && message.has("content")) {
                    return message.get("content").getAsString().trim();
                }
            }
            return "Error: Could not parse LLM response";
        } catch (Exception e) {
            STSHelpMod.logger.error("Error parsing LLM response", e);
            return "Error parsing LLM response: " + e.getMessage();
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
}
