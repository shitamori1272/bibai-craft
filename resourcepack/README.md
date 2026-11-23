# 石炭クリッカー リソースパック

ショップの説明文に、道具の実物の写真を出すためのパックです。写真はフォントの文字として登録しています。

## 写真を足す・差し替える

1. `photos/<道具の ID>.png` に写真を置く（横:縦 = 3:2 にしておくと切れずに収まる）
2. 新しい道具なら `make_photos.py` の `PHOTOS` と、プラグインの `ToolPhoto` に同じ順で足す
3. `python3 resourcepack/make_photos.py` で文字の画像とフォントを作り直す
4. `cd plugin && ./gradlew resourcePack` で `plugin/build/resourcepack/coalclicker-resourcepack.zip` ができる

ゲーム内では F3 + T でパックを読み込み直す。

## 写真の出典

| 道具 | ファイル | 出典・ライセンス |
|---|---|---|
| 発破 | `photos/blasting.png` | Underwood & Underwood「Powder man charging holes with dynamite sticks for blasting in rock salt mine, Kansas」（ステレオ写真の左半分を切り抜き）。パブリックドメイン。[Wikimedia Commons](https://commons.wikimedia.org/wiki/File:2015.028.044_Powder_man_charging_holes_with_dynamite_sticks_for_blasting_in_rock_salt_mine_Kansas.tif)。※炭鉱ではなく岩塩鉱山の写真 |
| 安全灯 | `photos/lamp.png` | 「Davy lamp」（1905 年の書籍のスキャン）。パブリックドメイン。[Wikimedia Commons](https://commons.wikimedia.org/wiki/File:Davy_lamp.png)（[Wikipedia「デービー灯」](https://ja.wikipedia.org/wiki/%E3%83%87%E3%83%BC%E3%83%93%E3%83%BC%E7%81%AF)に掲載） |
| 炭車 | `photos/minecart.png` | 「A mule pulls a load of coal」NIOSH（米国立労働安全衛生研究所）。パブリックドメイン。[Wikimedia Commons](https://commons.wikimedia.org/wiki/File:A_mule_pulls_a_load_of_coal.jpg)（[Wikipedia「炭鉱」](https://ja.wikipedia.org/wiki/%E7%82%AD%E9%89%B1)に掲載） |
| 立坑櫓 | `photos/headframe.png` | `sozai/05_k_001_0001.jpg`（旧三菱美唄炭鉱の立坑櫓）。[炭鉄港デジタル資料館](https://3city.net/archives/)のダウンロード表示のある資料。[サイトの利用について](https://3city.net/archives/about)：商用以外は使用可・トリミング可・第三者への譲渡や二次使用は不可 |
| ドラムカッター | `photos/drum_cutter.png` | 炭鉄港デジタル資料館「[三菱美唄炭鉱(1)](https://3city.net/archives/database/bibai/2430/)」05_n_001_0013。所蔵：美唄市教育委員会。**ダウンロード表示のない一般資料**のため、使用には協議会事務局か所蔵者の許可が必要 |
| 自走枠 | `photos/shield.png` | 同上 05_n_001_0017。所蔵：美唄市教育委員会。**一般資料**（同上） |
| コールピック | `photos/coal_pick.png` | [水巻町歴史資料館](https://www.town.mizumaki.lg.jp/museum/040/020/010/220/1183.html)。All rights reserved（使用の許可は未確認） |
| ベルトコンベア | `photos/conveyor.png` | [m-ac.jp「三井美唄炭鉱」](http://m-ac.jp/hokkaido/history/coal/bibai/enterprise/mitsui_bibai/mine/index_j.phtml)。ライセンス表示なし（使用の許可は未確認） |

ドラムカッター・自走枠・コールピック・ベルトコンベアの 4 枚は、ハッカソンでの試遊のためだけに使っている。ハッカソンのあとも使う、または一般に配るなら、許可をとるかパブリックドメインなどの写真に差し替える。
