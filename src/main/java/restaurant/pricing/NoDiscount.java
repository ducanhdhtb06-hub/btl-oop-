package restaurant.pricing;

/** Không giảm giá. */
public final class NoDiscount implements DiscountStrategy {

    @Override
    public long discountFor(long subtotal) {
        return 0;
    }

    @Override
    public String label() {
        return "Không giảm giá";
    }
}
