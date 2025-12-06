#!/bin/bash
# GoonCraft Dev Script
# ====================
# Cross-platform development script (Linux/macOS/Windows Git Bash)
#
# Usage: ./dev/dev.sh <command>
#   start   - Start server
#   stop    - Stop server
#   build   - Build plugin
#   reload  - Reload plugin
#   dev     - Build + Reload
#   logs    - View logs

set -e
cd "$(dirname "$0")"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

apply_ops() {
    if [ -f .env ]; then
        # Try MC_USERNAME first (current format), then fall back to OPS (legacy format)
        OPS=""
        if grep -q "^MC_USERNAME=" .env; then
            OPS=$(grep "^MC_USERNAME=" .env | cut -d'=' -f2- | xargs)
        elif grep -q "^OPS=" .env; then
            OPS=$(grep "^OPS=" .env | cut -d'=' -f2- | xargs)
        fi
        
        if [ -n "$OPS" ]; then
            echo -e "${CYAN}👑 Applying OPs...${NC}"
            IFS=',' read -ra PLAYERS <<< "$OPS"
            for player in "${PLAYERS[@]}"; do
                player=$(echo "$player" | xargs)
                if [ -n "$player" ]; then
                    docker exec gooncraft-server rcon-cli op "$player" 2>/dev/null || true
                    echo "   OP: $player"
                fi
            done
        else
            echo -e "${YELLOW}⚠️  No MC_USERNAME or OPS found in .env file${NC}"
        fi
    else
        echo -e "${YELLOW}⚠️  .env file not found${NC}"
    fi
}

case "$1" in
  start)
    echo -e "${CYAN}🚀 Starting GoonCraft server...${NC}"
    docker compose up -d minecraft
    echo -e "${GREEN}✅ Server starting at localhost:25565${NC}"
    echo "   Use './dev/dev.sh op' to apply OPs after server is ready"
    ;;
  op)
    apply_ops
    echo -e "${GREEN}✅ OPs applied${NC}"
    ;;
  stop)
    echo -e "${YELLOW}🛑 Stopping server...${NC}"
    docker compose down
    echo -e "${GREEN}✅ Server stopped${NC}"
    ;;
  build)
    echo -e "${CYAN}🔨 Building plugin...${NC}"
    docker compose run --rm build
    ;;
  reload)
    echo -e "${CYAN}🔄 Reloading plugin...${NC}"
    docker compose run --rm reload || echo -e "${YELLOW}⚠️  Server might not be running${NC}"
    ;;
  dev)
    echo -e "${CYAN}🔨 Building plugin...${NC}"
    docker compose run --rm build
    echo -e "${CYAN}🔄 Reloading plugin...${NC}"
    docker compose run --rm reload || echo -e "${YELLOW}⚠️  Server might not be running${NC}"
    echo -e "${GREEN}✅ Dev cycle complete!${NC}"
    ;;
  logs)
    docker compose logs -f minecraft
    ;;
  console)
    docker attach gooncraft-server
    ;;
  setup)
    echo -e "${CYAN}📦 Setting up development environment...${NC}"
    mkdir -p ../server-data/plugins
    docker compose pull
    docker compose run --rm build
    echo -e "${GREEN}✅ Setup complete! Run './dev/dev.sh start' to start the server${NC}"
    ;;
  *)
    echo ""
    echo "  GoonCraft Dev Script"
    echo "  ===================="
    echo ""
    echo "  Usage: ./dev/dev.sh <command>"
    echo ""
    echo "  Commands:"
    echo "    start   - Start the Minecraft server"
    echo "    op      - Apply OPs from .env file"
    echo "    stop    - Stop the server"
    echo "    build   - Build the plugin"
    echo "    reload  - Reload plugin on server"
    echo "    dev     - Build + Reload (main dev command)"
    echo "    logs    - View server logs"
    echo "    console - Attach to server console"
    echo "    setup   - First time setup"
    echo ""
    ;;
esac

