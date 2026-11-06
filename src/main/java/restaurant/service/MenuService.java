package restaurant.service;

import java.util.Comparator;
import java.util.List;
import restaurant.exception.MenuItemNotFoundException;
import restaurant.model.Drink;
import restaurant.model.Food;
import restaurant.model.ItemType;
import restaurant.model.MenuItem;
import restaurant.model.SimpleMenuItem;
import restaurant.repository.Repository;

/** Nghiệp vụ thực đơn: xem, lọc, tìm, thêm món, sửa giá, bật tắt còn món. */
public class MenuService {

    private final Repository<MenuItem, String> repo;

    /**
     * Tạo service.
     *
     * @param repo nơi lưu thực đơn
     */
    public MenuService(Repository<MenuItem, String> repo) {
        this.repo = repo;
    }

    /**
     * Trả về cả thực đơn, sắp theo mã.
     *
     * @return danh sách món
     */
    public List<MenuItem> all() {
        return repo.findAll().stream().sorted(Comparator.comparing(MenuItem::getId)).toList();
    }

    /**
     * Lọc theo loại món.
     *
     * @param type loại món
     * @return các món thuộc loại đó, sắp theo mã
     */
    public List<MenuItem> byType(ItemType type) {
        return all().stream().filter(item -> item.getType() == type).toList();
    }

    /**
     * Lọc món chay (món ăn và combo chay, không tính đồ uống).
     *
     * @return các món chay, sắp theo mã
     */
    public List<MenuItem> vegetarian() {
        return all().stream()
                .filter(MenuItem::isVegetarian)
                .filter(item -> item.getType() != ItemType.DRINK)
                .toList();
    }

    /**
     * Tìm món theo mã.
     *
     * @param id mã món
     * @return món tìm được
     * @throws MenuItemNotFoundException nếu không có món này
     */
    public MenuItem find(String id) throws MenuItemNotFoundException {
        return repo.findById(id).orElseThrow(() -> new MenuItemNotFoundException(id));
    }

    /**
     * Thêm món ăn với mã tự sinh (F01, F02...).
     *
     * @param name tên món
     * @param price giá bán
     * @param vegetarian có phải món chay không
     * @return món vừa thêm
     */
    public Food addFood(String name, long price, boolean vegetarian) {
        Food food = new Food(nextId("F"), name, price, true, vegetarian);
        repo.save(food);
        return food;
    }

    /**
     * Thêm đồ uống với mã tự sinh (D01, D02...).
     *
     * @param name tên đồ uống
     * @param price giá bán
     * @param volumeMl dung tích
     * @return đồ uống vừa thêm
     */
    public Drink addDrink(String name, long price, int volumeMl) {
        Drink drink = new Drink(nextId("D"), name, price, true, volumeMl);
        repo.save(drink);
        return drink;
    }

    /**
     * Sửa giá một món đơn. Combo tự tính giá từ món thành phần nên không sửa giá trực tiếp được.
     *
     * @param id mã món
     * @param newPrice giá mới
     * @throws MenuItemNotFoundException nếu không có món này
     * @throws IllegalArgumentException nếu món không sửa giá được hoặc giá không dương
     */
    public void changePrice(String id, long newPrice) throws MenuItemNotFoundException {
        MenuItem item = find(id);
        // Chỉ kiểm tra món có sửa giá được không; tính giá, mô tả vẫn dùng đa hình.
        if (!(item instanceof SimpleMenuItem)) {
            throw new IllegalArgumentException("Combo tự tính giá, không sửa giá trực tiếp");
        }
        ((SimpleMenuItem) item).changePrice(newPrice);
        repo.save(item);
    }

    /**
     * Đánh dấu món còn hoặc hết.
     *
     * @param id mã món
     * @param available {@code true} nếu còn
     * @throws MenuItemNotFoundException nếu không có món này
     */
    public void setAvailable(String id, boolean available) throws MenuItemNotFoundException {
        MenuItem item = find(id);
        item.setAvailable(available);
        repo.save(item);
    }

    private String nextId(String prefix) {
        int max = repo.findAll().stream()
                .map(MenuItem::getId)
                .filter(id -> id.startsWith(prefix) && id.substring(prefix.length()).matches("\\d+"))
                .mapToInt(id -> Integer.parseInt(id.substring(prefix.length())))
                .max()
                .orElse(0);
        return prefix + String.format("%02d", max + 1);
    }
}
