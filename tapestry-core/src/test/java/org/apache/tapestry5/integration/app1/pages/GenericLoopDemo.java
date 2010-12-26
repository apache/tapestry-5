package org.apache.tapestry5.integration.app1.pages;

import org.apache.tapestry5.annotations.Component;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Loop;
import org.apache.tapestry5.integration.app1.data.Person;
import org.apache.tapestry5.integration.app1.data.Pet;
import org.apache.tapestry5.integration.app1.data.PetType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenericLoopDemo extends BaseGenericLoopDemo<Pet> {

    @Property
    @Component(parameters = {"source=integerSource"})
    private Loop<Integer> integerLoop;

    @Property
    private List<Integer> integerSource;

    @Property
    @Component(parameters = {"source=personSource"})
    private Loop<Person> personLoop;

    @Property
    private List<Person> personSource;

    void beginRender() {
        integerSource = Arrays.asList(1, 3, 5, 7, 11);
        personSource = new ArrayList<Person>();

        Person person = new Person();
        person.setAge(25);
        person.setName("John Doe");
        personSource.add(person);

        person = new Person();
        person.setAge(53);
        person.setName("Jane Dover");
        personSource.add(person);

        person = new Person();
        person.setAge(13);
        person.setName("James Jackson");
        personSource.add(person);
    }

    @Override
    List<Pet> initInheritedLoop() {
        final ArrayList<Pet> list = new ArrayList<Pet>();
        Pet pet = new Pet();
        pet.setAge(6);
        pet.setName("Dakota");
        pet.setType(PetType.DOG);
        list.add(pet);

        pet = new Pet();
        pet.setAge(3);
        pet.setName("Jill");
        pet.setType(PetType.CAT);
        list.add(pet);

        pet = new Pet();
        pet.setAge(3);
        pet.setName("Jack");
        pet.setType(PetType.CAT);
        list.add(pet);
        return list;
    }

}
