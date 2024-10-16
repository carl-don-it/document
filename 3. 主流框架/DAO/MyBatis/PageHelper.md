分页插件，PageHelper是mybatis的通用分页插件，通过mybatis的拦截器实现分页功能

官网https://github.com/pagehelper/Mybatis-PageHelper

## spring-boot

```xml
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>1.2.4</version>
</dependency>
```

```yaml
pagehelper:
  helper-dialect: mysql #配置pageHelper操作的数据库类型
```



```java
public QueryResponseResult<CourseInfo> findCourseList(String companyId, Integer page, Integer size,
	                                                      CourseListRequest courseListRequest) {
		if (courseListRequest == null) {
			courseListRequest = new CourseListRequest();
		}
		courseListRequest.setCompanyId(companyId);
		//1. 先把参数验证好,设置默认值
		if (page <= 0) {
			page = 1; // 默认是第一页是1
		}
		if (size <= 0) {
			size = 10;
		}
		//2. 进行分页查询，分页插件，PageHelper是mybatis的通用分页插件，通过mybatis的拦截器实现分页功能，
    	// 会把查到的list转化为Page（实际上也是Arraylist的子类），里面包含了各种分页信息
		PageHelper.startPage(page, size);
		Page<CourseInfo> infoPage = courseMapper.findCourseList(courseListRequest);

		//3. 封装进queryresult
		QueryResult<CourseInfo> courseInfoQueryResult = new QueryResult<>();
		courseInfoQueryResult.setList(infoPage.getResult());
		courseInfoQueryResult.setTotal(infoPage.getTotal());

		//4. 返回结果
		return new QueryResponseResult<>(CommonCode.SUCCESS, courseInfoQueryResult);

	}
```



```java
PageInfo
最后使用pageInfo包装page
```

## 多表联查分页

[mysql多表联合查询分页查询结果条数错误问题](https://blog.csdn.net/Mrqiang9001/article/details/101383868)

多表联合查询同时进行分页查询时，可能会出现查询结果条数不正确的情况或者说查询结果有遗漏的情况。

**解决办法为使用子查询的方式，在主表的子查询中进行分页查询**