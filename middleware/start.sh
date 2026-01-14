#!/bin/bash
# Startup script for STS Help middleware server

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}Starting STS Help Middleware Server${NC}"
echo "======================================"

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo -e "${RED}Error: Python 3 is not installed${NC}"
    exit 1
fi

# Check if we're in the right directory
if [ ! -f "server.py" ]; then
    echo -e "${YELLOW}Warning: server.py not found in current directory${NC}"
    echo "Changing to middleware directory..."
    cd middleware 2>/dev/null || {
        echo -e "${RED}Error: Cannot find middleware directory${NC}"
        exit 1
    }
fi

# Check if requirements are installed
if ! python3 -c "import flask" &> /dev/null; then
    echo -e "${YELLOW}Installing requirements...${NC}"
    pip3 install -r requirements.txt || {
        echo -e "${RED}Error: Failed to install requirements${NC}"
        exit 1
    }
fi

# Check for configuration
if [ ! -f "config.json" ]; then
    echo -e "${YELLOW}Warning: config.json not found${NC}"
    echo "You can create it from config.example.json or use environment variables"
    echo ""
    
    if [ -z "$OPENAI_API_KEY" ]; then
        echo -e "${RED}Error: No config.json and OPENAI_API_KEY environment variable not set${NC}"
        echo "Please either:"
        echo "  1. Copy config.example.json to config.json and edit it, or"
        echo "  2. Set OPENAI_API_KEY environment variable"
        exit 1
    fi
fi

# Start the server
echo -e "${GREEN}Starting server on port ${PORT:-5000}...${NC}"
echo "Press Ctrl+C to stop"
echo ""

python3 server.py
