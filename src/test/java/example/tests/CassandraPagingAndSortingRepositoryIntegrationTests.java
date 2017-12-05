package example.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.query.CassandraPageRequest;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.model.Person;
import example.app.repo.PersonRepository;
import example.tests.support.TestCassandraConfiguration;

/**
 * The CassandraPagingAndSortingRepositoryIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration
@SuppressWarnings("all")
public class CassandraPagingAndSortingRepositoryIntegrationTests {

	private static final String KEYSPACE_NAME = "TestPeopleKeyspace";

	@Autowired
	private PersonRepository personRepository;

	@Before
	public void setup() {
		assertThat(this.personRepository.count()).isEqualTo(10);
	}

	@Test
	public void pageResultsAreCorrect() {

		//Sort orderByAgeDescending = Sort.by(Sort.Direction.DESC, "age");

		List<Person> results = new ArrayList<>();

		Slice<Person> firstPage = this.personRepository.findAllSlicedByLastName("Doe",
			CassandraPageRequest.first(4));

		assertThat(firstPage.getNumberOfElements()).isEqualTo(4);

		results.addAll(firstPage.getContent());

		assertThat(firstPage.hasNext()).isTrue();

		Slice<Person> secondPage = this.personRepository.findAllSlicedByLastName("Doe",
			firstPage.nextPageable());

		assertThat(secondPage.getNumberOfElements()).isEqualTo(3);

		results.addAll(secondPage.getContent());

		assertThat(secondPage.hasNext()).isFalse();

		assertThat(results).containsExactlyInAnyOrder(
			Person.of(1L, "Jon Doe"),
			Person.of(2L, "Jane Doe"),
			Person.of(7L, "Sour Doe"),
			Person.of(6L, "Pie Doe"),
			Person.of(4L, "Fro Doe"),
			Person.of(5L, "Joe Doe"),
			Person.of(3L, "Cookie Doe")
		);
	}

	@Configuration
	@EnableCassandraRepositories(basePackageClasses = PersonRepository.class)
	static class TestConfiguration extends TestCassandraConfiguration {

		@Override
		protected List<String> getStartupScripts() {

			return injectKeyspaceName(
				"CREATE KEYSPACE IF NOT EXISTS %s WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1 };",
				"CREATE TABLE IF NOT EXISTS %s.people (id int, firstName text, lastName text, age int, gender text, PRIMARY KEY(lastName, age));",
				"TRUNCATE %s.people",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (1, 'Jon', 'Doe', 43, 'MALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (8, 'Jack', 'Handy', 50, 'MALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (2, 'Jane', 'Doe', 39, 'FEMALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (3, 'Cookie', 'Doe', 3, 'FEMALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (4, 'Fro', 'Doe', 8, 'MALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (9, 'Sany', 'Handy', 45, 'FEMALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (5, 'Joe', 'Doe', 5, 'MALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (6, 'Pie', 'Doe', 16, 'FEMALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (7, 'Sour', 'Doe', 18, 'MALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (10, 'Agent', 'Smith', 28, 'MALE');"
			);
		}

		@Override
		protected String getKeyspaceName() {
			return KEYSPACE_NAME;
		}
	}
}
