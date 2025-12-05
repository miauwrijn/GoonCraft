# GoonCraft Dev Script (PowerShell)
# ==================================
# Usage: .\dev\dev.ps1 <command>
#   start   - Start server
#   stop    - Stop server  
#   build   - Build plugin
#   reload  - Reload plugin
#   dev     - Build + Reload
#   logs    - View logs

param(
    [Parameter(Position=0)]
    [string]$Command = "help"
)

# Change to dev directory
Push-Location $PSScriptRoot

function Write-Success { param($msg) Write-Host $msg -ForegroundColor Green }
function Write-Info { param($msg) Write-Host $msg -ForegroundColor Cyan }
function Write-Warn { param($msg) Write-Host $msg -ForegroundColor Yellow }

try {
    switch ($Command.ToLower()) {
        "start" {
            Write-Info "🚀 Starting GoonCraft server..."
            docker compose up -d minecraft
            Write-Success "✅ Server starting at localhost:25565"
            Write-Host "   Use '.\dev\dev.ps1 logs' to view output"
        }
        "stop" {
            Write-Warn "🛑 Stopping server..."
            docker compose down
            Write-Success "✅ Server stopped"
        }
        "build" {
            Write-Info "🔨 Building plugin..."
            docker compose run --rm build
        }
        "reload" {
            Write-Info "🔄 Reloading plugin..."
            docker compose run --rm reload
        }
        "dev" {
            Write-Info "🔨 Building plugin..."
            docker compose run --rm build
            Write-Info "🔄 Reloading plugin..."
            docker compose run --rm reload
            Write-Success "✅ Dev cycle complete!"
        }
        "logs" {
            docker compose logs -f minecraft
        }
        "console" {
            docker attach gooncraft-server
        }
        "setup" {
            Write-Info "📦 Setting up development environment..."
            New-Item -ItemType Directory -Path "../server-data/plugins" -Force | Out-Null
            docker compose pull
            docker compose run --rm build
            Write-Success "✅ Setup complete! Run '.\dev\dev.ps1 start' to start the server"
        }
        default {
            Write-Host ""
            Write-Host "  GoonCraft Dev Script" -ForegroundColor Magenta
            Write-Host "  ====================" -ForegroundColor Magenta
            Write-Host ""
            Write-Host "  Usage: .\dev\dev.ps1 <command>"
            Write-Host ""
            Write-Host "  Commands:"
            Write-Host "    start   - Start the Minecraft server"
            Write-Host "    stop    - Stop the server"
            Write-Host "    build   - Build the plugin"
            Write-Host "    reload  - Reload plugin on server"
            Write-Host "    dev     - Build + Reload (main dev command)"
            Write-Host "    logs    - View server logs"
            Write-Host "    console - Attach to server console"
            Write-Host "    setup   - First time setup"
            Write-Host ""
        }
    }
} finally {
    Pop-Location
}

