package io.github.shitamori1272.coalclicker.shop;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.shitamori1272.coalclicker.game.Tool;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class ToolPhotoTest {

    /**
     * 文字の割り当ては並び順で決まるので、リソースパックを作るスクリプトの PHOTOS と
     * {@link ToolPhoto} の並びがずれると別の道具の写真が出る。
     */
    @Test
    void スクリプトと並び順が同じ() throws IOException {
        String script = Files.readString(Path.of("../resourcepack/make_photos.py"), StandardCharsets.UTF_8);
        Matcher m = Pattern.compile("(?m)^PHOTOS = \\[(.*)]").matcher(script);
        assertTrue(m.find());
        List<String> inScript = Arrays.stream(m.group(1).split(","))
                .map(s -> s.strip().replace("\"", ""))
                .toList();
        List<String> inPlugin = Arrays.stream(ToolPhoto.values())
                .map(photo -> Tool.valueOf(photo.name()).id())
                .toList();
        assertEquals(inPlugin, inScript);
    }

    @Test
    void 写真ごとに4文字ずつずれる() {
        assertEquals('', ToolPhoto.values()[0].glyph().charAt(0));
        assertEquals('', ToolPhoto.values()[1].glyph().charAt(0));
    }
}
