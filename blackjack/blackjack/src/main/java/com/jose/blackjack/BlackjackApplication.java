package com.jose.blackjack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BlackjackApplication {


    //                     TODO NewGame+                   //

    // IN PROGRESS: double con el player desde DB (Ref. Capa de servicio)
    // TODO mirar el metodo de Gwenael para interactuar con el Mongo
    // TODO dockerizar


    public static void main(String[] args) {
        SpringApplication.run(BlackjackApplication.class, args);
    }


}
