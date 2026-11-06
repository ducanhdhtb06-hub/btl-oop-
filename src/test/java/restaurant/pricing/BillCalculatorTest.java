package restaurant.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restaurant.exception.RestaurantException;
import restaurant.model.Bill;
import restaurant.model.BillLine;
import restaurant.model.Drink;
import restaurant.model.Food;
import restaurant.model.Order;
import restaurant.model.Table;

class BillCalculatorTest {

    private final LocalDateTime at = LocalDateTime.of(2026, 11, 20, 12, 30);
    private final BillCalculator calc = new BillCalculator();
    private Order order;
    private Order oddOrder;
    private Order smallOrder;

    @BeforeEach
    void setUp() throws RestaurantException {
        order = new Order(1, new Table(3, 4), at);
        order.addItem(new Food("F01", "Phở bò", 55_000, true, false), 2);
        order.addItem(new Drink("D01", "Trà đá", 5_000, true, 300));
        oddOrder = new Order(2, new Table(4, 4), at);
        oddOrder.addItem(new Drink("D09", "Nước test", 12_345, true, 330));
        smallOrder = new Order(3, new Table(5, 4), at);
        smallOrder.addItem(new Drink("D03", "Nước cam", 30_000, true, 330));
    }

    @Test
    @DisplayName("Giảm 10%: tạm tính 115.000, giảm 11.500, tổng 103.500")
    void percentDiscount() {
        Bill b = calc.calculate(order, new PercentageDiscount(10), at);
        assertEquals(115_000, b.subtotal());
        assertEquals(11_500, b.discount());
        assertEquals(103_500, b.total());
        assertEquals("Giảm 10%", b.discountLabel());
        assertEquals(1, b.orderId());
        assertEquals(3, b.tableNumber());
        assertEquals(at, b.issuedAt());
        assertEquals(2, b.lines().size());
        assertEquals(new BillLine("Phở bò", 2, 55_000, 110_000), b.lines().get(0));
    }

    @Test
    @DisplayName("Không giảm giá")
    void noDiscount() {
        Bill b = calc.calculate(order, new NoDiscount(), at);
        assertEquals(0, b.discount());
        assertEquals(115_000, b.total());
        assertEquals("Không giảm giá", b.discountLabel());
    }

    @Test
    @DisplayName("Tiền giảm theo % được làm tròn đến đồng")
    void percentRounding() {
        assertEquals(1_235, calc.calculate(oddOrder, new PercentageDiscount(10), at).discount());
    }

    @Test
    @DisplayName("Giảm cố định lớn hơn tạm tính thì tổng bằng 0, không âm")
    void fixedDiscountNeverNegative() {
        Bill b = calc.calculate(smallOrder, new FixedAmountDiscount(50_000), at);
        assertEquals(30_000, b.discount());
        assertEquals(0, b.total());
        assertEquals("Giảm 50.000 đ", b.discountLabel());
    }

    @Test
    @DisplayName("Tiền giảm luôn bị kẹp trong khoảng 0 đến tạm tính, kể cả khi lambda trả giá trị sai")
    void discountIsClamped() {
        Bill tooMuch = calc.calculate(order, subtotal -> 200_000, at);
        assertEquals(115_000, tooMuch.discount());
        assertEquals(0, tooMuch.total());
        assertEquals("Giảm giá", tooMuch.discountLabel());
        Bill negative = calc.calculate(order, subtotal -> -5_000, at);
        assertEquals(0, negative.discount());
        assertEquals(115_000, negative.total());
    }
}
