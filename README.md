# 🍆 GoonCraft

> *The Minecraft plugin your server never knew it needed (and probably doesn't).*

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-brightgreen)
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

---

## 📦 Installation

1. Download the latest `gooncraft-x.x.x.jar` from releases
2. Drop it in your server's `plugins` folder
3. Restart your server
4. Question your life choices
5. Have fun!

**Requirements:**
- Minecraft 1.21.1+
- Java 21+
- Spigot/Paper server

---

## 🎮 Commands

| Command | Description |
|---------|-------------|
| `/penis size` | Check your size |
| `/penis size <player>` | Check someone else's size |
| `/penis girth` | Check your girth |
| `/penis girth <player>` | Check someone else's girth |
| `/penis bbc` | Check your BBC status |
| `/penis bbc <player>` | Check someone else's BBC status |
| `/penis toggle` | Whip it out / Put it away |
| `/buttfinger <player>` | 👆 *You know what this does* |
| `/viagra` | Spawn a Viagra pill (requires permission) |

### Command Aliases
- `/penis` → `/pp`
- `/buttfinger` → `/bf`

### Admin Commands

| Command | Description |
|---------|-------------|
| `/penis size set <player> <size>` | Set someone's size (5-30cm) |
| `/penis girth set <player> <girth>` | Set someone's girth (5-15cm) |
| `/penis bbc set <player> <true/false>` | Assign BBC status |

---

## 🔑 Permissions

| Permission | Description |
|------------|-------------|
| `gooncraft.size.set` | Allows setting player sizes |
| `gooncraft.girth.set` | Allows setting player girths |
| `gooncraft.bbc.set` | Allows assigning BBC status |
| `gooncraft.viagra` | Allows spawning Viagra pills |

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

### All Commands

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

### Server Details
- **Address**: `localhost:25565`
- **RCON Port**: `25575` (password: `gooncraft`)
- **Version**: Paper 1.21.1
- **Mode**: Creative (for testing)

### Manual Build (without Docker)

```bash
mvn clean package -DskipTests
```

**Requirements for manual build:**
- Maven 3.6+
- JDK 21+

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
