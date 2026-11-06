package restaurant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restaurant.exception.MenuItemNotFoundException;
import restaurant.model.ItemType;
import restaurant.model.MenuItem;
import restaurant.repository.InMemoryRepository;
import restaurant.repository.SampleMenu;

class MenuServiceTest {

    private InMemoryRepository<MenuItem, String> repo;
    private MenuService menu;

    @BeforeEach
    void setUp() {
        repo = new InMemoryRepository<>();
        SampleMenu.items().forEach(repo::save);
        menu = new MenuService(repo);
    }

    @Test
    @DisplayName("Liệt kê thực đơn theo mã và lọc theo loại")
    void listAndFilter() {
        assertEquals(SampleMenu.items().size(), menu.all().size());
        assertEquals("C01", menu.all().get(0).getId());
        assertEquals(4, menu.byType(ItemType.DRINK).size());
        assertTrue(menu.byType(ItemType.FOOD).stream().allMatch(i -> i.getType() == ItemType.FOOD));
    }

    @Test
    @DisplayName("Lọc món chay không hiện đồ uống")
    void vegetarianExcludesDrinks() {
        assertEquals(List.of("C02", "F04", "F05"), menu.vegetarian().stream().map(MenuItem::getId).toList());
    }

    @Test
    @DisplayName("Tìm món theo mã, không có thì báo lỗi")
    void find() throws MenuItemNotFoundException {
        assertEquals("Phở bò", menu.find("F01").getName());
        assertThrows(MenuItemNotFoundException.class, () -> menu.find("F99"));
    }

    @Test
    @DisplayName("Thêm món tự sinh mã kế tiếp theo loại")
    void addGeneratesNextId() {
        assertEquals("F06", menu.addFood("Bánh xèo", 45_000, false).getId());
        assertEquals("D05", menu.addDrink("Sinh tố bơ", 40_000, 400).getId());
        assertTrue(repo.findById("F06").isPresent());
        assertEquals("F01", new MenuService(new InMemoryRepository<>()).addFood("Món đầu", 1, false).getId());
    }

    @Test
    @DisplayName("Lọc được combo; combo không sửa giá trực tiếp được")
    void comboFilterAndPrice() {
        assertEquals(List.of("C01", "C02"), menu.byType(ItemType.COMBO).stream().map(MenuItem::getId).toList());
        assertThrows(IllegalArgumentException.class, () -> menu.changePrice("C01", 1_000));
    }

    @Test
    @DisplayName("Sửa giá và bật tắt còn món")
    void changePriceAndAvailability() throws MenuItemNotFoundException {
        menu.changePrice("F01", 60_000);
        assertEquals(60_000, menu.find("F01").getPrice());
        menu.setAvailable("D04", true);
        assertTrue(menu.find("D04").isAvailable());
        menu.setAvailable("F01", false);
        assertFalse(menu.find("F01").isAvailable());
        assertThrows(MenuItemNotFoundException.class, () -> menu.changePrice("F99", 1));
        assertThrows(IllegalArgumentException.class, () -> menu.changePrice("F01", 0));
    }

    @Test
    @DisplayName("Bật tắt combo theo cờ của chính combo, kể cả khi món thành phần đang hết")
    void toggleComboUsesItsOwnFlag() throws MenuItemNotFoundException {
        menu.setAvailable("D01", false);
        assertFalse(menu.toggleAvailable("C01"));
        assertFalse(menu.find("C01").isMarkedAvailable());
        menu.setAvailable("D01", true);
        assertFalse(menu.find("C01").isAvailable());
        assertTrue(menu.toggleAvailable("C01"));
        assertTrue(menu.find("C01").isAvailable());
    }
}
