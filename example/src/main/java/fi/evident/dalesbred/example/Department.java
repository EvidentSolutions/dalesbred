package fi.evident.dalesbred.example;

import fi.evident.dalesbred.Reflective;

public final class Department {
    public final int id;
    public final String name;

    @Reflective
    public Department(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Department [id=" + id + ", name=" + name + "]";
    }
}
