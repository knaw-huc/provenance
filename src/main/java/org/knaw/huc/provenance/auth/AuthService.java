package org.knaw.huc.provenance.auth;

import org.jdbi.v3.core.Handle;

import static org.knaw.huc.provenance.util.Config.JDBI;

public class AuthService {
    private static final String USER_SQL = "SELECT id, email, who_person FROM users WHERE id = :id::uuid";

    public User getUserById(String id) {
        try (Handle handle = JDBI.open()) {
            return handle.createQuery(USER_SQL)
                    .bind("id", id)
                    .map((rs, ctx) -> new User(
                            rs.getString("id"),
                            rs.getString("email"),
                            rs.getString("who_person")))
                    .one();
        }
    }
}
