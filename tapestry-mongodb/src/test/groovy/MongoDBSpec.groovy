import org.apache.tapestry5.internal.mongodb.MongoDBTestModule
import org.apache.tapestry5.internal.mongodb.People
import org.apache.tapestry5.ioc.Registry
import org.apache.tapestry5.ioc.RegistryBuilder
import org.apache.tapestry5.ioc.modules.TapestryIOCModule
import org.apache.tapestry5.mongodb.MongoDB
import org.apache.tapestry5.mongodb.MongoDBSource
import org.apache.tapestry5.mongodb.modules.MongodbModule
import org.jongo.Jongo
import org.jongo.MongoCollection

import de.flapdoodle.embed.mongo.MongodExecutable
import de.flapdoodle.embed.mongo.MongodProcess
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class MongoDBSpec extends Specification
{
    final int total = 1000

    @Shared @AutoCleanup("shutdown")
    Registry registry;

    @Shared
    MongoDBSource mongoDBSource
    @Shared
    MongoDB mongoDB

    static int PORT = 12345
    static MongodExecutable mongodExe
    static MongodProcess mongod

    static MongoCollection peoples;
    static Jongo jongo

    def setupSpec()
    {
        // Start embedded MongoDB instance
        MongodStarter runtime = MongodStarter.getDefaultInstance();
        mongodExe = runtime.prepare(new MongodConfig(Version.Main.V2_2, 12345, Network.localhostIsIPv6()));
        mongod = mongodExe.start();

        // Populate and start the Registry
        RegistryBuilder builder = new RegistryBuilder().add(MongoDBTestModule, MongodbModule, TapestryIOCModule)
        registry = builder.build();
        registry.performRegistryStartup();

        // Get our service
        mongoDBSource = registry.getService(MongoDBSource)
        mongoDB = registry.getService(MongoDB)

        jongo = new Jongo(mongoDB.getDefaultMongoDb())
        peoples = jongo.getCollection("peoples")
    }

    def cleanupSpec()
    {
        peoples.drop()
        if (mongod != null) mongod.stop()
        if (mongodExe != null) mongodExe.stop();
    }

    def "Lets check mongodb source"()
    {
        expect:
        mongoDBSource.getMongo() != null
    }

    def "Lets populate it"()
    {
        when:
        for (int i = 0; i < total; i++)
        {
            People p = new People();
            p.setBirthDate(new Date());
            p.setName("Name-" + i);
            p.setSurname("Surname-" + i);
            peoples.save(p)
        }

        then:
        peoples.count() == total
        People p42 = peoples.findOne("{name: 'Name-42'}").as(People.class)
        p42.getSurname().equals("Surname-42")

        cleanup:
        peoples.remove("{}")
    }
}