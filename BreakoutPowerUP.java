
/*
 * File: Breakout.java
 * -------------------
 * Name:
 * Section Leader:
 * 
 * This file will eventually implement the game of Breakout.
 */

import acm.graphics.*;
import acm.program.*;
import acm.util.*;

import java.applet.*;
import java.awt.*;
import java.awt.event.*;

public class BreakoutPowerUP extends GraphicsProgram {

	/** Width and height of application window in pixels */
	public static final int APPLICATION_WIDTH = 400;
	public static final int APPLICATION_HEIGHT = 600;

	/** Dimensions of game board (usually the same) */
	private static final int WIDTH = APPLICATION_WIDTH;
	private static final int HEIGHT = APPLICATION_HEIGHT;

	/** Dimensions of the paddle */
	private static final int PADDLE_WIDTH = 60;
	private static final int PADDLE_HEIGHT = 10;

	/** Offset of the paddle up from the bottom */
	private static final int PADDLE_Y_OFFSET = 30;

	/** Number of bricks per row */
	private static final int NBRICKS_PER_ROW = 10;

	/** Number of rows of bricks */
	private static final int NBRICK_ROWS = 10;

	/** Separation between bricks */
	private static final int BRICK_SEP = 4;

	/** Width of a brick */
	private static final int BRICK_WIDTH = (WIDTH - (NBRICKS_PER_ROW - 1) * BRICK_SEP) / NBRICKS_PER_ROW;

	/** Height of a brick */
	private static final int BRICK_HEIGHT = 8;

	/** Radius of the ball in pixels */
	private static final int BALL_RADIUS = 10;

	/** Offset of the top brick row from the top */
	private static final int BRICK_Y_OFFSET = 70;

	/** Number of turns */
	private static final int NTURNS = 3;

	/**
	 * Number of points around the ball that we check to find out if the ball
	 * touches other object
	 */
	private static final int NB_CHECKPOINTS = 20;

	private int BRICKS_NUM = 100;

	private GRect paddle;
	private GRect brick;
	private GOval ball;

	private RandomGenerator rgen = RandomGenerator.getInstance();
	private double vx = rgen.nextDouble(1.0, 3.0);

	private double vy = 3;
	private GLabel scoreLabel;

	private boolean startAnimation;
	private AudioClip bounceClip = MediaTools.loadAudioClip("bounce.au");

	/* Method: run() */
	/** Runs the Breakout program. */
	public void run() {
		if (rgen.nextBoolean(0.5))
			vx = -vx;
		startAnimation = false;
		addMouseListeners();
		initialization();
		gameProcess();
	}

	// this method initializes application window
	private void initialization() {
		addscoreLablel(100 - BRICKS_NUM);
		addLives();
		setBackground(Color.BLACK);
		addBricks();
		addPaddle();
	}

	// this method adds bricks on the top of the window
	private void addBricks() {
		for (int i = 0; i < NBRICK_ROWS; i++) {
			fillRow(i);
		}
	}

	// this method fills every row separately
	private void fillRow(int i) {
		// x coordinate of the first brick
		double x0 = (double) WIDTH / 2
				- (double) ((NBRICKS_PER_ROW - 1) * BRICK_SEP + BRICK_WIDTH * NBRICKS_PER_ROW) / 2;

		// y coordinate of the first brick
		double y0 = BRICK_Y_OFFSET;

		// puts bricks for NBRICKS_PER_ROW times
		for (int j = 0; j < NBRICKS_PER_ROW; j++) {

			// changes coordinates for every following brick
			putBrick(x0 + (BRICK_SEP + BRICK_WIDTH) * j, y0 + (BRICK_SEP + BRICK_HEIGHT) * i, i);
		}
	}

	// this method adds separate bricks on x and y coordinates and gives them
	// dimentions
	private void putBrick(double x, double y, int i) {
		brick = new GRect(x, y, BRICK_WIDTH, BRICK_HEIGHT);
		brick.setFilled(true);
		Color color = Color.WHITE;

		// color changes in every two rows
		if (i == 0 || i == 1) {
			color = Color.RED;
		}
		if (i == 2 || i == 3) {
			color = Color.ORANGE;
		}
		if (i == 4 || i == 5) {
			color = Color.YELLOW;
		}
		if (i == 6 || i == 7) {
			color = Color.GREEN;
		}
		if (i == 8 || i == 9) {
			color = Color.CYAN;
		}
		brick.setColor(color);
		add(brick);
	}

	// this method adds paddle in the center
	private void addPaddle() {
		paddle = new GRect(WIDTH / 2 - PADDLE_WIDTH / 2, getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT, PADDLE_WIDTH,
				PADDLE_HEIGHT);
		paddle.setFilled(true);
		paddle.setColor(Color.WHITE);
		add(paddle);
	}

	// while mouse is moved center of the paddle gets the coorcinates of the
	// mouse
	public void mouseMoved(MouseEvent e) {
		double x = e.getX() - PADDLE_WIDTH / 2;

		// prevents paddle from crossing borders of the window
		if (x >= WIDTH - PADDLE_WIDTH) {
			x = WIDTH - PADDLE_WIDTH;
		}
		if (x <= 0) {
			x = 0;
		}

		// changes the coordinates of the paddle
		paddle.setLocation(x, getHeight() - PADDLE_Y_OFFSET - PADDLE_HEIGHT);
	}

	public void mouseClicked(MouseEvent b) {
		startAnimation = true;

	}

