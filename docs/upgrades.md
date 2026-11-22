# 改良の名前と解説（下書き）

> **これは歴史班に確認してもらうための下書きです。**（Refs #64、企画書 6-2「改良」）
>
> - 「確認状況」が **確認済み** の行は、出典の URL で解説の中心になる事実を確かめています。**要確認** の行は、出典が見つからなかった・弱かったものです。名前・解説の差し替えや出典の追加をお願いします。
> - 確認済みでも、出典が海外（英国・米国）の事例や三池炭鉱（福岡県大牟田市）の用語のものがあります。美唄の炭鉱（三菱美唄炭鉱・三井美唄炭鉱）で同じだったかは、別に確かめる必要があります。
> - 年や数字は、出典にあるものだけを書いています。
> - 道具の改良は各段階で「その道具の効果 2 倍」、ツルハシの改良は「手掘り 2 倍」、熟練の改良は「手掘りに毎秒の石炭の 1% を上乗せ」です（企画書 6-2）。

## 道具の改良

### 安全灯（`lamp`）

| ID | 名前 | 解説 | 確認状況 | 出典 |
|---|---|---|---|---|
| `lamp_1` | デービー灯 | 1815 年にイギリスのハンフリー・デービーが発表した安全灯。細い金網で炎を囲み、坑内のガスに火が移らないようにしました。 | 確認済み | https://ja.wikipedia.org/wiki/安全灯 , https://en.wikipedia.org/wiki/Mining_lamp |
| `lamp_2` | ミューゼラー灯 | 1840 年にベルギーで発表された改良型の安全灯。ベルギーでは 1864 年にこの型の使用が義務づけられました。 | 確認済み | https://en.wikipedia.org/wiki/Safety_lamp |
| `lamp_3` | ウルフ灯（揮発油安全灯） | 揮発油を燃料にした明るい安全灯。坑内で勝手に開けられないよう鍵のしくみを備え、炎が消えても灯を開けずに再点火できるものもありました。 | 要確認 | （出典未確認） |
| `lamp_4` | ガス検定の炎 | 炎の安全灯は、燃えやすいガスがあると炎が高く青くなり、酸素が少ないと消えます。この性質を使って、ガスを調べる道具としても使われました。 | 確認済み | https://ja.wikipedia.org/wiki/安全灯 , https://en.wikipedia.org/wiki/Safety_lamp |
| `lamp_5` | 電気安全灯 | 電池で光る、火を使わない安全灯。1900 年ごろから炭鉱に電灯が入りはじめ、炎の灯りに代わって照明の主役になりました。 | 確認済み | https://en.wikipedia.org/wiki/Safety_lamp , https://ja.wikipedia.org/wiki/炭鉱 |
| `lamp_6` | キャップランプ | 帽子（ヘルメット）に付け、腰の電池とコードでつないだ電灯。両手が空くので作業がしやすくなりました。スコットランドでは 1909 年に使われはじめ、米国では 1915 年にエジソンの電池式キャップランプが認められました。 | 確認済み | https://en.wikipedia.org/wiki/Mining_lamp , https://www.si.edu/spotlight/mining-lights-and-hats/safety-lamps |
| `lamp_7` | LED キャップランプ | 発光ダイオード（LED）を使ったキャップランプ。電池が長持ちし、明るさも増しました。 | 要確認 | https://en.wikipedia.org/wiki/Mining_lamp （写真の説明のみ） |

### 炭車（`minecart`）

