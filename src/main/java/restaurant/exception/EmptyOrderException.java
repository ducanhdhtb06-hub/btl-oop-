package restaurant.exception;

/** Đơn chưa có món nào nên không thanh toán được. */
public class EmptyOrderException extends RestaurantException {

    private static final long serialVersionUID = 1L;

    /**
     * Tạo lỗi.
     *
     * @param orderId mã đơn
     */
    public EmptyOrderException(int orderId) {
        super("Đơn " + orderId + " chưa có món nào. Nếu khách không gọi món, hãy dùng Hủy đơn");
    }
}
