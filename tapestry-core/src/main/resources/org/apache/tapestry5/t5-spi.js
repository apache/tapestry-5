/* Copyright 2011 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Defines the SPI (service provider interface). This represents an abstract
 * layer between Tapestry's JavaScript and an underlying framework (such as
 * Prototype and JQuery).
 * 
 * <p>
 * The SPI defines placeholders for functions whose implementations are provided
 * elsehwere. In some cases, an SPI may define a function in terms of other SPI
 * functions; a framework layer may leave such a function alone or re-implement
 * it.
 */
T5.define("spi", {

});