| ID | 名前 | 解説 | 確認状況 | 出典 |
|---|---|---|---|---|
| `minecart_1` | 炭函（たんがん） | 炭車は「炭函（炭鑵）」とも呼ばれました。初期の炭車は木でできていたとされます。 | 要確認（呼び名は確認済み、木製は要確認） | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf （炭鉱用語解説） |
| `minecart_2` | 鋼製炭車 | 車体を鉄（鋼）で作った炭車。じょうぶで長く使えました。 | 要確認 | （出典未確認） |
| `minecart_3` | エンドレス運搬 | 輪になって回り続けるワイヤロープに炭車をつなぎ、まとめて引いて運ぶ方式。主に炭層に沿った坑道に設けられました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `minecart_4` | 大型炭車 | 1 両に積める石炭を増やした大きな炭車。坑道を広げることとあわせて運搬量を増やしました。 | 要確認 | （出典未確認） |
| `minecart_5` | 電気機関車けん引 | 坑内で電気機関車が炭車の列を引くようになりました。電線を自分で巻き取り・巻き戻ししながら走る「リールカー」という電気機関車もありました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `minecart_6` | 人車（じんしゃ） | 坑夫を乗せて坑道を運ぶ車。遠い切羽まで歩く時間が減りました。 | 要確認 | （出典未確認） |
| `minecart_7` | 美唄鉄道の石炭列車 | 三菱美唄炭鉱の石炭を運ぶため 1914 年に開業した鉄道。1919 年製造の 4110 形蒸気機関車が 1972 年の廃止まで走り、今も旧東明駅に展示されています。 | 確認済み | https://www2.t-expo.jp/exhibitors/downloadPdf/47929/美唄市観光PRパンフレット.pdf |

### 発破（`blasting`）

| ID | 名前 | 解説 | 確認状況 | 出典 |
|---|---|---|---|---|
| `blasting_1` | 黒色火薬 | 古くから岩を砕くのに使われた火薬。のちにダイナマイトに置き換えられていきました。 | 確認済み | https://en.wikipedia.org/wiki/Dynamite |
| `blasting_2` | 雷管 | 爆薬を確実に起爆させるための小さな火工品。ノーベルが 1864 年に特許を出願しました。 | 確認済み | https://en.wikipedia.org/wiki/Dynamite , https://ja.wikipedia.org/wiki/発破 |
| `blasting_3` | ダイナマイト | 1866 年にノーベルが発明した爆薬。黒色火薬より強く、扱いも安全になったため、鉱山で広く使われました。 | 確認済み | https://en.wikipedia.org/wiki/Dynamite |
| `blasting_4` | 削岩機（ジャックハンマー） | 爆薬を詰める穴（発破孔）を岩にあける機械。「ジャック・ハンマー」「レッグ・ハンマー」と呼ばれました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `blasting_5` | 電気雷管 | 電気で起爆する雷管。離れた安全な場所から、決まったときに起爆できます。 | 確認済み | https://ja.wikipedia.org/wiki/発破 |
| `blasting_6` | 段発発破 | 複数の穴の爆薬を、わずかな時間差をつけて順に起爆する方法。振動や岩の飛び散り方を調整しやすくなりました。 | 確認済み | https://ja.wikipedia.org/wiki/発破 |
| `blasting_7` | 炭鉱用の検定爆薬 | 坑内のガスや炭じんに火がつきにくいように作られ、検定に合格した炭鉱用の爆薬。 | 要確認 | （出典未確認） |

### コールピック（`coal_pick`）

| ID | 名前 | 解説 | 確認状況 | 出典 |
|---|---|---|---|---|
| `coal_pick_1` | 圧縮空気の配管 | 地上の圧縮機から坑内の切羽まで、圧縮空気を送る管。コールピックはこの空気の力で動きます。 | 要確認 | （出典未確認） |
| `coal_pick_2` | 替えノミ | 先端のノミ（刃）を取り替えられるようにし、すり減っても掘る力が落ちないようにしました。 | 要確認 | （出典未確認） |
| `coal_pick_3` | コールカッター | 刃の付いたチェーンを回して炭層に切り込みを入れる採炭機。はじめは圧縮空気で動きました。 | 確認済み | https://www.ncm.org.uk/?p=20361 |
| `coal_pick_4` | 電動コールカッター | コールカッターは、のちに電気で動くようになりました。 | 確認済み | https://www.ncm.org.uk/?p=20361 |
| `coal_pick_5` | 油圧駆動コールカッター | 油圧で動くしくみを取り入れたコールカッター。日本でも 1950 年代に実用化が報告されています。 | 要確認 | https://www.hitachihyoron.com/jp/pdf/1959/01/1959_01_18.pdf （検索結果の抜粋のみ。本文未確認） |
| `coal_pick_6` | 長壁式採炭 | 採掘面を 20 m 以上に長くとって掘る方法。機械を使った採炭に向いていて、戦間期の筑豊などで広まりました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf , https://repository.kulib.kyoto-u.ac.jp/dspace/bitstream/2433/239373/1/shirin_079_3_391.pdf |
| `coal_pick_7` | ホーベル | 炭壁に沿って刃を引きずり、石炭を削り取る採炭機。ドイツで開発され、1950 年代後期ごろ日本の炭鉱にも入りました。 | 確認済み | https://ja.wikipedia.org/wiki/炭鉱 |

