package com.kagelump.stshelp;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.screens.mainMenu.MainMenuScreen;

public class AdviceScreen {
    
    private static final float SCREEN_X = Settings.WIDTH / 4.0f;
    private static final float SCREEN_Y = Settings.HEIGHT / 4.0f;
    private static final float SCREEN_W = Settings.WIDTH / 2.0f;
    private static final float SCREEN_H = Settings.HEIGHT / 2.0f;
    
    private boolean isOpen = false;
    private String currentAdvice = "";
    private float scrollY = 0.0f;
    
    public AdviceScreen() {
        // Initialize
    }

    public void showAdvice(String advice) {
        this.currentAdvice = advice;
        this.isOpen = true;
        this.scrollY = 0.0f;
        
        // Pause the game when showing advice
        CardCrawlGame.isPopupOpen = true;
    }

    public void hide() {
        this.isOpen = false;
        CardCrawlGame.isPopupOpen = false;
    }

    public void update() {
        if (!isOpen) {
            return;
        }

        // Handle close on ESC or click outside
        if (InputHelper.pressedEscape || 
            (InputHelper.justClickedLeft && !isMouseInBounds())) {
            InputHelper.pressedEscape = false;
            hide();
        }

        // Handle scrolling if needed
        updateScroll();
    }

    private void updateScroll() {
        // Simple scroll handling
        if (isMouseInBounds() && InputHelper.scrolledDown) {
            scrollY -= 50.0f * Settings.scale;
        } else if (isMouseInBounds() && InputHelper.scrolledUp) {
            scrollY += 50.0f * Settings.scale;
        }
        
        // Clamp scroll
        if (scrollY > 0) scrollY = 0;
    }

    private boolean isMouseInBounds() {
        float mx = InputHelper.mX;
        float my = InputHelper.mY;
        return mx >= SCREEN_X && mx <= SCREEN_X + SCREEN_W &&
               my >= SCREEN_Y && my <= SCREEN_Y + SCREEN_H;
    }

    public void render(SpriteBatch sb) {
        if (!isOpen) {
            return;
        }

        // Render background overlay
        sb.setColor(new Color(0, 0, 0, 0.8f));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, 0, 0, Settings.WIDTH, Settings.HEIGHT);

        // Render advice panel
        sb.setColor(new Color(0.2f, 0.2f, 0.25f, 0.95f));
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, SCREEN_X, SCREEN_Y, SCREEN_W, SCREEN_H);

        // Render border
        sb.setColor(new Color(0.4f, 0.6f, 0.8f, 1.0f));
        float borderWidth = 3.0f * Settings.scale;
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, SCREEN_X - borderWidth, SCREEN_Y - borderWidth, 
                SCREEN_W + 2 * borderWidth, borderWidth); // bottom
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, SCREEN_X - borderWidth, SCREEN_Y + SCREEN_H, 
                SCREEN_W + 2 * borderWidth, borderWidth); // top
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, SCREEN_X - borderWidth, SCREEN_Y, 
                borderWidth, SCREEN_H); // left
        sb.draw(ImageMaster.WHITE_SQUARE_IMG, SCREEN_X + SCREEN_W, SCREEN_Y, 
                borderWidth, SCREEN_H); // right

        // Render title
        String title = "AI Coach Advice";
        FontHelper.renderFontCentered(sb, FontHelper.panelNameFont, title,
                SCREEN_X + SCREEN_W / 2, SCREEN_Y + SCREEN_H - 50.0f * Settings.scale,
                Color.WHITE);

        // Render advice text
        float textX = SCREEN_X + 40.0f * Settings.scale;
        float textY = SCREEN_Y + SCREEN_H - 120.0f * Settings.scale + scrollY;
        float textWidth = SCREEN_W - 80.0f * Settings.scale;
        
        FontHelper.renderSmartText(sb, FontHelper.tipBodyFont, currentAdvice,
                textX, textY, textWidth, 30.0f * Settings.scale, Color.LIGHT_GRAY);

        // Render close hint
        String closeHint = "Press ESC or click outside to close";
        FontHelper.renderFontCentered(sb, FontHelper.tipHeaderFont, closeHint,
                SCREEN_X + SCREEN_W / 2, SCREEN_Y + 30.0f * Settings.scale,
                new Color(0.7f, 0.7f, 0.7f, 1.0f));
        
        sb.setColor(Color.WHITE);
    }

    public boolean isOpen() {
        return isOpen;
    }
}
