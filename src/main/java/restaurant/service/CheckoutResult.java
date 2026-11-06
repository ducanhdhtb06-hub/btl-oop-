package restaurant.service;

import restaurant.model.Bill;

/**
 * Kết quả thanh toán.
 *
 * @param bill hóa đơn
 * @param archived {@code true} nếu hóa đơn đã được lưu; {@code false} nếu lưu lỗi
 *                 (thanh toán vẫn được ghi nhận)
 */
public record CheckoutResult(Bill bill, boolean archived) {
}
