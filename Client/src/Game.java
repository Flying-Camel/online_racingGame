import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.text.DecimalFormat;

public class Game extends JFrame implements KeyListener, Runnable {
	
	int checkCrush=0;
	float n_time = 0;
	float nn_time = 0;
	
	
	static int Game_State = 0; // 0 = 기본상태 , 1 = 레디상태 , 2 = 게임시작 상태
	static int Player_num; // 내 플레이어 번호

	boolean player_1 = true;
	boolean player_2 = true; // 플레이어가 있는지 없는지를 체크 해주는 boolean
	boolean player_3 = true;
	
	boolean exit_checker = true;
	boolean ready = false; // ready 상태 판별

	int[] coin_array = { 0, 1, 3, 2, 1, 1, 3, 2, 3, 1, 0, 1, 0, 3, 2, 1, 2, 3, 0, 1, 3, 2, 3, 1, 2, 2, 3, 2, 3, 1, 1, 2,
			3, 0, 1, 0, 1, 3, 2, 1, 0, 1, 0, 3, 2, 3, 1, 2, 0, 3 };
	int coin_p = 0;
	int[] corn_array = { 1, 3, 2, 0, 3, 2, 1, 0, 2, 0, 1, 2, 3, 2, 3, 0, 1, 2, 1, 3, 0, 3, 2, 0, 3, 0, 1, 3, 2, 0, 3, 1,
			2, 3, 0, 3, 2, 1, 0, 3, 1, 2, 1, 3, 0, 1, 0, 1, 3, 0 };

	int corn_p = 0;

	static String player_ID = null;	//접속자 ID
	int player_score;				//접속자 점수
	int player_health;				//접속자 체력
	int x, y; // 캐릭터의 좌표 변수		//접속자 좌표

	String p1_ID;	//player 1 ID
	int p1_score = 0;	//player 1 점수
	String p2_ID;	//player 2 ID
	int p2_x;		//player 2 x좌표
	int p2_y;		//player 2 y좌표
	int p2_score = 0;

	String p3_ID;	//player 3 ID
	int p3_x;		//player 3 x좌표
	int p3_y;		//player 3 y좌표
	int p3_score = 0;

	float time;		//시간
	int f_width;	//x길이
	int f_height;	//y길이

	int bx = 0; // 도로 스크롤 변수
	int cx = 0; // 도로 스크롤 변수2

	int my_w, my_h; // 내 자동차
	int c_w, c_h; // coin
	int cr_w, cr_h; // corn

	boolean KeyUp = false; // 키보드 입력 처리를 위한 변수
	boolean KeyDown = false;
	boolean KeyLeft = false;
	boolean KeyRight = false;

	int coin_cnt;	//터널 
	int corn_cnt;	//방지턱
	int road_cnt;	//도로 흰선
	boolean dup_state = true;	//중복여부 확인

	Thread th; // 스레드 생성
	Toolkit tk = Toolkit.getDefaultToolkit();

	Image me_img1;// = tk.getImage("1번자동차.png");
	Image me_img2;// = tk.getImage("2번자동차.png");
	Image me_img3;// = tk.getImage("3번자동차.png");

	Image Heart_img;
	Image Corn_img;
	Image Coin_img;
	Image Road_img;
	Image title_img;
	Image Start_img;
	Image ready_img;
	Image damage_img;
	
	Image buffImage;
	Graphics buffg;
	Image road;

	ArrayList<Coin> Coin_List = new ArrayList<Coin>();	//터널 좌표 배열
	ArrayList<Corn> Corn_List = new ArrayList<Corn>();	//방지턱 좌표 배열
	ArrayList<Score> Score_List = new ArrayList<Score>();	//플레이어가 터널이미지와 부딪쳤을 때 +10 보일 좌표 배열

	Coin en;		//coin class
	Corn cn;		//corn class
	Score sc;		//score class

	int corn_loc;
	int coin_loc;

	JFrame frame = new JFrame("");
	JButton ReadyBtn = new JButton("준 비");
	JButton QuitBtn = new JButton("Quit");

	static InetAddress ia = null;
	static Socket sock = null;
	PrintWriter out = null;
	BufferedReader in = null;

	public static void main(String[] args) throws IOException {

		// ia = InetAddress.getByName("192.168.43.67"); // 주소값 필요
		ia = InetAddress.getByName("203.253.22.106");
		sock = new Socket(ia, 9999);

		while (true) {
			if (Game_State == 0) {
				player_ID = JOptionPane.showInputDialog(null, "ID를 입력하세요", "5자이내로 입력");
				if (player_ID == null)
					System.exit(1);
				while (player_ID.equals("") || player_ID.length() > 5) {
					player_ID = JOptionPane.showInputDialog(null, "ID를 재입력하세요", "5자이내로 입력");
					if (player_ID == null)
						System.exit(1);
				}
				Game_State = 1;
			} else if (Game_State == 1) {
				Game g = new Game();
				break;
			}
		}

	}

