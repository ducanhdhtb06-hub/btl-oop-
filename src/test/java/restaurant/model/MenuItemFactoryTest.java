package restaurant.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MenuItemFactoryTest {

    private final Function<String, Optional<MenuItem>> none = id -> Optional.empty();

    @Test
    @DisplayName("Factory tạo đúng lớp con theo cột loại món")
    void createsRightSubclass() {
        MenuItem pho = MenuItemFactory.fromCsvRow("FOOD;F01;Phở bò;55000;true;false", none);
        Food food = assertInstanceOf(Food.class, pho);
        assertEquals(55_000, food.getPrice());
        assertFalse(food.isVegetarian());
        Drink drink = assertInstanceOf(Drink.class, MenuItemFactory.fromCsvRow("DRINK;D01;Trà đá;5000;false;300", none));
        assertEquals(300, drink.getVolumeMl());
        assertFalse(drink.isAvailable());
    }

    @Test
    @DisplayName("Ghi ra CSV rồi đọc lại được món giống hệt")
    void roundTrip() {
        Food goi = new Food("F04", "Gỏi cuốn chay", 35_000, true, true);
        MenuItem again = MenuItemFactory.fromCsvRow(goi.toCsvRow(), none);
        assertEquals(goi, again);
        assertEquals(goi.getPrice(), again.getPrice());
        assertTrue(again.isVegetarian());
        assertEquals(goi.describe(), again.describe());
    }

    @Test
    @DisplayName("Dòng sai thì báo IllegalArgumentException kèm lý do")
    void rejectsBadRows() {
        assertThrows(IllegalArgumentException.class, () -> MenuItemFactory.fromCsvRow("XYZ;X01;Lạ;1;true;x", none));
        assertThrows(IllegalArgumentException.class, () -> MenuItemFactory.fromCsvRow("FOOD;F01;Thiếu", none));
        assertThrows(IllegalArgumentException.class, () -> MenuItemFactory.fromCsvRow("DRINK;D01;Trà;abc;true;300", none));
        assertThrows(IllegalArgumentException.class, () -> MenuItemFactory.fromCsvRow("FOOD;F01;Phở;1;yes;false", none));
    }
}
