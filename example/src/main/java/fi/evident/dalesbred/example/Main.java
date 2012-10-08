package fi.evident.dalesbred.example;

import fi.evident.dalesbred.ConnectionCallback;
import fi.evident.dalesbred.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;

import static fi.evident.dalesbred.SqlQuery.query;

public class Main {

    @NotNull
    public final Database db = Database.forUrlAndCredentials("jdbc:postgresql://db/dalesbred-test", "evident", "Evident11");

    public void run() {
        db.withTransaction(new ConnectionCallback<Object>() {
            @Override
            public Object execute(Connection connection) throws SQLException {
                db.update("create table foo (id serial primary key, name varchar(64) not null)");
                db.update("insert into foo (name) values ('foo')");
                db.update("insert into foo (name) values ('bar')");

                System.out.println(db.findUniqueInt(query("select count(*) from foo")));

                connection.rollback();
                return null;
            }
        });
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