### ベルトコンベア（`conveyor`）

| ID | 名前 | 解説 | 確認状況 | 出典 |
|---|---|---|---|---|
| `conveyor_1` | チェーンコンベア | 鎖で鉄の板を引いて石炭を送るコンベア。鉄柱・カッペと組み合わせて、採炭機械化の土台になりました。 | 要確認 | https://www.hitachihyoron.com/jp/pdf/1957/01/1957_01_16.pdf （検索結果の抜粋のみ。本文未確認） |
| `conveyor_2` | シュート | 石炭を積み替える口に付ける鉄板の装置。すり減りにくい鉄板でできていました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `conveyor_3` | 切羽コンベア | 採炭の現場（払）に沿って置くコンベア。積まれた石炭は「落口」に向かって流れていきます。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `conveyor_4` | 鋼製の切羽コンベア | 鋼でじょうぶに作った切羽コンベア。採炭機がこの上を走り、支保の枠の前に置かれます。 | 確認済み | https://en.wikipedia.org/wiki/Longwall_mining , https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `conveyor_5` | 坑道ベルトコンベア | 坑道に長いゴムのベルトを走らせ、石炭を休みなく運び出す設備。 | 要確認 | https://en.wikipedia.org/wiki/Conveyor_belt （炭鉱用ベルトの発明者についての記述の出典が弱い） |
| `conveyor_6` | スチールコードベルト | 中に鋼のワイヤを入れて強くしたベルト。長い距離・重い荷を運べるようになりました。 | 要確認 | （出典未確認） |
| `conveyor_7` | 選炭機 | 掘り出した石炭から岩や不純物を取り除く設備。美唄では三井美唄炭鉱第 2 坑の原炭ポケットと選炭機の一部が残るとされています。 | 要確認 | https://www.asahi-net.or.jp/~re4m-idgc/3HOUMONG.htm （個人サイト） |

### ドラムカッター（`drum_cutter`）

| ID | 名前 | 解説 | 確認状況 | 出典 |
|---|---|---|---|---|
| `drum_cutter_1` | 替え刃（ビット） | 回転ドラムに並ぶ刃を取り替えられるようにし、硬い炭壁でも削り続けられるようにしました。 | 要確認 | （出典未確認） |
| `drum_cutter_2` | ドラムカッタローダ | 炭壁を削りながら、そのまま石炭をコンベアに積み込む機械。1960 年代はじめに需要が増えたとされます。 | 要確認 | https://www.hitachihyoron.com/jp/pdf/1962/01/1962_01_18.pdf （検索結果の抜粋のみ。本文未確認） |
| `drum_cutter_3` | ダブルドラム | 前後 2 つのドラムで、炭壁の上と下を一度に削る型。 | 要確認 | （出典未確認） |
| `drum_cutter_4` | 高出力モーター | モーターを強くして、削る速さと積み込む量を増やしました。 | 要確認 | https://www.hitachihyoron.com/jp/pdf/1962/01/1962_01_18.pdf （検索結果の抜粋のみ。本文未確認） |
| `drum_cutter_5` | キャプタイヤケーブル | 採炭現場のきびしい条件でも使えるじょうぶな電線。採炭機に電気を送り続けます。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `drum_cutter_6` | 岩粉散布 | 岩石の粉を坑道にまき、炭じんが舞い上がって爆発するのを防ぐ保安の工夫。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `drum_cutter_7` | SD 採炭 | ドラムカッター（S）と自走枠（D）を組み合わせた機械化採炭。1980 年代ごろには日本の主要な炭鉱の多くで使われていました。 | 確認済み（S・D の略の意味は要確認） | https://ja.wikipedia.org/wiki/炭鉱 |

### 自走枠（`shield`）

