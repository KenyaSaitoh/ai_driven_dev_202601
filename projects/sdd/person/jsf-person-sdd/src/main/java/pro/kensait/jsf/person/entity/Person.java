package pro.kensait.jsf.person.entity;

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Person entity class that maps to the PERSON table.
 * This entity represents personal information including ID, name, age, and gender.
 */
@Entity
@Table(name = "PERSON")
public class Person implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Person ID (Primary Key, Auto-increment)
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PERSON_ID")
    private Integer personId;

    /**
     * Person Name (Required, Max 30 characters)
     */
    @Column(name = "PERSON_NAME", nullable = false, length = 30)
    private String personName;

    /**
     * Age (Required)
     */
    @Column(name = "AGE", nullable = false)
    private Integer age;

    /**
     * Gender (Required, Max 10 characters)
     * Valid values: "male" or "female"
     */
    @Column(name = "GENDER", nullable = false, length = 10)
    private String gender;

    /**
     * Default constructor
     */
    public Person() {
    }

    /**
     * Constructor without personId (for new Person)
     * 
     * @param personName Person name
     * @param age Age
     * @param gender Gender ("male" or "female")
     */
    public Person(String personName, Integer age, String gender) {
        this.personName = personName;
        this.age = age;
        this.gender = gender;
    }

    /**
     * Constructor with all fields (for existing Person)
     * 
     * @param personId Person ID
     * @param personName Person name
     * @param age Age
     * @param gender Gender ("male" or "female")
     */
    public Person(Integer personId, String personName, Integer age, String gender) {
        this.personId = personId;
        this.personName = personName;
        this.age = age;
        this.gender = gender;
    }

    // Getters and Setters

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public String getPersonName() {
        return personName;
    }

    public void setPersonName(String personName) {
        this.personName = personName;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public String toString() {
        return "Person [personId=" + personId + ", personName=" + personName
                + ", age=" + age + ", gender=" + gender + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((personId == null) ? 0 : personId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Person other = (Person) obj;
        if (personId == null) {
            if (other.personId != null)
                return false;
        } else if (!personId.equals(other.personId))
            return false;
        return true;
    }
}
