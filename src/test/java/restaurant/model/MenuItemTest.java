package restaurant.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MenuItemTest {

    private final Food pho = new Food("F01", "Phở bò", 55_000, true, false);
    private final Food goi = new Food("F04", "Gỏi cuốn chay", 35_000, true, true);
    private final Drink tra = new Drink("D01", "Trà đá", 5_000, true, 300);

    @Test
    @DisplayName("Mỗi lớp con tự mô tả theo cách riêng (đa hình)")
    void describeDiffersBySubclass() {
        assertEquals("Món ăn - Phở bò", pho.describe());
        assertEquals("Món ăn - Gỏi cuốn chay [chay]", goi.describe());
        assertEquals("Đồ uống - Trà đá (300 ml)", tra.describe());
    }

    @Test
    @DisplayName("Mỗi lớp con tự ghi ra một dòng CSV theo loại")
    void csvRowBySubclass() {
        assertEquals("FOOD;F01;Phở bò;55000;true;false", pho.toCsvRow());
        assertEquals("DRINK;D01;Trà đá;5000;true;300", tra.toCsvRow());
    }

    @Test
    @DisplayName("Dùng chung qua kiểu cha MenuItem")
    void polymorphicThroughSuperType() {
        List<MenuItem> items = List.of(pho, tra);
        assertEquals(60_000, items.stream().mapToLong(MenuItem::getPrice).sum());
        assertEquals(ItemType.FOOD, pho.getType());
        assertEquals(ItemType.DRINK, tra.getType());
    }

    @Test
    @DisplayName("Món chay: món ăn theo cờ chay, đồ uống luôn chay")
    void vegetarian() {
        assertTrue(goi.isVegetarian());
        assertFalse(pho.isVegetarian());
        assertTrue(tra.isVegetarian());
    }

    @Test
    @DisplayName("Hai món bằng nhau khi cùng mã")
    void equalityById() {
        assertEquals(pho, new Food("F01", "Tên khác", 1, false, true));
        assertEquals(pho.hashCode(), new Food("F01", "Tên khác", 1, false, true).hashCode());
    }

    @Test
    @DisplayName("Đổi giá và bật tắt còn món")
    void changePriceAndAvailability() {
        pho.changePrice(60_000);
        assertEquals(60_000, pho.getPrice());
        pho.setAvailable(false);
        assertFalse(pho.isAvailable());
        assertThrows(IllegalArgumentException.class, () -> pho.changePrice(-1));
        assertThrows(IllegalArgumentException.class, () -> pho.changePrice(0));
    }

    @Test
    @DisplayName("Từ chối tên món chứa dấu ; hoặc xuống dòng vì làm hỏng file CSV")
    void rejectsNamesThatBreakCsv() {
        assertThrows(IllegalArgumentException.class, () -> new Food("F09", "Phở; bò", 1, true, false));
        assertThrows(IllegalArgumentException.class, () -> new Food("F09", "Phở\nbò", 1, true, false));
        assertThrows(IllegalArgumentException.class, () -> new Food("F09", "Phở\rbò", 1, true, false));
    }

    @Test
    @DisplayName("Từ chối mã rỗng, tên rỗng, giá không dương, dung tích không dương")
    void rejectsInvalidData() {
        assertThrows(IllegalArgumentException.class, () -> new Food("F09", "  ", 1, true, false));
        assertThrows(IllegalArgumentException.class, () -> new Food("", "Phở", 1, true, false));
        assertThrows(IllegalArgumentException.class, () -> new Food(null, "Phở", 1, true, false));
        assertThrows(IllegalArgumentException.class, () -> new Food("F09", "Phở", 0, true, false));
        assertThrows(IllegalArgumentException.class, () -> new Drink("D09", "Nước", 1, true, 0));
    }
}
