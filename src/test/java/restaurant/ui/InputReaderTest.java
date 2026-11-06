package restaurant.ui;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InputReaderTest {

    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    private InputReader reader(String input) {
        return new InputReader(new Scanner(input), new PrintStream(output, true, UTF_8));
    }

    private String printed() {
        return output.toString(UTF_8);
    }

    @Test
    @DisplayName("Gõ chữ vào chỗ cần số thì báo và hỏi lại")
    void retriesOnText() {
        assertEquals(5, reader("abc\n5\n").readInt("Chọn: ", 1, 10));
        assertTrue(printed().contains("Vui lòng nhập một số"));
    }

    @Test
    @DisplayName("Tự bỏ khoảng trắng thừa quanh số")
    void trimsSpaces() {
        assertEquals(7, reader(" 7 \n").readInt("Chọn: ", 1, 10));
    }

    @Test
    @DisplayName("Số ngoài khoảng hoặc dòng trống thì hỏi lại")
    void retriesOutOfRangeAndBlank() {
        assertEquals(3, reader("0\n11\n\n3\n").readInt("Chọn: ", 1, 10));
        assertTrue(printed().contains("từ 1 đến 10"));
    }

    @Test
    @DisplayName("Hết dữ liệu nhập thì báo InputClosedException, không lặp vô hạn")
    void endOfInput() {
        assertThrows(InputClosedException.class, () -> reader("").readInt("Chọn: ", 1, 10));
        assertThrows(InputClosedException.class, () -> reader("abc\n").readInt("Chọn: ", 1, 10));
        assertThrows(InputClosedException.class, () -> reader("").readNonBlank("Tên: "));
    }

    @Test
    @DisplayName("Đọc chuỗi bỏ qua dòng trống")
    void readNonBlank() {
        assertEquals("Phở", reader("\n   \nPhở\n").readNonBlank("Tên: "));
        assertTrue(printed().contains("Không được để trống"));
    }

    @Test
    @DisplayName("Hỏi xác nhận c/k, không phân biệt hoa thường, câu khác thì hỏi lại")
    void confirm() {
        assertTrue(reader("x\nC\n").confirm("Hủy? "));
        assertTrue(reader("có\n").confirm("Hủy? "));
        assertFalse(reader("không\n").confirm("Hủy? "));
        assertFalse(reader("K\n").confirm("Hủy? "));
    }
}
