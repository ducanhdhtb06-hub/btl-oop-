package restaurant.pricing;

import java.time.LocalDateTime;
import java.util.List;
import restaurant.common.MoneyFormat;
import restaurant.model.Bill;
import restaurant.model.BillLine;
import restaurant.model.Order;

/** Tính hóa đơn cho một đơn: tạm tính, tiền giảm và tổng tiền. */
public class BillCalculator {

    /**
     * Tính hóa đơn.
     *
     * @param order đơn cần tính
     * @param type cách giảm giá
     * @param value với {@code PERCENT} là phần trăm 0 đến 100, với {@code FIXED} là số tiền không âm,
     *              với {@code NONE} bị bỏ qua
     * @param issuedAt thời điểm xuất hóa đơn
     * @return hóa đơn
     * @throws IllegalArgumentException nếu {@code value} không hợp lệ với cách giảm giá
     */
    public Bill calculate(Order order, DiscountType type, long value, LocalDateTime issuedAt) {
        long subtotal = order.subtotal();
        long discount;
        String label;
        if (type == DiscountType.PERCENT) {
            if (value < 0 || value > 100) {
                throw new IllegalArgumentException("Phần trăm giảm phải từ 0 đến 100");
            }
            discount = Math.round(subtotal * value / 100.0);
            label = "Giảm " + value + "%";
        } else if (type == DiscountType.FIXED) {
            if (value < 0) {
                throw new IllegalArgumentException("Số tiền giảm không được âm");
            }
            discount = Math.min(value, subtotal);
            label = "Giảm " + MoneyFormat.format(value);
        } else {
            discount = 0;
            label = "Không giảm giá";
        }
        List<BillLine> lines = order.getItems().stream()
                .map(line -> new BillLine(line.getItem().getName(), line.getQuantity(),
                        line.getUnitPrice(), line.lineTotal()))
                .toList();
        return new Bill(order.getId(), order.getTable().getNumber(), issuedAt, lines,
                subtotal, label, discount, subtotal - discount);
    }
}
