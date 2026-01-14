package com.kagelump.stshelp;

import basemod.TopPanelItem;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;

public class HelpButton extends TopPanelItem {
    
    private static final String BUTTON_ID = "stshelp:HelpButton";
    private static final String IMG_PATH = "images/helpButton.png";
    private static final float SCALE = Settings.scale;
    
    private STSHelpMod mod;
    private Texture buttonTexture;
    private boolean isHovered = false;

    public HelpButton(STSHelpMod mod) {
        super(loadTexture(), BUTTON_ID);
        this.mod = mod;
        this.buttonTexture = loadTexture();
    }

    private static Texture loadTexture() {
        try {
            return ImageMaster.loadImage(IMG_PATH);
        } catch (Exception e) {
            // Use a placeholder if image not found
            STSHelpMod.logger.warn("Help button image not found, using placeholder");
            return ImageMaster.loadImage("images/ui/top/config.png");
        }
    }

    @Override
    protected void onClick() {
        STSHelpMod.logger.info("Help button clicked");
        mod.requestAdvice();
    }

    public void update() {
        // Update hover state based on mouse position
        updateHover();
    }

    private void updateHover() {
        // Check if mouse is hovering over the button
        if (this.hitbox != null && this.hitbox.hovered) {
            isHovered = true;
        } else {
            isHovered = false;
        }
    }

    @Override
    public void render(SpriteBatch sb) {
        super.render(sb);
        
        // Additional rendering logic if needed
        if (isHovered) {
            // Could add hover effect here
        }
    }
}
