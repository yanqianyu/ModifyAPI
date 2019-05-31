import org.eclipse.jdt.core.dom.CompilationUnit;
import java.io.*;
import java.lang.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class Main {

    public static void main(String[] args){
        // 链接数据库
        Connection conn = null;
        String driver = "com.mysql.cj.jdbc.Driver";

        String url = "jdbc:mysql://localhost:3306/githubreposfile?serverTimezone=UTC";
        String user = "root";
        String password = "Taylorswift-1997";
        String readSQL = "SELECT id, rawcode FROM reposFile";

        // String sql = "insert into reposFile (`methName`, `tokens`, `comments`, `rawcode`, `apiseq`, `ast`) values (?,?,?,?,?,?)" ;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            PreparedStatement pstmst = null;
            pstmst = conn.prepareStatement(readSQL);
            ResultSet rs = null;
            InputStream is = null;
            InputStream id = null;

            rs = pstmst.executeQuery();
            if(rs.next()){
                is = rs.getBinaryStream("rawcode");
                id = rs.getBinaryStream("id");
                ByteArrayInputStream msg = (ByteArrayInputStream)rs.getBinaryStream("rawcode");
                String rawcode = "";
                byte[] byte_data = new byte[msg.available()];
                msg.read(byte_data, 0, byte_data.length);
                rawcode = new String(byte_data);
                CompilationUnit cu = JdtAstUtil.getCompilationUnit(rawcode);

                String updateSQL = "update reposFile set apiseq='" + "' , set newapiseq = '" + ",where id=" + id;

                MyVisitor myVisitor = new MyVisitor(conn, updateSQL);
                cu.accept(myVisitor);
            }
            // System.out.println("Already handled files: "  + (this.succes.getAndIncrement()+1));

        } catch (Exception e) {
            // System.err.println(path);
            // System.out.println("Already failed filess: " + (this.failed.getAndIncrement()+1));
            e.printStackTrace();
        }

    }
}