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
import org.springframework.data.cassandra.core.CassandraTemplate;
import org.springframework.data.cassandra.core.query.Columns;
import org.springframework.data.cassandra.core.query.Criteria;
import org.springframework.data.cassandra.core.query.Query;
import org.springframework.data.cassandra.core.query.Update;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import example.app.model.Gender;
import example.app.model.Person;
import example.tests.support.TestCassandraConfiguration;

/**
 * The CassandraClientQueryUpdateIntegrationTests class...
 *
 * @author John Blum
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ContextConfiguration
public class CassandraClientQueryUpdateIntegrationTests {

	private static final String KEYSPACE_NAME = "TestPeopleKeyspace";

	@Autowired
	@SuppressWarnings("all")
	private CassandraTemplate cassandraTemplate;

	@Test
	public void firstAssertSetup() {
		assertThat(this.cassandraTemplate.count(Person.class)).isEqualTo(8);
	}

	/**
	 * NOTE:
	 * ORDER BY (SORT) with 2ndary indexes is not supported, and...
	 * You must allowFiltering, when...
	 * Querying with age >= 18 is not support unless there is EQ predicate/condition...
	 *
	 * com.datastax.driver.core.exceptions.InvalidQueryException: Cannot execute this query
	 * as it might involve data filtering and thus may have unpredictable performance.
	 * If you want to execute this query despite the performance unpredictability, use ALLOW FILTERING
	 */
	@Test
	public void queryForAdults() {

		Query queryForAdults = Query.query(Criteria.where("lastName").is("Doe"),
				Criteria.where("age" ).gte(18))
			.columns(Columns.from("id", "firstName", "lastName", "age", "gender"))
			.withAllowFiltering();

		List<Person> adults = this.cassandraTemplate.select(queryForAdults, Person.class);

		assertThat(adults).contains(
			Person.of(1L, "Jon Doe"),
			Person.of(2L, "Jane Doe"),
			Person.of(5L, "Ho Doe"),
			Person.of(8L, "Sour Doe")
		);
	}

	@Test
	public void queryForFemales() {

		Query queryForFemales = Query.query(Criteria.where("gender").is(Gender.FEMALE))
			.columns(Columns.from("id", "firstName", "lastName", "age", "gender"));

		List<Person> females = this.cassandraTemplate.select(queryForFemales, Person.class);

		assertThat(females).contains(
			Person.of(2L, "Jane Doe"),
			Person.of(7L, "Pie Doe"),
			Person.of(3L, "Cookie Doe")
		);
	}

	@Test
	public void updateHoDoeToHoeDoe() {

		Query idIsFive = Query.query(Criteria.where("id").is(5));

		Update setFirstNameToHoe = Update.update("firstName", "Hoe");

		this.cassandraTemplate.update(idIsFive, setFirstNameToHoe, Person.class);

		Person hoeDoe = this.cassandraTemplate.selectOneById(5, Person.class);

		assertThat(hoeDoe).isNotNull();
		assertThat(hoeDoe.getId()).isEqualTo(5);
		assertThat(hoeDoe.getFirstName()).isEqualTo("Hoe");
		assertThat(hoeDoe.getLastName()).isEqualTo("Doe");
	}

	@Configuration
	@SuppressWarnings("all")
	static class TestConfiguration extends TestCassandraConfiguration {

		@Override
		protected List<String> getStartupScripts() {
			return append(super.getStartupScripts(), injectKeyspaceName(
				"CREATE INDEX IF NOT EXISTS AgeIdx ON %s.people(age);",
				"CREATE INDEX IF NOT EXISTS GenderIdx ON %s.people(gender);",
				"CREATE INDEX IF NOT EXISTS LastNameIdx ON %s.people(lastName);",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (3, 'Cookie', 'Doe', 3, 'FEMALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (4, 'Fro', 'Doe', 8, 'MALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (5, 'Ho', 'Doe', 21, 'MALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (6, 'Joe', 'Doe', 5, 'MALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (7, 'Pie', 'Doe', 16, 'FEMALE');",
				"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (8, 'Sour', 'Doe', 18, 'MALE');"
			));
		}

		@Override
		protected String getKeyspaceName() {
			return KEYSPACE_NAME;
		}
	}
}
