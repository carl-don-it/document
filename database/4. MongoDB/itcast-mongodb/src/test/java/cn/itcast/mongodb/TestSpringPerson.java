package cn.itcast.mongodb;

import cn.itcast.mongodb.spring.PersonDAO;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestSpringPerson {

    @Autowired
    private PersonDAO personDAO;

    @Test
    public void testSave(){
        Person person = new Person(ObjectId.get(), "刘德华", 50,new Address("人民路", "香港市", "666666"));
        this.personDAO.savePerson(person);
    }

    @Test
    public void testQueryPersonListByName(){
        List<Person> list = this.personDAO.queryPersonListByName("李四");
        for (Person person : list) {
            System.out.println(person);
        }
    }

    @Test
    public void testQueryPagePersonList(){
        List<Person> list = this.personDAO.queryPagePersonList(2, 2);
        for (Person person : list) {
            System.out.println(person);
        }
    }

    @Test
    public void testUpdatae(){
        Person person = new Person();
        person.setId(new ObjectId("5c0c8a19235e194494ae65cc"));
        person.setAge(23);
        UpdateResult update = this.personDAO.update(person);
        System.out.println(update);
    }

    @Test
    public void testDelete(){
        DeleteResult deleteResult = this.personDAO.deleteById("5c0c9c1d235e1936645533ad");
        System.out.println(deleteResult);
    }

}
