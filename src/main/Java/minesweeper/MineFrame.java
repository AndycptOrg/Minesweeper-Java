package minesweeper;

import java.util.ArrayList;
import java.util.Arrays;

import static java.util.function.Predicate.not;
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

    private final Tile[] panel;
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
            Tile tile = new Tile(i, this);
            tile.setBackground(COVERED);
            tile.addMouseListener(listener);
            panel[i] = tile;
            this.add(tile);
        }
        setName("content");

        populate();
        this.setVisible(true);
    }

    
    /**
     * checks if index is a valid coordinate
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
            current.setAsMine();
            for(int j :current.getValidSurroundings()){
                neighbor.add(j);
            }
        }

        // batch update
        neighbor.parallelStream().forEach(index -> panel[index].updateBack());
    }
    
    /**
     * handles game end event
     */
    public void gameEnd(){
        System.out.println("Ending game");
        ended = true;

        // reveal all tiles in content pane
        for(Tile tile: panel){
                if (tile.isMine()) {
                    if (tile.getBackground()==FLAGGED) continue;
                    tile.setBackground(MINE);
                    continue;
                }
                tile.reveal();
        }

        if (isWon()) {
            parent.switchToWin();
        }
        else {
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
            if (SwingUtilities.isRightMouseButton(event)) {
                if (t.getBackground()==FLAGGED) t.setBackground(COVERED);
                else if (t.getBackground()==COVERED) t.setBackground(FLAGGED);
            }
            //if LeftClick(flag)
            else if(SwingUtilities.isLeftMouseButton(event)){

                // possibly buggy
                //make it so that the firt click is always on 0;
                
                if (revealed==0 && (!t.isZero())) {
                    List<Integer> startingArea = Arrays.stream(t.getValidSurroundings())
                                                    .boxed()
                                                    .toList();
                    List<Tile> startingAreaTiles = startingArea.stream().map(MineFrame.this::getTileAt).toList();
                    List<Tile> minesInTheWay = startingAreaTiles.stream()
                                                    .filter(tile -> tile.isMine())
                                                    .toList();

                    minesInTheWay.forEach(tile -> tile.reset());

                    // generate new mine positions
                    List<Tile> newMinTiles = new Random()
                                    .ints(0, MineFrame.this.x * MineFrame.this.y)
                                    .boxed() // convert from primitive stream to allow Predicate<Object> casting
                                    .filter(not(mines::contains))
                                    .filter(not(startingArea::contains))
                                    .distinct()
                                    .limit(minesInTheWay.size())
                                    .map(MineFrame.this::getTileAt)
                                    .toList();
                    
                    newMinTiles.forEach(tile -> tile.setAsMine());

                    // maintin use of index instead of tile reference till equivalence is proved
                    Set<Integer> toUpdate = new HashSet<>();
                    startingAreaTiles.forEach(tile -> 
                                            IntStream.of(tile.getValidSurroundings())
                                                .forEach(toUpdate::add)
                                            );
                                            
                    newMinTiles.forEach(tile -> 
                                            IntStream.of(tile.getValidSurroundings())
                                            .forEach(toUpdate::add)
                                            );

                    toUpdate.forEach(index -> panel[index].updateBack());
                }
                if (t.getBackground() == COVERED) {
                    t.clickOn();
                }
                else if (t.getBackground() == FLAGGED) {
                    // ignore when flagged
                }
                else { // REVEALED
                    // if the # of flagged tiles are the same as the number on the revealed tile
                    // click on the remaining covered tiles
                    if (Arrays.stream(t.getValidSurroundingTiles())
                        .filter(tile -> tile.getBackground() == FLAGGED)
                        .count() == t.getNumOfSurroundingMines()) {
                        

                        Arrays.stream(t.getValidSurroundingTiles())
                        .filter(tile -> tile.getBackground() == COVERED)
                        .forEach(tile -> tile.clickOn());

                        for (Tile tile: t.getValidSurroundingTiles()) {
                            if(tile.getBackground() == COVERED) {
                                tile.clickOn();
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
