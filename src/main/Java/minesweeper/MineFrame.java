package minesweeper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class MineFrame extends JPanel{
    private final double  MINE_RATIO = 0.128;//0.208
    private int NUM_MINES = 0;//99;
    public static final Color COVERED = Color.WHITE;
    public static final Color REVEALED = Color.DARK_GRAY;
    public static final Color FLAGGED = Color.ORANGE;
    public static final Color MINE = Color.RED;
    private final int x,y;
    private ArrayList<Integer> mines;

    private App parent; // parent app

    private Tile[] panel;
    private boolean ended = false;// to freeze the final moment
    private int revealed = 0;//count tiles opened

    
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
        }
        setName("content");

        populate();
        this.setVisible(true);
    }

    
    /**
     * checks if pair is a valid coordinate
     * @param pair index of Tile
     */ 
    public boolean isValid(int pair){
        return (0 <= pair && pair < this.x * this.y);
    }

    /**
     * checks board state if won
     */
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
    
    public Tile getTileAt(int index){return panel[index];}
    
    public void incrementRevealedCounter(){revealed++;}

    /**
     * debug unhides tiles/mines
     */
    public void revealTiles() { 
        for(Tile t : panel){
            t.reveal();
        }
    }

    /**
     * debug hides tiles/mines
     */
    public void hideTiles() {
        ended = false; // for case where if game ended but want to restart
        for(Tile t:panel){
            t.hideTile();
        }  
        revealed = 0;
    }
   
    /**
     * resets game panel + associated states
     */
    public void reset(){
        ended = false;
        revealed = 0;
        mines = new ArrayList<Integer>();

        for(Tile t:panel){
            t.reset();
        }
        parent.switchToContent();
        revalidate();
        repaint();
    }

    /**
     * populates gameboard with mines
     */
    public void populate() {
        //create mines
        //generating mines without duplicates
        mines = new Random().ints(0, x*y)
            .distinct()
            .limit(NUM_MINES)
            .boxed().collect(Collectors.toCollection(ArrayList::new));
        
        // settinng tiles as mines
        // delay updates
        Set<Integer> neighbor = new HashSet<Integer>(8*NUM_MINES);
        Tile current;
        for(Integer i : mines){
            System.out.print("("+i/y+","+i%y+"), ");
            current = panel[i];
            current.setMine();
            for(int j :current.getSurroundings()){
                neighbor.add(j);
            }
        }

        // sanitise data
        neighbor.remove(-1);
        // batch update
        neighbor.parallelStream().forEach(index -> panel[index].updateBack());
    }
    
    /**
     * handles game end event
     */
    public void gameEnd(){
        ended = true;

        for(int i = 0; i < y*x; i++){
                if(panel[i].isMine()){
                    if(panel[i].getBackground()==FLAGGED)continue;
                    panel[i].setBackground(MINE);
                    continue;
                }
                JLabel l= (JLabel)panel[i].getComponents()[0];
                l.setText(Integer.toString(panel[i].getNumOfSurroundingMines()));
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
    }


    /**
     * listener handling click events for each tile
     */
    private class PanelListener implements MouseListener {

        void clickResponder(Tile t, MouseEvent event){
            assert t.getBackground() != MINE;

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
            else if(SwingUtilities.isLeftMouseButton(event)){

                // extremely buggy
                //make it so that the firt click is always on 0;
                
                if (revealed==0 && (!t.isZero())) {
                    ArrayList<Integer> startingArea = IntStream.of(t.getSurroundings())
                                                        .filter(index -> index != -1)
                                                        .boxed()
                                                        .collect(Collectors.toCollection(ArrayList::new));
                    List<Integer> minesInTheWay = startingArea.stream()
                                                    .filter(index -> panel[index].isMine())
                                                    .toList();

                    minesInTheWay.forEach(index -> panel[index].reset());

                    Set<Integer> toUpdate = new HashSet<>();
                    startingArea.forEach(index -> 
                                            IntStream.of(panel[index].getSurroundings())
                                            .filter(index1 -> index1 != -1)
                                                .forEach(adjacent -> 
                                                    toUpdate.add(adjacent)
                                                    )
                                            );

                    // generate new mine positions
                    List<Integer> newPositions = new Random()
                                    .ints(0, MineFrame.this.x * MineFrame.this.y)
                                    .filter(index -> !mines.contains(index))
                                    .distinct()
                                    .limit(minesInTheWay.size())
                                    .boxed()
                                    .collect(Collectors.toList());
                    
                    newPositions.forEach(pos -> panel[pos].setMine());

                    newPositions.forEach(pos -> 
                                            IntStream.of(panel[pos].getSurroundings())
                                            .filter(index -> index != -1)
                                            .forEach(index -> 
                                                        toUpdate.add(index)
                                                        )
                                            );

                    toUpdate.forEach(index -> panel[index].updateBack());
                    /*
                    // number of mines removed
                    int mineCount = 0;
                    for(int i: t.getSurroundings()){
                        if(!isValid(i))continue;
                        //remove all mines
                        if(panel[i].isMine()){
                            mines.remove(mines.indexOf(i));
                            mineCount++;
                            for(int j:panel[i].getSurroundings()){
                                if(!isValid(j))continue;
                                if(panel[j].isMine()||panel[j].getNumOfSurroundingMines()==0)continue;
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
                        panel[i].setMine();
                        for(int j:panel[i].getSurroundings()){
                            if(!isValid(j))continue;
                            if(panel[j].isMine())continue;
                            panel[j].back++;
                        }
                    }
                    
                    for(int i:t.getSurroundings()){
                        if(!isValid(i))continue;
                        panel[i].back = 0;//prevent recount
                        for(int j:panel[i].getSurroundings()){
                            if(!isValid(j))continue;
                            if(panel[j].isMine())panel[i].back++;
                        }
                    }
                    
                    */
                }
                if (t.getBackground() == COVERED) {
                    t.clickOn();
                }
                else if (t.getBackground() == FLAGGED) {
                    // ignore when flagged
                }
                else { // REVEALED
                    if (IntStream.of(t.getSurroundings())
                        .filter(index -> isValid(index) && panel[index].getBackground() == FLAGGED)
                        .count() == t.getNumOfSurroundingMines()) {
                        for(int index: t.getSurroundings()) {
                            if(isValid(index) && panel[index].getBackground() == COVERED) {
                                panel[index].clickOn();
                            }
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
