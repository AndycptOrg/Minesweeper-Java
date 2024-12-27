package minesweeper;

import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;

public class Tile extends JPanel{
    public final int id;
    private final int[] surroundings;
    public int back;//indicate type of tile
    private final MineFrame container;

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
    }
    public int[] getSurroundings(){return surroundings;}
    
    public void setState(int state){
        back = state;
    }
    
    public void reveal(){
        //only reveal if there is something to reveal
        if(getBackground()!=container.COVERED)return;

        if(back==10){
            setBackground(container.MINE);
            //container.ended = true;
            return;
        }
        if(back!=0){
            ((JLabel)getComponents()[0]).setText(Integer.toString(back));
            setBackground(container.REVEALED);
            container.incrementRevealedCounter();
            if (container.isWon())container.gameEnd();
            return;
        }
        else if(back==0){
            ArrayList<Integer> stack = new ArrayList<Integer>();
            ArrayList<Integer> res = new ArrayList<Integer>();
            stack.add(this.id);
            int current = this.id;
            Tile curTile = this;

            while (stack.size()>0){//breadth first search
                current = stack.remove(0);
                curTile = container.getPanelAt(current);
                if(curTile.getBackground()==container.REVEALED||curTile.getBackground()==container.FLAGGED)continue;//if it is already revealed or flagged, don't look into it
                res.add(current);
                //System.out.println(current);
                //panel[current/y][current%y].back*=1;
                if(curTile.back!=0)continue;
                for(int i:curTile.surroundings){//look around if 0
                    if(stack.contains(i)||res.contains(i)||!container.isValid(i))continue;//don't add if already in stack or checked // save mem
                    stack.add(i);
                }
            }
            //System.out.println(res.size()+"size");
            for(Integer i:res){
                curTile = container.getPanelAt(i);
                ((JLabel)curTile.getComponents()[0]).setText(Integer.toString(curTile.back));
                curTile.setBackground(container.REVEALED);
                container.incrementRevealedCounter();
                container.refresh();
            }
            if (container.isWon())container.gameEnd();
        }

    }

    public void reset(){
        back = 0;
        this.setBackground(Color.WHITE);
        ((JLabel)this.getComponent(0)).setText("");
    }

}
