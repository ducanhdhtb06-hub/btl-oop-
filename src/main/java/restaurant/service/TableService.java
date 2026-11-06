package restaurant.service;

import java.util.ArrayList;
import java.util.List;
import restaurant.exception.TableNotFoundException;
import restaurant.model.Table;
import restaurant.repository.Repository;

/** Nghiệp vụ bàn: liệt kê và tìm bàn. */
public class TableService {

    private final Repository<Table, Integer> repo;

    /**
     * Tạo service.
     *
     * @param repo nơi lưu bàn
     */
    public TableService(Repository<Table, Integer> repo) {
        this.repo = repo;
    }

    /**
     * Tạo 10 bàn mặc định của nhà hàng: bàn 1–6 có 4 ghế, bàn 7–8 có 6 ghế, bàn 9–10 có 2 ghế.
     *
     * @return danh sách bàn mới, đều đang trống
     */
    public static List<Table> defaultTables() {
        List<Table> tables = new ArrayList<>();
        for (int number = 1; number <= 10; number++) {
            int seats = number <= 6 ? 4 : number <= 8 ? 6 : 2;
            tables.add(new Table(number, seats));
        }
        return List.copyOf(tables);
    }

    /**
     * Trả về mọi bàn.
     *
     * @return danh sách bàn
     */
    public List<Table> all() {
        return repo.findAll();
    }

    /**
     * Tìm bàn theo số.
     *
     * @param number số bàn
     * @return bàn tìm được
     * @throws TableNotFoundException nếu không có bàn này
     */
    public Table find(int number) throws TableNotFoundException {
        return repo.findById(number).orElseThrow(() -> new TableNotFoundException(number));
    }
}
