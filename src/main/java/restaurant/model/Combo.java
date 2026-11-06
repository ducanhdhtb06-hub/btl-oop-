package restaurant.model;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Combo gồm nhiều món, bán với giá giảm theo phần trăm (pattern Composite).
 * Combo cũng là một {@link MenuItem}, nên {@link Order} gọi combo y như gọi một món đơn.
 * Các món thành phần được truyền vào từ bên ngoài và vẫn tồn tại độc lập trên thực đơn (aggregation).
 * Một combo có thể chứa combo khác; giá và tình trạng còn món được tính đệ quy.
 */
public final class Combo extends MenuItem {

    private final int discountPercent;
    private final List<MenuItem> components;

    /**
     * Tạo combo.
     *
     * @param id mã combo, ví dụ {@code C01}
     * @param name tên combo
     * @param available cờ còn bán của riêng combo
     * @param discountPercent phần trăm giảm so với tổng giá món thành phần, từ 0 đến 100
     * @param components các món thành phần, ít nhất 1 món
     * @throws IllegalArgumentException nếu không có món thành phần hoặc phần trăm ngoài khoảng 0 đến 100
     */
    public Combo(String id, String name, boolean available, int discountPercent, List<MenuItem> components) {
        super(id, name, available);
        if (components == null || components.isEmpty()) {
            throw new IllegalArgumentException("Combo phải có ít nhất 1 món");
        }
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Phần trăm giảm của combo phải từ 0 đến 100");
        }
        this.discountPercent = discountPercent;
        this.components = List.copyOf(components);
    }

    /**
     * Trả về các món thành phần.
     *
     * @return danh sách chỉ đọc
     */
    public List<MenuItem> getComponents() {
        return components;
    }

    /**
     * Trả về phần trăm giảm của combo.
     *
     * @return phần trăm giảm
     */
    public int getDiscountPercent() {
        return discountPercent;
    }

    /** Tổng giá các món thành phần, trừ phần trăm giảm làm tròn đến đồng. */
    @Override
    public long getPrice() {
        long sum = components.stream().mapToLong(MenuItem::getPrice).sum();
        return sum - Math.round(sum * discountPercent / 100.0);
    }

    /** Combo chỉ bán được khi bản thân combo và mọi món thành phần đều còn. */
    @Override
    public boolean isAvailable() {
        return super.isAvailable() && components.stream().allMatch(MenuItem::isAvailable);
    }

    /** Combo là món chay khi mọi món thành phần đều chay. */
    @Override
    public boolean isVegetarian() {
        return components.stream().allMatch(MenuItem::isVegetarian);
    }

    @Override
    public ItemType getType() {
        return ItemType.COMBO;
    }

    @Override
    public String describe() {
        String names = components.stream().map(MenuItem::getName).collect(Collectors.joining(" + "));
        return "Combo - " + getName() + ": " + names + " (giảm " + discountPercent + "%)";
    }

    @Override
    public String toCsvRow() {
        String ids = components.stream().map(MenuItem::getId).collect(Collectors.joining("|"));
        return String.join(";", "COMBO", getId(), getName(), Integer.toString(discountPercent),
                Boolean.toString(isMarkedAvailable()), ids);
    }
}
