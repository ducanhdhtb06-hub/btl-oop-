package restaurant.model;

/**
 * Món đơn có giá cố định do nhà hàng đặt (món ăn, đồ uống). Lớp cha chung của {@link Food} và {@link Drink}.
 */
public abstract class SimpleMenuItem extends MenuItem {

    private long price;

    /**
     * Tạo món đơn.
     *
     * @param id mã món
     * @param name tên món
     * @param price giá bán, phải lớn hơn 0
     * @param available món còn hay đã hết
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ
     */
    protected SimpleMenuItem(String id, String name, long price, boolean available) {
        super(id, name, available);
        this.price = requirePositive(price);
    }

    @Override
    public long getPrice() {
        return price;
    }

    /**
     * Sửa giá bán. Các đơn đã gọi món này vẫn giữ giá cũ.
     *
     * @param newPrice giá mới, phải lớn hơn 0
     * @throws IllegalArgumentException nếu giá không dương
     */
    public void changePrice(long newPrice) {
        this.price = requirePositive(newPrice);
    }

    private static long requirePositive(long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("Giá phải lớn hơn 0");
        }
        return value;
    }
}
