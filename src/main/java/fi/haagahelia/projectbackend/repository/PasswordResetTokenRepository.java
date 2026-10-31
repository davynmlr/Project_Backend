package fi.haagahelia.projectbackend.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import fi.haagahelia.projectbackend.model.AppUser;
import fi.haagahelia.projectbackend.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);

    @Transactional
    void deleteByUser(AppUser user);
}
