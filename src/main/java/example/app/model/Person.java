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

package example.app.model;

import static org.cp.elements.lang.RuntimeExceptionsFactory.newIllegalArgumentException;

import java.util.Optional;

import org.springframework.data.annotation.Id;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.util.ObjectUtils;

import lombok.Data;

/**
 * The {@link Person} class is a Abstract Data Type (ADT) modeling a person.
 *
 * @author John Blum
 * @see org.springframework.data.annotation.Id
 * @see org.springframework.data.cassandra.core.mapping.Table
 * @since 1.0.0
 */
@Data
@Table("People")
@SuppressWarnings("unused")
public class Person {

	public static Person of(Long id, String name) {

		int indexOfSpace = name.indexOf(" ");

		return of(id, name.substring(0, indexOfSpace).trim(), name.substring(indexOfSpace).trim());
	}

	public static Person of(Long id, String firstName, String lastName) {
		return new Person().identifiedBy(id).with(firstName, lastName);
	}

	@Id
	private Long id;

	private Gender gender;

	private Integer age;

	private String firstName;
	private String lastName;

	public String getName() {
		return String.format("%1$s %2$s", getFirstName(), getLastName());
	}

	public Person age(int age) {

		this.age = Optional.of(age).filter(it-> it > 0)
			.orElseThrow(() -> newIllegalArgumentException("Person must be greater than 0 years old [%d]", age));

		return this;
	}

	public Person as(Gender gender) {
		this.gender = gender;
		return this;
	}

	public Person identifiedBy(Long id) {
		this.id = id;
		return this;
	}

	public Person with(String firstName, String lastName) {

		this.firstName = firstName;
		this.lastName = lastName;

		return this;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == this) {
			return true;
		}

		if (!(obj instanceof Person)) {
			return false;
		}

		Person that = (Person) obj;

		return ObjectUtils.nullSafeEquals(this.getId(), that.getId())
			&& ObjectUtils.nullSafeEquals(this.getFirstName(), that.getFirstName())
			&& ObjectUtils.nullSafeEquals(this.getLastName(), that.getLastName());
	}

	@Override
	public int hashCode() {

		int hashValue = 17;

		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getId());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getFirstName());
		hashValue = 37 * hashValue + ObjectUtils.nullSafeHashCode(getLastName());

		return hashValue;
	}
	@Override
	public String toString() {
		return String.format("%s(%d)", getName(), getId());
	}
}
