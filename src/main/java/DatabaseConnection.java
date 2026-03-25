import java.sql.*;

public class DatabaseConnection {

    private static DatabaseConnection instance;

    private Connection conn;

    private DatabaseConnection() {
        try {

            // CARGAR DRIVER
            Class.forName("org.sqlite.JDBC");

            // ESTABLECER CONEXION
            String url = ("jdbc:sqlite:inventario.db");
            conn = DriverManager.getConnection(url);

            IO.println("CONEXION ESTABLECIDA");

            crearTablaInventario();

        } catch (Exception e) {
            IO.println("CONEXION FALLIDA: " + e.getMessage());
        }
    }

    private void crearTablaInventario() {
        String sql = """
                CREATE TABLE IF NOT EXISTS inventario (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT NOT NULL UNIQUE,
                descripcion TEXT,
                stock INTEGER NOT NULL DEFAULT 0,
                precio REAL NOT NULL DEFAULT 0.0
                );
                """;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            IO.println("Error al crear la tabla: " + e.getMessage());
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConn() {
        return conn;
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                IO.println("CONEXION CERRADA");
            }
        } catch (SQLException e) {
            IO.println("ERROR AL CERRAR BBDD: " + e.getMessage());
        }
    }

}
