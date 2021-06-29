package cn.itcast.mongodb.spring;

import cn.itcast.mongodb.Person;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PersonDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    public Person savePerson(Person person){
        Map<String,Object> document = new HashMap<>();
        document.put("id", 9999);
        document.put("uzsername", "张三");
        document.put("age", 30);
        Map<String,Object> document2 = new HashMap<>();
        document2.put("id", 9999);
        document2.put("username", "张三");
        document2.put("age", 30);
        document2.put("other", document);
        ArrayList<Map<String,Object>> objects = new ArrayList<>();
        objects.add(document);
        objects.add(document);
        objects.add(document);
        document2.put("array", objects);
        this.mongoTemplate.save(document2, "personels");
        return this.mongoTemplate.save(person);
    }

    public List<Person> queryPersonListByName(String name) {
        Query query = Query.query(Criteria.where("name").is(name));
        return this.mongoTemplate.find(query, Person.class);
    }

    public List<Person> queryPagePersonList(Integer page, Integer rows) {
        Query query = new Query().limit(rows).skip((page - 1) * rows);
        return this.mongoTemplate.find(query, Person.class);
    }

    public UpdateResult update(Person person) {
        Query query = Query.query(Criteria.where("id").is(person.getId()));
        Update update = Update.update("age", person.getAge());
        return this.mongoTemplate.updateFirst(query, update, Person.class);
    }

    public DeleteResult deleteById(String id) {
        Query query = Query.query(Criteria.where("id").is(id));
        return this.mongoTemplate.remove(query, Person.class);
    }

}
