/*
 * Copyright (c) 2012 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.evident.dalesbred.example;

import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.TransactionCallback;
import fi.evident.dalesbred.TransactionContext;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class Main {

    @NotNull
    public final Database db = Database.forUrlAndCredentials("jdbc:postgresql://db/dalesbred-test", "evident", "Evident11");

    public void exampleTransaction() {
        db.update("drop table if exists department");
        db.update("create table department (id serial primary key, name varchar(64) not null)");
        db.update("insert into department (name) values ('foo')");
        db.update("insert into department (name) values ('bar')");

        int count = db.findUniqueInt("select count(*) from department");
        List<Department> departments = db.findAll(Department.class, "select id, name from department");

        System.out.println("department count: " + count);
        System.out.println("departments: " + departments);
    }

    public void run() {
        db.withTransaction(new TransactionCallback<Object>() {
            @Override
            public Object execute(TransactionContext tx) throws SQLException {
                exampleTransaction();
                tx.setRollbackOnly();
                return null;
            }
        });
    }

    public static void main(String[] args) {
        new Main().run();
    }
}
