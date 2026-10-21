package fi.haagahelia.projectbackend.repository;

import org.springframework.data.repository.CrudRepository;

import fi.haagahelia.projectbackend.model.AppUser;

public interface UserRepository extends CrudRepository<AppUser, Long> {

    AppUser findByUsername(String username);
}
