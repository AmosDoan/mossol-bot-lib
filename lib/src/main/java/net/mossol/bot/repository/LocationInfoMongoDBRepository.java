package net.mossol.bot.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import net.mossol.bot.model.LocationInfo;

public interface LocationInfoMongoDBRepository extends MongoRepository<LocationInfo, String> {
    @Override
    List<LocationInfo> findAll();
}
