package restaurant.model;

/** Món ăn, có thể là món chay. */
public final class Food extends SimpleMenuItem {

    private final boolean vegetarian;

    /**
     * Tạo món ăn.
     *
     * @param id mã món, ví dụ {@code F01}
     * @param name tên món
     * @param price giá bán
     * @param available món còn hay đã hết
     * @param vegetarian có phải món chay không
     */
    public Food(String id, String name, long price, boolean available, boolean vegetarian) {
        super(id, name, price, available);
        this.vegetarian = vegetarian;
    }

    @Override
    public boolean isVegetarian() {
        return vegetarian;
    }

    @Override
    public ItemType getType() {
        return ItemType.FOOD;
    }

    @Override
    public String describe() {
        return "Món ăn - " + getName() + (vegetarian ? " [chay]" : "");
    }

    @Override
    public String toCsvRow() {
        return String.join(";", "FOOD", getId(), getName(), Long.toString(getPrice()),
                Boolean.toString(isMarkedAvailable()), Boolean.toString(vegetarian));
    }
}
