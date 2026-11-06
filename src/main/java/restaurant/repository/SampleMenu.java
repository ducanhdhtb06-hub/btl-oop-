package restaurant.repository;

import java.util.List;
import restaurant.model.Drink;
import restaurant.model.Food;
import restaurant.model.MenuItem;

/** Thực đơn mẫu, dùng khi chưa có file menu.csv. */
public final class SampleMenu {

    private SampleMenu() {
    }

    /**
     * Tạo thực đơn mẫu mới (mỗi lần gọi là các đối tượng mới).
     *
     * @return danh sách món mẫu, Trà đào đang hết để demo
     */
    public static List<MenuItem> items() {
        return List.of(
                new Food("F01", "Phở bò", 55_000, true, false),
                new Food("F02", "Cơm gà", 50_000, true, false),
                new Food("F03", "Bún chả", 50_000, true, false),
                new Food("F04", "Gỏi cuốn chay", 35_000, true, true),
                new Food("F05", "Đậu hũ sốt cà chua", 40_000, true, true),
                new Drink("D01", "Trà đá", 5_000, true, 300),
                new Drink("D02", "Cà phê sữa đá", 25_000, true, 250),
                new Drink("D03", "Nước cam", 30_000, true, 330),
                new Drink("D04", "Trà đào", 35_000, false, 500));
    }
}