| ID | 名前 | 解説 | 確認状況 | 出典 |
|---|---|---|---|---|
| `shield_1` | 木の枠と当たり | 坑道の天井や壁を支える枠。枠と天井のすき間には「当たり」という木片を入れました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `shield_2` | 鉄柱 | 木の柱に代わって使われた鉄の柱。くり返し使えました。 | 要確認 | （出典未確認） |
| `shield_3` | カッペ | 鉄柱の上に渡して天井を支える鋼の梁。チェーンコンベアと組み合わせて機械化の土台になりました。 | 要確認 | https://www.hitachihyoron.com/jp/pdf/1957/01/1957_01_16.pdf （検索結果の抜粋のみ。本文未確認） |
| `shield_4` | 水圧鉄柱 | 高い水圧を入れて柱を伸ばす鉄柱。天井の高さに合わせて支えやすくなりました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `shield_5` | 動枠（うごきわく） | 水圧鉄柱と枠を組み合わせた支保。三池炭鉱では、はじめ「動枠」、のちに「自走枠」と呼ばれました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `shield_6` | アングル車道 | L 字形の鋼材で作った幅の広い軌道。重い自走枠を据え付けたり撤去したりするときに、この上に載せて引きました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `shield_7` | 自走枠の長い切羽 | 長さ 150 m の切羽には 100 セットほどの自走枠が並びました。レバー操作で枠をゆるめ、ジャッキで前へ引き寄せ、また天井を支えます。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |

### 立坑櫓（`headframe`）

| ID | 名前 | 解説 | 確認状況 | 出典 |
|---|---|---|---|---|
| `headframe_1` | 斜坑から立坑へ | 採掘が深くなると、斜めの坑道（斜坑）では限界があり、まっすぐ下へ掘る立坑へ移っていきました。 | 確認済み | https://ja.wikipedia.org/wiki/炭鉱 |
| `headframe_2` | ケージ | 立坑の中を上下するかご。人や石炭、資材を運ぶ坑夫用のリフトでした。 | 確認済み | https://www2.t-expo.jp/exhibitors/downloadPdf/47929/美唄市観光PRパンフレット.pdf |
| `headframe_3` | 鋼製の櫓 | 木に代わり鋼で組んだ櫓。巻き胴式の巻揚機と組み合わせることが多い形です。 | 確認済み | https://en.wikipedia.org/wiki/Headframe |
| `headframe_4` | 電気巻揚機 | 巻揚機を電気で動かすようにし、ケージを速く確実に上げ下げできるようにしました。 | 要確認 | （出典未確認） |
| `headframe_5` | 排気立坑の大型扇風機 | 立坑の地上部に大型の扇風機を置き、坑内の空気を吸い出して新しい空気を送り込みました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `headframe_6` | 常盤台の竪坑櫓 | 三菱美唄炭鉱では 1923 年に竪坑の巻揚櫓が建てられました。2 本の櫓が並んで立っています。 | 確認済み（「2 本一対は国内唯一」などの評価は要確認のため書いていない） | https://ja.wikipedia.org/wiki/美唄炭鉱 |
| `headframe_7` | 炭鉱メモリアル森林公園 | 三菱美唄炭鉱の竪坑櫓は、換気や石炭の運搬、坑夫用のリフトに使われました。真っ赤な櫓と原炭ポケット、開閉所が今も元の場所に残っています。 | 確認済み | https://www2.t-expo.jp/exhibitors/downloadPdf/47929/美唄市観光PRパンフレット.pdf |

## ツルハシの改良（`pickaxe`、手掘り 2 倍）

| ID | 名前 | 解説 | 確認状況 | 出典 |
|---|---|---|---|---|
| `pickaxe_1` | 鋼のツルハシ | 先を鋼で作ったツルハシ。硬い炭層にも負けません。 | 要確認 | （出典未確認） |
| `pickaxe_2` | ツルハシの研ぎ直し | 炭鉱の鍛冶場で、すり減ったツルハシの先を打ち直して使いました。 | 要確認 | （出典未確認） |
| `pickaxe_3` | 残柱式採炭 | 天井が崩れないよう石炭の一部を柱として残しながら掘る方法。手掘りの時代の基本的な掘り方でした。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf , https://repository.kulib.kyoto-u.ac.jp/dspace/bitstream/2433/239373/1/shirin_079_3_391.pdf |

