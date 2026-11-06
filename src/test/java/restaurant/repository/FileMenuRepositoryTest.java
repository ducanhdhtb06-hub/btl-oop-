package restaurant.repository;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import restaurant.model.Food;
import restaurant.model.MenuItem;

class FileMenuRepositoryTest {

    @TempDir
    Path dir;

    @Test
    @DisplayName("Thiếu cả thư mục data thì tự tạo thư mục và thực đơn mẫu")
    void createsSampleMenuWhenMissing() {
        Path file = dir.resolve("data/menu.csv");
        FileMenuRepository repo = new FileMenuRepository(file);
        assertTrue(Files.exists(file));
        assertEquals(SampleMenu.items().size(), repo.findAll().size());
        assertTrue(repo.getWarnings().isEmpty());
    }

    @Test
    @DisplayName("Ghi rồi đọc lại đúng thực đơn, giữ tiếng Việt UTF-8")
    void roundTripKeepsVietnamese() throws IOException {
        Path file = dir.resolve("data/menu.csv");
        FileMenuRepository repo = new FileMenuRepository(file);
        repo.save(new Food("F06", "Bánh xèo", 45_000, true, false));
        FileMenuRepository again = new FileMenuRepository(file);
        MenuItem banhXeo = again.findById("F06").orElseThrow();
        assertEquals("Bánh xèo", banhXeo.getName());
        assertEquals(45_000, banhXeo.getPrice());
        assertFalse(again.findById("D04").orElseThrow().isAvailable());
        assertTrue(Files.readString(file, UTF_8).contains("Phở bò"));
        assertTrue(Files.readString(file, UTF_8).startsWith("type;id;name;value;available;detail"));
    }

    @Test
    @DisplayName("Bỏ qua dòng hỏng, dòng trùng mã, loại lạ; báo kèm số dòng")
    void skipsBadLines() throws IOException {
        Path file = dir.resolve("menu.csv");
        Files.writeString(file, String.join("\n",
                "type;id;name;value;available;detail",
                "FOOD;F01;Phở bò;55000;true;false",
                "FOOD;F02;Thiếu cột",
                "FOOD;F01;Phở trùng;1;true;false",
                "XYZ;X01;Lạ;1;true;x",
                "DRINK;D01;Trà đá;abc;true;300"), UTF_8);
        FileMenuRepository repo = new FileMenuRepository(file);
        assertEquals(1, repo.findAll().size());
        assertEquals("Phở bò", repo.findById("F01").orElseThrow().getName());
        assertEquals(4, repo.getWarnings().size());
        assertTrue(repo.getWarnings().get(0).startsWith("Dòng 3: "));
        assertTrue(repo.getWarnings().get(1).startsWith("Dòng 4: "));
        assertTrue(repo.getWarnings().get(3).startsWith("Dòng 6: "));
    }

    @Test
    @DisplayName("Bỏ qua dòng trống ở cuối file")
    void ignoresBlankLines() throws IOException {
        Path file = dir.resolve("menu.csv");
        Files.writeString(file, "type;id;name;value;available;detail\nFOOD;F01;Phở bò;55000;true;false\n\n", UTF_8);
        FileMenuRepository repo = new FileMenuRepository(file);
        assertEquals(1, repo.findAll().size());
        assertTrue(repo.getWarnings().isEmpty());
    }

    @Test
    @DisplayName("Không đọc được file thì báo lỗi và dùng thực đơn mẫu")
    void unreadableFileFallsBackToSample() {
        FileMenuRepository repo = new FileMenuRepository(dir);
        assertEquals(SampleMenu.items().size(), repo.findAll().size());
        assertEquals(1, repo.getWarnings().size());
        assertTrue(repo.getWarnings().get(0).startsWith("Không đọc được"));
    }
}
