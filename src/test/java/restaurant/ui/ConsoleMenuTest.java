package restaurant.ui;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Scanner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restaurant.model.MenuItem;
import restaurant.model.Table;
import restaurant.pricing.BillCalculator;
import restaurant.repository.InMemoryRepository;
import restaurant.repository.SampleMenu;
import restaurant.service.MenuService;
import restaurant.service.OrderService;
import restaurant.service.TableService;

class ConsoleMenuTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-20T05:30:00Z"), ZoneId.of("Asia/Ho_Chi_Minh"));

    /** Chạy menu console với dữ liệu nhập cho trước, trả về toàn bộ những gì được in ra. */
    private String run(String input) {
        InMemoryRepository<MenuItem, String> menuRepo = new InMemoryRepository<>();
        SampleMenu.items().forEach(menuRepo::save);
        InMemoryRepository<Table, Integer> tableRepo = new InMemoryRepository<>();
        TableService.defaultTables().forEach(tableRepo::save);
        MenuService menu = new MenuService(menuRepo);
        TableService tables = new TableService(tableRepo);
        OrderService orders = new OrderService(new InMemoryRepository<>(), tables, menu,
                new BillCalculator(), bill -> { }, CLOCK);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(bytes, true, UTF_8);
        new ConsoleMenu(new InputReader(new Scanner(input), out), out, menu, tables, orders).run();
        return bytes.toString(UTF_8);
    }

    @Test
    @DisplayName("Kịch bản demo chạy hết và ra đúng tổng tiền")
    void demoScript() throws IOException {
        String output = run(Files.readString(Path.of("scripts", "demo-input.txt"), UTF_8));
        assertTrue(output.contains("Lỗi: Món Trà đào đã hết"));
        assertTrue(output.contains("Vui lòng nhập một số"));
        assertTrue(output.contains("TỔNG CỘNG: 152.550 đ"));
        assertTrue(output.trim().endsWith("Tạm biệt"));
    }

    @Test
    @DisplayName("Thanh toán bàn chưa có đơn thì báo lỗi ngay, không hỏi cách giảm giá")
    void checkoutWithoutOrderFailsFast() {
        String output = run("7\n5\n0\n");
        assertTrue(output.contains("Lỗi: Bàn 5 chưa có đơn đang mở"));
        assertFalse(output.contains("Chọn cách giảm giá"));
    }

    @Test
    @DisplayName("Thanh toán đơn chưa có món thì báo lỗi ngay, không hỏi cách giảm giá")
    void checkoutEmptyOrderFailsFast() {
        String output = run("3\n5\n7\n5\n0\n");
        assertTrue(output.contains("chưa có món nào"));
        assertFalse(output.contains("Chọn cách giảm giá"));
    }

    @Test
    @DisplayName("Thống kê ghi rõ doanh thu là số tiền trước giảm giá")
    void bestSellersSayRevenueIsBeforeDiscount() {
        String output = run("3\n3\n4\n3\nF01\n2\n7\n3\n2\n10\n10\n0\n");
        assertTrue(output.contains("TỔNG CỘNG: 99.000 đ"));
        assertTrue(output.contains("trước giảm giá"));
    }
}
