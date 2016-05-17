/*******************************************************************************
 * Copyright 2013 Marco Balduini, Emanuele Della Valle
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package csparql.json_dataset_deserialization;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Sparql_json_results {

	private Head head;
	private Result results;

	public Head getHead() {
		return head;
	}
	public void setHead(Head head) {
		this.head = head;
	}
	public Result getResults() {
		return results;
	}
	public void setResults(Result results) {
		this.results = results;
	}

	public void printSerializationOnConsole() {

		for(Map<String, Variable> m : results.getBindings()){
			Set<Entry<String, Variable>> set = m.entrySet();
			for(Entry<String, Variable> e : set){
				System.out.println(e.getValue().getType());
				if(e.getValue().getType().equals("typed-literal"))
					System.out.println(e.getValue().getDatatype());
				System.out.println(e.getValue().getValue());
				System.out.println();
			}
		}

	}

}