	public Game() throws IOException { // Constructor

		out = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
		in = new BufferedReader(new InputStreamReader(sock.getInputStream()));

		init();
		setTitle("레이싱 게임 Project");
		setSize(f_width, f_height);
		setResizable(false);

		Dimension screen = tk.getScreenSize();
		int f_xpos = (int) (screen.getWidth() / 2 - f_width / 2);
		int f_ypos = (int) (screen.getHeight() / 2 - f_height / 2);
		start();
		
		setLocation(f_xpos, f_ypos);
		setVisible(true);
	}

	public void init() { // 초기화가 필요한 부분들을 초기화 시킴
		x = 150; // 캐릭터의 최초 좌표.
		y = 480;
		p2_x = 280;
		p2_y = 480;
		p3_x = 430;
		p3_y = 480;

		f_width = 750;
		f_height = 600;
		time = 90;
		player_score = 0;
		player_health = 5;

		Coin_List = new ArrayList<Coin>();
		Corn_List = new ArrayList<Corn>();

		road = tk.getImage("도로.png");
		Coin_img = new ImageIcon("터널.png").getImage();
		Corn_img = new ImageIcon("방지턱.png").getImage();
		Road_img = new ImageIcon("라인.png").getImage();
		Heart_img = new ImageIcon("하트.png").getImage();
		title_img = new ImageIcon("타이틀.jpg").getImage();
		Start_img = new ImageIcon("Start.png").getImage();
		ready_img = new ImageIcon("레디.png").getImage();
		damage_img = new ImageIcon("해골.PNG").getImage();
	}

	public void start() {
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addKeyListener(this);	 // 키보드 이벤트 실행
		th = new Thread(this);	 // 스레드 생성
		th.start();				 // 스레드 실행
	}

	public void run() { // 스레드가 무한 루프될 부분

		try { 					// 예외옵션 설정으로 에러 방지
			int check_Button = 0;
			int wait_check = 0;
			int btn_check = 0;
			Runnable run1 = new GetReady();
			Thread t1 = new Thread(run1);
			t1.start();

			add(ReadyBtn);		//레디     버튼
			add(QuitBtn);		//Quit 버튼

			while (true) { // while 문으로 무한 루프 시키기
				if (Game_State == 1) {	//레디 누르기 전 상태
					if(btn_check == 0){
						add(ReadyBtn);
						add(QuitBtn);
						btn_check++;
					}
					time = 70;
					ReadyProcess();
					QuitProcess();
					if (wait_check >= 1) {
						wait_check = 0;
					}
					ReadyBtn.setText("준비");
				}
				if (Game_State == 2) {	//레디 누른 상태
					if (wait_check == 0) {
						time = 70;
						Runnable run4 = new Waiting();
						Thread t3 = new Thread(run4);
						t3.start();
						ReadyBtn.setText("준비완료");
						System.out.println("준비중 state 2");
						wait_check++;
						remove(ReadyBtn);
						remove(QuitBtn);
						btn_check = 0;
						repaint();
					}
					requestFocusInWindow();
					//ReadyProcess();
					//QuitProcess();
					check_Button = 0;
				}
				if (Game_State == 3) {	//게임 시작 상태 
					if (check_Button == 0) {
						requestFocusInWindow();
						if (Player_num == 1) {	// 처음 시작할떄 각 차량의 위치 초기화
							x = 150;
							y = 480;
							p2_x = 280;
							p2_y = 480;
							p3_x = 430;
							p3_y = 480;
							System.out.print("1번 옵션 실행");
						} else if (Player_num == 2) {
							p2_x = 150;
							p2_y = 480;
							x = 280;
							y = 480;
							p3_x = 430;
							p3_y = 480;
						} else if (Player_num == 3) {
							p2_x = 150;
							p2_y = 480;
							p3_x = 280;
							p3_y = 480;
							x = 430;
							y = 480;
						}
						
						Runnable run2 = new LocTest(p2_x, p2_y, p3_x, p3_y);
						Thread t2 = new Thread(run2);
						t2.start();

						Runnable run5 = new LocWrite();
						Thread t5 = new Thread(run5);
						t5.start();
						check_Button++;
					}

					KeyProcess(); // 키보드 입력처리를 하여 x,y 갱신
					CoinProcess();
					ScoreProcess();
					CornProcess();
					Draw_CoinScore();
					
					repaint(); // 갱신된 x,y값으로 이미지 새로 그리기
					
					if(time>40)// 시간이 지남에 따라 속도가 빨라짐
						Thread.sleep(10);
					else if(time<=40 && time >10)
						Thread.sleep(7);
					else 
						Thread.sleep(6);
					
					QuitProcess();
					// WriteScoreProcess();
					if (time != 0) {
						coin_cnt++;
						corn_cnt++;
					}
				}
			}
		} catch (Exception e) {
		}
	}

	
	public void ReadyProcess() { // 준비버튼 생성 및 준비 완료됨을 알려주는 부분
		ReadyBtn.setSize(100, 50);
		ReadyBtn.setLocation(640, 100);
		ReadyBtn.setVisible(true);
		ReadyBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				char[] temp = new char[10];
				if (Game_State == 1) {
					System.out.println("현재 state 1");
					ReadyBtn.setText("준비완료");
					Game_State = 2;

					String tmp = "READY|1\n";
					temp = tmp.toCharArray();
					out.print(temp);
					out.flush();
				}
			}
		});
	}

	public void QuitProcess() { // 종료버튼 생성 및 종료를 알려주는 부분
		// frame.add(QuitBtn);
		QuitBtn.setSize(100, 50);
		QuitBtn.setLocation(640, 500);
		QuitBtn.setVisible(true);
		QuitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (Game_State == 1 || Game_State == 2) {
					String tmp = "QUIT|" + player_ID + "\n";
					char[] c = new char[10];
					c = tmp.toCharArray();
					out.print(c);
					out.flush();
					System.exit(1);
				}
			}
		});
	}

	public void WaitProcess() throws IOException {	//레디상태 스레드 실행
		Runnable run1 = new GetReady();
		Thread t4 = new Thread(run1);
		t4.start();
	}
