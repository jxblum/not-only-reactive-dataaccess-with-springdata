package example.app.repo;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.repository.CrudRepository;

import example.app.model.Person;

/**
 * The {@link PersonRepository} class is a Spring Data {@link CrudRepository} implementing basic CRUD
 * and simple Query data access operations for {@link Person people}.
 *
 * @author John Blum
 * @see org.springframework.data.cassandra.repository.CassandraRepository
 * @see org.springframework.data.repository.CrudRepository
 * @since 1.0.0
 */
@SuppressWarnings("all")
public interface PersonRepository extends CassandraRepository<Person, Long> {

	Slice<Person> findAllSlicedByLastName(String lastName, Pageable pageable);

}
