# 🍆 GoonCraft

> *The Minecraft plugin your server never knew it needed (and probably doesn't).*

![Minecraft](https://img.shields.io/badge/Minecraft-1.21-brightgreen)
![Spigot](https://img.shields.io/badge/Spigot-API-orange)
![Java](https://img.shields.io/badge/Java-21-blue)
![Maturity](https://img.shields.io/badge/Maturity-Questionable-red)

---

## 🤔 What is this?

GoonCraft is a *highly sophisticated* Minecraft plugin that adds anatomically... *creative* features to your server. It's the mod you install when you've given up on running a family-friendly server.

**Features include:**
- 🎲 Randomly generated stats for every player
- 📊 Detailed size & girth tracking 
- 🌈 BBC support (Big Block Construct, obviously)
- 💊 Craftable Viagra for *temporary enhancements*
- 👆 The legendary **Buttfinger** command
- ✨ Real-time 3D models using Block Displays
- 🏆 **27 Achievements** to unlock
- 📈 **Statistics tracking** for all your... activities
- 🖥️ **Beautiful GUI menus** for stats & achievements
- 🏅 **Leaderboards** to see who's the biggest gooner

---

## 📦 Installation

1. Download the latest `gooncraft-x.x.x.jar` from releases
2. Drop it in your server's `plugins` folder
3. Restart your server
4. Question your life choices
5. Have fun!

**Requirements:**
- Minecraft 1.21+
- Java 21+
- Spigot/Paper server

---

## 🎮 Commands

### Main Commands

| Command | Alias | Description |
|---------|-------|-------------|
| `/gooncraft stats [player]` | `/gc stats` | Open stats GUI |
| `/gooncraft achievements [player]` | `/gc achievements` | Open achievements GUI |
| `/gooncraft leaderboard [category]` | `/gc lb` | View leaderboards |
| `/gooncraft reload` | `/gc reload` | Reload config (OP) |

### Penis Commands

| Command | Alias | Description |
|---------|-------|-------------|
| `/penis size` | `/pp size` | Check your size |
| `/penis size <player>` | `/pp size` | Check someone else's size |
| `/penis girth` | `/pp girth` | Check your girth |
| `/penis girth <player>` | `/pp girth` | Check someone else's girth |
| `/penis bbc` | `/pp bbc` | Check your BBC status |
| `/penis bbc <player>` | `/pp bbc` | Check someone else's BBC status |
| `/penis toggle` | `/pp toggle` | Whip it out / Put it away |

### Other Commands

| Command | Alias | Description |
|---------|-------|-------------|
| `/buttfinger <player>` | `/bf` | 👆 *You know what this does* |
| `/viagra` | - | Spawn a Viagra pill (requires permission) |

### Admin Commands

| Command | Description |
|---------|-------------|
| `/penis size set <player> <size>` | Set someone's size (5-30cm) |
| `/penis girth set <player> <girth>` | Set someone's girth (5-15cm) |
| `/penis bbc set <player> <true/false>` | Assign BBC status |

---

## 📊 Statistics & Achievements

### Tracked Statistics
- **Fap Count** - Total times you've... expressed yourself
- **Cummed on Others** - Times you've hit nearby players
- **Got Cummed On** - Times you've been a victim
- **Exposure Time** - Total time with your equipment out
- **Buttfingers Given/Received** - Self-explanatory
- **Viagra Used** - Pills consumed

### Achievement Categories

| Category | Achievements | Examples |
|----------|-------------|----------|
| 🦴 Fapping | 6 | First Timer → Legendary Gooner (1-1000 faps) |
| 💦 Cumming | 4 | Oops! → Bukakke Master (1-100) |
| 😵 Got Cummed | 3 | Victim → Cum Magnet (1-50) |
| ⏱️ Exposure | 4 | Quick Flash → Public Menace (1min-10hrs) |
| 👆 Buttfinger | 3 | Probing → Master Fingerer (1-50) |
| 💊 Viagra | 3 | Performance Issues → Pharmacist's Best Friend (1-50) |

**Total: 27 achievements to unlock!**

### Leaderboard Categories
Use `/gc lb <category>` with: `faps`, `cumon`, `cummed`, `time`, `bf`

---

## 🔑 Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `gooncraft.reload` | Allows reloading the config | OP |
| `gooncraft.size.set` | Allows setting player sizes | OP |
| `gooncraft.girth.set` | Allows setting player girths | OP |
| `gooncraft.bbc.set` | Allows assigning BBC status | OP |
| `gooncraft.viagra` | Allows spawning Viagra pills | OP |

---

## 💊 Crafting Recipes

### Viagra Pill
```
[ 💎 ] [ 💎 ] [ 💎 ]
[ 💎 ] [ 👻 ] [ 💎 ]
[ 💎 ] [ 💎 ] [ 💎 ]

💎 = Diamond
👻 = Ghast Tear
```

*Grants +5cm temporary boost when used. Must have your equipment "toggled on" to use.*

---

## 🎭 Hidden Features

- **Sneaking + Swinging** while toggled on triggers a... *special animation*
- Get close to other players during the animation for a surprise message
- There's a 1/50 chance for an *extra special* moment 😏
- All activities are tracked for stats and achievements!

---

## ⚠️ Disclaimer

This plugin is:
- 100% a joke
- Not suitable for servers with minors
- Probably going to get you banned from hosting providers
- Absolutely hilarious (if you're 12)

**Use at your own risk. The developers are not responsible for:**
- Server bans
- Lost friendships
- Existential crises
- Your mom finding out what you've been coding

---

## 🛠️ Development Setup

### Prerequisites
- Docker & Docker Compose

That's it! No need to install Java or Maven locally.

### Quick Start

```bash
# First time setup
./dev/dev.sh setup      # Linux/macOS
.\dev\dev.ps1 setup     # Windows PowerShell
make -C dev setup       # If you have Make

# Start the Minecraft server
./dev/dev.sh start      # Linux/macOS  
.\dev\dev.ps1 start     # Windows PowerShell
make -C dev start       # Make

# Build & reload (main dev command)
./dev/dev.sh dev        # Linux/macOS
.\dev\dev.ps1 dev       # Windows PowerShell
make -C dev dev         # Make
```

### Environment Configuration

Copy `dev/.env.example` to `dev/.env` and configure:

```env
# Your Minecraft username (for OP)
OPS=YourUsername

# Server memory allocation
MEMORY=2G
```

Then apply OPs after server starts:
```bash
./dev/dev.sh op         # Linux/macOS
.\dev\dev.ps1 op        # Windows PowerShell
```

### All Dev Commands

| Command | Description |
|---------|-------------|
| `start` | Start the Minecraft server |
| `stop` | Stop the server |
| `build` | Build the plugin |
| `reload` | Reload plugin on server |
| `dev` | Build + Reload (main workflow) |
| `logs` | View server logs |
| `console` | Attach to server console |
| `setup` | First time setup |
| `op` | Apply OPs from .env file |

### Server Details
- **Address**: `localhost:25565`
- **RCON Port**: `25575` (password: `gooncraft`)
- **Version**: Paper 1.21
- **Mode**: Creative (for testing)

### Manual Build (without Docker)

```bash
mvn clean package -DskipTests
```

**Requirements for manual build:**
- Maven 3.6+
- JDK 21+

---

## 📁 Project Structure

```
com.miauwrijn.gooncraft/
├── Plugin.java              # Main plugin class
├── data/
│   ├── PenisStatistics.java # Penis data model
│   └── PlayerStats.java     # Player statistics
├── gui/
│   ├── GUI.java             # Base GUI class
│   ├── GUIListener.java     # Click event handler
│   ├── ItemBuilder.java     # Fluent item builder
│   ├── StatsGUI.java        # Statistics menu
│   └── AchievementsGUI.java # Achievements menu
├── handlers/
│   ├── ButtFingerCommandHandler.java
│   ├── PenisCommandHandler.java
│   └── StatsCommandHandler.java
├── managers/
│   ├── AchievementManager.java
│   ├── ConfigManager.java
│   ├── CooldownManager.java
│   ├── PenisStatisticManager.java
│   ├── PillManager.java
│   └── StatisticsManager.java
└── models/
    └── PenisModel.java      # 3D block display model
```

---

## 📜 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

*Yes, even this cursed creation has a proper license.*

---

## 🤝 Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Actually, maybe don't contribute. Maybe let this die. Maybe we've gone too far.

---

<p align="center">
  <i>Made with questionable judgment by Miauwrijn</i>
  <br><br>
  <b>Remember: Just because you CAN code something, doesn't mean you SHOULD.</b>
</p>
