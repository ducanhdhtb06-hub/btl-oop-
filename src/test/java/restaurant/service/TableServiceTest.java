package restaurant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import restaurant.exception.TableNotFoundException;
import restaurant.model.Table;
import restaurant.repository.InMemoryRepository;

class TableServiceTest {

    @Test
    @DisplayName("Có 10 bàn mặc định: bàn 1–6 có 4 ghế, 7–8 có 6 ghế, 9–10 có 2 ghế")
    void defaultTables() {
        assertEquals(10, TableService.defaultTables().size());
        assertEquals(4, TableService.defaultTables().get(0).getSeats());
        assertEquals(6, TableService.defaultTables().get(6).getSeats());
        assertEquals(2, TableService.defaultTables().get(9).getSeats());
        assertEquals(10, TableService.defaultTables().get(9).getNumber());
    }

    @Test
    @DisplayName("Tìm bàn theo số, không có thì báo lỗi")
    void find() throws TableNotFoundException {
        InMemoryRepository<Table, Integer> repo = new InMemoryRepository<>();
        TableService.defaultTables().forEach(repo::save);
        TableService tables = new TableService(repo);
        assertEquals(10, tables.all().size());
        assertEquals(3, tables.find(3).getNumber());
        assertThrows(TableNotFoundException.class, () -> tables.find(11));
    }
}
