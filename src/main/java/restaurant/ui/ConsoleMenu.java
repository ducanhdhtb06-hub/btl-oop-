package restaurant.ui;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;
import restaurant.common.MoneyFormat;
import restaurant.exception.RestaurantException;
import restaurant.model.Bill;
import restaurant.model.BillLine;
import restaurant.model.ItemType;
import restaurant.model.MenuItem;
import restaurant.model.Order;
import restaurant.model.OrderItem;
import restaurant.model.Table;
import restaurant.pricing.DiscountType;
import restaurant.service.CheckoutResult;
import restaurant.service.ItemSales;
import restaurant.service.MenuService;
import restaurant.service.OrderService;
import restaurant.service.TableService;

/**
 * Giao diện console: in menu, đọc lựa chọn, gọi service và in kết quả.
 * Mọi lỗi nghiệp vụ được bắt lại, in thông báo rồi quay về menu chính.
 */
public class ConsoleMenu {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final Scanner in;
    private final PrintStream out;
    private final MenuService menu;
    private final TableService tables;
    private final OrderService orders;

    /**
     * Tạo giao diện console.
     *
     * @param in nguồn đọc dữ liệu nhập
     * @param out nơi in kết quả
     * @param menu nghiệp vụ thực đơn
     * @param tables nghiệp vụ bàn
     * @param orders nghiệp vụ đơn
     */
    public ConsoleMenu(Scanner in, PrintStream out, MenuService menu, TableService tables, OrderService orders) {
        this.in = in;
        this.out = out;
        this.menu = menu;
        this.tables = tables;
        this.orders = orders;
    }

    /** Chạy vòng lặp menu cho tới khi người dùng chọn 0 hoặc hết dữ liệu nhập. */
    public void run() {
        out.println("===== NHÀ HÀNG - HỆ THỐNG GỌI MÓN =====");
        try {
            while (true) {
                printMainMenu();
                int choice = readInt("Chọn chức năng: ", 0, 10);
                if (choice == 0) {
                    break;
                }
                try {
                    handle(choice);
                } catch (RestaurantException | IllegalArgumentException e) {
                    out.println("Lỗi: " + e.getMessage());
                } catch (UncheckedIOException e) {
                    out.println("Lỗi: " + e.getMessage());
                }
            }
        } catch (EndOfInput e) {
            out.println();
        }
        out.println("Tạm biệt");
    }

    private void printMainMenu() {
        out.println();
        out.println("1. Xem thực đơn        6. Xem đơn của bàn");
        out.println("2. Xem tình trạng bàn  7. Thanh toán");
        out.println("3. Mở bàn              8. Hủy đơn");
        out.println("4. Gọi món             9. Quản lý thực đơn");
        out.println("5. Sửa hoặc bớt món    10. Thống kê món bán chạy");
        out.println("0. Thoát");
    }

    private void handle(int choice) throws RestaurantException {
        switch (choice) {
            case 1 -> showMenu();
            case 2 -> showTables();
            case 3 -> openTable();
            case 4 -> addItem();
            case 5 -> changeItem();
            case 6 -> showOrder();
            case 7 -> checkout();
            case 8 -> cancelOrder();
            case 9 -> manageMenu();
            case 10 -> showBestSellers();
            default -> out.println("Chức năng không tồn tại");
        }
    }

    private void showMenu() {
        out.println("1. Tất cả  2. Món ăn  3. Đồ uống  4. Món chay");
        int filter = readInt("Chọn: ", 1, 4);
        List<MenuItem> items = switch (filter) {
            case 2 -> menu.byType(ItemType.FOOD);
            case 3 -> menu.byType(ItemType.DRINK);
            case 4 -> menu.vegetarian();
            default -> menu.all();
        };
        out.println("----- THỰC ĐƠN -----");
        for (MenuItem item : items) {
            out.printf("%-4s %-45s %12s%s%n", item.getId(), item.describe(),
                    MoneyFormat.format(item.getPrice()), item.isAvailable() ? "" : "  (hết)");
        }
    }

