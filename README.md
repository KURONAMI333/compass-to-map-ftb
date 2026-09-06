# Compass to Map: FTB Chunks & Explorer's Compass & Nature's Compass Addon

> Explorer's Compass / Nature's Compass で構造物・バイオームを見つけた瞬間に、FTB Chunks の地図へ**色分けされた waypoint** を自動登録する。プロンプト無し、UI 介入ゼロ。**クライアント側だけで完結する。**

[![License: All Rights Reserved](https://img.shields.io/badge/License-All%20Rights%20Reserved-lightgrey.svg)](LICENSE)
[![CurseForge](https://img.shields.io/badge/CurseForge-compass--to--map--ftb-F16436)](https://www.curseforge.com/minecraft/mc-mods/compass-to-map-ftb)

---

## Supported Loaders / Versions

| Minecraft | NeoForge | Forge | Fabric |
|---|:---:|:---:|:---:|
| 1.21.1 | ✅ | — | — |

---

## なにをするやつ?

コンパスは座標を教えてくれる。FTB Chunks は waypoint を持っている。その間をつなぐものが無いので、
コンパスの HUD に出た数字を読んで waypoint 画面に手で打ち込むことになる。このアドオンはそこだけを埋める。

- 🧭 **Explorer's Compass（構造物）と Nature's Compass（バイオーム）の両対応** — 片方だけでも動く
- 🎨 **カテゴリ別の色** — 村は金、要塞は紫、寺院と海底神殿はシアン、ネザー要塞は橙、古代都市は水色。バイオームは地形ごとの配色
- 🏷️ **対象名から命名** — `minecraft:village_plains` → `Village Plains`
- ✋ **立てた後は普通の FTB Chunks の waypoint** — 名前も色も自由に変えられ、こちらは二度と触らない
- 🔁 **重複に強い** — 同じバイオームを再検索してもピンは増えず、別の村を見つければ別のピンが立つ
- 📡 **クライアント側だけで完結** — サーバーにこの MOD を入れる必要は無い

## 設定

`config/compasstomapftb-client.toml`（または Mods 画面）。3項目だけで、既定のまま使う想定。

| キー | 既定 | 意味 |
|---|---|---|
| `structures` | `true` | Explorer's Compass の発見を登録する |
| `biomes` | `true` | Nature's Compass の発見を登録する |
| `chatNotification` | `true` | 登録時にチャットへ1行出す |

## 依存

- **FTB Chunks**（必須） — waypoint の登録先。普段どおりに導入する（サーバーで遊ぶならサーバーにも）
- **Explorer's Compass** / **Nature's Compass**（どちらか一方以上） — 検索の実体
- JourneyMap や Xaero's が同居していても構わない。このアドオンは FTB Chunks にしか書かない

## 姉妹 MOD

| 地図 | MOD |
|---|---|
| JourneyMap | Compass to Map |
| Xaero's Minimap | Compass to Map: Xaero's |
| FTB Chunks | このリポジトリ |

## ビルド

```bash
export JAVA_HOME="<JDK 21 のパス>"
./gradlew build
```

出力は `build/libs/compasstomapftb-<version>.jar`。

## 不具合・質問

CurseForge のコメント欄か、X の [@kuronami333](https://x.com/kuronami333) の DM へ。

## ライセンス

All Rights Reserved. modpack への収録は許諾もクレジットも不要。
