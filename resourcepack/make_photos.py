"""道具の写真をフォントの文字に切り分けて、リソースパックに書き出す。

使い方: python3 resourcepack/make_photos.py
  - resourcepack/photos/<道具の ID>.png（3:2 くらいの写真）を読み、PHOTOS の順に文字を割り当てる
  - assets/coalclicker/textures/font/photo/<ID>_<番号>.png と assets/coalclicker/font/photo.json を作り直す

フォントの 1 文字は 256×256 の枠に入る大きさまでなので（それより大きいと □ になる）、写真は 2×2 の 4 枚に分け、
マイナスの幅の文字で戻しながら 1 枚の絵に並べる。並べ方はプラグインの ToolPhoto と合わせる。
"""
import json
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parent
PHOTOS = ["drum_cutter", "headframe", "lamp", "minecart", "coal_pick", "shield", "conveyor", "blasting"]  # ToolPhoto の並びと同じ順。1 枚につき 4 文字（U+E000 から）

TEX_W, TEX_H = 480, 320      # 写真の解像度（表示の 4 倍）
SHOW_W, SHOW_H = 120, 80     # 説明文での大きさ（ToolPhoto.WIDTH / HEIGHT）
COLS, ROWS = 2, 2
TOP_ASCENT = 7               # 1 行目の文字の上端に写真の上端を合わせる

tex_dir = ROOT / "assets/coalclicker/textures/font/photo"
tex_dir.mkdir(parents=True, exist_ok=True)
for old in tex_dir.glob("*.png"):
    old.unlink()

tile_w, tile_h = TEX_W // COLS, TEX_H // ROWS
show_tile_h = SHOW_H // ROWS
providers = []
for index, name in enumerate(PHOTOS):
    im = Image.open(ROOT / "photos" / f"{name}.png").convert("RGB")
    im = im.resize((TEX_W, TEX_H), Image.LANCZOS).convert("RGBA")
    for r in range(ROWS):
        for c in range(COLS):
            n = r * COLS + c
            im.crop((c * tile_w, r * tile_h, (c + 1) * tile_w, (r + 1) * tile_h)).save(
                tex_dir / f"{name}_{n}.png", optimize=True)
            providers.append({
                "type": "bitmap",
                "file": f"coalclicker:font/photo/{name}_{n}.png",
                "height": show_tile_h,
                "ascent": TOP_ASCENT - r * show_tile_h,
                "chars": [chr(0xE000 + index * 4 + n)],
            })

# 文字の後ろに 1 ピクセルの隙間が入るので -1 で詰める。行の頭へ戻るのは -(写真の幅 + 1)
providers.append({"type": "space", "advances": {"": -1, "": -(SHOW_W + 1)}})

(ROOT / "assets/coalclicker/font/photo.json").write_text(
    json.dumps({"providers": providers}, ensure_ascii=True, indent=2) + "\n", encoding="utf-8")
print("wrote", len(PHOTOS), "photo(s)")
