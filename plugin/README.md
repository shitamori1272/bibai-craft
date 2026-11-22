# CoalClicker（石炭クリッカー）

美唄の炭鉱を題材にした放置ゲームの Paper プラグインです。企画は [docs/spec.md](../docs/spec.md) を参照してください。

## 必要なもの

- JDK 25 以降（macOS なら `brew install openjdk`）
- Gradle は同梱の Gradle Wrapper（`./gradlew`）を使うので、別途インストールは不要です

Homebrew の JDK を使う場合は、先に `export JAVA_HOME=/opt/homebrew/opt/openjdk` を実行します。

## ビルド

```sh
cd plugin
./gradlew build
```

`build/libs/CoalClicker-<バージョン>.jar` ができます。共有サーバーで使うときは、この JAR を `server-data/plugins/` に置いてサーバーを起動し直します。

## 開発用サーバーで試す

```sh
cd plugin
./gradlew runServer
```

`plugin/run/` に Paper 26.3 の開発用サーバーが起動し、ビルドしたプラグインが自動で読み込まれます。初回は [Minecraft EULA](https://www.minecraft.net/ja-jp/eula) を確認し、同意する場合は `plugin/run/eula.txt` の `eula=false` を `eula=true` に変えて、もう一度実行します。ゲームからは `localhost:25565` で接続できます。

`plugin/run/` のワールドやログは Git の対象外です。

## 構成

| パス | 内容 |
|---|---|
| `build.gradle.kts` | 依存（`paper-api`）とビルド設定。Paper のビルド番号はサーバーの `start.command` と合わせる |
| `src/main/resources/paper-plugin.yml` | プラグインの定義 |
| `src/main/java/io/github/shitamori1272/coalclicker/` | ソースコード |
