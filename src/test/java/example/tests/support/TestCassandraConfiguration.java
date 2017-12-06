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

package example.tests.support;

import static java.util.Arrays.stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.cassandra.config.AbstractCassandraConfiguration;

/**
 * The {@link TestCassandraConfiguration} class is a Spring Data Cassandra {@link AbstractCassandraConfiguration} class
 * for configuring an Apache Cassandra {@link Cluster} and {@link Session}.
 *
 * @author John Blum
 * @see org.springframework.data.cassandra.config.AbstractCassandraConfiguration
 * @since 1.0.0
 */
@SuppressWarnings("all")
public abstract class TestCassandraConfiguration extends AbstractCassandraConfiguration {

	protected <T> List<T> append(List<T> list, T... elements) {

		List<T> newList = new ArrayList<>(list);

		Collections.addAll(newList, elements);

		return newList;
	}

	protected <T> List<T> append(List<T> list, List<T> elements) {
		return append(list, (T[]) elements.toArray());
	}

	protected List<String> injectKeyspaceName(String... scripts) {

		return stream(scripts)
			.map(script -> String.format(script, getKeyspaceName()))
			.collect(Collectors.toList());
	}

	@Override
	protected List<String> getStartupScripts() {

		return injectKeyspaceName(
			"CREATE KEYSPACE IF NOT EXISTS %s WITH REPLICATION = { 'class':'SimpleStrategy', 'replication_factor':1 };",
			"CREATE TABLE IF NOT EXISTS %s.people (id int, firstName text, lastName text, age int, gender text, PRIMARY KEY(id));",
			"TRUNCATE %s.people",
			"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (1, 'Jon', 'Doe', 43, 'MALE');",
			"INSERT INTO %s.people (id, firstName, lastName, age, gender) VALUES (2, 'Jane', 'Doe', 39, 'FEMALE');"
		);
	}

	@Override
	protected List<String> getShutdownScripts() {
		return injectKeyspaceName("DROP KEYSPACE %s;");
	}
}
