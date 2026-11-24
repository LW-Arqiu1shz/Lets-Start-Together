# 联机小厅 · Let's Start Together!

> 本分支 / This branch: **26.1.2NeoForge**

[中文](#中文) · [English](#english)

> Author / 作者：**Arqiu-owo** · Code: `LGPL-3.0-only` · Assets: `CC-BY-NC-SA-4.0` — see [LICENSE.md](LICENSE.md)

---

## 中文

“我还没进来呢你们怎么开玩了”
“没事儿，我们就砍点木头，你快点，不然天黑了”
或许你和朋友们联机的时候总有个人网不太好，一时半会儿进不来。可进都进去了，要是不开玩……
本模组是来解决这个问题的！等大家都进来再开始，就没有进度差的烦恼啦~再也不用因为怕干等导致的时间流逝了awa

### 功能

- 多人世界先进**联机小厅**：大家在小厅里集合，世界里的时间不走，房主点「开始游戏」后所有人一起进入世界。
- 小厅里能聊天；房主右键玩家头像可以把人踢出小厅。
- 「联机设置」用的是 [LAN World Plug-n-Play](https://github.com/satxm/mcwifipnp)（mcwifipnp）的界面，它已经内嵌在本模组的 jar 里，不用另装。IP、e4mc 地址等联机提示会发到小厅聊天里，所有人都看得到。
- 创建世界时可以选「是否为多人世界」，也可以直接点「创建多人世界」；之后在世界的「编辑」里随时开关「联机小厅」。多人世界在世界列表里有一个小皇冠。
- 头像大小、单位（像素 / 磅）、是否显示名字都能自己设置；人多时头像会缩小，再放不下就翻页。

### 配置

这些是每个玩家自己的显示设置，在 `config/lets_start_together-client.json`（Fabric）或 `config/lets_start_together-client.toml`（NeoForge）。
也可以在游戏里改：Fabric 版通过 Mod Menu，NeoForge 版在模组列表里点「配置」。

房主和加入的玩家都要安装本模组；没装的人加入时会收到提示。

### 支持的版本

| 分支 | Minecraft | 加载器 |
|---|---|---|
| [`26.1.2Fabric`](../../tree/26.1.2Fabric) | 26.1.2 | Fabric Loader 0.18.4+、Fabric API（Mod Menu 可选） |
| [`26.1.2NeoForge`](../../tree/26.1.2NeoForge) | 26.1.2 | NeoForge 26.1.2.112+ |
| [`26.3Fabric`](../../tree/26.3Fabric) | 26.3 | Fabric Loader 0.19.3+、Fabric API（Mod Menu 可选） |
| [`26.3NeoForge`](../../tree/26.3NeoForge) | 26.3 | NeoForge 26.3.0.101+ |

每个版本是仓库里的一个**分支**（`26.1.2NeoForge` 是默认分支），分支根目录就是 Gradle 工程。

### 自己编译

需要 **JDK 25** 和 **Gradle 9.7+**（仓库里没有附带 Gradle Wrapper）。切换到想要的版本分支，在根目录运行：

```
gradle build
```

jar 会生成在 `build/libs/` 里。编译要用的 mcwifipnp jar 已经放在 `compile-libs/` 里（Apache-2.0，来自 [satxm/mcwifipnp](https://github.com/satxm/mcwifipnp)），会一起打进本模组的 jar。

---

## English

"I'm not even in yet, why did you guys start?"
"Relax, we're just chopping some wood. Hurry up, it'll be dark soon!"
Maybe there's always someone in your group whose connection is slow, and they just can't get in for a while. But everyone else is already in, and if you don't start playing...
This mod is here to fix that! Start only once everyone is in: no more falling behind, and no more worrying about time passing while you wait. awa

### Features

- Multiplayer worlds open into a **lobby** first: everyone gathers there, no time passes in the world, and when the host clicks "Start Game" everyone enters together.
- Chat in the lobby; the host can right-click a player's head to kick them from the lobby.
- "LAN Settings" uses the screen from [LAN World Plug-n-Play](https://github.com/satxm/mcwifipnp) (mcwifipnp), which is embedded in this mod's jar, so there's nothing else to install. Connection hints such as the IP or e4mc address are posted to the lobby chat for everyone.
- When creating a world, choose whether it's a multiplayer world, or just click "Create Multiplayer World"; you can switch the lobby on or off later under the world's "Edit". Multiplayer worlds show a small crown in the world list.
- Head size, its unit (pixels / points) and whether names are shown are up to each player; with many players the heads shrink, and if they still don't fit the lobby pages.

### Configuration

These are each player's own display settings, stored in `config/lets_start_together-client.json` (Fabric) or `config/lets_start_together-client.toml` (NeoForge).
You can also change them in game: via Mod Menu on Fabric, or the "Config" button in the mod list on NeoForge.

Both the host and joining players need this mod; anyone without it is told so when they try to join.

### Supported versions

| Branch | Minecraft | Loader |
|---|---|---|
| [`26.1.2Fabric`](../../tree/26.1.2Fabric) | 26.1.2 | Fabric Loader 0.18.4+, Fabric API (Mod Menu optional) |
| [`26.1.2NeoForge`](../../tree/26.1.2NeoForge) | 26.1.2 | NeoForge 26.1.2.112+ |
| [`26.3Fabric`](../../tree/26.3Fabric) | 26.3 | Fabric Loader 0.19.3+, Fabric API (Mod Menu optional) |
| [`26.3NeoForge`](../../tree/26.3NeoForge) | 26.3 | NeoForge 26.3.0.101+ |

Each version lives on its own **branch** (`26.1.2NeoForge` is the default branch); the branch root is the Gradle project.

### Building

Requires **JDK 25** and **Gradle 9.7+** (no Gradle Wrapper is included). Check out the branch for the version you want and run, in the repository root:

```
gradle build
```

The jar is written to `build/libs/`. The mcwifipnp jar needed to compile is already in `compile-libs/` (Apache-2.0, from [satxm/mcwifipnp](https://github.com/satxm/mcwifipnp)) and is bundled into this mod's jar.

---

## 贡献者 / Contributors

- **Arqiu-owo** — 设计、美术、文案、调试 / Design, art, writing and testing
- **Claude**（Anthropic）— 程序、项目管理 / Programming and project management
- **DeepSeek** — 程序、项目管理 / Programming and project management

## License / 许可证

- Code / 代码：[GNU LGPL v3.0](COPYING.LESSER)（with [GPL v3.0](COPYING)）
- Assets (images) / 素材（图片）：[CC BY-NC-SA 4.0](https://creativecommons.org/licenses/by-nc-sa/4.0/)

Details / 详情：[LICENSE.md](LICENSE.md)
