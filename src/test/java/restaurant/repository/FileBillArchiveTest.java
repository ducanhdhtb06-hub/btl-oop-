package restaurant.repository;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import restaurant.model.Bill;
import restaurant.model.BillLine;

class FileBillArchiveTest {

    @TempDir
    Path dir;

    private final Bill bill = new Bill(1, 3, LocalDateTime.of(2026, 11, 20, 12, 30),
            List.of(new BillLine("Phở bò", 1, 55_000, 55_000)), 55_000, "Không giảm giá", 0, 55_000);

    @Test
    @DisplayName("Ghi nối tiếp nhiều hóa đơn, tự tạo thư mục, giữ tiếng Việt UTF-8")
    void appendsBills() throws IOException {
        Path file = dir.resolve("data/hoa-don.txt");
        FileBillArchive archive = new FileBillArchive(file);
        archive.archive(bill);
        archive.archive(bill);
        String text = Files.readString(file, UTF_8);
        assertEquals(2, text.split("========== HÓA ĐƠN", -1).length - 1);
        assertTrue(text.contains("Phở bò x1 @ 55.000 đ = 55.000 đ"));
    }

    @Test
    @DisplayName("Không ghi được file thì báo IOException cho nơi gọi xử lý")
    void reportsWriteFailure() {
        FileBillArchive archive = new FileBillArchive(dir);
        assertThrows(IOException.class, () -> archive.archive(bill));
    }
}
