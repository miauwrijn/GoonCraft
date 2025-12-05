# GoonCraft Development Makefile
# ==============================
# Run from project root: make -C dev <command>
# Or from dev folder: make <command>
#
# Commands:
#   make start   - Start the Minecraft server
#   make stop    - Stop the server
#   make build   - Build the plugin
#   make reload  - Reload the plugin
#   make dev     - Build + Reload (full dev cycle)
#   make logs    - View server logs

.PHONY: start stop build reload dev logs console clean setup help

help:
	@echo ""
	@echo "  GoonCraft Dev Commands"
	@echo "  ======================"
	@echo ""
	@echo "  make start   - Start the Minecraft server"
	@echo "  make stop    - Stop the server"
	@echo "  make build   - Build the plugin"
	@echo "  make reload  - Reload plugin on server"
	@echo "  make dev     - Build + Reload (main dev command)"
	@echo "  make logs    - View server logs"
	@echo "  make console - Attach to server console"
	@echo "  make setup   - First time setup"
	@echo ""

start:
	@echo "🚀 Starting GoonCraft server..."
	@docker compose up -d minecraft
	@echo "✅ Server starting at localhost:25565"

stop:
	@echo "🛑 Stopping server..."
	@docker compose down
	@echo "✅ Server stopped"

build:
	@echo "🔨 Building plugin..."
	@docker compose run --rm build

reload:
	@echo "🔄 Reloading plugin..."
	@docker compose run --rm reload || echo "⚠️  Server might not be running"

dev: build reload
	@echo "✅ Dev cycle complete!"

logs:
	@docker compose logs -f minecraft

console:
	@docker attach gooncraft-server

setup:
	@echo "📦 Setting up development environment..."
	@mkdir -p ../server-data/plugins
	@docker compose pull
	@$(MAKE) build
	@echo "✅ Setup complete! Run 'make start' to start the server"

clean:
	@echo "🧹 Cleaning..."
	@docker compose run --rm build mvn clean -q
	@echo "✅ Clean complete"

