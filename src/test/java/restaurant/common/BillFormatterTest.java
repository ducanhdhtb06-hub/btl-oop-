package restaurant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restaurant.model.Bill;
import restaurant.model.BillLine;

class BillFormatterTest {

    @Test
    @DisplayName("Định dạng hóa đơn đúng mẫu in và lưu file")
    void formatsBill() {
        Bill bill = new Bill(1, 3, LocalDateTime.of(2026, 11, 20, 12, 30),
                List.of(new BillLine("Phở bò", 2, 55_000, 110_000), new BillLine("Trà đá", 1, 5_000, 5_000)),
                115_000, "Giảm 10%", 11_500, 103_500);
        String expected = String.join("\n",
                "========== HÓA ĐƠN ==========",
                "Đơn #1 - Bàn 3 - 20/11/2026 12:30",
                "Phở bò x2 @ 55.000 đ = 110.000 đ",
                "Trà đá x1 @ 5.000 đ = 5.000 đ",
                "Tạm tính: 115.000 đ",
                "Giảm 10%: -11.500 đ",
                "TỔNG CỘNG: 103.500 đ",
                "=============================") + "\n";
        assertEquals(expected, BillFormatter.format(bill));
    }
}
