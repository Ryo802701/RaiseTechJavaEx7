package com.example.RaiseTechJavaEx7;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/person")
@Validated
public class PersonController {
	// IDとPersonクラスのマップ
	private Map<Integer, Person> persons = new HashMap<>();

	public PersonController() {
		// 初期データ
		persons.put(1, new Person("Kato", "1980-01-01"));
		persons.put(2, new Person("Yamada", "1985-04-10"));
		persons.put(3, new Person("Tanaka", "1980-01-01"));
		persons.put(4, new Person("Kato", "1992-11-28"));
	}

	@GetMapping("/names")
	public List<String> getNames() {
		// 名前のリストを返す。
		List<String> lstNames = persons.entrySet().stream().map(p -> p.getValue().getName())
				.collect(Collectors.toList());

		return lstNames;
	}

	@GetMapping("/search")
	public Map<Integer, Person> getPersons(
			@RequestParam(value = "name") @Valid @NotBlank @Length(min = 1, max = 19) String name,
			@RequestParam(value = "birthday") String birthday) {
		if (birthday.isEmpty()) {
			// birthdayが空ならnameのみが一致するデータを返す。
			return persons.entrySet().stream().filter(p -> p.getValue().getName().equals(name))
					.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));
		} else {
			// nameとbirthdayが一致するデータを返す。
			return persons.entrySet().stream()
					.filter(p -> p.getValue().getName().equals(name) && p.getValue().getBirthday().equals(birthday))
					.collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

		}
	}

	@PostMapping("/create")
	public ResponseEntity<String> create(@RequestBody CreateForm form) {
		// マップのキーの最大値＋1を新しいIDとする。
		int newid = Collections.max(persons.keySet()) + 1;
		// マップに登録。
		persons.put(newid, new Person(form.getName(), form.getBirthDay()));

		URI uri = UriComponentsBuilder.fromUriString("http://localhost:8080").path("/names/" + String.valueOf(newid))
				.build().toUri();
		return ResponseEntity.created(uri).body("successfully created");
	}

	@PatchMapping("/update/{id}")
	public ResponseEntity<String> update(@PathVariable("id") int id, @RequestBody UpdateForm form) {
		// idからマップを検索し、Personクラスを取得。
		Person person = persons.get(id);
		// リクエストデータで更新する。
		person.setName(form.getName());
		person.setBirthday(form.getBirthDay());
		// マップの要素を置き換える。
		persons.replace(id, person);

		return ResponseEntity.ok("successfully updated");
	}

	@DeleteMapping("/delete/{id}")
	public ResponseEntity<String> delete(@PathVariable("id") int id) {
		// マップの要素を削除する。
		persons.remove(id);

		return ResponseEntity.ok("successfully deleted");
	}
}
