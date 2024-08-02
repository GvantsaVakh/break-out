
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

public class Breakout extends GraphicsProgram {

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

	/** Number of bricks */
	private int BRICKS_NUM = 100;

	private GRect paddle;
	private GRect brick;
	private GOval ball;

	private RandomGenerator rgen = RandomGenerator.getInstance();
	private double vx = rgen.nextDouble(1.0, 3.0);
	private double vy = 3;

	/* Method: run() */
	/** Runs the Breakout program. */
	public void run() {
		/* You fill this in, along with any subsidiary methods */
		addMouseListeners();
		initialization();
		gameProcess();
	}

	// this method initializes application window
	private void initialization() {
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

	// this method adds separate bricks on x and y coordinates and gives them dimentions
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
		add(paddle);
	}

	// while mouse is moved center of the paddle gets the coorcinates of the mouse
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

	// game starts here
	private void gameProcess() {

		// number of turns player gets
		for (int i = 0; i < NTURNS; i++) {
			addBall();
			ballMovement();
			if (BRICKS_NUM == 0) {
				break;
			}
		}
		if (BRICKS_NUM == 0) {
			addLabel("YOU WON");
		} else {
			addLabel("GAME OVER");
		}

	}

	// creates ball in the center of the window
	private void addBall() {
		ball = new GOval((double) WIDTH / 2 - BALL_RADIUS, (double) HEIGHT / 2 - BALL_RADIUS, BALL_RADIUS * 2,
				BALL_RADIUS * 2);
		ball.setFilled(true);
		add(ball);
	}

	// ball starts moving in specific direction
	private void ballMovement() {
		while (true) {
			ball.move(vx, vy);
			pause(10);

			ballTouchesWalls();

			ballTouchesColliders();

			// if ball doesn't touch the paddle and cross the window it is romoved
			// and the player loses
			if (ball.getY() >= HEIGHT) {
				remove(ball);
				break;
			}

			// it means the player has removed every brick
			if (BRICKS_NUM == 0) {
				remove(ball);
				break;
			}

		}

	}

	private void ballTouchesWalls() {
		// if ball reaches the upper wall, it changes direcrion and falls down
		if (ball.getY() <= 0) {
			vy = -vy;
		}

		/*
		 * if ball reaches the right or the left walls it continues moving on
		 * the opposite direction 
		 */
		else if (ball.getX() >= WIDTH - 2 * BALL_RADIUS || ball.getX() <= 0) {
			vx = -vx;
		}
	}

	private void ballTouchesColliders() {

		GObject collider = getCollidingObject();
		// if collider is paddle, ball changes direction
		if (collider == paddle) {
			ball.setLocation(ball.getX(), paddle.getY() - 2 * BALL_RADIUS);
			vy = -vy;
		}

		// if collider is brick, brick is removed
		if (collider != null && collider != paddle && collider != ball) {
			remove(collider);
			if (ball.getY() >= collider.getY() + BRICK_HEIGHT - 2
					|| ball.getY() + 2 * BALL_RADIUS <= collider.getY() + 2) {
				vy = -vy;
			} else {
				vx = -vx;
			}

			BRICKS_NUM -= 1;
		}
	}

	// retirns object that touches the ball
	private GObject getCollidingObject() {
		GObject collider = null;
		if (getElementAt(ball.getX(), ball.getY()) != null) {
			collider = getElementAt(ball.getX(), ball.getY());
		} else if (getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY()) != null) {
			collider = getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY());
		} else if (getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS) != null) {
			collider = getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS);
		} else if (getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2 * BALL_RADIUS) != null) {
			collider = getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + 2 * BALL_RADIUS);
		} else if (getElementAt(ball.getX() + 2 * BALL_RADIUS + 2, ball.getY() + BALL_RADIUS) != null) {
			collider = getElementAt(ball.getX() + 2 * BALL_RADIUS, ball.getY() + BALL_RADIUS);
		} else if (getElementAt(ball.getX() - 2, ball.getY() + 2 * BALL_RADIUS) != null) {
			collider = getElementAt(ball.getX(), ball.getY() + 2 * BALL_RADIUS);
		}
		return (collider);
	}

	// adds label in the center of the window
	private void addLabel(String str) {
		GLabel label = new GLabel(str);
		double labelWidth = label.getWidth();
		double labelHeight = label.getAscent();
		add(label, WIDTH / 2 - labelWidth / 2, HEIGHT / 2 + labelHeight / 2);
	}

}