/*
	public void WriteLocProcess() throws IOException { // 위치를 서버로 보내주는 부분
		String buf_x = "";
		String buf_y = "";
		buf_x = String.valueOf(x);
		buf_y = String.valueOf(y);
		out.println("LOC|" + buf_x + "|" + buf_y + "\n");
	}

	public void WriteScoreProcess() throws IOException { // Score를 서버로 보내주는 부분
		String coin = "";
		coin = String.valueOf(player_score);
		out.println("SCORE|" + coin + "\n");
	}*/

	public void CoinProcess() {			 // 터널 을 위치에 그려주고 부딪쳤을때의 처리를 해주는 부분
		for (int i = 0; i < Coin_List.size(); ++i) {
			en = (Coin) (Coin_List.get(i));
			en.move();
			if (en.y < -100) {
				Coin_List.remove(i);
			}
		}

		if (coin_cnt % 45 == 0 && time != 0) { // 루프 카운트 300회 마다
			coin_loc = coin_array[coin_p];
			coin_p++;
			if(coin_p == 50){
				coin_p = 0;
			}
			if (coin_loc == 0)
				en = new Coin(130, -100);
			else if (coin_loc == 1)
				en = new Coin(260, -100);
			else if (coin_loc == 2)
				en = new Coin(420, -100);
			else if (coin_loc == 3)
				en = new Coin(540, -100);
			Coin_List.add(en);
		}

	
		for (int j = 0; j < Coin_List.size(); ++j) {
			en = (Coin) Coin_List.get(j);
			if (Crash(x, y, en.x, en.y, me_img1, Coin_img)) {
				
				
				
				if (time > 0 && en.check) {
					player_score += 10;
					// Coin_List.remove(j);
					Sound("Coin.wav", false);

					sc = new Score(en.x + Coin_img.getWidth(null) / 2, en.y + Coin_img.getHeight(null) / 2);
					Score_List.add(sc);
					// System.out.println(sc.x + " " + sc.y);
				}
				en.check = false;
			}
		}
	}

	public void CornProcess() { 			// 방지턱 을 위치에 그려주고 부딪쳤을때의 처리를 해주는 부분
		for (int i = 0; i < Corn_List.size(); ++i) {
			cn = (Corn) (Corn_List.get(i));
			cn.move();
			if (cn.y < -100) {
				Corn_List.remove(i);
			}
		}

		if (corn_cnt % 45 == 0 && time != 0) {
			corn_loc = corn_array[corn_p];
			corn_p++;
			if(corn_p == 50){
				corn_p = 0;
			}
			if (corn_loc == 0)
				cn = new Corn(130, -100);
			else if (corn_loc == 1)
				cn = new Corn(260, -100);
			else if (corn_loc == 2)
				cn = new Corn(420, -100);
			else if (corn_loc == 3)
				cn = new Corn(540, -100);
			Corn_List.add(cn);
		}

		for (int j = 0; j < Corn_List.size(); ++j) {
			cn = (Corn) Corn_List.get(j);
			if (Crash(x, y, cn.x, cn.y, me_img1, Corn_img)) {

				checkCrush = 1;
				n_time = time;

				if (player_health > 0 && cn.check) {
					player_health--;
					player_score -= 10;
					y += 60;
				}
				//Sound("차충돌.WAV",false);
				cn.check = false;
			}
		}

		if (player_health == 0 && time != 0) { // 체력이 0이 됬을떄 Game Over를 알려준다.
			char[] buf3 = new char[5];
			String tmp = "END|\n";
			buf3 = tmp.toCharArray();
			out.print(buf3);
			out.flush();
			
			Game_State = 1;
			JOptionPane.showMessageDialog(null, "Player GAME OVER\n" + p1_ID + " : " + p1_score + "\n" + p2_ID
					+ " : " + p2_score + "\n" + p3_ID + " : " + p3_score + "\n");
		}
	}

	public void ScoreProcess() { // Score_List 를 처리해주는 부분
		for (int i = 0; i < Score_List.size(); ++i) {
			sc = (Score) Score_List.get(i);
			sc.effect();
		}
	}

	public boolean Crash(int x1, int y1, int x2, int y2, Image img1, Image img2) { // 그림간에 부딪쳤을때를 계산해주는 함수

		boolean check = false;

		if (Math.abs((x1 + img1.getWidth(null) / 2) - (x2 + img2.getWidth(null) / 2)) < (img2.getWidth(null) / 2
				+ img1.getWidth(null) / 2)
				&& Math.abs((y1 + img1.getHeight(null) / 2)
						- (y2 + img2.getHeight(null) / 2)) < (img2.getHeight(null) / 2 + img1.getHeight(null) / 2)) {
			check = true;
		} else {
			check = false;
		}

		return check; // check의 값을 메소드에 리턴 시킵니다.
	}

	public void paint(Graphics g) { // paint 정의 부분

		buffImage = createImage(f_width, f_height);
		buffg = buffImage.getGraphics();

		update(g);
	}

	public void update(Graphics g) { // 전체 부분을 그려주는 부분
		Draw_background();
		Draw_Timer(time);
		Draw_Char();// 실제로 그려진 그림을 가져온다
		Draw_Coin();
		Draw_Corn();
		Draw_CoinScore();
		
		if(checkCrush == 1){
			nn_time = time;
			
			//System.out.println(n_time);
			//System.out.println(nn_time);
			
			buffg.setFont(new Font("Defualt", Font.BOLD, 20));
			buffg.setColor(Color.red);
			buffg.drawString("쾅!!", x+10, y-10);
			buffg.drawImage(damage_img, x, y-5, this);
			if(n_time - nn_time >= 0.5){
				checkCrush = 0;
			}
		}
		
		Draw_StatusText();

		g.drawImage(buffImage, 0, 0, this);		// 화면에 버퍼에 그린 그림을 가져와 그리기
	}

	public void Draw_Timer(float x) { 					// 타이머 부분을 그려주며 시간이 끝났을때 종료와 결과를 알려줌
		DecimalFormat fmt = new DecimalFormat("0.##");	//소수 2째자리가만 표시

		buffg.setFont(new Font("Defualt", Font.BOLD, 20));
		buffg.drawString("< Timer >", 650, 50);
		if (x <= 10)
			buffg.setColor(Color.red);
		buffg.drawString("" + fmt.format(x), 690, 80);
		if (time > 0) {
			time -= 0.01;
			if (time < 0)
				time = 0;
		}
		if (time <= 0 && exit_checker) {
			exit_checker = false;
			int winner = 0;
			if (p1_score > p2_score && p1_score > p3_score)
				winner = 1;
			else if (p2_score > p3_score)
				winner = 2;
			else
				winner = 3;
			
			Game_State = 1;
			char[] buf3 = new char[20];
			String tmp = "END|\n";
			buf3 = tmp.toCharArray();
			out.print(buf3);
			out.flush();
			
			
			if (winner == 1) {			//시간 종료시 승자 판별
				JOptionPane.showMessageDialog(null, "게임종료\nWinner : "+p1_ID+"\n" + p1_ID + " : " + p1_score + "\n" + p2_ID + " : "
						+ p2_score + "\n" + p3_ID + " : " + p3_score + "\n");
			} else if (winner == 2) {
				JOptionPane.showMessageDialog(null, "게임종료\nWinner : "+p2_ID+"\n" + p1_ID + " : " + p1_score + "\n" + p2_ID + " : "
						+ p2_score + "\n" + p3_ID + " : " + p3_score + "\n");
			} else if (winner == 3) {
				JOptionPane.showMessageDialog(null, "게임종료\nWinner : "+p3_ID+"\n"  + p1_ID + " : " + p1_score + "\n" + p2_ID + " : "
						+ p2_score + "\n" + p3_ID + " : " + p3_score + "\n");
			}
		}
	}

	public void Draw_background() { // 배경을 그려주는 부분, 도로라인을 그려줌
		buffg.clearRect(0, 0, f_width, f_height);
		buffg.drawImage(road, 0, 0, this);
		buffg.drawImage(road, 0, 140, this);
		buffg.drawImage(road, 0, 280, this);
		buffg.drawImage(road, 0, 420, this);
		buffg.drawImage(road, 0, 560, this);

		if (bx <= 650) {
			buffg.drawImage(Road_img, 220, 0 + bx, this);
			bx += 4;
			if (bx >= 650)
				bx = 0;
		}
		if (bx > 0) {
			buffg.drawImage(Road_img, 220, -650 + cx, this);
			cx += 4;
			if (cx >= 650)
				cx = 0;
		}
	}

	public void Draw_Char() { // 차량들 그려주는 부분
		if (Game_State == 1) {
			buffg.drawImage(title_img, 121, 130, this);
			buffg.drawImage(Start_img, 232, 427, this);
		}
		if (Game_State == 2) {
			buffg.drawImage(ready_img, 150, 130, this);
		}
		if (Game_State == 3) {
			buffg.drawImage(me_img1, x, y, this);
			if (p2_ID != null && player_2){
				buffg.drawImage(me_img2, p2_x, p2_y, this);
			}
			if (p3_ID != null && player_3){
				buffg.drawImage(me_img3, p3_x, p3_y, this);
			}
		}
	}

	public void Draw_Coin() { // Coin_List에 존재하는 코인들을 위치에 그려주는 부분
		for (int i = 0; i < Coin_List.size(); ++i) {
			en = (Coin) (Coin_List.get(i));
			buffg.drawImage(Coin_img, en.x, en.y, this);
		}
	}

	public void Draw_Corn() { // Corn_List에 존재하는 코인들을 위치에 그려주는 부분
		for (int i = 0; i < Corn_List.size(); ++i) {
			cn = (Corn) (Corn_List.get(i));
			buffg.drawImage(Corn_img, cn.x, cn.y, this);
		}
	}

	public void Draw_CoinScore() { // Score_List에 존재하는 위치에 점수를 표시해주는 그려주는 부분
		for (int i = 0; i < Score_List.size(); ++i) {
			sc = (Score) Score_List.get(i);

			if (sc.ex_cnt < 10) {
				buffg.setFont(new Font("Defualt", Font.BOLD, 20));
				buffg.setColor(Color.yellow);
				buffg.drawString("+10", sc.x, sc.y);
			} else if (sc.ex_cnt < 20) {
				buffg.setFont(new Font("Defualt", Font.BOLD, 20));
				buffg.setColor(Color.yellow);
				buffg.drawString("+10", sc.x, sc.y);
			} else if (sc.ex_cnt > 30) {
				Score_List.remove(i);
				// sc.ex_cnt = 0;
			}
		}
	}

	public void Draw_StatusText() { // 좌측 상단에 현재 상태및 점수 표지해주는 부분

		buffg.setFont(new Font("Defualt", Font.BOLD, 20));
		// 폰트 설정을 합니다. 기본폰트, 굵게, 사이즈 20
		buffg.setColor(Color.black);
		buffg.drawString("< SCORE >", 10, 50);
		buffg.setFont(new Font("Defualt", Font.BOLD, 15));
		buffg.drawString(p1_ID + " : " + p1_score, 10, 70);
		buffg.drawString(p2_ID + " : " + p2_score, 10, 90);
															
		buffg.drawString(p3_ID + " : " + p3_score, 10, 110);															
		buffg.setFont(new Font("Defualt", Font.BOLD, 12));
		
		int y = 130;
		for (int i = 0; i < player_health; i++) {
			buffg.drawImage(Heart_img, 10, y, this);
			y += 30;
		}
	}

	@Override
	public void keyPressed(KeyEvent e) { // 내 차량을 움직일때 키보드를 눌렀을때의 이벤트처리
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			KeyUp = true;
			break;
		case KeyEvent.VK_DOWN:
			KeyDown = true;
			break;
		case KeyEvent.VK_LEFT:
			KeyLeft = true;
			break;
		case KeyEvent.VK_RIGHT:
			KeyRight = true;
			break;
		}
	}

	public void keyReleased(KeyEvent e) { // 내 차량을 움직일때 키보드를 눌렀다 땠을때의 이벤트처리
		switch (e.getKeyCode()) {
		case KeyEvent.VK_UP:
			KeyUp = false;
			break;
		case KeyEvent.VK_DOWN:
			KeyDown = false;
			break;
		case KeyEvent.VK_LEFT:
			KeyLeft = false;
			break;
		case KeyEvent.VK_RIGHT:
			KeyRight = false;
			break;
		}
	}

	public void keyTyped(KeyEvent e) {
	}

	// 키보드가 타이핑 될때 이벤트 처리하는 곳

	public void KeyProcess() { // 내 차량을 움직일때 이벤트처리
		// 실제로 캐릭터 움직임 실현을 위해 위에서 받아들인 키값을 바탕으로 키 입력시마다 5만큼의 이동을 시킨다.
		if (KeyUp == true)
			if (y > 20) {
				y -= 3;
			}
		if (KeyDown == true)
			if (y + me_img1.getHeight(null) < f_height) {
				y += 8;
			}
		if (KeyLeft == true)
			if (x > 120)
				x -= 5;
		if (KeyRight == true)
			if (x < 560)
				x += 5;
	}

	public void Sound(String file, boolean Loop) {

		Clip clip;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
			clip = AudioSystem.getClip();
			clip.open(ais);
			clip.start();
			if (Loop)
				clip.loop(-1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public class LocWrite implements Runnable {		//위치를 서버로 보내주는 스레드
		char[] buf3 = new char[20];

		public LocWrite() {
		}

		public void run() {
			while (Game_State != 1) {
				String buf_x = "";
				String buf_y = "";
				buf_x = String.valueOf(x);
				buf_y = String.valueOf(y);

				String tmp = "LOC|" + buf_x + '|' + buf_y + '|' +player_score+"\n";
				buf3 = tmp.toCharArray();
				out.print(buf3);
				out.flush();
				System.out.println(buf3);

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public class LocTest implements Runnable { 	// 다른 차량의 위치를 읽어와서 해당위치의 좌표가 어디인지
		// 처리해준다.
		private int x;
		private int y;
		private int x2;
		private int y2;

		public LocTest(int x, int y, int x2, int y2) {
			this.x = x;
			this.y = y;
			this.x2 = x2;
			this.y2 = y2;
		}

		public void run() {

			while (true) {
				char[] buf = new char[50];
				String buf_1x = "";
				String buf_1y = "";
				String buf_2x = "";
				String buf_2y = "";
				String buf_3x = "";
				String buf_3y = "";
				String buf_1s = "";
				String buf_2s = "";
				String buf_3s = "";
				
				try {
					in.read(buf, 0, 50);
					System.out.print("읽어들인 좌표 : ");
					System.out.println(buf);
				} catch (IOException e) {
					e.printStackTrace();
				}
				if(buf[0] == 'E' && buf[1] == 'N' && buf[2] == 'D'){
					player_health = 5;
					time = 70;
					player_score = 0;
					coin_cnt = 0;
					corn_cnt = 0;
					coin_p = 0;
					corn_p = 0;
					Coin_List.clear();
					Score_List.clear();
					Corn_List.clear();
					bx = 0; // 도로 스크롤 변수
					cx = 0; // 도로 스크롤 변수2
					checkCrush = 0;
					KeyUp = false; // 키보드 입력 처리를 위한 변수
					KeyDown = false;
					KeyLeft = false;
					KeyRight = false;
					
					if (Player_num == 1) {
						x = 150;
						y = 480;
						p2_x = 280;
						p2_y = 480;
						p3_x = 430;
						p3_y = 480;
						System.out.print("1번 옵션 실행");
					} else if (Player_num == 2) {
						p2_x = 150;
						p2_y = 480;
						x = 280;
						y = 480;
						p3_x = 430;
						p3_y = 480;
					} else if (Player_num == 3) {
						p2_x = 150;
						p2_y = 480;
						p3_x = 280;
						p3_y = 480;
						x = 430;
						y = 480;
					}
					
					break;
				}
								
				int i = 4;
				if (buf[0] == 'L' && buf[1] == 'O' && buf[2] == 'C') {
					while (buf[i] != '|') {
						buf_1x += buf[i];
						i++;
					}
					i++;
					while (buf[i] != '|') {
						buf_1y += buf[i];
						i++;
					}
					i++;
					while (buf[i] != '|') {
						buf_2x += buf[i];
						i++;
					}
					i++;
					while (buf[i] != '|') {
						buf_2y += buf[i];
						i++;
					}
					i++;
					while (buf[i] != '|') {
						buf_3x += buf[i];
						i++;
					}
					i++;
					while (buf[i] != '|') {
						buf_3y += buf[i];
						i++;
					}
					i++;
					while (buf[i] != '|') {
						buf_1s += buf[i];
						i++;
					}
					i++;
					while (buf[i] != '|') {
						buf_2s += buf[i];
						i++;
					}
					i++;
					while (buf[i] != '|') {
						buf_3s += buf[i];
						i++;
					}
					System.out.println("buf 출력 : "+buf_1s+" "+buf_2s+" "+buf_3s);
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				try {
					if (Player_num == 1) {
						p2_x = Integer.valueOf(buf_2x);
						p2_y = Integer.valueOf(buf_2y);
						p3_x = Integer.valueOf(buf_3x);
						p3_y = Integer.valueOf(buf_3y);
						p1_score = player_score;
						p2_score = Integer.valueOf(buf_2s);
						p3_score = Integer.valueOf(buf_3s);					
					} else if (Player_num == 2) {
						p2_x = Integer.valueOf(buf_1x);
						p2_y = Integer.valueOf(buf_1y);
						p3_x = Integer.valueOf(buf_3x);
						p3_y = Integer.valueOf(buf_3y);
						p1_score = Integer.valueOf(buf_1s);
						p2_score = player_score;
						p3_score = Integer.valueOf(buf_3s);
					} else if (Player_num == 3) {
						p2_x = Integer.valueOf(buf_1x);
						p2_y = Integer.valueOf(buf_1y);
						p3_x = Integer.valueOf(buf_2x);
						p3_y = Integer.valueOf(buf_2y);
						p1_score = Integer.valueOf(buf_1s);
						p2_score = Integer.valueOf(buf_2s);
						p3_score = player_score;
					}
					if(p2_x == -1 && p2_y == -1){
						player_2 = false;
					}
					else
						player_2 = true;
					if(p3_x == -1 && p3_y == -1){
						player_3 = false;
					}
					else
						player_3 = true;
				} catch (Exception e) {
					// TODO Auto-generated catch block
				}
				
			}
		}
	}

	public class GetReady implements Runnable { // Ready하기전 이름과 플레이어 번호를 확인해주는
		// 클래스
		char[] buf = new char[10];
		char[] buf2 = new char[10];

		char[] buf3 = new char[30];
		char[] buf4 = new char[90];
		byte[] b = new byte[11];
		boolean dup_check = true;

		public GetReady() {
		}

		public void run() {
			while (dup_state) {
				String tmp = "NAME|" + player_ID + "\n";
				buf3 = tmp.toCharArray();
				out.print(buf3);
				out.flush();

				try {
					in.read(buf, 0, 5);
					System.out.println(buf);
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (buf[0] == 'D' && buf[1] == 'U' && buf[2] == 'P') {
					if (buf[4] == '1') {

						try {
							in.read(buf2, 0, 10); // 내가 몇번쨰 ID인지 읽어오기
							System.out.println(buf2);
						} catch (IOException e) {
							e.printStackTrace();
						}
						if (buf2[0] == 'N' && buf2[1] == 'U' && buf2[2] == 'M') {
							if (buf2[4] == '1') {
								Player_num = 1;
								System.out.println("player_num = 1");
							} else if (buf2[4] == '2') {
								Player_num = 2;
								System.out.println("player_num = 2");
							} else if (buf2[4] == '3') {
								Player_num = 3;
								System.out.println("player_num = 3");
							}
						}
						try {
							in.read(buf4, 0, 90); // 상대방 ID 읽어오기
							System.out.println(buf4);
						} catch (IOException e) {
							e.printStackTrace();
						}
						char[] ID1 = new char[10];
						char[] ID2 = new char[10];
						char[] ID3 = new char[10];
						if (buf4[0] == 'N' && buf4[1] == 'A' && buf4[2] == 'M' && buf4[3] == 'E') {
							int i = 5;
							int j = 0;
							int k = 0;
							int p = 0;
							while (buf4[i] != '|') {
								ID1[j] = buf4[i];
								i++;
								j++;
							}
							i++;
							while (buf4[i] != '|') {
								ID2[k] = buf4[i];
								i++;
								k++;
							}
							i++;
							while (buf4[i] != '|') {
								ID3[p] = buf4[i];
								i++;
								p++;
							}

							p1_ID = new String(ID1, 0, j);
							if (k != 0)
								p2_ID = new String(ID2, 0, k);
							if (p != 0)
								p3_ID = new String(ID3, 0, p);
							if (p1_ID.equals(player_ID)) {
								p1_ID = player_ID;
								if (k != 0)
									p2_ID = ID2.toString();
								if (p != 0)
									p3_ID = ID3.toString();
								System.out.println("1번입니다 ");
							} else if (k != 0 && p2_ID.equals(player_ID)) {
								p1_ID = new String(ID1, 0, j);
								p2_ID = player_ID;
								if (p != 0)
									p3_ID = new String(ID3, 0, p);
								System.out.println("2번입니다");
							} else if (p != 0 && p3_ID.equals(player_ID)) {
								p1_ID = new String(ID1, 0, j);
								p2_ID = new String(ID2, 0, k);
								p3_ID = player_ID;
								System.out.println("3번입니다");
							}
							System.out.println(p1_ID);
							System.out.println(p2_ID);
							System.out.println(p3_ID);
						}
						repaint();
						if (Player_num == 1) {
							me_img1 = tk.getImage("1번자동차.png");
							me_img2 = tk.getImage("2번자동차.png");
							me_img3 = tk.getImage("3번자동차.png");
							x = 150;
							y = 480;
							p2_x = 280;
							p2_y = 480;
							p3_x = 430;
							p3_y = 480;
							System.out.println("1번 옵션 실행");
						} else if (Player_num == 2) {
							me_img1 = tk.getImage("2번자동차.png");
							me_img2 = tk.getImage("1번자동차.png");
							me_img3 = tk.getImage("3번자동차.png");
							System.out.println("2번 옵션 실행");
							p2_x = 150;
							p2_y = 480;
							x = 280;
							y = 480;
							p3_x = 430;
							p3_y = 480;
						} else if (Player_num == 3) {
							me_img1 = tk.getImage("3번자동차.png");
							me_img2 = tk.getImage("1번자동차.png");
							me_img3 = tk.getImage("2번자동차.png");
							System.out.println("3번 옵션 실행");
							p2_x = 150;
							p2_y = 480;
							p3_x = 280;
							p3_y = 480;
							x = 430;
							y = 480;
						}
						dup_state = false;
						System.out.println("dup_state = false");
					}

					else if (buf[4] == '0') {
						player_ID = JOptionPane.showInputDialog(null, "ID를 입력하세요", "");
						if (player_ID == null)
							System.exit(1);
						while (player_ID.equals("")) {
							player_ID = JOptionPane.showInputDialog(null, "ID를 입력하세요", "");
							if (player_ID == null)
								System.exit(1);
						}
						p1_ID = player_ID;
						repaint();
					}
				}
			}
		}
	}

	public class Waiting implements Runnable { // Ready하기전 이름과 플레이어 번호를 확인해주는
		// 클래스
		char[] buf = new char[10];
		char[] buf2 = new char[10];

		boolean wait_check = true;

		public Waiting() {
		}

		public void run() {
			char[] buf = new char[10];
			char[] buf4 = new char[40];

			char[] ID1 = new char[10];
			char[] ID2 = new char[10];
			char[] ID3 = new char[10];

			try {
				in.read(buf4, 0, 40); // 상대방 ID 읽어오기
				System.out.println(buf4);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (buf4[0] == 'N' && buf4[1] == 'A' && buf4[2] == 'M' && buf4[3] == 'E') {
				int i = 5;
				int j = 0;
				int k = 0;
				int p = 0;
				while (buf4[i] != '|') {
					ID1[j] = buf4[i];
					i++;
					j++;
				}
				i++;
				while (buf4[i] != '|') {
					ID2[k] = buf4[i];
					i++;
					k++;
				}
				i++;
				while (buf4[i] != '|') {
					ID3[p] = buf4[i];
					i++;
					p++;
				}
				
				if (p1_ID.equals(player_ID)) {
					p1_ID = player_ID;
					if (k != 0)
						p2_ID = new String(ID2, 0, k);
					if (p != 0)
						p3_ID = new String(ID3, 0, p);
					System.out.println("1번입니다 ");
				} else if (k != 0 && p2_ID.equals(player_ID)) {
					p1_ID = new String(ID1, 0, j);
					p2_ID = player_ID;
					if (p != 0)
						p3_ID = new String(ID3, 0, p);
					System.out.println("2번입니다 ");
				} else if (p != 0 && p3_ID.equals(player_ID)) {
					p1_ID = new String(ID1, 0, j);
					p2_ID = new String(ID2, 0, k);
					p3_ID = player_ID;
					System.out.println("3번입니다");
				}
				System.out.println("player 1 :"+p1_ID);
				System.out.println("player 2 :"+p2_ID);
				System.out.println("player 3 :"+p3_ID);
			}

			try {
				System.out.println("준비스레드 실행  start 대 기");
				in.read(buf, 0, 10);
				System.out.println(buf);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(buf);

			if (buf[0] == 'S' && buf[1] == 'T' && buf[2] == 'A' && buf[3] == 'R' && buf[4] == 'T') {
				Game_State = 3;
			}
		}
	}
}

class Coin { // coin class
	int x;
	int y;
	boolean check;

	Coin(int x, int y) {
		this.x = x;
		this.y = y;
		check = true;
	}

	public void move() {
		y += 4;
	}
}

class Corn { // corn class
	int x;
	int y;
	boolean check;

	Corn(int x, int y) {
		this.x = x;
		this.y = y;
		check = true;
	}

	public void move() {
		y += 4;
	}
}

class Score { // score class
	int x;
	int y;
	int ex_cnt;

	Score(int x, int y) {
		this.x = x;
		this.y = y;
		ex_cnt = 0;
	}

	public void effect() {
		ex_cnt++;
	}
}

class CountDown extends Thread { // 시간초를 재주는 thread
	private long sec;
	private long start;
	private long cur = 0;

	public CountDown(long sec) {
		this.sec = sec;
	}

	public void run() {
		start = System.currentTimeMillis();
		processStart();
		while (cur < sec) {
			if (System.currentTimeMillis() - cur * 1000 - start >= 1000) {
				cur++;
				process(sec - cur);
			}
		}
		processEnd();
	}

	private void processStart() {
	}

	private void process(long left) {
		sec = sec - cur;
	}

	private void processEnd() {
	}
}
