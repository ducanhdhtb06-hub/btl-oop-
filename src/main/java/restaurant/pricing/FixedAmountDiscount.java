package restaurant.pricing;

import restaurant.common.MoneyFormat;

/** Giảm một số tiền cố định, không vượt quá tạm tính. */
public final class FixedAmountDiscount implements DiscountStrategy {

    private final long amount;

    /**
     * Tạo cách giảm số tiền cố định.
     *
     * @param amount số tiền giảm, không âm
     * @throws IllegalArgumentException nếu số tiền âm
     */
    public FixedAmountDiscount(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Số tiền giảm không được âm");
        }
        this.amount = amount;
    }

    @Override
    public long discountFor(long subtotal) {
        return Math.min(amount, subtotal);
    }

    @Override
    public String label() {
        return "Giảm " + MoneyFormat.format(amount);
    }
}
