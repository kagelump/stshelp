#!/usr/bin/env python3
"""
Test script for STS Help middleware
Tests state extraction, prompt generation, and server endpoints

Project structure:
  stshelp/
    ├── middleware/
    │   └── server.py
    └── test_middleware.py (this file)
"""

import sys
import os
import json
import requests
import time

# Add middleware to path - expects to be run from project root
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'middleware'))

from server import strip_to_essential_state, create_prompt

# Test data
SAMPLE_GAME_STATE = {
    "player": {
        "current_hp": 45,
        "max_hp": 80,
        "current_energy": 3,
        "gold": 125,
        "character": "The Ironclad"
    },
    "deck": [
        "Strike", "Strike", "Strike", "Strike", "Strike",
        "Defend", "Defend", "Defend", "Defend",
        "Bash", "Carnage+", "Twin Strike"
    ],
    "relics": [
        "Burning Blood",
        "Anchor",
        "Red Skull"
    ],
    "combat": {
        "floor": 12,
        "act": 1,
        "hand": ["Strike", "Strike", "Defend", "Carnage+"],
        "draw_pile_size": 8,
        "discard_pile_size": 3,
        "enemies": [
            {
                "name": "Jaw Worm",
                "current_hp": 25,
                "max_hp": 44,
                "intent": "ATTACK",
                "intent_damage": 11
            }
        ]
    },
    "floor": 12,
    "act": 1
}

SAMPLE_NON_COMBAT_STATE = {
    "player": {
        "current_hp": 60,
        "max_hp": 80,
        "current_energy": 3,
        "gold": 250,
        "character": "The Silent"
    },
    "deck": [
        "Strike", "Strike", "Strike", "Strike",
        "Defend", "Defend", "Defend", "Defend",
        "Neutralize", "Blade Dance+", "Poison Stab"
    ],
    "relics": [
        "Ring of the Snake",
        "Oddly Smooth Stone",
        "Vajra"
    ],
    "floor": 8,
    "act": 1
}

def test_state_extraction():
    """Test that state extraction works correctly"""
    print("Testing state extraction...")
    
    essential = strip_to_essential_state(SAMPLE_GAME_STATE)
    
    # Verify essential fields
    assert "hp" in essential
    assert essential["hp"] == "45/80"
    assert "energy" in essential
    assert essential["energy"] == 3
    assert "deck" in essential
    assert len(essential["deck"]) == 12
    assert "relics" in essential
    assert len(essential["relics"]) == 3
    assert "enemies" in essential
    assert len(essential["enemies"]) == 1
    
    print("✓ State extraction test passed")
    return True

def test_prompt_generation():
    """Test that prompt generation works correctly"""
    print("\nTesting prompt generation...")
    
    essential = strip_to_essential_state(SAMPLE_GAME_STATE)
    prompt = create_prompt(essential)
    
    # Verify prompt contains key information
    assert "The Ironclad" in prompt
    assert "45/80" in prompt
    assert "Jaw Worm" in prompt
    assert "ATTACK" in prompt
    assert "Carnage+" in prompt
    
    print("✓ Prompt generation test passed")
    print("\nGenerated prompt:")
    print("=" * 60)
    print(prompt)
    print("=" * 60)
    return True

def test_non_combat_prompt():
    """Test prompt generation for non-combat state"""
    print("\nTesting non-combat prompt generation...")
    
    essential = strip_to_essential_state(SAMPLE_NON_COMBAT_STATE)
    prompt = create_prompt(essential)
    
    assert "The Silent" in prompt
    assert "Not currently in combat" in prompt
    assert "Blade Dance+" in prompt
    
    print("✓ Non-combat prompt test passed")
    return True

def test_server_health(base_url="http://localhost:5000"):
    """Test server health endpoint"""
    print("\nTesting server health endpoint...")
    
    try:
        response = requests.get(f"{base_url}/health", timeout=5)
        
        if response.status_code != 200:
            print(f"✗ Health check failed with status {response.status_code}")
            return False
        
        data = response.json()
        assert "status" in data
        assert data["status"] == "ok"
        
        print(f"✓ Server health check passed")
        print(f"  Status: {data['status']}")
        print(f"  Model: {data.get('model', 'N/A')}")
        return True
    
    except requests.exceptions.ConnectionError:
        print("✗ Server is not running")
        return False
    except Exception as e:
        print(f"✗ Health check failed: {e}")
        return False

def test_advice_endpoint(base_url="http://localhost:5000"):
    """Test advice endpoint (will fail without API key)"""
    print("\nTesting advice endpoint...")
    
    try:
        response = requests.post(
            f"{base_url}/advice",
            json=SAMPLE_GAME_STATE,
            timeout=10
        )
        
        # We expect either success or API key error
        if response.status_code == 200:
            advice = response.text
            if "Error" in advice and "API key" in advice:
                print("✓ Advice endpoint working (no API key configured)")
                print(f"  Response: {advice}")
                return True
            else:
                print("✓ Advice endpoint working (got advice)")
                print(f"  Advice: {advice[:100]}...")
                return True
        else:
            print(f"✗ Advice endpoint returned status {response.status_code}")
            print(f"  Response: {response.text}")
            return False
    
    except requests.exceptions.ConnectionError:
        print("✗ Server is not running")
        return False
    except Exception as e:
        print(f"✗ Advice endpoint test failed: {e}")
        return False

def main():
    """Run all tests"""
    print("=" * 60)
    print("STS Help Middleware Test Suite")
    print("=" * 60)
    
    results = []
    
    # Unit tests (always run)
    results.append(("State Extraction", test_state_extraction()))
    results.append(("Prompt Generation", test_prompt_generation()))
    results.append(("Non-Combat Prompt", test_non_combat_prompt()))
    
    # Server tests (only if server is available)
    print("\n" + "=" * 60)
    print("Server Integration Tests")
    print("=" * 60)
    print("Note: These tests require the server to be running")
    print("Start server with: cd middleware && python server.py")
    
    time.sleep(1)
    
    health_ok = test_server_health()
    results.append(("Server Health", health_ok))
    
    if health_ok:
        results.append(("Advice Endpoint", test_advice_endpoint()))
    
    # Summary
    print("\n" + "=" * 60)
    print("Test Summary")
    print("=" * 60)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for name, result in results:
        status = "✓ PASS" if result else "✗ FAIL"
        print(f"{status}: {name}")
    
    print(f"\n{passed}/{total} tests passed")
    
    if passed == total:
        print("\n🎉 All tests passed!")
        return 0
    else:
        print(f"\n⚠️  {total - passed} test(s) failed")
        return 1

if __name__ == "__main__":
    sys.exit(main())
