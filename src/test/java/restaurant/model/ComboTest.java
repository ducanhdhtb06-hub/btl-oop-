package restaurant.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restaurant.exception.ItemUnavailableException;
import restaurant.exception.RestaurantException;

class ComboTest {

    private final Food comGa = new Food("F02", "Cơm gà", 50_000, true, false);
    private final Food goiCuon = new Food("F04", "Gỏi cuốn chay", 35_000, true, true);
    private final Food dauHu = new Food("F05", "Đậu hũ sốt cà chua", 40_000, true, true);
    private final Drink traDa = new Drink("D01", "Trà đá", 5_000, true, 300);
    private final Drink nuocCam = new Drink("D03", "Nước cam", 30_000, true, 330);
    private final Combo trua = new Combo("C01", "Combo trưa", true, 10, List.of(comGa, traDa));

    @Test
    @DisplayName("Giá combo = tổng giá món thành phần trừ % combo")
    void priceFromComponents() {
        assertEquals(49_500, trua.getPrice());
        assertEquals(89_250, new Combo("C02", "Combo chay", true, 15, List.of(goiCuon, dauHu, nuocCam)).getPrice());
    }

    @Test
    @DisplayName("Tiền giảm của combo làm tròn đến đồng: 12.345 giảm 10% còn 11.110")
    void priceRounding() {
        Drink odd = new Drink("D09", "Nước test", 12_345, true, 330);
        assertEquals(11_110, new Combo("C08", "Thử", true, 10, List.of(odd)).getPrice());
    }

    @Test
    @DisplayName("Combo lồng combo: giá tính đệ quy")
    void nestedCombo() {
        Combo lon = new Combo("C09", "Combo lớn", true, 0, List.of(trua, nuocCam));
        assertEquals(79_500, lon.getPrice());
    }

    @Test
    @DisplayName("Giá combo đổi theo giá món thành phần")
    void priceFollowsComponents() {
        comGa.changePrice(60_000);
        assertEquals(58_500, trua.getPrice());
    }

    @Test
    @DisplayName("Combo hết khi một món thành phần hết, kể cả combo lồng nhau")
    void availabilityFollowsComponents() {
        Combo lon = new Combo("C09", "Combo lớn", true, 0, List.of(trua, nuocCam));
        assertTrue(trua.isAvailable());
        traDa.setAvailable(false);
        assertFalse(trua.isAvailable());
        assertFalse(lon.isAvailable());
        traDa.setAvailable(true);
        trua.setAvailable(false);
        assertFalse(trua.isAvailable());
    }

    @Test
    @DisplayName("Combo chay khi mọi món thành phần đều chay")
    void vegetarian() {
        assertTrue(new Combo("C02", "Combo chay", true, 15, List.of(goiCuon, dauHu, nuocCam)).isVegetarian());
        assertFalse(trua.isVegetarian());
    }

    @Test
    @DisplayName("Combo tự mô tả và tự ghi ra CSV")
    void describeAndCsv() {
        assertEquals("Combo - Combo trưa: Cơm gà + Trà đá (giảm 10%)", trua.describe());
        assertEquals("COMBO;C01;Combo trưa;10;true;F02|D01", trua.toCsvRow());
        assertEquals(ItemType.COMBO, trua.getType());
        traDa.setAvailable(false);
        assertEquals("COMBO;C01;Combo trưa;10;true;F02|D01", trua.toCsvRow());
    }

    @Test
    @DisplayName("Danh sách món thành phần chỉ đọc")
    void componentsAreReadOnly() {
        assertThrows(UnsupportedOperationException.class, () -> trua.getComponents().clear());
        assertEquals(List.of(comGa, traDa), trua.getComponents());
    }

    @Test
    @DisplayName("Từ chối combo rỗng và % ngoài khoảng 0 đến 100")
    void rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new Combo("C06", "Rỗng", true, 0, List.of()));
        assertThrows(IllegalArgumentException.class, () -> new Combo("C06", "Sai", true, 101, List.of(traDa)));
        assertThrows(IllegalArgumentException.class, () -> new Combo("C06", "Sai", true, -1, List.of(traDa)));
    }

    @Test
    @DisplayName("Gọi combo trong đơn dùng chung code tính tiền của Order (OCP, LSP)")
    void comboInOrder() throws RestaurantException {
        Order order = new Order(1, new Table(3, 4), LocalDateTime.of(2026, 11, 20, 12, 0));
        order.addItem(trua, 2);
        assertEquals(99_000, order.subtotal());
        traDa.setAvailable(false);
        assertThrows(ItemUnavailableException.class, () -> order.addItem(trua));
    }
}
