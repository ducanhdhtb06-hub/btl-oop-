package restaurant.pricing;

/**
 * Một cách giảm giá (pattern Strategy). Mỗi cách giảm là một lớp riêng, nên thêm cách giảm mới
 * chỉ cần viết lớp mới, không phải sửa {@link BillCalculator} (nguyên tắc O).
 * Vì chỉ có một phương thức trừu tượng, có thể truyền một lambda làm cách giảm giá.
 */
@FunctionalInterface
public interface DiscountStrategy {

    /**
     * Tính số tiền được giảm.
     *
     * @param subtotal tạm tính của đơn
     * @return số tiền giảm
     */
    long discountFor(long subtotal);

    /**
     * Nhãn in trên hóa đơn.
     *
     * @return nhãn, mặc định "Giảm giá"
     */
    default String label() {
        return "Giảm giá";
    }
}
