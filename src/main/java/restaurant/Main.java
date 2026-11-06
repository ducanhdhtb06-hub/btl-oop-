package restaurant;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Scanner;
import restaurant.model.Order;
import restaurant.model.Table;
import restaurant.pricing.BillCalculator;
import restaurant.repository.FileBillArchive;
import restaurant.repository.FileMenuRepository;
import restaurant.repository.InMemoryRepository;
import restaurant.service.MenuService;
import restaurant.service.OrderService;
import restaurant.service.TableService;
import restaurant.ui.ConsoleMenu;

/** Điểm khởi động: tạo các đối tượng, truyền phụ thuộc vào nhau rồi chạy giao diện console. */
public final class Main {

    private Main() {
    }

    /**
     * Chạy ứng dụng.
     *
     * @param args không dùng
     */
    public static void main(String[] args) {
        PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out), true, UTF_8);
        Scanner in = new Scanner(System.in, UTF_8);

        FileMenuRepository menuRepo = new FileMenuRepository(Path.of("data", "menu.csv"));
        menuRepo.getWarnings().forEach(warning -> out.println("Cảnh báo: " + warning));
        InMemoryRepository<Table, Integer> tableRepo = new InMemoryRepository<>();
        TableService.defaultTables().forEach(tableRepo::save);

        MenuService menu = new MenuService(menuRepo);
        TableService tables = new TableService(tableRepo);
        OrderService orders = new OrderService(new InMemoryRepository<Order, Integer>(), tables, menu,
                new BillCalculator(), new FileBillArchive(Path.of("data", "hoa-don.txt")), Clock.systemDefaultZone());

        new ConsoleMenu(in, out, menu, tables, orders).run();
    }
}
