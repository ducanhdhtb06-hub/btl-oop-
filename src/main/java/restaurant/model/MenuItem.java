package restaurant.model;

/**
 * Một món trên thực đơn. Lớp trừu tượng vì giữ dữ liệu chung (mã, tên, còn hay hết),
 * còn cách tính giá, cách mô tả và cách ghi ra file do từng lớp con tự quyết định.
 */
public abstract class MenuItem implements Identifiable<String> {

    private final String id;
    private final String name;
    private boolean available;

    /**
     * Tạo món.
     *
     * @param id mã món, không rỗng
     * @param name tên món, không rỗng, không chứa {@code ;} hay ký tự xuống dòng
     * @param available món còn hay đã hết
     * @throws IllegalArgumentException nếu mã hoặc tên không hợp lệ
     */
    protected MenuItem(String id, String name, boolean available) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Mã món không được rỗng");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên món không được rỗng");
        }
        if (name.contains(";") || name.contains("\n") || name.contains("\r")) {
            throw new IllegalArgumentException("Tên món không được chứa dấu ; hoặc ký tự xuống dòng");
        }
        this.id = id.trim();
        this.name = name.trim();
        this.available = available;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Trả về tên món.
     *
     * @return tên món
     */
    public String getName() {
        return name;
    }

    /**
     * Cho biết món có gọi được không.
     *
     * @return {@code true} nếu món còn
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * Đánh dấu món còn hoặc hết.
     *
     * @param available {@code true} nếu món còn
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    /**
     * Cờ còn món do chính món này giữ, không tính tới món khác (với combo là cờ của riêng combo).
     * Dùng khi bật tắt món và khi ghi ra file.
     *
     * @return cờ còn món gốc
     */
    public final boolean isMarkedAvailable() {
        return available;
    }

    /**
     * Cho biết món có phải món chay không. Mặc định là không; lớp con ghi đè khi cần.
     *
     * @return {@code true} nếu là món chay
     */
    public boolean isVegetarian() {
        return false;
    }

    /**
     * Trả về giá bán hiện tại.
     *
     * @return giá tính bằng đồng
     */
    public abstract long getPrice();

    /**
     * Trả về loại món.
     *
     * @return loại món
     */
    public abstract ItemType getType();

    /**
     * Mô tả món để hiện trên thực đơn (không gồm giá).
     *
     * @return dòng mô tả
     */
    public abstract String describe();

    /**
     * Biến món thành một dòng của file menu.csv.
     *
     * @return dòng CSV phân cách bằng dấu {@code ;}
     */
    public abstract String toCsvRow();

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MenuItem)) {
            return false;
        }
        return id.equals(((MenuItem) other).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return describe();
    }
}
