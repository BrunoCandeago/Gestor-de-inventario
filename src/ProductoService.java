import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoService {

    public void addProducto(Producto p) {
        String sql = "INSERT INTO inventario (nombre, descripcion, stock, precio) VALUES (?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getInstance().getConn();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getNombre());
            stmt.setString(2, p.getDescripcion());
            stmt.setInt(3, p.getStock());
            stmt.setBigDecimal(4, p.getPrecio());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                IO.println("Error: No se pudo crear el producto.");
                return;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    p.setId(generatedId);
                    IO.println("Producto creado con ID: " + generatedId);
                } else {
                    IO.println("Error: Se creó el producto pero no se pudo obtener el ID.");
                }
            }


        } catch (SQLException e) {
            IO.println("Error al ejecutar INSERT: " + e.getMessage());
        }
    }


    public List<Producto> getAllProductos() {
        List<Producto> productos = new ArrayList<>();

        String sql = "SELECT * FROM inventario";

        Connection conn = DatabaseConnection.getInstance().getConn();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Producto producto = new Producto();
                producto.setId(rs.getInt("id"));
                producto.setNombre(rs.getString("nombre"));
                producto.setDescripcion(rs.getString("descripcion"));
                producto.setStock(rs.getInt("stock"));
                producto.setPrecio(rs.getBigDecimal("precio"));

                productos.add(producto);
            }

        } catch (SQLException e) {
            IO.println("Error al ejecutar SELECT *: " + e.getMessage());
        }
        return productos;
    }

    public void updateProductoInfo(Producto p) {
        String sql = "UPDATE inventario SET nombre = ?, descripcion = ?, stock = ?, precio = ? WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConn();

        int affectedRows;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setString(2, p.getDescripcion());
            stmt.setInt(3, p.getStock());
            stmt.setBigDecimal(4, p.getPrecio());
            stmt.setInt(5, p.getId());

            affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                IO.println("Producto con ID " + p.getId() + " actualizado.");
            } else {
                IO.println("No se encontró producto con ID " + p.getId() + " para actualizar.");
            }
        } catch (SQLException e) {
            IO.println("Error al ejecutar UPDATE: " + e.getMessage());
        }
    }

    public void deleteProducto(int id) {
        String sql = "DELETE FROM inventario WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConn();

        int affectedRows;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);

            affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                IO.println("Producto con ID " + id + " eliminado.");
            } else {
                IO.println("No se encontró producto con ID " + id + " para eliminar.");
            }

        } catch (SQLException e) {
            IO.println("Error al ejecutar DELETE: " + e.getMessage());
        }
    }

    public boolean ajustarStock(int id, int cantidad) {
        String sql = "UPDATE inventario SET stock = stock + ? WHERE id = ? AND (stock + ?) >= 0";

        Connection conn = DatabaseConnection.getInstance().getConn();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, cantidad);
            stmt.setInt(2, id);
            stmt.setInt(3, cantidad);

            int affectedRows = stmt.executeUpdate();

            return affectedRows > 0;
        } catch (SQLException e) {
            IO.println("Error al ejecutar ajustarStock: " + e.getMessage());
            return false;
        }
    }
}
