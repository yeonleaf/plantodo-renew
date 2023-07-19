package yeonleaf.plantodo.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class MemoryRepository<T> {

    public Map<Long, T> data = new HashMap<>();
    public abstract T save(T t);

    public abstract Optional<T> findById(Long id);

    public abstract void delete(T t);

}
