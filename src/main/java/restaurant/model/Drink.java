package restaurant.model;

/** Đồ uống, có dung tích tính bằng ml. */
public final class Drink extends SimpleMenuItem {

    private final int volumeMl;

    /**
     * Tạo đồ uống.
     *
     * @param id mã món, ví dụ {@code D01}
     * @param name tên đồ uống
     * @param price giá bán
     * @param available còn hay đã hết
     * @param volumeMl dung tích, phải lớn hơn 0
     * @throws IllegalArgumentException nếu dung tích không dương
     */
    public Drink(String id, String name, long price, boolean available, int volumeMl) {
        super(id, name, price, available);
        if (volumeMl <= 0) {
            throw new IllegalArgumentException("Dung tích phải lớn hơn 0");
        }
        this.volumeMl = volumeMl;
    }

    /**
     * Trả về dung tích.
     *
     * @return dung tích tính bằng ml
     */
    public int getVolumeMl() {
        return volumeMl;
    }

    /** Đồ uống không có thịt nên luôn tính là chay. */
    @Override
    public boolean isVegetarian() {
        return true;
    }

    @Override
    public ItemType getType() {
        return ItemType.DRINK;
    }

    @Override
    public String describe() {
        return "Đồ uống - " + getName() + " (" + volumeMl + " ml)";
    }

    @Override
    public String toCsvRow() {
        return String.join(";", "DRINK", getId(), getName(), Long.toString(getPrice()),
                Boolean.toString(isMarkedAvailable()), Integer.toString(volumeMl));
    }
}
