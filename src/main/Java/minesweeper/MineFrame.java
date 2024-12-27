package minesweeper;

import java.util.ArrayList;
import java.util.stream.IntStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class MineFrame extends JPanel{
    private final double  MINE_RATIO = 0.128;//0.208
    private int NUM_MINES = 0;//99;
    public final Color COVERED = Color.WHITE;
    public final Color REVEALED = Color.DARK_GRAY;
    public final Color FLAGGED = Color.ORANGE;
    public final Color MINE = Color.RED;
    private final int x,y;
    private ArrayList<Integer> mines;

    private App parent; // parent app

    private Tile[] panel;
    private boolean ended = false;// to freeze the final moment
    private int revealed = 0;//count tiles opened

    // convienience
    public void refresh(){
        revalidate();
        repaint();
    }
    
    public MineFrame(int x, int y, App parent){
        super(new GridLayout(y, x,1,1));
        this.x = x;
        this.y = y;
        this.parent = parent;
        
        //makes num mines if set to 0 initally
        if(NUM_MINES==0) NUM_MINES = (int)(x*y*MINE_RATIO);

        this.panel = new Tile[y*x];
        PanelListener listener = new PanelListener();
        
        
        //constructing content panel
        for(int i = 0; i < y*x; i++){
            panel[i] = new Tile(i, this);
            panel[i].setBackground(COVERED);
            panel[i].addMouseListener(listener);
            this.add(panel[i]);
            //!! simplify?
            JLabel t = new JLabel();
            t.setForeground(Color.GREEN);
            panel[i].add(BorderLayout.CENTER, t);
        }
        setName("content");

        populate();
        refresh();
        this.setVisible(true);
    }

    
    @Deprecated
    public static boolean isValid(int[] pair, int x, int y){
        return (0<=pair[0]&&pair[0]<x)&&(0<=pair[1]&&pair[1]<y);
    }
    public boolean isValid(int pair){
        return (0<=pair&&pair<this.x*this.y);
    }

    public boolean isWon(){
        System.out.println("Tile Count: "+(x*y));
        System.out.println("Mine Count: "+NUM_MINES);
        System.out.println("Not Mines Count: "+(x*y-NUM_MINES));
        System.out.println("Revealed Count: "+revealed);
        System.out.println("Decision: "+(revealed==x*y-NUM_MINES));
        System.out.println();
        return revealed==x*y-NUM_MINES;
    }

    public int getYValue(){return y;}
    
    public int getXValue(){return x;}
    
    public int getNumMines(){return mines.size();}
    
    public Tile getPanelAt(int index){return panel[index];}
    
    public void incrementRevealedCounter(){revealed++;}

    public void revealTiles() { 
        /* debug unhides tiles/mines
         */
        for(Tile t : panel){
            if(t.back==10){
                t.setBackground(MINE);
                continue;
            }
            t.setBackground(REVEALED);
            ((JLabel)t.getComponents()[0]).setText(Integer.toString(t.back));
        }
    }

    public void hideTiles() {
        /* debug hides tiles/mines
         */
        ended = false;
        for(Tile t:panel){
            t.setBackground(COVERED);
            if(t.back==10)continue;
            ((JLabel)t.getComponents()[0]).setText("");
        }  
        revealed = 0;
    }

    public void populate() {
        //create mines
        int next;//save mem
        mines = new ArrayList<Integer>();
        mines.add((int)(Math.random()*x*y));
    
        //generating mines without duplicates
        while (mines.size()<NUM_MINES){
            next = (int)(Math.random()*x*y);
            if(mines.contains(next))continue;
            mines.add(next);
        }
        
        //settinng tiles as mines
        Tile current;
        for(Integer i : mines){
            System.out.print("("+i/y+","+i%y+"), ");
            current = panel[i];
            current.setState(10);
            for(int j :current.getSurroundings()){
                if(!isValid(j))continue;
                if(panel[j].back==10)continue;
                panel[j].back++;
            }
        }
    }
    
    public void gameEnd(){
        ended = true;

        for(int i = 0; i < y*x; i++){
                if(panel[i].back==10){
                    if(panel[i].getBackground()==FLAGGED)continue;
                    panel[i].setBackground(MINE);
                    continue;
                }
                JLabel l= (JLabel)panel[i].getComponents()[0];
                l.setText(Integer.toString(panel[i].back));
                panel[i].setBackground(REVEALED);
        }
        if(isWon()){
            System.out.println("win");
            parent.switchToWin();
        }
        else{
            System.out.println("lose");
            parent.switchToLose();
        }
        refresh();
    }
    
    
    public void reset(){
        ended = false;
        revealed = 0;
        mines = new ArrayList<Integer>();
        for(Tile t:panel){
            t.setBackground(COVERED);;
            t.back = 0;
            ((JLabel)t.getComponents()[0]).setText("");
        }
        parent.switchToContent();
        revalidate();
        repaint();
    }


    private class PanelListener implements MouseListener {

        void clickResponder(Tile t, MouseEvent event){
            if(ended){
                if(isWon()) parent.switchToWin();
                else parent.switchToLose();
            }
            //if RightClick(flag)
            if (SwingUtilities.isRightMouseButton(event)){
                //for(int i:t.getSurroundings())System.out.println(i);
                if(t.getBackground()==FLAGGED)t.setBackground(COVERED);
                else if(t.getBackground()==COVERED)t.setBackground(FLAGGED);
            }
            //if LeftClick(flag)
            else if(SwingUtilities.isLeftMouseButton(event)&& t.getBackground()==COVERED){

                // extremely buggy
                //make it so that the firt click is always on 0;
                if(revealed==0&&t.back!=0){
                    // number of mines removed
                    int mineCount = 0;
                    for(int i: t.getSurroundings()){
                        if(!isValid(i))continue;
                        //remove all mines
                        if(panel[i].back==10){
                            mines.remove(mines.indexOf(i));
                            mineCount++;
                            for(int j:panel[i].getSurroundings()){
                                if(!isValid(j))continue;
                                if(panel[j].back==10||panel[j].back==0)continue;
                                panel[j].back--;
                            }
                            panel[i].back = 0; // ??? how does this not cause a bug
                        }

                    }

                    // index of moved mines
                    int[] moved = new int[mineCount];
                    
                    // if the number of spaces remaining is less than number of mines
                    if (x*y-IntStream.of(t.getSurroundings()).map(x -> isValid(x)?1:0).sum() < NUM_MINES){
                        System.err.println("\nError: cannot find enough space for mines");
                        System.exit(0);
                    }
                    //warning: this segment/feature can cause forever loop if there are not enough spaces for mines
                    //find spaces for mines
                    while (mineCount>0){
                        int next = (int)(Math.random()*x*y);
                        //if next is already a mine or in the starting box
                        if(mines.contains(next)||IntStream.of(t.getSurroundings()).anyMatch(x -> x == next))continue;
                        mines.add(next);
                        moved[moved.length-mineCount--] = next;
                    }

                    //update new mines
                    for(int i:moved){
                        panel[i].back = 10;
                        for(int j:panel[i].getSurroundings()){
                            if(!isValid(j))continue;
                            if(panel[j].back==10)continue;
                            panel[j].back++;
                        }
                    }

                    for(int i:t.getSurroundings()){
                        if(!isValid(i))continue;
                        panel[i].back = 0;//prevent recount
                        for(int j:panel[i].getSurroundings()){
                            if(!isValid(j))continue;
                            if(panel[j].back==10)panel[i].back++;
                        }
                    }

                }
                if(t.back==10){
                    gameEnd();
                    return;
                }
                t.reveal();
                
            }
            // else if LeftClick(uncovered)
            else{
                if(t.back == IntStream.of(t.getSurroundings()).filter(x -> isValid(x) && panel[x].getBackground() == FLAGGED).count()){
                    for(int i: t.getSurroundings()){
                        if(isValid(i) && panel[i].getBackground() == COVERED){
                            panel[i].reveal();
                        }
                    }
                }
            }
        }
        public void onClick(MouseEvent event){
            /* source is the object that got clicked
            * 
            * If the source is actually a JPanel, 
            * then will the object be parsed to JPanel 
            * since we need the setBackground() method
            */
            
            Object source = event.getSource();
            if(source instanceof Tile){
                JPanel panelPressed = (JPanel) source;
                clickResponder((Tile)panelPressed, event);
                refresh();
            }
        }

        @Override
        public void mouseClicked(MouseEvent event) {
            //onClick(event);
        }

        @Override
        public void mouseEntered(MouseEvent arg0) {
            //onClick(arg0);
        }

        @Override
        public void mouseExited(MouseEvent arg0) {}

        @Override
        public void mousePressed(MouseEvent event) {
            onClick(event);
        }

        @Override
        public void mouseReleased(MouseEvent arg0) {}

    }
    
}
