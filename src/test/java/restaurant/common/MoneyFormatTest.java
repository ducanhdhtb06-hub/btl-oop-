package restaurant.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyFormatTest {

    @Test
    @DisplayName("Định dạng tiền kiểu Việt Nam: nhóm 3 chữ số bằng dấu chấm, đơn vị đ")
    void formatsVietnamese() {
        assertEquals("185.000 đ", MoneyFormat.format(185_000));
        assertEquals("1.000.000 đ", MoneyFormat.format(1_000_000));
        assertEquals("0 đ", MoneyFormat.format(0));
        assertEquals("5.000 đ", MoneyFormat.format(5_000));
        assertEquals("999 đ", MoneyFormat.format(999));
    }

    @Test
    @DisplayName("Số âm (dòng tiền giảm trên hóa đơn) có dấu trừ phía trước")
    void formatsNegative() {
        assertEquals("-16.500 đ", MoneyFormat.format(-16_500));
    }
}
