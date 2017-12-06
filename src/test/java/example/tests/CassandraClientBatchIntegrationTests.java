/*
 *  Copyright 2017 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 *  or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package example.tests;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.CassandraBatchOperations;
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Columns;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.model.Person;
import example.tests.support.TestCassandraConfiguration;

/**
 * Integration Tests demonstrating Spring Data Cassandra {@link CassandraBatchOperations}.
 *
 * @author John Blum
 * @see org.junit.Test
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration
public class CassandraClientBatchIntegrationTests {

	private static final String KEYSPACE_NAME = "TestPeopleKeyspace";

	@Autowired
	@SuppressWarnings("all")
	private CassandraTemplate cassandraTemplate;

	private Query queryForAllPeople = Query.empty().columns(Columns.from("id", "firstName", "lastName"));

	@Test
	public void firstAssertSetup() {

		List<Person> people = this.cassandraTemplate.select(this.queryForAllPeople, Person.class);

		assertThat(people).containsExactly(
			Person.of(1L, "Jon Doe"),
			Person.of(2L, "Jane Doe")
		);
	}

	@Test
	public void nextAssertBatchInsertUpdateIsSuccessful() {

		Person jonDoe = Person.of(1L, "John Doe");
		Person pieDoe = Person.of(3L, "Pie Doe");

		this.cassandraTemplate.batchOps()
			.insert(pieDoe)
			.update(jonDoe)
			.execute();

		List<Person> people = this.cassandraTemplate.select(this.queryForAllPeople, Person.class);

		assertThat(people).containsExactly(
			Person.of(1L, "John Doe"),
			Person.of(2L, "Jane Doe"),
			Person.of(3L, "Pie Doe")
		);
	}

	@Test
	public void thenAssertBatchDeleteIsSuccessful() {

		Person janeDoe = Person.of(2L, "Jane Doe");
		Person pieDoe = Person.of(3L, "Pie Doe");

		this.cassandraTemplate.batchOps()
			.delete(janeDoe, pieDoe)
			.execute();

		List<Person> people = this.cassandraTemplate.select(this.queryForAllPeople, Person.class);

		assertThat(people).containsExactly(Person.of(1L, "John Doe"));
	}

	@Configuration
	@SuppressWarnings("all")
	static class TestConfiguration extends TestCassandraConfiguration {

		@Override
		protected String getKeyspaceName() {
			return KEYSPACE_NAME;
		}
	}
}
