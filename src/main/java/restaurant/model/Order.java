package restaurant.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import restaurant.exception.InvalidQuantityException;
import restaurant.exception.ItemNotInOrderException;
import restaurant.exception.ItemUnavailableException;
import restaurant.exception.OrderClosedException;

/**
 * Đơn gọi món của một bàn. Đơn tự tạo và tự quản lý các {@link OrderItem} của mình (composition):
 * bên ngoài chỉ thêm, sửa, bớt món qua các phương thức của đơn và chỉ đọc được danh sách món.
 */
public final class Order implements Identifiable<Integer> {

    /** Số lượng tối đa của một dòng món. */
    public static final int MAX_QUANTITY = 99;

    private final int id;
    private final Table table;
    private final LocalDateTime createdAt;
    private final List<OrderItem> items = new ArrayList<>();
    private OrderStatus status = OrderStatus.OPEN;

    /**
     * Tạo đơn đang mở.
     *
     * @param id mã đơn
     * @param table bàn của đơn
     * @param createdAt thời điểm mở đơn
     */
    public Order(int id, Table table, LocalDateTime createdAt) {
        if (table == null || createdAt == null) {
            throw new IllegalArgumentException("Đơn phải có bàn và thời điểm tạo");
        }
        this.id = id;
        this.table = table;
        this.createdAt = createdAt;
    }

    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Trả về bàn của đơn.
     *
     * @return bàn
     */
    public Table getTable() {
        return table;
    }

    /**
     * Trả về thời điểm mở đơn.
     *
     * @return thời điểm mở đơn
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Trả về trạng thái của đơn.
     *
     * @return trạng thái
     */
    public OrderStatus getStatus() {
        return status;
    }

    /**
     * Gọi một phần món.
     *
     * @param item món được gọi
     * @throws OrderClosedException nếu đơn đã thanh toán hoặc đã hủy
     * @throws ItemUnavailableException nếu món đã hết
     * @throws InvalidQuantityException nếu cộng dồn vượt 99
     */
    public void addItem(MenuItem item)
            throws OrderClosedException, ItemUnavailableException, InvalidQuantityException {
        addItem(item, 1);
    }

    /**
     * Gọi món với số lượng cho trước. Món đã có trong đơn thì cộng dồn và giữ đơn giá của dòng cũ.
     *
     * @param item món được gọi
     * @param quantity số lượng, từ 1 đến 99
     * @throws OrderClosedException nếu đơn đã thanh toán hoặc đã hủy
     * @throws ItemUnavailableException nếu món đã hết
     * @throws InvalidQuantityException nếu số lượng, hoặc tổng sau khi cộng dồn, ngoài khoảng 1 đến 99
     */
    public void addItem(MenuItem item, int quantity)
            throws OrderClosedException, ItemUnavailableException, InvalidQuantityException {
        requireOpen();
        requireValidQuantity(quantity);
        if (!item.isAvailable()) {
            throw new ItemUnavailableException(item.getName());
        }
        Optional<OrderItem> existing = findLine(item.getId());
        if (existing.isPresent()) {
            int newQuantity = existing.get().getQuantity() + quantity;
            requireValidQuantity(newQuantity);
            existing.get().setQuantity(newQuantity);
        } else {
            items.add(new OrderItem(item, quantity));
        }
    }

    /**
     * Đổi số lượng của một dòng món.
     *
     * @param itemId mã món
     * @param quantity số lượng mới, từ 1 đến 99
     * @throws OrderClosedException nếu đơn đã thanh toán hoặc đã hủy
     * @throws ItemNotInOrderException nếu đơn không có món này
     * @throws InvalidQuantityException nếu số lượng ngoài khoảng 1 đến 99
     */
    public void updateQuantity(String itemId, int quantity)
            throws OrderClosedException, ItemNotInOrderException, InvalidQuantityException {
        requireOpen();
        requireValidQuantity(quantity);
        findLine(itemId).orElseThrow(() -> new ItemNotInOrderException(itemId)).setQuantity(quantity);
    }

    /**
     * Bớt hẳn một món khỏi đơn.
     *
     * @param itemId mã món
     * @throws OrderClosedException nếu đơn đã thanh toán hoặc đã hủy
     * @throws ItemNotInOrderException nếu đơn không có món này
     */
    public void removeItem(String itemId) throws OrderClosedException, ItemNotInOrderException {
        requireOpen();
        OrderItem line = findLine(itemId).orElseThrow(() -> new ItemNotInOrderException(itemId));
        items.remove(line);
    }

    /**
     * Trả về danh sách dòng món chỉ đọc.
     *
     * @return danh sách không sửa được
     */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /**
     * Tính tạm tính.
     *
     * @return tổng thành tiền các dòng
     */
    public long subtotal() {
        return items.stream().mapToLong(OrderItem::lineTotal).sum();
    }

    /**
     * Cho biết đơn chưa có món nào.
     *
     * @return {@code true} nếu đơn rỗng
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Đánh dấu đơn đã thanh toán.
     *
     * @throws OrderClosedException nếu đơn không còn mở
     */
    public void markPaid() throws OrderClosedException {
        requireOpen();
        status = OrderStatus.PAID;
    }

    /**
     * Hủy đơn.
     *
     * @throws OrderClosedException nếu đơn không còn mở
     */
    public void cancel() throws OrderClosedException {
        requireOpen();
        status = OrderStatus.CANCELLED;
    }

    private Optional<OrderItem> findLine(String itemId) {
        return items.stream().filter(line -> line.getItem().getId().equals(itemId)).findFirst();
    }

    private void requireOpen() throws OrderClosedException {
        if (status != OrderStatus.OPEN) {
            throw new OrderClosedException(id, status);
        }
    }

    private static void requireValidQuantity(int quantity) throws InvalidQuantityException {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new InvalidQuantityException(quantity);
        }
    }
}
