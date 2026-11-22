# Bibai Craft

美唄市をテーマにしたハッカソン用の Minecraft サーバーです。全員で **Java Edition 26.3** と公式のサーバーを使います。開発する人は自分の PC で起動でき、プレイに参加するだけなら会場のホスト 1 台で足ります。

## 開発用サーバーを起動する

JDK 25 以降をインストールして、このリポジトリを取得します。動作確認にはゲーム側でも Java Edition 26.3 を選んでください。

```sh
git clone https://github.com/shitamori1272/bibai-craft.git
cd bibai-craft
```

### macOS

`./start.command` を実行します。Finder からダブルクリックしても起動できます。初回は運営者が [Minecraft EULA](https://www.minecraft.net/ja-jp/eula) を確認して同意すると、スクリプトが公式 JAR を取得します。接続先はターミナルに表示されます。

### Windows・Linux

1. リポジトリ内に `server-data` フォルダを作り、[server/server.properties](server/server.properties) をその中にコピーします。
2. [Minecraft EULA](https://www.minecraft.net/ja-jp/eula) を確認し、[Mojang の 26.3 サーバー JAR](https://piston-data.mojang.com/v1/objects/33680f5f2ac32864d6d7cf5e56a705fdb3e05f4c/server.jar) を同じフォルダに `minecraft_server.26.3.jar` という名前で保存します。
3. PowerShell またはターミナルで以下を実行します。初回はサーバーが終了するので、同意する場合は生成された `eula.txt` の `eula=false` を `eula=true` に変更し、同じ `java` コマンドでもう一度起動します。

```sh
cd server-data
java -Xms512M -Xmx2G -jar minecraft_server.26.3.jar nogui
```

## LAN から参加する

ホストのターミナルに `Done` が出たら、同じ Wi-Fi または有線 LAN の参加者は「マルチプレイ」→「ダイレクト接続」に `ホストのLANアドレス:25565` を入力します。Windows では `ipconfig`、Linux では `ip -4 addr` でホストの Wi-Fi・有線 LAN の IPv4 アドレスを確認できます。ファイアウォールで確認された場合はローカルネットワークからの Java の受信接続を許可してください。ルーターのポート転送は不要です。

## 設定を変えて共有する

例えば、サーバー一覧の表示名は [server/server.properties](server/server.properties) の `motd` で変更できます。サーバーのコンソールで `stop` と入力して保存終了し、再起動して変更を確認します。Windows・Linux では再起動前に編集したファイルをもう一度 `server-data` にコピーしてください。

Git で共有するのはリポジトリ側の設定です。各自のワールドは `server-data/bibai/` に保存され、JAR・ログ・EULA の同意記録とともに Git の対象外です。
