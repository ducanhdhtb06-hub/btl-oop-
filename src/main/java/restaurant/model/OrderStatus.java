package restaurant.model;

/** Trạng thái của một đơn gọi món. */
public enum OrderStatus {
    /** Đơn đang mở, còn gọi thêm hoặc sửa món được. */
    OPEN,
    /** Đơn đã thanh toán. */
    PAID,
    /** Đơn đã bị hủy. */
    CANCELLED
}
