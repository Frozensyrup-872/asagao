import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Main {

    public static void main(String[] args) {

        try {

            Connection conn = DriverManager.getConnection(
                    "jdbc:sqlite:asagao.db"
            );

            Statement stmt = conn.createStatement();

            String sql = """
                    CREATE TABLE IF NOT EXISTS plants (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        growth REAL,
                        water INTEGER,
                        day INTEGER,
                        season TEXT,
                        weather TEXT
                    );
                    """;

            stmt.execute(sql);

            System.out.println("テーブル作成成功");

            conn.close();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}