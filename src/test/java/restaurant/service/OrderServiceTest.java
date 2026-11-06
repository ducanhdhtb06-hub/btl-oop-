package restaurant.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import restaurant.exception.EmptyOrderException;
import restaurant.exception.ItemUnavailableException;
import restaurant.exception.MenuItemNotFoundException;
import restaurant.exception.NoOpenOrderException;
import restaurant.exception.RestaurantException;
import restaurant.exception.TableNotAvailableException;
import restaurant.exception.TableNotFoundException;
import restaurant.model.MenuItem;
import restaurant.model.Order;
import restaurant.model.OrderStatus;
import restaurant.model.Table;
import restaurant.pricing.BillCalculator;
import restaurant.pricing.NoDiscount;
import restaurant.pricing.PercentageDiscount;
import restaurant.repository.InMemoryRepository;
import restaurant.repository.SampleMenu;

class OrderServiceTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-20T05:30:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    @TempDir
    Path dir;

    private TableService tables;
    private MenuService menu;
    private InMemoryRepository<Order, Integer> orders;
    private Path billFile;
    private OrderService service;

    @BeforeEach
    void setUp() {
        InMemoryRepository<Table, Integer> tableRepo = new InMemoryRepository<>();
        TableService.defaultTables().forEach(tableRepo::save);
        tables = new TableService(tableRepo);
        InMemoryRepository<MenuItem, String> menuRepo = new InMemoryRepository<>();
        SampleMenu.items().forEach(menuRepo::save);
        menu = new MenuService(menuRepo);
        orders = new InMemoryRepository<>();
        billFile = dir.resolve("data/hoa-don.txt");
        service = newService(billFile);
    }

    private OrderService newService(Path file) {
        return new OrderService(orders, tables, menu, new BillCalculator(), file, CLOCK);
    }

    @Test
    @DisplayName("Mở bàn tạo đơn mới và đánh dấu bàn có khách; mã đơn tăng dần từ 1")
    void openOrder() throws RestaurantException {
        Order first = service.openOrder(3);
        assertEquals(1, first.getId());
        assertFalse(tables.find(3).isFree());
        assertEquals(2, service.openOrder(4).getId());
        assertEquals(first, service.currentOrder(3));
    }

    @Test
    @DisplayName("Không mở được bàn đang có khách hoặc bàn không tồn tại")
    void openOrderErrors() throws RestaurantException {
        service.openOrder(3);
        assertThrows(TableNotAvailableException.class, () -> service.openOrder(3));
        assertThrows(TableNotFoundException.class, () -> service.openOrder(11));
    }

    @Test
    @DisplayName("Gọi món: bàn chưa mở, món không có, món đã hết đều báo lỗi")
    void addItemErrors() throws RestaurantException {
        service.openOrder(3);
        assertThrows(NoOpenOrderException.class, () -> service.addItem(4, "F01", 1));
        assertThrows(MenuItemNotFoundException.class, () -> service.addItem(3, "F99", 1));
        assertThrows(ItemUnavailableException.class, () -> service.addItem(3, "D04", 1));
    }

    @Test
    @DisplayName("Sửa số lượng và bớt món qua service")
    void updateAndRemove() throws RestaurantException {
        service.openOrder(3);
        service.addItem(3, "F01", 2);
        service.addItem(3, "D01", 1);
        service.updateQuantity(3, "D01", 2);
        assertEquals(120_000, service.currentOrder(3).subtotal());
        service.removeItem(3, "D01");
        assertEquals(110_000, service.currentOrder(3).subtotal());
    }

    @Test
    @DisplayName("Thanh toán đơn rỗng bị từ chối, bàn vẫn giữ")
    void checkoutEmptyOrder() throws RestaurantException {
        service.openOrder(3);
        assertThrows(EmptyOrderException.class, () -> service.checkout(3, new NoDiscount()));
        assertFalse(tables.find(3).isFree());
    }

    @Test
    @DisplayName("Thanh toán giảm 10%: tổng 103.500, đơn PAID, bàn trống, hóa đơn được lưu ra file")
    void checkout() throws RestaurantException, IOException {
        Order order = service.openOrder(3);
        service.addItem(3, "F01", 2);
        service.addItem(3, "D01", 1);
        CheckoutResult result = service.checkout(3, new PercentageDiscount(10));
        assertEquals(103_500, result.bill().total());
        assertTrue(result.archived());
        assertTrue(tables.find(3).isFree());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertThrows(NoOpenOrderException.class, () -> service.currentOrder(3));
        String text = Files.readString(billFile, UTF_8);
        assertTrue(text.contains("========== HÓA ĐƠN =========="));
        assertTrue(text.contains("Đơn #1 - Bàn 3 - 20/11/2026 12:30"));
        assertTrue(text.contains("Phở bò x2 @ 55.000 đ = 110.000 đ"));
        assertTrue(text.contains("Tạm tính: 115.000 đ"));
        assertTrue(text.contains("Giảm 10%: -11.500 đ"));
        assertTrue(text.contains("TỔNG CỘNG: 103.500 đ"));
    }

    @Test
    @DisplayName("Thanh toán hai lần thì file hóa đơn có cả hai hóa đơn")
    void billsAreAppended() throws RestaurantException, IOException {
        service.openOrder(3);
        service.addItem(3, "F01", 1);
        service.checkout(3, new NoDiscount());
        service.openOrder(3);
        service.addItem(3, "D01", 1);
        service.checkout(3, new NoDiscount());
        String text = Files.readString(billFile, UTF_8);
        assertEquals(2, text.split("========== HÓA ĐƠN", -1).length - 1);
    }

    @Test
    @DisplayName("Lưu hóa đơn lỗi thì vẫn ghi nhận thanh toán, chỉ báo chưa lưu được")
    void checkoutWhenArchiveFails() throws RestaurantException {
        OrderService broken = newService(dir);
        Order order = broken.openOrder(5);
        broken.addItem(5, "F02", 1);
        CheckoutResult result = broken.checkout(5, new NoDiscount());
        assertFalse(result.archived());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertTrue(tables.find(5).isFree());
    }

    @Test
    @DisplayName("Hủy đơn trả bàn và chuyển đơn sang CANCELLED")
    void cancel() throws RestaurantException {
        Order order = service.openOrder(5);
        service.addItem(5, "F01", 1);
        service.cancel(5);
        assertTrue(tables.find(5).isFree());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        assertThrows(NoOpenOrderException.class, () -> service.cancel(5));
    }

    @Test
    @DisplayName("Thống kê chỉ tính đơn đã thanh toán; bằng số lượng thì xếp theo tên")
    void bestSellers() throws RestaurantException {
        service.openOrder(1);
        service.addItem(1, "F01", 2);
        service.addItem(1, "D01", 1);
        service.checkout(1, new NoDiscount());
        service.openOrder(2);
        service.addItem(2, "D01", 2);
        service.addItem(2, "F02", 2);
        service.checkout(2, new NoDiscount());
        service.openOrder(3);
        service.addItem(3, "F01", 5);
        service.openOrder(4);
        service.addItem(4, "F02", 3);
        service.cancel(4);
        List<ItemSales> top = service.bestSellers(5);
        assertEquals(List.of(
                new ItemSales("Trà đá", 3, 15_000),
                new ItemSales("Cơm gà", 2, 100_000),
                new ItemSales("Phở bò", 2, 110_000)), top);
        assertEquals(1, service.bestSellers(1).size());
    }
}
