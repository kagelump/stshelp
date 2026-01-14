package com.kagelump.stshelp;

import basemod.BaseMod;
import basemod.ModPanel;
import basemod.interfaces.*;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SpireInitializer
public class STSHelpMod implements
        PostInitializeSubscriber,
        PostUpdateSubscriber,
        OnStartBattleSubscriber {

    public static final Logger logger = LogManager.getLogger(STSHelpMod.class.getName());
    private static final String MOD_NAME = "STS Help";
    private static final String AUTHOR = "kagelump";
    private static final String DESCRIPTION = "AI Coach for Slay the Spire";

    private HelpButton helpButton;
    private AdviceScreen adviceScreen;
    private AICoachClient aiClient;
    private static STSHelpMod instance;

    public STSHelpMod() {
        logger.info("Initializing STS Help Mod");
        BaseMod.subscribe(this);
        instance = this;
        
        // Initialize AI client
        aiClient = new AICoachClient();
        
        // Add shutdown hook for cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down STS Help Mod");
            if (aiClient != null) {
                aiClient.shutdown();
            }
        }));
    }

    public static void initialize() {
        new STSHelpMod();
    }

    public static STSHelpMod getInstance() {
        return instance;
    }

    @Override
    public void receivePostInitialize() {
        logger.info("STS Help Post-Initialize");
        
        // Create mod panel
        ModPanel settingsPanel = new ModPanel();
        
        // Load mod badge with fallback
        try {
            BaseMod.registerModBadge(
                ImageMaster.loadImage("images/modBadge.png"),
                MOD_NAME,
                AUTHOR,
                DESCRIPTION,
                settingsPanel
            );
        } catch (Exception e) {
            logger.warn("Failed to load mod badge image, using default", e);
            // Use a default image or skip badge registration
        }

        // Initialize Help button
        helpButton = new HelpButton(this);
        
        // Initialize advice screen
        adviceScreen = new AdviceScreen();
    }

    @Override
    public void receivePostUpdate() {
        // Update components each frame
        if (helpButton != null) {
            helpButton.update();
        }
        if (adviceScreen != null) {
            adviceScreen.update();
        }
    }

    public void requestAdvice() {
        logger.info("Requesting AI advice");
        try {
            // Get current game state
            GameStateExtractor extractor = new GameStateExtractor();
            String gameState = extractor.extractState();
            
            // Send to AI client
            aiClient.requestAdvice(gameState, new AICoachClient.AdviceCallback() {
                @Override
                public void onAdviceReceived(String advice) {
                    logger.info("Advice received");
                    adviceScreen.showAdvice(advice);
                }

                @Override
                public void onError(String error) {
                    logger.error("Error getting advice: " + error);
                    adviceScreen.showAdvice("Error: " + error);
                }
            });
        } catch (Exception e) {
            logger.error("Failed to request advice", e);
            adviceScreen.showAdvice("Error: Failed to extract game state");
        }
    }

    @Override
    public void receiveOnBattleStart(com.megacrit.cardcrawl.rooms.AbstractRoom room) {
        // Optional: Could be used for pre-battle advice
    }

    public AdviceScreen getAdviceScreen() {
        return adviceScreen;
    }
}
