import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.util.Random;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class AsagaoGameGUI extends JFrame {

    CardLayout cardLayout = new CardLayout();
    JPanel mainPanel = new JPanel(cardLayout);

    double growth = 0;
    int water = 50;
    int day = 1;
    int noWaterDays = 0;

    int lastWater = 50;
    double lastGrowthRate = 1.0;

    boolean isDead = false;
    boolean isBloomed = false;
    boolean showWaterMessage = false;

    LocalDate date = LocalDate.of(2026, 4, 1);
    Random random = new Random();

    String currentWeather = "晴れ";

    JLabel imageLabel;
    JLabel statusLabel;

    JButton waterButton;
    JButton skipButton;
    JButton resetButton;

    public AsagaoGameGUI() {

        
        setTitle("あさがお育成ゲーム");
setSize(900, 520);
setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
setExtendedState(JFrame.MAXIMIZED_BOTH);

        createStartScreen();
        createGameScreen();

        loadGame();
        updateScreen();

        add(mainPanel);

        cardLayout.show(mainPanel, "START");

        setVisible(true);
    }

    void createStartScreen() {

    JPanel startPanel = new JPanel() {

        Image background = new ImageIcon("start.jpg").getImage();

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    };

    startPanel.setLayout(null);

    JButton startButton = new JButton();

    startButton.setOpaque(false);
    startButton.setContentAreaFilled(false);
    startButton.setBorderPainted(false);
    startButton.setFocusPainted(false);

    startButton.addActionListener(e -> {
        cardLayout.show(mainPanel, "GAME");
    });

    startPanel.add(startButton);

    startPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
        @Override
        public void componentResized(java.awt.event.ComponentEvent e) {

            int w = startPanel.getWidth();
            int h = startPanel.getHeight();

            startButton.setBounds(
                    (int)(w * 0.35),
                    (int)(h * 0.448),
                    (int)(w * 0.32),
                    (int)(h * 0.21)
            );
        }
    });

    mainPanel.add(startPanel, "START");
}

    void createGameScreen() {

        JPanel gamePanel = new JPanel(new BorderLayout());

        statusLabel = new JLabel();
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);

        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);

        waterButton = new JButton(new ImageIcon("waterIcon.png"));
        skipButton = new JButton("日付を進める");
        resetButton = new JButton("リセット");

        waterButton.addActionListener(e -> {

            water += 5;

            showWaterMessage = true;



            updateScreen();

            saveGame();
        });
        

        skipButton.addActionListener(e -> {

           
           day++;

           date = date.plusDays(1);
           

           currentWeather = getWeather();

           

           passOneDay();

           showWaterMessage = false;

           

           updateScreen();
           saveGame();
        });

        resetButton.addActionListener(e -> {

    growth = 0;
    water = 50;
    day = 1;

    noWaterDays = 0;

    lastWater = 50;
    lastGrowthRate = 1.0;

    isDead = false;
    isBloomed = false;

    currentWeather = "晴れ";

    date = LocalDate.of(2026, 4, 1);

    saveGame();

    updateScreen();
});

        JPanel buttonPanel = new JPanel();

        buttonPanel.add(waterButton);
        buttonPanel.add(skipButton);
        buttonPanel.add(resetButton);

        gamePanel.add(statusLabel, BorderLayout.NORTH);
        gamePanel.add(imageLabel, BorderLayout.CENTER);
        gamePanel.add(buttonPanel, BorderLayout.SOUTH);

        mainPanel.add(gamePanel, "GAME");

        updateScreen();
    }

    void updateScreen() {

        statusLabel.setText(
                getSeason() + " " + getSeasonDay() + "日目"
                        + "　天気：" + currentWeather
                        + "　成長段階：" + getGrowthStage()
                        + "　成長：" + String.format("%.1f", growth) + "%"
                        + "　水分：" + water + "%"
                        + "　判定水分：" + lastWater + "%"
                        + "　成長率：" + String.format("%.1f", lastGrowthRate)
        );

        if (showWaterMessage) {

            imageLabel.setIcon(new ImageIcon("watered.jpg"));

        } else {

            String imageName = getGrowthImage();

            imageLabel.setIcon(new ImageIcon(imageName));
        }

        if (isBloomed) {

            JOptionPane.showMessageDialog(this,
                    "あさがおの花が咲きました！");

            waterButton.setEnabled(false);
            skipButton.setEnabled(false);
        }

        if (isDead) {

            JOptionPane.showMessageDialog(this,
                    "あさがおは枯れてしまいました……");

            waterButton.setEnabled(false);
            skipButton.setEnabled(false);
        }
    }
    void saveGame() {
    try {
        Connection conn = DriverManager.getConnection("jdbc:sqlite:asagao.db");

        String sql = """
                UPDATE asagao_status
                SET day = ?,
                    date = ?,
                    growth = ?,
                    water = ?,
                    weather = ?,
                    is_dead = ?,
                    is_bloomed = ?
                WHERE id = 1;
                """;

        PreparedStatement pstmt = conn.prepareStatement(sql);

        pstmt.setInt(1, day);
        pstmt.setString(2, date.toString());
        pstmt.setDouble(3, growth);
        pstmt.setInt(4, water);
        pstmt.setString(5, currentWeather);
        pstmt.setInt(6, isDead ? 1 : 0);
        pstmt.setInt(7, isBloomed ? 1 : 0);

        pstmt.executeUpdate();

        pstmt.close();
        conn.close();

        System.out.println("セーブしました");

    } catch (Exception e) {
        e.printStackTrace();
    }
}

