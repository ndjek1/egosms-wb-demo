package org.pahappa.systems.egoSms.models.demo;

import javax.persistence.*;
import org.sers.webutils.model.BaseEntity;

@Entity
@Table(name = "my_demo")
public class MyDemo extends BaseEntity {
  private String firstName;
  private String lastName;

  @Column(name = "first_name", nullable = false)
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  @Column(name = "last_name", nullable = false)
  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
}
