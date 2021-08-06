package org.knaw.huc.provenance;

import org.jdbi.v3.core.Jdbi;

public class Config {
    public static final int PORT = createPort();
    public static final Jdbi JDBI = createJdbi();

    private static int createPort() {
        return Integer.parseInt(System.getenv().getOrDefault("PROVENANCE_PORT", "8080"));
    }

    private static Jdbi createJdbi() {
        String host = System.getenv().getOrDefault("PROVENANCE_DATABASE_HOST", "localhost");
        String port = System.getenv().getOrDefault("PROVENANCE_DATABASE_PORT", "5432");
        String database = System.getenv().getOrDefault("PROVENANCE_DATABASE_DB", "postgres");

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s", host, port, database);

        String user = System.getenv().getOrDefault("PROVENANCE_DATABASE_USER", null);
        String password = System.getenv().getOrDefault("PROVENANCE_DATABASE_PASSWORD", null);
        if (user != null && password != null)
            return Jdbi.create(jdbcUrl, user, password);

        return Jdbi.create(jdbcUrl);
    }
}
