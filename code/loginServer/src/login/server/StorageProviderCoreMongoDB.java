package login.server;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import static com.mongodb.client.model.Filters.ne;

public class StorageProviderCoreMongoDB {
    protected static MongoDatabase database;
    private static StorageProviderCoreMongoDB sp;

    protected StorageProviderCoreMongoDB() throws Exception {
        throw new Exception();
    }

    private StorageProviderCoreMongoDB(boolean allowed) {

    }

    @SuppressWarnings("deprecation")
    protected static synchronized void init(String mongoURI, String dbName) throws Exception {
        if (sp == null) {
            sp = new StorageProviderCoreMongoDB(true);
            MongoClient mongoClient = new MongoClient(
                    new MongoClientURI(mongoURI));
            database = mongoClient.getDatabase(dbName);
            // Used to check for valid connection!
            try {
                mongoClient.getDatabaseNames();
            } catch (Exception e) {
                System.out.printf("Could not connect to mongodb at %s because of %s \n", mongoURI, e.getMessage());
                System.exit(-1);
            }
            System.out.println("MongoDB storage provider initialized.");
        }
        else {
            throw new Exception("Already initialized.");
        }
    }

    protected static StorageProviderCoreMongoDB getStorageProvider(){
        return sp;
    }

    protected static MongoCollection<Document> deleteCollection(String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        // Deletes all items in the collection
        collection.drop();
        return collection;
    }
}
