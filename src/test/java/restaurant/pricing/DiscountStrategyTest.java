package restaurant.pricing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DiscountStrategyTest {

    @Test
    @DisplayName("Không giảm giá")
    void noDiscount() {
        assertEquals(0, new NoDiscount().discountFor(115_000));
        assertEquals("Không giảm giá", new NoDiscount().label());
    }

    @Test
    @DisplayName("Giảm theo %, làm tròn đến đồng")
    void percentage() {
        assertEquals(11_500, new PercentageDiscount(10).discountFor(115_000));
        assertEquals(1_235, new PercentageDiscount(10).discountFor(12_345));
        assertEquals(5_000, new PercentageDiscount(15).discountFor(33_333));
        assertEquals(0, new PercentageDiscount(0).discountFor(115_000));
        assertEquals(115_000, new PercentageDiscount(100).discountFor(115_000));
        assertEquals("Giảm 10%", new PercentageDiscount(10).label());
    }

    @Test
    @DisplayName("Giảm số tiền cố định, không vượt tạm tính")
    void fixedAmount() {
        assertEquals(30_000, new FixedAmountDiscount(50_000).discountFor(30_000));
        assertEquals(20_000, new FixedAmountDiscount(20_000).discountFor(30_000));
        assertEquals("Giảm 50.000 đ", new FixedAmountDiscount(50_000).label());
    }

    @Test
    @DisplayName("Từ chối % ngoài khoảng 0 đến 100 và tiền giảm âm")
    void rejectsInvalid() {
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(101));
        assertThrows(IllegalArgumentException.class, () -> new PercentageDiscount(-1));
        assertThrows(IllegalArgumentException.class, () -> new FixedAmountDiscount(-1));
    }

    @Test
    @DisplayName("Lambda cũng là một cách giảm giá, nhãn mặc định là 'Giảm giá'")
    void lambdaIsAStrategy() {
        DiscountStrategy flat = subtotal -> Math.min(subtotal, 50_000);
        assertEquals(50_000, flat.discountFor(115_000));
        assertEquals("Giảm giá", flat.label());
    }
}
