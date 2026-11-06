package restaurant.model;

/**
 * Một dòng món trong đơn. Chỉ {@link Order} tạo và sửa được đối tượng này (composition):
 * constructor và hàm đổi số lượng chỉ dùng được trong gói {@code model}.
 */
public final class OrderItem {

    private final MenuItem item;
    private final long unitPrice;
    private int quantity;

    /**
     * Tạo dòng món và chốt đơn giá theo giá hiện tại của món.
     *
     * @param item món được gọi
     * @param quantity số lượng (đã được {@link Order} kiểm tra)
     */
    OrderItem(MenuItem item, int quantity) {
        this.item = item;
        this.unitPrice = item.getPrice();
        this.quantity = quantity;
    }

    /**
     * Trả về món được gọi.
     *
     * @return món
     */
    public MenuItem getItem() {
        return item;
    }

    /**
     * Trả về đơn giá đã chốt lúc gọi món.
     *
     * @return đơn giá tính bằng đồng
     */
    public long getUnitPrice() {
        return unitPrice;
    }

    /**
     * Trả về số lượng.
     *
     * @return số lượng
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Tính thành tiền của dòng.
     *
     * @return đơn giá nhân số lượng
     */
    public long lineTotal() {
        return unitPrice * quantity;
    }

    void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
