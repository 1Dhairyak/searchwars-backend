package com.higherlower.game.repository;

import com.higherlower.game.entity.PlayerName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerNameRepository extends JpaRepository<PlayerName, Long> {

    Optional<PlayerName> findByNameKey(String nameKey);

    Optional<PlayerName> findByOwnerKey(String ownerKey);
}
