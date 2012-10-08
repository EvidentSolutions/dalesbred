A library for painless JDBC.

    Database db = Database.forUrlAndCredentials(user, login, password);
    List<Department> departments = db.findAll(query("select id, name from department where manager_id=?", managerId), Department.class);

See the example project for examples.
