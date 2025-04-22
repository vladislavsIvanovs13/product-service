package org.example.objects;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@EqualsAndHashCode
public class Client {
    private int id;
    private String name;
    private int age;
    private double height;
    private double weight;
}
