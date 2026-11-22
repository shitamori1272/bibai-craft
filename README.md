# Bibai Craft

美唄市をテーマにしたハッカソン用の Minecraft サーバーです。全員で **Java Edition 26.3** と公式のサーバーを使います。開発する人は自分の PC で起動でき、プレイに参加するだけなら会場のホスト 1 台で足ります。

## 石炭クリッカー

美唄の炭鉱の歴史を題材にした放置ゲームのプラグインを開発しています。企画は [docs/spec.md](docs/spec.md) を参照してください。

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
java ../server/World.java local
```

## 制作の続きと初期マップでの試遊

通常起動・試遊ともに、参加時のゲームモードはクリエイティブです。これまでサバイバルで遊んでいたプレイヤーにも再接続時に適用されます。

Gitで共有する初期マップは `maps/bibai/` です。通常起動では、初回にこの原本を `server-data/bibai/` にコピーします。すでに制作中のワールドがあれば、その続きから始まります。`git pull` 後も制作中のセーブはそのまま使えます。

| やりたいこと | macOS（リポジトリ直下） | Windows・Linux（`server-data` 内） |
|---|---|---|
| 建築・開発の続きを開く | `./start.command` | `java ../server/World.java local` |
| 今の原本から新しく試遊する | `./start.command preview` | `java ../server/World.java preview` |
| 制作ワールドを原本に反映する | `./start.command publish` | `java ../server/World.java publish` |

**地形担当**は通常起動したサーバーで一緒に建築します。共有する段階でコンソールに `stop` と入力し、終了を待ってから `publish` を実行します。地形・建築・チェストの中身・ワールド設定などが原本に入り、個人の持ち物・位置・進捗は除外されます。差分を確認して `git add -A -- maps/bibai` で追加し、チームのブランチにコミット・プッシュします。地形の原本更新はホスト1人が担当します。

**ゲーム機能担当**は自分のPCで通常起動して繰り返し開発できます。共有された地形でテストするときは変更を取り込み（例: `git pull`）、同じポートで動いているサーバーを `stop` で終了してから `preview` で起動します。今回の試遊用コピーだけが起動し、終了時に片付けられます。次に通常起動すると、元の作業の続きに戻れます。

## 地形の差分を並行開発する

`world-overlays/<パック名>/` に、各機能を独立したJava版データパックとして置きます。`./start.command preview`（Windows・Linux は `java ../server/World.java preview`）で、原本のコピーに全パックが加わります。通常起動はベースマップの制作に使い、地形担当はそこで作った地形を従来どおり `publish` できます。各自の差分は `world-overlays/` にコミットしてください。

最初のパック [bibai_headframes](world-overlays/bibai_headframes) は、初期マップの炭鉱公園付近に赤い竪坑櫓を2基配置します。公園の地表に合わせた作例です。試遊に入ったら、コマンドを実行できるプレイヤーは次のコマンドで南側から両方を見られます。

```mcfunction
/tp @s 10189.5 118 1242.5 155 -22
```

新しい担当区画を追加する手順は次のとおりです。

1. `world-overlays/` に固有名のフォルダを作り、上の作例から `pack.mcmeta` をコピーします。各自のパック内の `data/minecraft/tags/function/load.json` に自分の名前空間の起動関数を登録します。共通の登録一覧の編集は不要です。
2. ブロックをコードで配置する場合は `data/<名前空間>/function/` に `.mcfunction` を置きます。ゲーム内で建てたものは、ストラクチャーブロックで保存した `.nbt` を `data/<名前空間>/structure/` に置き、関数から `place template <名前空間>:<構造物名> X Y Z` で配置できます。床・空気を含め、書き込む範囲が他担当と重ならないように座標を決めてください。
3. 作例の `load` → `prepare` → `wait_for_chunk` → `install` は、遠方のチャンクの読み込みを待ち、ワールドごとに1回だけ配置します。自分の区画に合わせて読み込み範囲・設置座標・状態の名前空間を変更します。試遊中の `/reload` では完成済みの櫓を再配置しません。
4. 手元のパックを修正したら試遊を `stop` し、再び `preview` で初期状態から確認します。PRには自分のパックを含めます。ほかの人も同じブランチで `preview` すれば、ベースマップと差分を合わせた結果を確認できます。

## LAN から参加する

ホストのターミナルに `Done` が出たら、同じ Wi-Fi または有線 LAN の参加者は「マルチプレイ」→「ダイレクト接続」に `ホストのLANアドレス:25565` を入力します。Windows では `ipconfig`、Linux では `ip -4 addr` でホストの Wi-Fi・有線 LAN の IPv4 アドレスを確認できます。ファイアウォールで確認された場合はローカルネットワークからの Java の受信接続を許可してください。ルーターのポート転送は不要です。

## 設定を変えて共有する

例えば、サーバー一覧の表示名は [server/server.properties](server/server.properties) の `motd` で変更できます。サーバーのコンソールで `stop` と入力して保存終了し、再起動して変更を確認します。Windows・Linux では再起動前に編集したファイルをもう一度 `server-data` にコピーしてください。

各自のセーブ、JAR、ログ、EULA の同意記録は `server-data/` に保存され、Git の対象外です。
