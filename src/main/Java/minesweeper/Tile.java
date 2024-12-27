package minesweeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.awt.*;
import javax.swing.*;

public class Tile extends JPanel{
    public final int id;
    private final int[] surroundings;
    private final MineFrame container;
    private int back; // indicate type of tile
    private JLabel text;

    public Tile(int id, MineFrame container){
        super();
        this.id = id;
        int x = container.getXValue();
        int y = container.getYValue();
        int index = 0;
        this.container = container;
        int[] temp = new int[9];
        for(int i=-1;i<2;i++){
            for(int j=-1;j<2;j++){
                if((id%x)+i>=x||(id%x)+i<0||id+j*x>=x*y||id+j*x<0){temp[index++]=-1;}//-1 bc index of 0 is also in the board
                else temp[index++] = id+i+j*x;
            }
        }
        this.surroundings = temp;

        text = new JLabel();
        text.setForeground(Color.GREEN);
        add(BorderLayout.CENTER, text);
        
    }
    public int[] getSurroundings(){return surroundings;}

    public void updateBack() {
        if (isMine()) return;
        back = getNumOfSurroundingMines();
    }

    public void setMine() {
        this.back = 10;
    }

    public boolean isMine() {return back == 10;}

    public boolean isZero() {return back == 0;}

    public int getNumOfSurroundingMines() {
        return (int) Arrays.stream(surroundings)
            .filter((index) -> index != -1)
            .filter((index) -> container.getTileAt(index).isMine())
            .count();
    }
    
    /**
     * Handle event when Tile being clicked on
     */
    public void clickOn(){
        //only reveal if there is something to reveal
        if(getBackground()!=MineFrame.COVERED) {}

        else if (isMine()) {
            setBackground(MineFrame.MINE);
            container.gameEnd();
        }
        else if(!isZero()) {
            text.setText(Integer.toString(back));
            setBackground(MineFrame.REVEALED);
            container.incrementRevealedCounter();
            if (container.isWon()) container.gameEnd();
        }
        // zero spreading
        else {
            ArrayList<Integer> stack = new ArrayList<Integer>();
            ArrayList<Integer> toReveal = new ArrayList<Integer>();
            stack.add(this.id);
            int current = this.id;
            Tile curTile = this;

            // breadth first search
            while (stack.size() > 0){
                current = stack.remove(0);
                curTile = container.getTileAt(current);
                // if it is already revealed or flagged, don't look into it
                if (curTile.getBackground()==MineFrame.REVEALED ||
                    curTile.getBackground()==MineFrame.FLAGGED) continue;
                
                toReveal.add(current);

                // stop at non-zero tiles
                if (!curTile.isZero()) continue;

                // look around if 0
                for (int i:curTile.surroundings) {
                    // don't add if already in stack or checked // save mem
                    if (stack.contains(i) || 
                        toReveal.contains(i) || 
                        !container.isValid(i)) continue;
                    stack.add(i);
                }
            }
            
            // reveal the saved tiles
            for(Integer i:toReveal){
                curTile = container.getTileAt(i);
                // since all saved tiles are not mines, .back will be valid
                curTile.text.setText(Integer.toString(curTile.back));
                curTile.setBackground(MineFrame.REVEALED);
                container.incrementRevealedCounter();
            }
            if (container.isWon()) container.gameEnd();
        }

    }

    /**
     * Reveals Tile
     */
    public void reveal() {
        if (isMine()) {
            setBackground(MineFrame.MINE);
            // return;
        }
        else {
            setBackground(MineFrame.REVEALED);
        }
        text.setText(Integer.toString(back));
    }

    /**
     * Hides Tile
     */
    public void hideTile() {
        setBackground(MineFrame.COVERED);
        text.setText("");
    }

    /**
     * Resets Tile
     */
    public void reset() {
        back = 0;
        this.setBackground(MineFrame.COVERED);
        ((JLabel)this.getComponent(0)).setText("");
    }

}
