# FormsAPI

A Geyser Extension that provides reliable Bedrock form delivery for Minecraft server networks.

## Why FormsAPI Exists

When running a Minecraft server network with **Geyser Standalone** + **Velocity Proxy** + **Backend Spigot Servers**, sending Bedrock forms to players becomes problematic:

- **Floodgate API** on backend Spigot servers cannot send forms because they don't have direct access to the Bedrock session
- **Floodgate API** on Velocity also fails because Geyser Standalone runs as a separate process
- **Plugin messaging** between Spigot → Velocity → Geyser doesn't work reliably for form delivery

The only reliable way to send forms is through `GeyserSession.sendForm()` which is only accessible from **inside Geyser itself**.

**FormsAPI solves this** by running as a Geyser Extension with a TCP server that accepts form requests from external plugins and delivers them directly using `session.sendForm()`.

## Features

- Reliable form delivery using Geyser's native `session.sendForm()`
- TCP server for receiving form requests from Spigot/Paper plugins
- Support for Modal forms (Yes/No dialogs)
- Support for Simple forms (button menus)
- Automatic command execution on form response
- Configurable TCP port
- Works with any network topology (Standalone, Velocity, BungeeCord)

## Requirements

- Geyser Standalone 2.4.0+
- Java 17+

## Installation

1. Download `FormsAPI.jar`
2. Place it in your Geyser Standalone's `extensions/` folder
3. Start Geyser Standalone
4. Configure `extensions/FormsAPI/config.yml` if needed

## Configuration

```yaml
# FormsAPI Extension Configuration

# TCP Server Settings
# The port to listen for form requests from Spigot plugins
port: 9876
```

## How It Works

```
┌─────────────────────────────────────────────────────────────┐
│                    GEYSER STANDALONE                        │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  FormsAPI Extension                                   │  │
│  │  - Listens on TCP port 9876                           │  │
│  │  - Receives JSON form requests                        │  │
│  │  - Finds player session by UUID                       │  │
│  │  - Calls session.sendForm() directly                  │  │
│  │  - Executes commands on form response                 │  │
│  └───────────────────────────────────────────────────────┘  │
│                           │                                 │
│                           ▼                                 │
│                   Bedrock Client                            │
│                   (sees the form!)                          │
└─────────────────────────────────────────────────────────────┘
                                          ▲
                            │ TCP Connection
                            │
┌───────────────────────────┴─────────────────────────────────┐
│                    SPIGOT SERVER                            │
│  Any plugin can send form requests via TCP                  │
└─────────────────────────────────────────────────────────────┘
```

## Protocol

Form requests are sent as JSON over TCP (one JSON object per line):

### Modal Form Request

```json
{
  "player_uuid": "00000000-0000-0000-0009-01f200e0da15",
  "form_type": "modal",
  "title": "Teleport Request",
  "content": "PlayerA wants to teleport to you.\n\nDo you accept?",
  "button1": "Accept",
  "button2": "Deny",
  "command_accept": "tpaccept",
  "command_deny": "tpdeny"
}
```

### Simple Form Request

```json
{
  "player_uuid": "00000000-0000-0000-0009-01f200e0da15",
  "form_type": "simple",
  "title": "Menu",
  "content": "Select an option:",
  "buttons": [
    {"text": "Option 1"},
    {"text": "Option 2"},
    {"text": "Option 3"}
  ]
}
```

## Building from Source

```bash
git clone https://github.com/DronzerStudios/FormsAPI.git
cd FormsAPI
./gradlew build
```


## License

MIT License with Attribution Requirement

Copyright (c) 2024 DronzerStudios.tech

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

1. The above copyright notice and this permission notice shall be included in all
   copies or substantial portions of the Software.

2. **Attribution Requirement**: Any use, modification, or distribution of this
   software must include visible credit to **DronzerStudios.tech** in:
   - The project's README or documentation
   - Any derivative works or forks
   - Any public releases that include this software

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Credits

Developed by [DronzerStudios](https://dronzerstudios.tech)

## Support

For issues, feature requests, or contributions, please visit:
- Website: https://dronzerstudios.tech
- GitHub: https://github.com/DronzerStudios
