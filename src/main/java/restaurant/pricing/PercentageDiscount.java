package restaurant.pricing;

/** Giảm theo phần trăm tạm tính, làm tròn đến đồng. */
public final class PercentageDiscount implements DiscountStrategy {

    private final int percent;

    /**
     * Tạo cách giảm theo phần trăm.
     *
     * @param percent phần trăm từ 0 đến 100
     * @throws IllegalArgumentException nếu ngoài khoảng 0 đến 100
     */
    public PercentageDiscount(int percent) {
        if (percent < 0 || percent > 100) {
            throw new IllegalArgumentException("Phần trăm giảm phải từ 0 đến 100");
        }
        this.percent = percent;
    }

    @Override
    public long discountFor(long subtotal) {
        return Math.round(subtotal * percent / 100.0);
    }

    @Override
    public String label() {
        return "Giảm " + percent + "%";
    }
}
