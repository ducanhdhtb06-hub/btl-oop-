package restaurant.service;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import restaurant.exception.EmptyOrderException;
import restaurant.exception.NoOpenOrderException;
import restaurant.exception.RestaurantException;
import restaurant.exception.TableNotAvailableException;
import restaurant.exception.TableNotFoundException;
import restaurant.model.Bill;
import restaurant.model.Order;
import restaurant.model.OrderItem;
import restaurant.model.OrderStatus;
import restaurant.model.Table;
import restaurant.pricing.BillCalculator;
import restaurant.pricing.DiscountStrategy;
import restaurant.repository.BillArchive;
import restaurant.repository.Repository;

/** Nghiệp vụ đơn: mở bàn, gọi món, sửa món, thanh toán, hủy đơn, thống kê. */
public class OrderService {

    private final Repository<Order, Integer> orders;
    private final TableService tables;
    private final MenuService menu;
    private final BillCalculator calculator;
    private final BillArchive archive;
    private final Clock clock;
    private int nextOrderId = 1;

    /**
     * Tạo service.
     *
     * @param orders nơi lưu đơn
     * @param tables nghiệp vụ bàn
     * @param menu nghiệp vụ thực đơn
     * @param calculator bộ tính hóa đơn
     * @param archive nơi lưu hóa đơn đã thanh toán
     * @param clock đồng hồ, truyền vào để test có giờ cố định
     */
    public OrderService(Repository<Order, Integer> orders, TableService tables, MenuService menu,
                        BillCalculator calculator, BillArchive archive, Clock clock) {
        this.orders = orders;
        this.tables = tables;
        this.menu = menu;
        this.calculator = calculator;
        this.archive = archive;
        this.clock = clock;
    }

    /**
     * Mở bàn: tạo đơn mới cho bàn trống.
     *
     * @param tableNo số bàn
     * @return đơn vừa tạo
     * @throws TableNotFoundException nếu không có bàn này
     * @throws TableNotAvailableException nếu bàn đang có khách
     */
    public Order openOrder(int tableNo) throws TableNotFoundException, TableNotAvailableException {
        Table table = tables.find(tableNo);
        if (!table.isFree()) {
            throw new TableNotAvailableException(tableNo);
        }
        Order order = new Order(nextOrderId++, table, LocalDateTime.now(clock));
        table.occupy();
        orders.save(order);
        return order;
    }

    /**
     * Lấy đơn đang mở của bàn.
     *
     * @param tableNo số bàn
     * @return đơn đang mở
     * @throws TableNotFoundException nếu không có bàn này
     * @throws NoOpenOrderException nếu bàn chưa có đơn đang mở
     */
    public Order currentOrder(int tableNo) throws TableNotFoundException, NoOpenOrderException {
        tables.find(tableNo);
        return orders.findAll().stream()
                .filter(order -> order.getTable().getNumber() == tableNo)
                .filter(order -> order.getStatus() == OrderStatus.OPEN)
                .findFirst()
                .orElseThrow(() -> new NoOpenOrderException(tableNo));
    }

    /**
     * Gọi món cho bàn.
     *
     * @param tableNo số bàn
     * @param itemId mã món
     * @param quantity số lượng
     * @throws RestaurantException nếu bàn, món hoặc số lượng không hợp lệ
     */
    public void addItem(int tableNo, String itemId, int quantity) throws RestaurantException {
        currentOrder(tableNo).addItem(menu.find(itemId), quantity);
    }

    /**
     * Sửa số lượng một món trong đơn của bàn.
     *
     * @param tableNo số bàn
     * @param itemId mã món
     * @param quantity số lượng mới
     * @throws RestaurantException nếu bàn, món hoặc số lượng không hợp lệ
     */
    public void updateQuantity(int tableNo, String itemId, int quantity) throws RestaurantException {
        currentOrder(tableNo).updateQuantity(itemId, quantity);
    }

    /**
     * Bớt một món khỏi đơn của bàn.
     *
     * @param tableNo số bàn
     * @param itemId mã món
     * @throws RestaurantException nếu bàn không có đơn hoặc đơn không có món này
     */
    public void removeItem(int tableNo, String itemId) throws RestaurantException {
        currentOrder(tableNo).removeItem(itemId);
    }

    /**
     * Thanh toán theo thứ tự: tính hóa đơn, đánh dấu đơn đã thanh toán, trả bàn, rồi lưu hóa đơn.
     * Lưu hóa đơn lỗi thì vẫn ghi nhận thanh toán.
     *
     * @param tableNo số bàn
     * @param strategy cách giảm giá
     * @return hóa đơn và cho biết đã lưu được chưa
     * @throws RestaurantException nếu bàn không có đơn hoặc đơn rỗng
     */
    public CheckoutResult checkout(int tableNo, DiscountStrategy strategy) throws RestaurantException {
        Order order = currentOrder(tableNo);
        if (order.isEmpty()) {
            throw new EmptyOrderException(order.getId());
        }
        Bill bill = calculator.calculate(order, strategy, LocalDateTime.now(clock));
        order.markPaid();
        order.getTable().release();
        try {
            archive.archive(bill);
            return new CheckoutResult(bill, true);
        } catch (IOException e) {
            return new CheckoutResult(bill, false);
        }
    }

    /**
     * Hủy đơn đang mở của bàn và trả bàn.
     *
     * @param tableNo số bàn
     * @throws RestaurantException nếu bàn không có đơn đang mở
     */
    public void cancel(int tableNo) throws RestaurantException {
        Order order = currentOrder(tableNo);
        order.cancel();
        order.getTable().release();
    }

    /**
     * Thống kê món bán chạy từ các đơn đã thanh toán.
     *
     * @param limit số món tối đa
     * @return các món sắp theo số lượng giảm dần, bằng nhau thì theo tên
     */
    public List<ItemSales> bestSellers(int limit) {
        Map<String, List<OrderItem>> linesByItem = orders.findAll().stream()
                .filter(order -> order.getStatus() == OrderStatus.PAID)
                .flatMap(order -> order.getItems().stream())
                .collect(Collectors.groupingBy(line -> line.getItem().getId()));
        return linesByItem.values().stream()
                .map(lines -> new ItemSales(
                        lines.get(0).getItem().getName(),
                        lines.stream().mapToInt(OrderItem::getQuantity).sum(),
                        lines.stream().mapToLong(OrderItem::lineTotal).sum()))
                .sorted(Comparator.comparingInt(ItemSales::quantity).reversed()
                        .thenComparing(ItemSales::itemName))
                .limit(limit)
                .toList();
    }
}