    private void showTables() {
        for (Table table : tables.all()) {
            out.printf("Bàn %2d (%d ghế): %s%n", table.getNumber(), table.getSeats(),
                    table.isFree() ? "Trống" : "Có khách");
        }
    }

    private void openTable() throws RestaurantException {
        int tableNo = readInt("Số bàn: ", 1, 99);
        Order order = orders.openOrder(tableNo);
        out.println("Đã mở bàn " + tableNo + " (đơn #" + order.getId() + ")");
    }

    private void addItem() throws RestaurantException {
        int tableNo = readInt("Số bàn: ", 1, 99);
        String itemId = readItemId();
        int quantity = readInt("Số lượng (1-99): ", 1, 99);
        orders.addItem(tableNo, itemId, quantity);
        out.println("Đã thêm " + quantity + " x " + menu.find(itemId).getName() + " vào bàn " + tableNo
                + ". Tạm tính: " + MoneyFormat.format(orders.currentOrder(tableNo).subtotal()));
    }

    private void changeItem() throws RestaurantException {
        int tableNo = readInt("Số bàn: ", 1, 99);
        String itemId = readItemId();
        int quantity = readInt("Số lượng mới (0 = bớt món): ", 0, 99);
        if (quantity == 0) {
            orders.removeItem(tableNo, itemId);
            out.println("Đã bớt món " + itemId);
        } else {
            orders.updateQuantity(tableNo, itemId, quantity);
            out.println("Đã sửa " + itemId + " thành " + quantity + " phần");
        }
    }

    private void showOrder() throws RestaurantException {
        int tableNo = readInt("Số bàn: ", 1, 99);
        Order order = orders.currentOrder(tableNo);
        out.println("Đơn #" + order.getId() + " - Bàn " + tableNo);
        if (order.isEmpty()) {
            out.println("(chưa có món)");
        }
        for (OrderItem line : order.getItems()) {
            out.println(line.getItem().getName() + " x" + line.getQuantity() + " @ "
                    + MoneyFormat.format(line.getUnitPrice()) + " = " + MoneyFormat.format(line.lineTotal()));
        }
        out.println("Tạm tính: " + MoneyFormat.format(order.subtotal()));
    }

    private void checkout() throws RestaurantException {
        int tableNo = readInt("Số bàn: ", 1, 99);
        out.println("1. Không giảm  2. Giảm theo %  3. Giảm số tiền");
        int kind = readInt("Chọn cách giảm giá: ", 1, 3);
        CheckoutResult result;
        if (kind == 2) {
            result = orders.checkout(tableNo, DiscountType.PERCENT, readInt("Phần trăm giảm (0-100): ", 0, 100));
        } else if (kind == 3) {
            result = orders.checkout(tableNo, DiscountType.FIXED, readInt("Số tiền giảm: ", 0, 100_000_000));
        } else {
            result = orders.checkout(tableNo, DiscountType.NONE, 0);
        }
        printBill(result.bill());
        if (!result.archived()) {
            out.println("Cảnh báo: chưa lưu được hóa đơn vào file, thanh toán vẫn được ghi nhận");
        }
    }

    // Bản M4: ConsoleMenu tự định dạng hóa đơn, lặp lại code trong OrderService (sẽ gom ở M5).
    private void printBill(Bill bill) {
        out.println("========== HÓA ĐƠN ==========");
        out.println("Đơn #" + bill.orderId() + " - Bàn " + bill.tableNumber() + " - " + TIME.format(bill.issuedAt()));
        for (BillLine line : bill.lines()) {
            out.println(line.itemName() + " x" + line.quantity() + " @ " + MoneyFormat.format(line.unitPrice())
                    + " = " + MoneyFormat.format(line.lineTotal()));
        }
        out.println("Tạm tính: " + MoneyFormat.format(bill.subtotal()));
        out.println(bill.discountLabel() + ": " + MoneyFormat.format(-bill.discount()));
        out.println("TỔNG CỘNG: " + MoneyFormat.format(bill.total()));
        out.println("=============================");
    }

