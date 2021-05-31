/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tapestry5.spock

import org.apache.tapestry5.ioc.annotations.*

import spock.lang.*

@SubModule(Module1)
abstract class BaseSpec extends Specification {
  static beforeRegistryCreatedCount = 0

  @Inject
  Service1 service1

  @Inject
  Service2 service2

  private beforeRegistryCreated() {
	assert beforeRegistryCreatedCount == 0
	beforeRegistryCreatedCount++
  }

  def setupSpec() {
	assert beforeRegistryCreatedCount == 2
  }

  def setup() {
	assert service1 instanceof Service1
	assert service2 instanceof Service2
  }
}

@SubModule(Module2)
class TapestrySpecInheritance extends BaseSpec {
  @Inject
  Service2 anotherService2

  def beforeRegistryCreated() {
	assert beforeRegistryCreatedCount == 1
	beforeRegistryCreatedCount++
  }

  def setup() {
	assert service1 instanceof Service1
	assert service2 instanceof Service2
	assert anotherService2 instanceof Service2
  }

  def "fields of base class have been injected"() {
	expect:
	service1 instanceof Service1
	service2 instanceof Service2
  }

  def "fields of derived class have been injected"() {
	expect:
	anotherService2 instanceof Service2
  }
}
