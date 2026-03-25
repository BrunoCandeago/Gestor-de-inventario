import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;

public class VentanaPrincipal extends JFrame {

    // --- 1. Componentes Visuales ---
    private JTable tablaProductos;
    private DefaultTableModel tableModel;

    // Panel de Formulario
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JSpinner spinnerStock; // Usamos JSpinner para números
    private JTextField txtPrecio; // Usamos JTextField y lo validamos
    private JButton btnAgregar;
    private JButton btnActualizar;
    private JButton btnEliminar;
    private JButton btnLimpiar; // Botón extra para limpiar el formulario

    // Panel de Ajuste de Stock
    private JSpinner spinnerCantidad; // Para sumar o restar
    private JButton btnIngresar;
    private JButton btnRetirar;

    // --- 2. Lógica y Datos ---
    private final ProductoService productoService;
    private Producto productoSeleccionado = null; // Para saber qué producto editar/eliminar

    public VentanaPrincipal() {
        this.productoService = new ProductoService();

        setTitle("Gestor de Inventario 🚀");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inicializar y organizar los componentes visuales
        initUI();

        // Conectar los botones a sus funciones
        initListeners();

        // --- CAMBIO IMPORTANTE 2: Cerrar conexión al salir ---
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 1. Cerramos la conexión a la BD de forma segura
                DatabaseConnection.getInstance().closeConnection();

                // 2. Cerramos la ventana y terminamos el proceso
                dispose();
                System.exit(0);
            }
        });

        // Cargar los datos de la BD en la tabla al iniciar
        cargarDatosTabla();
    }

    /**
     * Inicializa y organiza todos los componentes visuales (Layout)
     */
    private void initUI() {
        // Usamos BorderLayout como layout principal de la ventana
        setLayout(new BorderLayout(10, 10)); // 10px de espacio horizontal y vertical

        // --- PANEL CENTRAL: La Tabla ---
        String[] columnas = {"ID", "Nombre", "Descripción", "Stock", "Precio"};
        tableModel = new DefaultTableModel(columnas, 0) {
            // Hacemos que las celdas de la tabla no sean editables
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaProductos = new JTable(tableModel);

        // Añadimos la tabla a un JScrollPane (para poder hacer scroll)
        JScrollPane scrollPane = new JScrollPane(tablaProductos);
        add(scrollPane, BorderLayout.CENTER);

        // --- PANEL DERECHO: Formularios y Botones ---
        JPanel panelDerecho = new JPanel();
        // Usamos BoxLayout para apilar los paneles verticalmente
        panelDerecho.setLayout(new BoxLayout(panelDerecho, BoxLayout.Y_AXIS));
        panelDerecho.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen

        // --- Sub-panel 1: Formulario de Datos ---
        JPanel panelFormulario = new JPanel(new GridLayout(0, 2, 5, 5)); // 0 filas, 2 cols
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Producto"));

        txtNombre = new JTextField(20);
        txtDescripcion = new JTextField(20);
        // Modelo para el Spinner de Stock (min 0, max 9999, paso 1)
        spinnerStock = new JSpinner(new SpinnerNumberModel(0, 0, 9999, 1));
        txtPrecio = new JTextField(20);

        panelFormulario.add(new JLabel("Nombre:"));
        panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel("Descripción:"));
        panelFormulario.add(txtDescripcion);
        panelFormulario.add(new JLabel("Stock Inicial:"));
        panelFormulario.add(spinnerStock);
        panelFormulario.add(new JLabel("Precio:"));
        panelFormulario.add(txtPrecio);

        // --- Sub-panel 2: Botones CRUD ---
        JPanel panelBotonesCRUD = new JPanel(new FlowLayout());
        btnAgregar = new JButton("Agregar Producto");
        btnActualizar = new JButton("Actualizar Info");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        panelBotonesCRUD.add(btnAgregar);
        panelBotonesCRUD.add(btnActualizar);
        panelBotonesCRUD.add(btnEliminar);
        panelBotonesCRUD.add(btnLimpiar);

        // --- Sub-panel 3: Ajuste de Stock ---
        JPanel panelStock = new JPanel();
        panelStock.setBorder(BorderFactory.createTitledBorder("Ajustar Stock"));
        spinnerCantidad = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1)); // min 1
        btnIngresar = new JButton("Ingresar");
        btnRetirar = new JButton("Retirar");

        panelStock.add(new JLabel("Cantidad:"));
        panelStock.add(spinnerCantidad);
        panelStock.add(btnIngresar);
        panelStock.add(btnRetirar);

        // --- Deshabilitar botones que requieren selección ---
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnIngresar.setEnabled(false);
        btnRetirar.setEnabled(false);

        // Añadir todos los sub-paneles al panel derecho
        panelDerecho.add(panelFormulario);
        panelDerecho.add(panelBotonesCRUD);
        panelDerecho.add(panelStock);

        // Añadir el panel derecho a la ventana
        add(panelDerecho, BorderLayout.EAST);
    }

    /**
     * Conecta todos los listeners (eventos de clic) a sus funciones
     */
    private void initListeners() {

        // --- Listener de la Tabla ---
        // Se activa cuando el usuario selecciona una fila
        tablaProductos.getSelectionModel().addListSelectionListener(e -> {
            // e.getValueIsAdjusting() evita que el evento se dispare dos veces
            if (!e.getValueIsAdjusting() && tablaProductos.getSelectedRow() != -1) {
                // Hay una fila seleccionada
                int filaSeleccionada = tablaProductos.getSelectedRow();

                // Creamos un objeto Producto con los datos de la fila
                // OJO: Los tipos de datos deben coincidir con tu TableModel
                productoSeleccionado = new Producto();
                productoSeleccionado.setId((Integer) tableModel.getValueAt(filaSeleccionada, 0));
                productoSeleccionado.setNombre((String) tableModel.getValueAt(filaSeleccionada, 1));
                productoSeleccionado.setDescripcion((String) tableModel.getValueAt(filaSeleccionada, 2));
                productoSeleccionado.setStock((Integer) tableModel.getValueAt(filaSeleccionada, 3));
                productoSeleccionado.setPrecio((BigDecimal) tableModel.getValueAt(filaSeleccionada, 4));

                // Llenar el formulario con los datos del producto
                txtNombre.setText(productoSeleccionado.getNombre());
                txtDescripcion.setText(productoSeleccionado.getDescripcion());
                spinnerStock.setValue(productoSeleccionado.getStock());
                txtPrecio.setText(productoSeleccionado.getPrecio().toString());

                // Habilitar botones
                btnActualizar.setEnabled(true);
                btnEliminar.setEnabled(true);
                btnIngresar.setEnabled(true);
                btnRetirar.setEnabled(true);
                btnAgregar.setEnabled(false); // Deshabilitar "Agregar"
            }
        });

        // --- Listeners de Botones ---

        btnLimpiar.addActionListener(_ -> limpiarFormulario());

        btnAgregar.addActionListener(_ -> agregarProducto());

        btnActualizar.addActionListener(_ -> actualizarProducto());

        btnEliminar.addActionListener(_ -> eliminarProducto());

        btnIngresar.addActionListener(_ -> ingresarStock());

        btnRetirar.addActionListener(_ -> retirarStock());
    }

    // --- 3. Métodos de Lógica (Acciones) ---

    /**
     * Carga/Refresca todos los productos de la BD en la JTable
     */
    private void cargarDatosTabla() {
        // Limpiar la tabla
        tableModel.setRowCount(0);

        // Obtener la lista de productos
        List<Producto> productos = productoService.getAllProductos();

        // Llenar la tabla
        for (Producto p : productos) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getNombre(),
                    p.getDescripcion(),
                    p.getStock(),
                    p.getPrecio()
            });
        }
    }

    /**
     * Limpia el formulario y la selección
     */
    private void limpiarFormulario() {
        txtNombre.setText("");
        txtDescripcion.setText("");
        txtPrecio.setText("");
        spinnerStock.setValue(0);
        productoSeleccionado = null;

        tablaProductos.clearSelection(); // Quitar selección de la tabla

        // Reiniciar botones
        btnActualizar.setEnabled(false);
        btnEliminar.setEnabled(false);
        btnIngresar.setEnabled(false);
        btnRetirar.setEnabled(false);
        btnAgregar.setEnabled(true); // Habilitar "Agregar"
    }

    /**
     * Valida y agrega un nuevo producto a la BD
     */
    private void agregarProducto() {
        // Validar datos
        String nombre = txtNombre.getText();
        String precioStr = txtPrecio.getText();
        if (nombre.isEmpty() || precioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y Precio son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BigDecimal precio = new BigDecimal(precioStr); // Validar que el precio sea un número
            int stock = (int) spinnerStock.getValue();
            String descripcion = txtDescripcion.getText();

            // Crear el objeto producto
            Producto p = new Producto();
            p.setNombre(nombre);
            p.setDescripcion(descripcion);
            p.setStock(stock);
            p.setPrecio(precio);

            // Llamar a la fachada
            productoService.addProducto(p);

            // Informar al usuario y refrescar
            JOptionPane.showMessageDialog(this, "Producto agregado con éxito (ID: " + p.getId() + ")", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarDatosTabla();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido (ej: 120.50).", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            // Esto atraparía un UNIQUE constraint (nombre duplicado)
            JOptionPane.showMessageDialog(this, "Error al agregar el producto: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Valida y actualiza la info de un producto seleccionado
     */
    private void actualizarProducto() {
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un producto de la tabla.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validar datos
        String nombre = txtNombre.getText();
        String precioStr = txtPrecio.getText();
        if (nombre.isEmpty() || precioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y Precio son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // Actualizar el objeto 'productoSeleccionado' con los datos del formulario
            productoSeleccionado.setNombre(nombre);
            productoSeleccionado.setDescripcion(txtDescripcion.getText());
            productoSeleccionado.setStock((int) spinnerStock.getValue());
            productoSeleccionado.setPrecio(new BigDecimal(precioStr));

            // Llamar a la fachada
            productoService.updateProductoInfo(productoSeleccionado);

            // Informar y refrescar
            JOptionPane.showMessageDialog(this, "Producto actualizado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarDatosTabla();
            limpiarFormulario();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El precio debe ser un número válido (ej: 120.50).", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina el producto seleccionado
     */
    private void eliminarProducto() {
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un producto de la tabla.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Pedir confirmación
        int respuesta = JOptionPane.showConfirmDialog(
                this,
                "¿Está seguro de que desea eliminar el producto '" + productoSeleccionado.getNombre() + "'?",
                "Confirmar Eliminación",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (respuesta == JOptionPane.YES_OPTION) {
            try {
                productoService.deleteProducto(productoSeleccionado.getId());
                JOptionPane.showMessageDialog(this, "Producto eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatosTabla();
                limpiarFormulario();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Ingresa stock al producto seleccionado
     */
    private void ingresarStock() {
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un producto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cantidad = (int) spinnerCantidad.getValue();

        boolean exito = productoService.ajustarStock(productoSeleccionado.getId(), cantidad);

        if (exito) {
            JOptionPane.showMessageDialog(this, "Stock agregado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarDatosTabla();
            limpiarFormulario();
        } else {
            // Esto no debería pasar al sumar, pero es buena práctica
            JOptionPane.showMessageDialog(this, "No se pudo agregar el stock.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Retira stock del producto seleccionado
     */
    private void retirarStock() {
        if (productoSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un producto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cantidad = (int) spinnerCantidad.getValue();
        // Pasamos la cantidad como negativa
        boolean exito = productoService.ajustarStock(productoSeleccionado.getId(), -cantidad);

        if (exito) {
            JOptionPane.showMessageDialog(this, "Stock retirado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            cargarDatosTabla();
            limpiarFormulario();
        } else {
            // Aquí es donde la validación de la BD (stock >= 0) actúa
            JOptionPane.showMessageDialog(this, "No se pudo retirar. Stock insuficiente.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    // --- 4. Método Main ---
    /**
     * El punto de entrada para ejecutar la aplicación
     */
    static void main() {
        // Aseguramos que la UI de Swing se ejecute en su propio hilo
        SwingUtilities.invokeLater(() -> {
            // Creamos una instancia de nuestra ventana y la hacemos visible
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}