package dam.jmdh;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCResultSetActualizable {

  public static void muestraErrorSQL(SQLException e) {
    System.out.printf("SQL ERROR mensaje: %s\n", e.getMessage());
    System.out.printf("SQL Estado: %s\n", e.getSQLState());
    System.out.printf("SQL código específico: %s\n", e.getErrorCode());
  }

  public static void main(String[] args) {

    String basedatos = "acceso_datos";
    String host = "localhost";
    String port = "3306";
    String parAdic = "";
    String urlConnection = "jdbc:mysql://" + host + ":" + port + "/" + basedatos + parAdic;
    String user = "root";
    String pwd = "12345";

    try (Connection c = DriverManager.getConnection(urlConnection, user, pwd)) {
      boolean autoComm = c.getAutoCommit();  // para restaurarlo después

      try (
              // Resultset.CONCUR_ UPDATABLE:el ResultSet es actualizable, es decir, los cambios
              //realizados en él se pueden grabar en la base de datos.
           Statement sConsulta = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
           ResultSet rs = sConsulta.executeQuery("select * from clientes where cp is not null");
      ) {

        c.setAutoCommit(false);

        // Modifica último cliente
        rs.last();
        rs.updateString("cp", "02568");
        rs.updateRow();
        System.out.printf("Modificado cliente con DNI: %s.\n", rs.getString("dni"));

        // Borra penúltimo cliente
        rs.previous();
        System.out.printf("Borrado cliente con DNI: %s.\n", rs.getString("dni"));
        rs.deleteRow();

        // Inserta nuevo cliente
        rs.moveToInsertRow();
        rs.updateString("dni", "24862486S");
        rs.updateString("apellidos", "ZURITA");
        rs.updateString("cp", "33983");
        rs.insertRow();

        c.commit();

      } catch (Exception ex) {
        if (ex instanceof SQLException) {
          muestraErrorSQL((SQLException) ex);
        } else {
          System.out.printf("ERROR: %s\n", ex.getMessage());
        }

        try {
          c.rollback();
          System.out.println("Se hace  ROLLBACK");
        } catch (SQLException exr) {
          System.out.println("ERROR haciendo ROLLBACK");
          muestraErrorSQL(exr);
        }
      } finally {
        c.setAutoCommit(autoComm);  // Restaurar modo autocommmit anterior
      }
    } catch (SQLException ex) {
      System.out.printf("ERROR de conexión");
      muestraErrorSQL(ex);
    }

  }
}
