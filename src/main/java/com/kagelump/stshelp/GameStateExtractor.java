package com.kagelump.stshelp;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.relics.AbstractRelic;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts essential game state information for AI processing.
 * Strips down to: HP, Deck, Relics, Enemy Intent
 */
public class GameStateExtractor {

    private Gson gson;

    public GameStateExtractor() {
        this.gson = new Gson();
    }

    public String extractState() {
        JsonObject state = new JsonObject();

        // Check if we're in a run
        if (AbstractDungeon.player == null) {
            state.addProperty("error", "No active game");
            return gson.toJson(state);
        }

        AbstractPlayer player = AbstractDungeon.player;

        // Extract player info
        JsonObject playerInfo = new JsonObject();
        playerInfo.addProperty("current_hp", player.currentHealth);
        playerInfo.addProperty("max_hp", player.maxHealth);
        playerInfo.addProperty("current_energy", player.energy.energy);
        playerInfo.addProperty("gold", player.gold);
        playerInfo.addProperty("character", player.name);
        state.add("player", playerInfo);

        // Extract deck
        List<String> deck = new ArrayList<>();
        for (AbstractCard card : player.masterDeck.group) {
            deck.add(card.name + (card.upgraded ? "+" : ""));
        }
        state.add("deck", gson.toJsonTree(deck));

        // Extract relics
        List<String> relics = new ArrayList<>();
        for (AbstractRelic relic : player.relics) {
            relics.add(relic.name);
        }
        state.add("relics", gson.toJsonTree(relics));

        // Extract combat state if in combat
        if (AbstractDungeon.isPlayerInDungeon() && AbstractDungeon.getCurrRoom() != null) {
            JsonObject combatInfo = new JsonObject();
            combatInfo.addProperty("floor", AbstractDungeon.floorNum);
            combatInfo.addProperty("act", AbstractDungeon.actNum);
            
            // Extract hand
            if (player.hand != null && !player.hand.isEmpty()) {
                List<String> hand = new ArrayList<>();
                for (AbstractCard card : player.hand.group) {
                    hand.add(card.name + (card.upgraded ? "+" : ""));
                }
                combatInfo.add("hand", gson.toJsonTree(hand));
            }

            // Extract draw pile count
            if (player.drawPile != null) {
                combatInfo.addProperty("draw_pile_size", player.drawPile.size());
            }

            // Extract discard pile count
            if (player.discardPile != null) {
                combatInfo.addProperty("discard_pile_size", player.discardPile.size());
            }

            // Extract enemy information
            if (AbstractDungeon.getMonsters() != null && !AbstractDungeon.getMonsters().areMonstersBasicallyDead()) {
                List<JsonObject> enemies = new ArrayList<>();
                for (AbstractMonster monster : AbstractDungeon.getMonsters().monsters) {
                    if (!monster.isDead && !monster.escaped) {
                        JsonObject enemyInfo = new JsonObject();
                        enemyInfo.addProperty("name", monster.name);
                        enemyInfo.addProperty("current_hp", monster.currentHealth);
                        enemyInfo.addProperty("max_hp", monster.maxHealth);
                        
                        // Get intent (this is key for AI advice)
                        if (monster.intent != null) {
                            enemyInfo.addProperty("intent", monster.intent.toString());
                            if (monster.intentDmg > 0) {
                                enemyInfo.addProperty("intent_damage", monster.intentDmg);
                            }
                        }
                        
                        enemies.add(enemyInfo);
                    }
                }
                combatInfo.add("enemies", gson.toJsonTree(enemies));
            }

            state.add("combat", combatInfo);
        }

        // Add current floor and act even outside combat
        state.addProperty("floor", AbstractDungeon.floorNum);
        state.addProperty("act", AbstractDungeon.actNum);

        return gson.toJson(state);
    }
}
