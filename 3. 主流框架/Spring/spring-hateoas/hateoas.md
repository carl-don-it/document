类似swagger的暴露接口的工具

```xml
<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-hateoas</artifactId>
		</dependency>
```

```java
import org.springframework.hateoas.ResourceSupport;

public class User extends ResourceSupport {

    @GetMapping(path = "/json/user",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public User user() {
		//暴露接口，不过很麻烦
        user.add(linkTo(methodOn(JSONRestController.class).setUserName(user.getName())).withSelfRel());
        user.add(linkTo(methodOn(JSONRestController.class).setUserAge(user.getAge())).withSelfRel());

        return user;
    }
```

