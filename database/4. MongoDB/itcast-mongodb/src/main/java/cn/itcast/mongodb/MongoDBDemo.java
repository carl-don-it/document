package cn.itcast.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.function.Consumer;

public class MongoDBDemo {

    public static void main(String[] args) {
        // 建立连接
        MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:27017");

        // 选择数据库
        MongoDatabase database = mongoClient.getDatabase("testdb");

        // 选择表
        MongoCollection<Document> userCollection = database.getCollection("user");

        // 操作
        userCollection.find().limit(10).forEach((Consumer<? super Document>) document -> {
            System.out.println(document.toJson());
        });

        // 关闭连接，释放资源
        mongoClient.close();
    }
}
