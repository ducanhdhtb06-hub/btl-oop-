package restaurant.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restaurant.model.Table;

class InMemoryRepositoryTest {

    private final InMemoryRepository<Table, Integer> repo = new InMemoryRepository<>();

    @Test
    @DisplayName("Giữ thứ tự thêm vào và tìm theo mã")
    void keepsInsertionOrder() {
        repo.save(new Table(2, 4));
        repo.save(new Table(1, 4));
        assertEquals(List.of(2, 1), repo.findAll().stream().map(Table::getNumber).toList());
        assertEquals(2, repo.findById(2).orElseThrow().getNumber());
        assertTrue(repo.findById(9).isEmpty());
    }

    @Test
    @DisplayName("Lưu lại cùng mã thì thay thế, không nhân đôi")
    void saveSameIdReplaces() {
        repo.save(new Table(1, 4));
        repo.save(new Table(1, 6));
        assertEquals(1, repo.findAll().size());
        assertEquals(6, repo.findById(1).orElseThrow().getSeats());
    }

    @Test
    @DisplayName("Danh sách trả về chỉ đọc")
    void findAllIsReadOnly() {
        repo.save(new Table(1, 4));
        assertThrows(UnsupportedOperationException.class, () -> repo.findAll().clear());
    }
}