void loadGame() {

    try {

        Connection conn = DriverManager.getConnection(
                "jdbc:sqlite:asagao.db"
        );

        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery(
                "SELECT * FROM asagao_status WHERE id = 1"
        );

        if (rs.next()) {

            day = rs.getInt("day");

            date = LocalDate.parse(
                    rs.getString("date")
            );

            growth = rs.getDouble("growth");

            water = rs.getInt("water");

            currentWeather = rs.getString("weather");

            isDead = rs.getInt("is_dead") == 1;

            isBloomed = rs.getInt("is_bloomed") == 1;
        }

        rs.close();
        stmt.close();
        conn.close();

        System.out.println("ロード成功");

    } catch (Exception e) {

        e.printStackTrace();
    }
}


    void passOneDay() {

        if (currentWeather.equals("雨")) {
            water += 25;
        }

        noWaterDays++;

        if (noWaterDays == 1) {
            water -= 5;
        } else if (noWaterDays == 2) {
            water -= 10;
        } else {
            water -= 20;
        }

        if (water > 100) {
            water = 100;
        }

        if (water < 0) {
            water = 0;
        }

        growth += lastGrowthRate;

        if (growth < 0) {
            growth = 0;
        }

        if (water <= 0) {
            isDead = true;
        }

        if (growth >= 100) {
            growth = 100;
            isBloomed = true;
        }
    }

    double getWaterGrowthRate(int targetWater) {

        int difference = targetWater - 50;

        if (difference == 0) {
            return 1.0;
        }

        if (difference > 0) {
            return 1.0 - (difference / 10) * 0.1;
        } else {
            return 1.0 - (Math.abs(difference) / 10) * 0.2;
        }
    }

    String getWeather() {

        int number = random.nextInt(100);

        String season = getSeason();

        if (season.equals("春")) {

            if (number < 45) {
                return "晴れ";
            } else if (number < 75) {
                return "曇り";
            } else {
                return "雨";
            }

        } else if (season.equals("夏")) {

            if (number < 60) {
                return "晴れ";
            } else if (number < 80) {
                return "曇り";
            } else {
                return "雨";
            }

        } else if (season.equals("秋")) {

            if (number < 40) {
                return "晴れ";
            } else if (number < 75) {
                return "曇り";
            } else {
                return "雨";
            }

        } else {

            if (number < 30) {
                return "晴れ";
            } else if (number < 75) {
                return "曇り";
            } else {
                return "雨";
            }
        }
    }

    String getSeason() {

        int seasonCycle = ((day - 1) / 30) % 4;

        if (seasonCycle == 0) {
            return "春";
        } else if (seasonCycle == 1) {
            return "夏";
        } else if (seasonCycle == 2) {
            return "秋";
        } else {
            return "冬";
        }
    }

    int getSeasonDay() {
        return ((day - 1) % 30) + 1;
    }

    String getGrowthStage() {

        if (growth < 10) {
            return "種";
        } else if (growth < 20) {
            return "発芽";
        } else if (growth < 30) {
            return "双葉";
        } else if (growth < 40) {
            return "小さい芽";
        } else if (growth < 50) {
            return "成長中";
        } else if (growth < 60) {
            return "つる成長";
        } else if (growth < 70) {
            return "葉が増える";
        } else if (growth < 80) {
            return "つぼみ準備";
        } else if (growth < 90) {
            return "つぼみ";
        } else if (growth < 100) {
            return "開花";
        } else {
            return "満開";
        }
    }

    String getGrowthImage() {

        if (growth < 10) {
            return "朝顔/1.png";
        } else if (growth < 20) {
            return "朝顔/2.png";
        } else if (growth < 30) {
            return "朝顔/3.png";
        } else if (growth < 40) {
            return "朝顔/4.png";
        } else if (growth < 50) {
            return "朝顔/5.png";
        } else if (growth < 60) {
            return "朝顔/6.png";
        } else if (growth < 70) {
            return "朝顔/7.png";
        } else if (growth < 80) {
            return "朝顔/8.png";
        } else if (growth < 90) {
            return "朝顔/9.png";
        } else if (growth < 100) {
            return "朝顔/10.png";
        } else {
            return "fullflower.png";
        }
    }

    public static void main(String[] args) {
        new AsagaoGameGUI();
    }
}