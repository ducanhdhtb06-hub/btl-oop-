package restaurant.ui;

import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Locale;
import restaurant.common.BillFormatter;
import restaurant.common.MoneyFormat;
import restaurant.exception.RestaurantException;
import restaurant.model.ItemType;
import restaurant.model.MenuItem;
import restaurant.model.Order;
import restaurant.model.OrderItem;
import restaurant.model.Table;
import restaurant.pricing.DiscountStrategy;
import restaurant.pricing.FixedAmountDiscount;
import restaurant.pricing.NoDiscount;
import restaurant.pricing.PercentageDiscount;
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

    private final InputReader input;
    private final PrintStream out;
    private final MenuService menu;
    private final TableService tables;
    private final OrderService orders;

    /**
     * Tạo giao diện console.
     *
     * @param input bộ đọc dữ liệu nhập
     * @param out nơi in kết quả
     * @param menu nghiệp vụ thực đơn
     * @param tables nghiệp vụ bàn
     * @param orders nghiệp vụ đơn
     */
    public ConsoleMenu(InputReader input, PrintStream out, MenuService menu, TableService tables, OrderService orders) {
        this.input = input;
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
                int choice = input.readInt("Chọn chức năng: ", 0, 10);
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
        } catch (InputClosedException e) {
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
        out.println("1. Tất cả  2. Món ăn  3. Đồ uống  4. Món chay  5. Combo");
        int filter = input.readInt("Chọn: ", 1, 5);
        List<MenuItem> items = switch (filter) {
            case 2 -> menu.byType(ItemType.FOOD);
            case 3 -> menu.byType(ItemType.DRINK);
            case 4 -> menu.vegetarian();
            case 5 -> menu.byType(ItemType.COMBO);
            default -> menu.all();
        };
        out.println("----- THỰC ĐƠN -----");
        for (MenuItem item : items) {
            out.printf("%-4s %11s  %s%s%n", item.getId(), MoneyFormat.format(item.getPrice()),
                    item.describe(), item.isAvailable() ? "" : "  (hết)");
        }
    }

    private void showTables() {
        for (Table table : tables.all()) {
            out.printf("Bàn %2d (%d ghế): %s%n", table.getNumber(), table.getSeats(),
                    table.isFree() ? "Trống" : "Có khách");
        }
    }

    private void openTable() throws RestaurantException {
        int tableNo = input.readInt("Số bàn: ", 1, 99);
        Order order = orders.openOrder(tableNo);
        out.println("Đã mở bàn " + tableNo + " (đơn #" + order.getId() + ")");
    }

    private void addItem() throws RestaurantException {
        int tableNo = input.readInt("Số bàn: ", 1, 99);
        String itemId = readItemId();
        int quantity = input.readInt("Số lượng (1-99): ", 1, 99);
        orders.addItem(tableNo, itemId, quantity);
        out.println("Đã thêm " + quantity + " x " + menu.find(itemId).getName() + " vào bàn " + tableNo
                + ". Tạm tính: " + MoneyFormat.format(orders.currentOrder(tableNo).subtotal()));
    }

    private void changeItem() throws RestaurantException {
        int tableNo = input.readInt("Số bàn: ", 1, 99);
        String itemId = readItemId();
        int quantity = input.readInt("Số lượng mới (0 = bớt món): ", 0, 99);
        if (quantity == 0) {
            orders.removeItem(tableNo, itemId);
            out.println("Đã bớt món " + itemId);
        } else {
            orders.updateQuantity(tableNo, itemId, quantity);
            out.println("Đã sửa " + itemId + " thành " + quantity + " phần");
        }
    }

    private void showOrder() throws RestaurantException {
        int tableNo = input.readInt("Số bàn: ", 1, 99);
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
        int tableNo = input.readInt("Số bàn: ", 1, 99);
        out.println("1. Không giảm  2. Giảm theo %  3. Giảm số tiền");
        int kind = input.readInt("Chọn cách giảm giá: ", 1, 3);
        DiscountStrategy strategy = switch (kind) {
            case 2 -> new PercentageDiscount(input.readInt("Phần trăm giảm (0-100): ", 0, 100));
            case 3 -> new FixedAmountDiscount(input.readInt("Số tiền giảm: ", 0, 100_000_000));
            default -> new NoDiscount();
        };
        CheckoutResult result = orders.checkout(tableNo, strategy);
        out.print(BillFormatter.format(result.bill()));
        if (!result.archived()) {
            out.println("Cảnh báo: chưa lưu được hóa đơn vào file, thanh toán vẫn được ghi nhận");
        }
    }

    private void cancelOrder() throws RestaurantException {
        int tableNo = input.readInt("Số bàn: ", 1, 99);
        orders.currentOrder(tableNo);
        if (input.confirm("Hủy đơn của bàn " + tableNo + "? (c/k): ")) {
            orders.cancel(tableNo);
            out.println("Đã hủy đơn, bàn " + tableNo + " đã trống");
        } else {
            out.println("Không hủy");
        }
    }

    private void manageMenu() throws RestaurantException {
        out.println("1. Thêm món ăn  2. Thêm đồ uống  3. Sửa giá  4. Đánh dấu hết hoặc còn món  0. Quay lại");
        int choice = input.readInt("Chọn: ", 0, 4);
        switch (choice) {
            case 1 -> {
                String name = input.readNonBlank("Tên món: ");
                long price = input.readInt("Giá: ", 1, 100_000_000);
                boolean vegetarian = input.confirm("Món chay? (c/k): ");
                MenuItem food = menu.addFood(name, price, vegetarian);
                out.println("Đã thêm " + food.getId() + " " + food.getName());
            }
            case 2 -> {
                String name = input.readNonBlank("Tên đồ uống: ");
                long price = input.readInt("Giá: ", 1, 100_000_000);
                int volume = input.readInt("Dung tích (ml): ", 1, 5_000);
                MenuItem drink = menu.addDrink(name, price, volume);
                out.println("Đã thêm " + drink.getId() + " " + drink.getName());
            }
            case 3 -> {
                String itemId = readItemId();
                long price = input.readInt("Giá mới: ", 1, 100_000_000);
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

    private String readItemId() {
        return input.readNonBlank("Mã món: ").toUpperCase(Locale.ROOT);
    }
}
