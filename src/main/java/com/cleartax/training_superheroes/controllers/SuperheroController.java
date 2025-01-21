package com.cleartax.training_superheroes.controllers;


import com.cleartax.training_superheroes.dto.Superhero;
import com.cleartax.training_superheroes.dto.SuperheroRequestBody;
import com.cleartax.training_superheroes.services.SuperheroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class SuperheroController {

    private SuperheroService superheroService;

    @Autowired
    public SuperheroController(SuperheroService superheroService){
        this.superheroService = superheroService;
    }

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "username", defaultValue = "World") String username) {
        System.out.println("Hello " + username);
        return String.format("Hello %s!", username);
    }

    @GetMapping("/superhero")
    public Superhero getSuperhero(@RequestParam(value = "name", defaultValue = "Batman") String name,
                                  @RequestParam(value = "universe", defaultValue = "DC") String universe){
        System.out.println("Fetching superhero with name: " + name + ", universe: " + universe);
        if (name == null && universe == null) {
            throw new IllegalArgumentException("At least one of 'name' or 'universe' must be provided");
        }
        return superheroService.getSuperhero(name, universe);
    }

    @PostMapping("/superhero")
    public Superhero persistSuperhero(@RequestBody SuperheroRequestBody superherorequestBody){
        System.out.println("Superhero " + superherorequestBody.getName() + " added in " + superherorequestBody.getUniverse() + " universe");
        return superheroService.persistSuperhero(superherorequestBody);
    }

    @DeleteMapping("/superhero")
    public String deleteSuperhero(@RequestParam(value = "name") String name,
                                  @RequestParam(value = "universe") String universe) {
        System.out.println("Deleting superhero: " + name + " from " + universe);
        boolean isDeleted = superheroService.deleteSuperhero(name, universe);

        if (isDeleted) {
            return String.format("Superhero %s from %s has been deleted successfully.", name, universe);
        } else {
            return String.format("Superhero %s from %s not found.", name, universe);
        }
    }
    @PutMapping("/superhero")
    public Superhero updateSuperhero(@RequestParam(value = "name") String name,
                                     @RequestParam(value = "universe") String universe,
                                     @RequestBody SuperheroRequestBody updatedDetails) {
        System.out.println("Updating superhero: " + name + " in universe: " + universe);
        return superheroService.updateSuperhero(name, universe, updatedDetails);
    }


}
