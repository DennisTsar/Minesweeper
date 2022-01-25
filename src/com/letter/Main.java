package com.letter;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

public class Main{
    public static void main(String[] args) {
        new Minesweeper();
    }
}
class Minesweeper extends JFrame implements ActionListener, MouseListener{
    JToggleButton[][] board;
    JPanel boardPanel;
    boolean firstClick;
    int numMines, dimR, dimC;

    ImageIcon[] nums;
    ImageIcon mineIcon, flag;
    GraphicsEnvironment ge;
    Font mineFont, timerFont;

    JMenuBar bar;
    JMenu menu;
    JButton reset;
    JMenuItem b, i, e;
    
    Timer timer;
    int timePassed;
    JTextField timeField;
    public Minesweeper(){
        
        try{
            ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            mineFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/mine-sweeper.ttf"));
            ge.registerFont(mineFont);
        }catch(IOException | FontFormatException e){}

        mineIcon = new ImageIcon("res/mine.png");
        mineIcon = new ImageIcon(mineIcon.getImage().getScaledInstance(40,40,Image.SCALE_SMOOTH));

        flag = new ImageIcon("res/flag.png");
        flag = new ImageIcon(flag.getImage().getScaledInstance(40,40, Image.SCALE_SMOOTH));

        nums = new ImageIcon[8];
        for(int i = 0; i<8; i++){
            nums[i] = new ImageIcon("res/"+(i+1)+".png");
            nums[i] = new ImageIcon(nums[i].getImage().getScaledInstance(40,40,Image.SCALE_SMOOTH));
        }

        UIManager.put("ToggleButton.select",Color.MAGENTA);

        bar = new JMenuBar();
        menu = new JMenu("Difficulty Level");
        b = new JMenuItem("Beginner");
        i = new JMenuItem("Intermediate");
        e = new JMenuItem("Expert");

        b.addActionListener(this);
        i.addActionListener(this);
        e.addActionListener(this);

        timeField = new JTextField();
        try{
            timerFont = Font.createFont(Font.TRUETYPE_FONT, new File("res/digital-7.ttf"));
            ge.registerFont(timerFont);
        }catch(IOException | FontFormatException e){}
        timeField.setFont(timerFont.deriveFont(20f));
        timeField.setForeground(Color.GREEN);
        timeField.setBackground(Color.BLUE);


        menu.add(b);
        menu.add(i);
        menu.add(e);
        bar.add(menu);
        bar.add(timeField);

        reset = new JButton("Reset");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                createBoard(dimR,dimC);
            }
        });

        setDefault();
        createBoard(dimR,dimC);

        this.setResizable(false);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);
    }
    public void createBoard(int row, int col){
        timePassed = 0;
        timeField.setText(timePassed+"");
        firstClick = true;
        if(boardPanel!=null)
            this.remove(boardPanel);
        boardPanel = new JPanel();
        board = new JToggleButton[row][col];
        boardPanel.setLayout(new GridLayout(row,col));

        for(int r = 0; r<row; r++){
            for(int c = 0; c<col; c++){
                board[r][c] = new JToggleButton();
                board[r][c].putClientProperty("row",r);
                board[r][c].putClientProperty("col",c);
                board[r][c].putClientProperty("state",0);
                board[r][c].setFont(mineFont.deriveFont(16f));
                board[r][c].setBorder(BorderFactory.createBevelBorder(0));
                board[r][c].setFocusPainted(false);
                board[r][c].addMouseListener(this);
                boardPanel.add(board[r][c]);
            }
        }
        this.setSize(40*col,40*row+20);
        this.setLayout(new BorderLayout());
        this.add(bar,BorderLayout.NORTH);
        this.add(boardPanel,BorderLayout.CENTER);
        this.revalidate();
    }
    public void actionPerformed(ActionEvent e){
        if(e.getSource()==b){
            setDefault();
        }
        else if(e.getSource()==i){
            numMines = 40;
            dimR = 16;
            dimC = 16;
        }
        else if(e.getSource()==this.e){
            numMines = 99;
            dimR = 16;
            dimC = 40;
        }
        createBoard(dimR,dimC);
    }
    public void mouseReleased(MouseEvent e){
        JToggleButton click = (JToggleButton)e.getComponent();
        int row = (int)click.getClientProperty("row");
        int col = (int)click.getClientProperty("col");

        if(e.getButton() == MouseEvent.BUTTON1 && click.isEnabled()){
            if(firstClick){
                setMinesAndCounts(row,col);
                firstClick = false;
                timer = new Timer();
                timer.schedule(new UpdateTimer(),0,1000);
            }
            if((int)click.getClientProperty("state")==-1){
                click.setContentAreaFilled(false);
                click.setOpaque(true);
                click.setBackground(Color.RED);
                revealMines();
                //JOptionPane.showMessageDialog(null, "You lose!");
                timer.cancel();
            }
            else {
                expand(row, col);
                checkWin();
            }
        }
        if(e.getButton() == MouseEvent.BUTTON3 && !firstClick && !click.isSelected()){
            ImageIcon temp = click.getIcon()==null ? flag : null;
            click.setIcon(temp);
            click.setDisabledIcon(temp);
            click.setEnabled(temp==null);
        }
    }
    public void setMinesAndCounts(int cRow, int cCol){
        int count = numMines;
        while(count>0){
            int rR = (int)(Math.random()*dimR);
            int rC = (int)(Math.random()*dimC);
            int state = (int)board[rR][rC].getClientProperty("state");
            if(state!=-1 && ((rR<cRow-1 || rR>cRow+1) && (rC<cCol-1 || rC>cCol+1))){
                board[rR][rC].putClientProperty("state",-1);
                for(int r = rR-1; r<=rR+1; r++){
                    for(int c = rC-1; c<=rC+1; c++){
                        if(r==rR && c==rC)
                            c++;
                        try {
                            int z = (int) board[r][c].getClientProperty("state");
                            if (z != -1)
                                board[r][c].putClientProperty("state", z + 1);
                        }
                        catch(ArrayIndexOutOfBoundsException e){}
                    }
                }
                count--;
            }
        }
    }
    public void expand(int row, int col){
        int state = (int)board[row][col].getClientProperty("state");
        if(!board[row][col].isSelected())
            board[row][col].setSelected(true);
        if(state>0) {
            board[row][col].setIcon(nums[state-1]);
            board[row][col].setDisabledIcon(nums[state-1]);
        }
        else if(state==0){
            for(int r = row-1; r<=row+1; r++){
                for(int c = col-1; c<=col+1; c++){
                    if(r==row && c==col)
                        c++;
                    try {
                        if (!board[r][c].isSelected())
                            expand(r,c);
                    }
                    catch(ArrayIndexOutOfBoundsException e){}
                }
            }
        }
    }
    public void revealMines(){
        for(JToggleButton[] i : board)
            for(JToggleButton j : i) {
                if ((int) j.getClientProperty("state") == -1) {
                    j.setIcon(mineIcon);
                    j.setDisabledIcon(mineIcon);
                    j.setSelected(true);
                }
                j.setEnabled(false);
            }
        boardPanel.setEnabled(false);
    }
    public void setDefault(){
        numMines = 10;
        dimR = 9;
        dimC = 9;
    }
    public void reset(){
        timePassed = 0;
        timeField.setText(timePassed+"");
        //reset.setIcon();
    }
    public void checkWin(){
        boolean won = true;
        for(JToggleButton[] i : board)
            for(JToggleButton j : i) {
                if (!j.isSelected() && (int) j.getClientProperty("state") > 0) {
                    won = false;
                    break;
                }
            }
        if(won)
            timer.cancel();
            //JOptionPane.showMessageDialog(null, "You win!");

    }
    
    public void mouseClicked(MouseEvent e){ }
    public void mousePressed(MouseEvent e){}
    public void mouseEntered(MouseEvent e){}
    public void mouseExited(MouseEvent e){}
    class UpdateTimer extends TimerTask{
        public void run(){
            timePassed++;
            timeField.setText(timePassed+"");
        }
    }
}