	// game starts here
	private void gameProcess() {

		// number of turns player gets
		for (int i = NTURNS - 1; i >= 0; i--) {
			addBall();
			waitForClick();
			// ball starts movement when mouse is clicked
			if (startAnimation) {
				ballMovement();
				startAnimation = false;
			}
			// when the player wins backgound turns green and the label appears
			// in the center
			if (BRICKS_NUM == 0) {
				setBackground(Color.GREEN);
				removeAll();
				addLabel("YOU WON :)", Color.WHITE);
				break;
			}
			remove(getElementAt(25 + 20 * i, 25));
		}
		// when the player loses, background turns gray and the label appears in
		// the center
		if (BRICKS_NUM != 0) {
			setBackground(Color.DARK_GRAY);
			remove(ball);
			remove(paddle);
			addLabel("GAME OVER :(", Color.RED);
		}

	}

	// creates ball in the center of the window
	private void addBall() {
		ball = new GOval((double) WIDTH / 2 - BALL_RADIUS, (double) HEIGHT / 2 - BALL_RADIUS, BALL_RADIUS * 2,
				BALL_RADIUS * 2);
		ball.setFilled(true);
		ball.setColor(Color.WHITE);
		add(ball);
	}

	// ball starts moving
	private void ballMovement() {
		while (true) {
			ball.move(vx, vy);
			pause(10);

			ballTouchesTheWalls();
			ballTouchesCollider();

			// it means the player has removed every brick
			if (BRICKS_NUM == 0) {
				break;
			}
			// if ball doesn't touch paddle and cross the window it is romoved
			// and the player loses
			if (ball.getY() >= HEIGHT) {
				remove(ball);
				break;
			}

		}

	}

	private void ballTouchesTheWalls() {
		// if ball reaches the upper wall, it changes direcrion and falls down
		if (ball.getY() <= 0) {
			vy = -vy;
		}

		// if ball reaches the right or the left walls it continues moving on
		// the opposite side
		else if (ball.getX() >= WIDTH - 2 * BALL_RADIUS  - 2|| ball.getX() <= 2) {
			vx = -1.003*vx;
		}

	}

	private void ballTouchesCollider() {

		GObject collider = getCollidingObject();

		// absolute value of vx
		double modX = module(vx);

		// if collider is paddle, ball changes direction
		if (collider == paddle) {
			bounceClip.play();

			// when ball tuches the left side of the paddle
			if (ball.getX() + BALL_RADIUS <= paddle.getX() + PADDLE_WIDTH / 2 + 1) {

				// ball touches paddle on the top
				if (ball.getY() + 2 * BALL_RADIUS <= paddle.getY()) {
					vy = -vy;
					if(ball.getX()>=0){
						vx = -modX;
					}
					// ball touches paddle on the side
				} else {
					ball.setLocation(paddle.getX() - 2 * BALL_RADIUS - 1, ball.getY());
					vx = -modX;
				}

				// when the player touches the right side of the paddle
			} else {

				// ball touches paddle on the top
				if (ball.getY() + 2 * BALL_RADIUS <= paddle.getY() + 1) {
					vy = -vy;
					if(ball.getX()<=WIDTH){
						vx = modX;
					}
					// ball touches paddle on the side
				} else {
					ball.setLocation(paddle.getX() + PADDLE_WIDTH + 1, ball.getY());
					vx = modX;

				}

			}
		}
		// if collider is brick, brick is removed
		if (collider != null && collider != paddle && collider != ball && collider.getY() >= BRICK_Y_OFFSET) {
			bounceClip.play();
			remove(collider);

			// ball changes the direction
			if (ball.getY() >= collider.getY() + BRICK_HEIGHT - 2
					|| ball.getY() + 2 * BALL_RADIUS <= collider.getY() + 2) {
				vy = -vy;
			} else {
				vx = -vx;
			}
			
			BRICKS_NUM -= 1;
			
			//changes score 
			scoreLabel.setLabel("score: " +(100 - BRICKS_NUM));
		}

	}

	// returns absolute value of x
	private double module(double x) {
		double modX;
		if (vx <= 0) {
			modX = -x;
		} else {
			modX = x;
		}
		return (modX);
	}

	// returns object that touches the ball
	private GObject getCollidingObject() {
		GObject collider = null;
		// checks points around the ball to find the collider
		for (int i = 0; i < NB_CHECKPOINTS; i++) {
			double x = ball.getX() + BALL_RADIUS + (BALL_RADIUS + 1) * Math.cos(i * (2 * Math.PI / NB_CHECKPOINTS));
			double y = ball.getY() + BALL_RADIUS - (BALL_RADIUS + 1) * Math.sin(i * (2 * Math.PI / NB_CHECKPOINTS));
			if (getElementAt(x, y) != null) {
				collider = getElementAt(x, y);
			}
		}
		return (collider);
	}

	// adds three circles that represent number of lives player has left
	private void addLives() {
		for (int i = 0; i < NTURNS; i++) {
			addHeart(20 + i * 20);
		}
	}

	// adds label in the center of the window
	private void addLabel(String str, Color color) {
		GLabel label = new GLabel(str);
		Font stringFont = new Font("SansSerif", Font.PLAIN, 40);
		label.setFont(stringFont);
		label.setColor(color);
		double labelWidth = label.getWidth();
		double labelHeight = label.getAscent();
		add(label, WIDTH / 2 - labelWidth / 2, HEIGHT / 2 + labelHeight / 2);

	}

	// adds red circles in specific coordinates
	private void addHeart(double x) {
		GOval heart = new GOval(x, 20, 10, 10);
		heart.setFilled(true);
		heart.setColor(Color.RED);
		add(heart);
	}
	
	//adds label for score 
	private void addscoreLablel(int score){
		scoreLabel = new GLabel ("score: " + score);
		Font stringFont = new Font("SansSerif", Font.PLAIN, 14);
		scoreLabel.setFont(stringFont);
		scoreLabel.setColor(Color.WHITE);
		add(scoreLabel, WIDTH - 100, 30);
	}
}