    private void cancelOrder() throws RestaurantException {
        int tableNo = readInt("Số bàn: ", 1, 99);
        orders.currentOrder(tableNo);
        if (readYesNo("Hủy đơn của bàn " + tableNo + "? (c/k): ")) {
            orders.cancel(tableNo);
            out.println("Đã hủy đơn, bàn " + tableNo + " đã trống");
        } else {
            out.println("Không hủy");
        }
    }

    private void manageMenu() throws RestaurantException {
        out.println("1. Thêm món ăn  2. Thêm đồ uống  3. Sửa giá  4. Đánh dấu hết hoặc còn món  0. Quay lại");
        int choice = readInt("Chọn: ", 0, 4);
        switch (choice) {
            case 1 -> {
                String name = readText("Tên món: ");
                long price = readInt("Giá: ", 1, 100_000_000);
                boolean vegetarian = readYesNo("Món chay? (c/k): ");
                MenuItem food = menu.addFood(name, price, vegetarian);
                out.println("Đã thêm " + food.getId() + " " + food.getName());
            }
            case 2 -> {
                String name = readText("Tên đồ uống: ");
                long price = readInt("Giá: ", 1, 100_000_000);
                int volume = readInt("Dung tích (ml): ", 1, 5_000);
                MenuItem drink = menu.addDrink(name, price, volume);
                out.println("Đã thêm " + drink.getId() + " " + drink.getName());
            }
            case 3 -> {
                String itemId = readItemId();
                long price = readInt("Giá mới: ", 1, 100_000_000);
                menu.changePrice(itemId, price);
                out.println("Đã sửa giá " + itemId + " thành " + MoneyFormat.format(price));
            }
            case 4 -> {
                String itemId = readItemId();
                boolean available = !menu.find(itemId).isAvailable();
                menu.setAvailable(itemId, available);
                out.println(itemId + (available ? " đã còn món" : " đã hết món"));
            }
            default -> out.println("Quay lại menu chính");
        }
    }

    private void showBestSellers() {
        List<ItemSales> top = orders.bestSellers(5);
        if (top.isEmpty()) {
            out.println("Chưa có đơn nào được thanh toán");
            return;
        }
        out.println("----- MÓN BÁN CHẠY -----");
        for (int i = 0; i < top.size(); i++) {
            ItemSales sales = top.get(i);
            out.println((i + 1) + ". " + sales.itemName() + " - " + sales.quantity() + " phần - "
                    + MoneyFormat.format(sales.revenue()));
        }
    }

    // Bản M4: ConsoleMenu tự đọc và kiểm tra dữ liệu nhập (sẽ tách ra InputReader ở M5).
    private int readInt(String prompt, int min, int max) {
        while (true) {
            String line = readLine(prompt);
            try {
                int value = Integer.parseInt(line);
                if (value >= min && value <= max) {
                    return value;
                }
                out.println("Vui lòng nhập số từ " + min + " đến " + max);
            } catch (NumberFormatException e) {
                out.println("Vui lòng nhập một số");
            }
        }
    }

    private String readText(String prompt) {
        while (true) {
            String line = readLine(prompt);
            if (!line.isEmpty()) {
                return line;
            }
            out.println("Không được để trống");
        }
    }

    private String readItemId() {
        return readText("Mã món: ").toUpperCase(Locale.ROOT);
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            String answer = readLine(prompt).toLowerCase(Locale.ROOT);
            if (answer.equals("c") || answer.equals("có")) {
                return true;
            }
            if (answer.equals("k") || answer.equals("không")) {
                return false;
            }
            out.println("Vui lòng nhập c hoặc k");
        }
    }

    private String readLine(String prompt) {
        out.print(prompt);
        if (!in.hasNextLine()) {
            throw new EndOfInput();
        }
        return in.nextLine().trim();
    }

    /** Báo hết dữ liệu nhập để thoát vòng lặp gọn gàng. */
    private static final class EndOfInput extends RuntimeException {
        private static final long serialVersionUID = 1L;
    }
}
