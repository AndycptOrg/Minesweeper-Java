package minesweeper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;



public class MineFrame extends JFrame{
    private final double  MINE_RATIO = 0.128;//0.208
    private int NUM_MINES = 0;//99;
    public final Color COVERED = Color.WHITE;
    public final Color REVEALED = Color.DARK_GRAY;
    public final Color FLAGGED = Color.ORANGE;
    public final Color MINE = Color.RED;
    private final int x,y;
    private ArrayList<Integer> mines;
    private JPanel content;//grid of tiles
    private Tile[] panel;
    private boolean ended = false;// to freeze the final moment
    private int revealed = 0;//count tiles opened

    //reset the game
    private final ActionListener resetActionListener = new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent event){
            reset();
            populate();
        }
    };

    //convienience
    public void refresh(){
        revalidate();
        repaint();
    }

    private JMenuBar bar;//menu bar
    
    private JPanel win, lose;
    
    public MineFrame(int x, int y){
        this.x = x;
        this.y = y;
        
        //makes num mines if set to 0 initally
        if(NUM_MINES==0) NUM_MINES = (int)(x*y*MINE_RATIO);

        content = new JPanel(new GridLayout(y, x,1,1));
        this.panel = new Tile[y*x];
        PanelListener listener = new PanelListener();
        
        
        JButton back = new JButton("back");
        back.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(win);
                remove(lose);
                remove(content);
                add(content);
                refresh();
            }
        });

        JButton winRes = new JButton("reset");
        winRes.addActionListener(resetActionListener);

        //constructing win panel
        win = new JPanel(new GridLayout(3, 1));
        //win.setName("win");
        win.setBackground(Color.GREEN);
        win.add(new JLabel("You Won"));
        win.add(BorderLayout.CENTER, back);
        win.add(BorderLayout.CENTER, winRes);

        //constructing lose panel
        JButton losBac = new JButton("back");
        losBac.addActionListener(back.getActionListeners()[0]);

        JButton losRes = new JButton("reset");
        losRes.addActionListener(resetActionListener);

        lose = new JPanel(new GridLayout(3,1));
        //lose.setName("lose");
        lose.setBackground(Color.RED);
        lose.add(new JLabel("Oops, Try Again"));
        lose.add(BorderLayout.CENTER, losBac);
        lose.add(BorderLayout.CENTER, losRes);

        
        
        JMenuItem reset = new JMenuItem("reset");
        reset.addActionListener(resetActionListener);
        
        JMenuItem reveal = new JMenuItem("reveal");
        reveal.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent event) {
                for(Tile t : panel){
                    if(t.back==10){
                        t.setBackground(MINE);
                        continue;
                    }
                    t.setBackground(REVEALED);
                    ((JLabel)t.getComponents()[0]).setText(Integer.toString(t.back));
                }
            }
        });
        JMenuItem hide = new JMenuItem("hide");
        hide.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent event) {
                System.out.println("hide");
                ended = false;
                for(Tile t:panel){
                    t.setBackground(COVERED);
                    if(t.back==10)continue;
                    ((JLabel)t.getComponents()[0]).setText("");
                }  
                revealed = 0;
            }
        });
        
        JMenuItem update = new JMenuItem("Update Screen");
        update.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        JMenuItem remWin = new JMenuItem("Remove Win Screen");
        remWin.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(win);
                refresh();
            }
        });
        JMenuItem remLose = new JMenuItem("Remove Lose Screen");
        remLose.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(lose);
                refresh();
            }
        });
        JMenuItem remCon = new JMenuItem("Remove Content Screen");
        remCon.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(content);
                refresh();
            }
        });
        JMenuItem remAll = new JMenuItem("Remove All Screens");
        remAll.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                remove(win);
                remove(lose);
                remove(content);
                refresh();
            }
        });
        JMenuItem addWin = new JMenuItem("Add Win Screen");
        addWin.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                add(win);
                refresh();
            }
        });
        JMenuItem addLose = new JMenuItem("Add Lose Screen");
        addLose.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                add(lose);
                refresh();
            }
        });
        JMenuItem addCon = new JMenuItem("Add Content Screen");
        addCon.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                add(content);
                refresh();
            }
        });
        //testing purpose
        JMenuItem rev = new JMenuItem("Reveal Content");
        rev.addActionListener(new ActionListener(){
            @SuppressWarnings("unused")
            @Override
            public void actionPerformed(ActionEvent e) {
                JMenuItem mI = (JMenuItem)e.getSource();
                JRootPane h= mI.getRootPane();
                System.out.println();
                System.out.println(mI.getClass().toString());
                System.out.println(mI.getName());
                System.out.println(mI.toString());
                
                JPopupMenu m = (JPopupMenu)mI.getParent();
                System.out.println();
                System.out.println(m.getClass().toString());
                System.out.println(m.getName());
                System.out.println(m.toString());
                
                JMenuBar mB = (JMenuBar)m.getParent();
                System.out.println();
                System.out.println(mB.getClass().toString());
                System.out.println(mB.getName());
                System.out.println(mB.toString());
                JFrame f = (JFrame)mB.getParent();
                System.out.println();
                System.out.println(f.getClass().toString());
                System.out.println(f.getName());
                System.out.println(f.toString());
                
                System.out.println(mI.getComponentCount());
                for(Component c:mI.getComponents()){
                    JPanel p = (JPanel)c;
                    System.out.println("win: "+p.equals(win));
                    System.out.println("lose: "+p.equals(lose));
                    System.out.println("content: "+p.equals(content));
                    System.out.println(p.getClass().toString());
                    System.out.println(p.getName());
                    System.out.println(p.toString());
                }
            }
        });

        //construct actions
        JMenu actions = new JMenu("Actions");
        
        actions.add(reset);
        actions.addSeparator();
        actions.add(reveal);
        actions.add(hide);
        
        //construct debug
        JMenu rem = new JMenu("Remove");
        rem.add(remWin);
        rem.add(remLose);
        rem.add(remCon);
        rem.add(remAll);

        JMenu add = new JMenu("Add");
        add.add(addWin);
        add.add(addLose);
        add.add(addCon);
        
        JMenu debug = new JMenu("Debug");
        debug.add(rem);
        debug.addSeparator();
        debug.add(add);
        debug.addSeparator();
        debug.add(update);
        debug.addSeparator();
        debug.add(rev);
        
        bar = new JMenuBar();
        bar.add(actions);
        bar.add(debug);
        this.getContentPane().add(BorderLayout.NORTH, bar);
        
        
        //constructing content panel
        for(int i = 0; i < y*x; i++){
            panel[i] = new Tile(i, this);
            panel[i].setBackground(COVERED);
            panel[i].addMouseListener(listener);
            content.add(panel[i]);
            //!! simplify?
            JLabel t = new JLabel();
            t.setForeground(Color.GREEN);
            panel[i].add(BorderLayout.CENTER, t);
        }
        content.setName("content");
        this.add(content);
        populate();
        refresh();
        this.setTitle("Grid");
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300,200);
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
    
    public void reveal(){revealed++;}


    private void populate(){
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
        remove(win);
        remove(lose);
        remove(content);
        if(isWon()){
            System.out.println("win");
            add(win);
        }
        else{
            System.out.println("lose");
            add(lose);
        }
        refresh();
    }
    
    
    private void reset(){
        ended = false;
        revealed = 0;
        mines = new ArrayList<Integer>();
        for(Tile t:panel){
            t.setBackground(COVERED);;
            t.back = 0;
            ((JLabel)t.getComponents()[0]).setText("");
        }
        remove(win);
        remove(lose);
        remove(content);
        add(content);
        revalidate();
        repaint();
    }


    private class PanelListener implements MouseListener {

        void mineSweeper(Tile t, MouseEvent event){
            if(ended){
                remove(content);
                if(isWon())add(win);
                else add(lose);
            }
            //if RightClick(flag)
            if (SwingUtilities.isRightMouseButton(event)){
                //for(int i:t.getSurroundings())System.out.println(i);
                if(t.getBackground()==FLAGGED)t.setBackground(COVERED);
                else if(t.getBackground()==COVERED)t.setBackground(FLAGGED);
            }
            //if LeftClick(flag)
            else if(SwingUtilities.isLeftMouseButton(event)&& t.getBackground()==COVERED){

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
                mineSweeper((Tile)panelPressed, event);
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

    private static boolean isInt(String input){
        try{
            Integer.parseInt(input);
            return true;
        }catch(NumberFormatException _){
            return false;
        }
    }

    @SuppressWarnings("unused")
    public static void main(String[] args){
        int input, row = 0;
        int column = 0;
        Stream<String> arg = Arrays.stream(args);
        if (args.length == 2 && arg.allMatch(x -> isInt(x))){
            row = Integer.parseInt(args[0]);
            column = Integer.parseInt(args[1]);
        }

        Scanner scan = new Scanner(System.in);
        while(row < 1){
            try{
                System.out.println("how many rows?");
                input = scan.nextInt();
                row = input;
                break;
            }
            catch (InputMismatchException e){
            }
        }
        while(column < 1){
            try{
                System.out.println("how many columns?");
                input = scan.nextInt();
                column = input;
                break;
            }
            catch (InputMismatchException e){
            }
        }
        scan.close();
        MineFrame theGUI = new MineFrame(column, row);
        // System.exit(0);
        //theGUI.setTitle("Grid");
        //theGUI.setVisible(true);
        //theGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //theGUI.setSize(330,400);

    }
    
}
