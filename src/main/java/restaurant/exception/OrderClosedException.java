package restaurant.exception;

import restaurant.model.OrderStatus;

/** Đơn đã thanh toán hoặc đã hủy nên không sửa được nữa. */
public class OrderClosedException extends RestaurantException {

    private static final long serialVersionUID = 1L;

    /**
     * Tạo lỗi.
     *
     * @param orderId mã đơn
     * @param status trạng thái hiện tại của đơn (PAID hoặc CANCELLED)
     */
    public OrderClosedException(int orderId, OrderStatus status) {
        super("Đơn " + orderId + " đã " + (status == OrderStatus.PAID ? "thanh toán" : "hủy") + ", không sửa được");
    }
}
