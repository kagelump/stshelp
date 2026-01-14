"""
STS Help Middleware
Receives game state from the mod, processes it, sends to LLM, and returns advice.
"""

from flask import Flask, request, jsonify
import json
import os
import requests
from typing import Dict, Any
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)

# Load configuration
def load_config():
    """Load configuration from config.json or environment variables."""
    config = {
        'openai_api_key': os.environ.get('OPENAI_API_KEY', ''),
        'openai_endpoint': os.environ.get('OPENAI_ENDPOINT', 'https://api.openai.com/v1/chat/completions'),
        'model': os.environ.get('OPENAI_MODEL', 'gpt-3.5-turbo'),
        'port': int(os.environ.get('PORT', 5000))
    }
    
    # Try to load from config file
    if os.path.exists('config.json'):
        try:
            with open('config.json', 'r') as f:
                file_config = json.load(f)
                config.update(file_config)
        except Exception as e:
            logger.warning(f"Failed to load config.json: {e}")
    
    return config

CONFIG = load_config()


def strip_to_essential_state(game_state: Dict[str, Any]) -> Dict[str, Any]:
    """
    Strip game state to essential tokens for LLM processing.
    Keeps only: HP, Deck, Relics, Enemy Intent, Hand, Energy
    """
    essential = {}
    
    # Player info
    if 'player' in game_state:
        player = game_state['player']
        essential['hp'] = f"{player.get('current_hp', 0)}/{player.get('max_hp', 0)}"
        essential['energy'] = player.get('current_energy', 0)
        essential['gold'] = player.get('gold', 0)
        essential['character'] = player.get('character', 'Unknown')
    
    # Deck
    if 'deck' in game_state:
        essential['deck'] = game_state['deck']
    
    # Relics
    if 'relics' in game_state:
        essential['relics'] = game_state['relics']
    
    # Combat info
    if 'combat' in game_state:
        combat = game_state['combat']
        essential['floor'] = combat.get('floor', 0)
        essential['act'] = combat.get('act', 0)
        
        if 'hand' in combat:
            essential['hand'] = combat['hand']
        
        if 'enemies' in combat:
            enemies = []
            for enemy in combat['enemies']:
                enemies.append({
                    'name': enemy.get('name', 'Unknown'),
                    'hp': f"{enemy.get('current_hp', 0)}/{enemy.get('max_hp', 0)}",
                    'intent': enemy.get('intent', 'Unknown'),
                    'damage': enemy.get('intent_damage', 0)
                })
            essential['enemies'] = enemies
    else:
        essential['floor'] = game_state.get('floor', 0)
        essential['act'] = game_state.get('act', 0)
    
    return essential


def create_prompt(essential_state: Dict[str, Any]) -> str:
    """
    Create a prompt for the LLM based on essential game state.
    """
    prompt_parts = [
        "You are an expert Slay the Spire coach. Analyze the current game state and provide concise, actionable advice.",
        "",
        "Game State:"
    ]
    
    # Character and health
    if 'character' in essential_state:
        prompt_parts.append(f"Character: {essential_state['character']}")
    if 'hp' in essential_state:
        prompt_parts.append(f"HP: {essential_state['hp']}")
    if 'energy' in essential_state:
        prompt_parts.append(f"Energy: {essential_state['energy']}")
    
    # Floor and act
    if 'floor' in essential_state and 'act' in essential_state:
        prompt_parts.append(f"Floor: {essential_state['floor']} (Act {essential_state['act']})")
    
    # Deck
    if 'deck' in essential_state:
        prompt_parts.append(f"\nDeck ({len(essential_state['deck'])} cards):")
        prompt_parts.append(", ".join(essential_state['deck']))
    
    # Relics
    if 'relics' in essential_state:
        prompt_parts.append(f"\nRelics:")
        prompt_parts.append(", ".join(essential_state['relics']))
    
    # Combat info
    if 'enemies' in essential_state:
        prompt_parts.append("\n=== COMBAT ===")
        
        if 'hand' in essential_state:
            prompt_parts.append(f"Hand: {', '.join(essential_state['hand'])}")
        
        prompt_parts.append("\nEnemies:")
        for enemy in essential_state['enemies']:
            prompt_parts.append(f"  - {enemy['name']}: HP {enemy['hp']}, Intent: {enemy['intent']}" + 
                              (f" (Damage: {enemy['damage']})" if enemy.get('damage', 0) > 0 else ""))
        
        prompt_parts.append("\nProvide specific advice for this combat turn. What cards should be played and in what order?")
    else:
        prompt_parts.append("\nNot currently in combat. Provide general strategy advice for the current run.")
    
    return "\n".join(prompt_parts)


def get_llm_advice(prompt: str) -> str:
    """
    Send prompt to OpenAI-compatible LLM API and get advice.
    """
    if not CONFIG['openai_api_key']:
        return "Error: OpenAI API key not configured. Please set OPENAI_API_KEY environment variable or add it to config.json"
    
    try:
        headers = {
            'Content-Type': 'application/json',
            'Authorization': f"Bearer {CONFIG['openai_api_key']}"
        }
        
        payload = {
            'model': CONFIG['model'],
            'messages': [
                {
                    'role': 'system',
                    'content': 'You are an expert Slay the Spire coach. Provide concise, actionable advice in 2-3 sentences.'
                },
                {
                    'role': 'user',
                    'content': prompt
                }
            ],
            'max_tokens': 200,
            'temperature': 0.7
        }
        
        response = requests.post(
            CONFIG['openai_endpoint'],
            headers=headers,
            json=payload,
            timeout=30
        )
        response.raise_for_status()
        
        result = response.json()
        advice = result['choices'][0]['message']['content'].strip()
        return advice
        
    except requests.exceptions.RequestException as e:
        logger.error(f"Error calling LLM API: {e}")
        return f"Error calling LLM API: {str(e)}"
    except (KeyError, IndexError) as e:
        logger.error(f"Error parsing LLM response: {e}")
        return f"Error parsing LLM response: {str(e)}"


@app.route('/advice', methods=['POST'])
def get_advice():
    """
    Main endpoint for receiving game state and returning advice.
    """
    try:
        # Parse incoming game state
        game_state = request.get_json()
        
        if not game_state:
            return jsonify({'error': 'No game state provided'}), 400
        
        logger.info(f"Received game state request")
        
        # Check for error in game state
        if 'error' in game_state:
            return game_state['error'], 200
        
        # Strip to essential state
        essential_state = strip_to_essential_state(game_state)
        logger.info(f"Essential state: {json.dumps(essential_state, indent=2)}")
        
        # Create prompt
        prompt = create_prompt(essential_state)
        logger.info(f"Prompt created")
        
        # Get advice from LLM
        advice = get_llm_advice(prompt)
        logger.info(f"Advice generated: {advice}")
        
        return advice, 200
        
    except Exception as e:
        logger.error(f"Error processing request: {e}", exc_info=True)
        return f"Error: {str(e)}", 500


@app.route('/health', methods=['GET'])
def health():
    """Health check endpoint."""
    return jsonify({'status': 'ok', 'config': {
        'has_api_key': bool(CONFIG['openai_api_key']),
        'model': CONFIG['model']
    }})


if __name__ == '__main__':
    logger.info("Starting STS Help Middleware")
    logger.info(f"Config: {json.dumps({k: v for k, v in CONFIG.items() if k != 'openai_api_key'}, indent=2)}")
    app.run(host='0.0.0.0', port=CONFIG['port'], debug=False)
