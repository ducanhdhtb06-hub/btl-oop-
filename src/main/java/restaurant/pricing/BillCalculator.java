package restaurant.pricing;

import java.time.LocalDateTime;
import java.util.List;
import restaurant.model.Bill;
import restaurant.model.BillLine;
import restaurant.model.Order;

/**
 * Tính hóa đơn cho một đơn: tạm tính, tiền giảm và tổng tiền.
 * Cách giảm giá được truyền vào dưới dạng {@link DiscountStrategy}, nên lớp này không phải sửa
 * khi nhà hàng thêm chương trình giảm giá mới.
 */
public class BillCalculator {

    /**
     * Tính hóa đơn. Tiền giảm luôn được kẹp trong khoảng 0 đến tạm tính, nên tổng tiền không bao giờ âm.
     *
     * @param order đơn cần tính
     * @param strategy cách giảm giá
     * @param issuedAt thời điểm xuất hóa đơn
     * @return hóa đơn
     */
    public Bill calculate(Order order, DiscountStrategy strategy, LocalDateTime issuedAt) {
        long subtotal = order.subtotal();
        long discount = Math.max(0, Math.min(subtotal, strategy.discountFor(subtotal)));
        List<BillLine> lines = order.getItems().stream()
                .map(line -> new BillLine(line.getItem().getName(), line.getQuantity(),
                        line.getUnitPrice(), line.lineTotal()))
                .toList();
        return new Bill(order.getId(), order.getTable().getNumber(), issuedAt, lines,
                subtotal, strategy.label(), discount, subtotal - discount);
    }
}