## 熟練の改良（`skill`、手掘りに毎秒の石炭の 1% を上乗せ）

| ID | 名前 | 解説 | 確認状況 | 出典 |
|---|---|---|---|---|
| `skill_1` | 後山（あとやま） | 熟練した先山の指示を受けて作業する坑夫。ここで仕事を覚えていきました。 | 確認済み（三池炭鉱での用語。美唄での呼び名は要確認） | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `skill_2` | 助先（すけさき） | 先山に次ぐ熟練の坑夫。作業グループの副班長の役割をしました。 | 確認済み（三池炭鉱での用語。美唄での呼び名は要確認） | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `skill_3` | 先山（さきやま） | 作業グループでいちばん熟練した坑夫で、班長の役割をしました。手掘りの時代は、坑夫の技能と経験が採炭を大きく左右しました。 | 確認済み | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf , https://repository.kulib.kyoto-u.ac.jp/dspace/bitstream/2433/239373/1/shirin_079_3_391.pdf |
| `skill_4` | 養成工 | 会社の中で、仕事に合わせた職業訓練を受ける社員。技能を計画的に身につけました。 | 確認済み（三池炭鉱での用語） | https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf |
| `skill_5` | 鉱山保安法 | 1949 年に定められた、鉱山で働く人を危険から守るための法律。保安の決まりや監督のしくみが整えられました。 | 確認済み（法律の制定年・目的のみ。保安教育の中身は要確認） | https://ja.wikipedia.org/wiki/鉱山保安法 |

## 候補（今回は使わなかったもの）

- `面交替（つらごうたい）`：交替のとき、次の組と前の組が現場で直接顔を合わせて情報を伝えること（三池炭鉱の用語、確認済み）。熟練の改良の差し替え候補。
- `ウルフ灯`の説明（揮発油の燃料・鍵のしくみ・再点火装置）と、日本の炭鉱へのキャップランプ導入時期は、日本語の資料（石炭博物館などの展示解説）で確かめたい。

## 出典一覧

- 大牟田市「炭鉱用語解説」（資料編）: https://www.city.omuta.lg.jp/kiji0037030/5_7030_12089_up_RWAPJQ3B.pdf
- 美唄市観光 PR パンフレット: https://www2.t-expo.jp/exhibitors/downloadPdf/47929/美唄市観光PRパンフレット.pdf
- Wikipedia「美唄炭鉱」: https://ja.wikipedia.org/wiki/美唄炭鉱
- Wikipedia「炭鉱」: https://ja.wikipedia.org/wiki/炭鉱
- Wikipedia「安全灯」: https://ja.wikipedia.org/wiki/安全灯
- Wikipedia「発破」: https://ja.wikipedia.org/wiki/発破
- Wikipedia「鉱山保安法」: https://ja.wikipedia.org/wiki/鉱山保安法
- Wikipedia "Safety lamp": https://en.wikipedia.org/wiki/Safety_lamp
- Wikipedia "Mining lamp": https://en.wikipedia.org/wiki/Mining_lamp
- Wikipedia "Dynamite": https://en.wikipedia.org/wiki/Dynamite
- Wikipedia "Longwall mining": https://en.wikipedia.org/wiki/Longwall_mining
- Wikipedia "Headframe": https://en.wikipedia.org/wiki/Headframe
- Wikipedia "Conveyor belt": https://en.wikipedia.org/wiki/Conveyor_belt
- Smithsonian "Mining Lights and Hats: Safety Lamps": https://www.si.edu/spotlight/mining-lights-and-hats/safety-lamps
- National Coal Mining Museum for England "From Picks to Shearers": https://www.ncm.org.uk/?p=20361
- 京都大学『史林』79 巻 3 号の論文（戦間期筑豊の採炭機構）: https://repository.kulib.kyoto-u.ac.jp/dspace/bitstream/2433/239373/1/shirin_079_3_391.pdf
- 日立評論（1957・1959・1962 年、検索結果の抜粋のみ。本文は画像 PDF で未確認）: https://www.hitachihyoron.com/jp/pdf/1957/01/1957_01_16.pdf ほか
- 個人サイト（三井美唄炭鉱第 2 坑の遺構）: https://www.asahi-net.or.jp/~re4m-idgc/3HOUMONG.htm
