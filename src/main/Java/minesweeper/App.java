package minesweeper;

import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.stream.Stream;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class App extends JFrame{
    private int x,y;

    private String WIN_SCREEN = "Win Screen";
    private String LOSE_SCREEN = "Lose Screen";
    private String CONTENT_SCREEN = "Content Screen";

    private MineFrame content; // grid of tiles
    private JPanel win; // win screen
    private JPanel lose; // lose screen

    private JMenuBar bar; // menu bar
    private JPanel pages; // cardlayout panel

    public App(int x, int y) {
        this.x = x;
        this.y = y;

        JButton winBackButton = new JButton("back");
        winBackButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToContent();
                refresh();
            }
        });

        JButton winResetButton = new JButton("reset");
        winResetButton.addActionListener(resetActionListener);

        //constructing win panel
        win = new JPanel(new GridLayout(3, 1));
        //win.setName("win");
        win.setBackground(Color.GREEN);
        win.add(new JLabel("You Won"));
        win.add(BorderLayout.CENTER, winBackButton);
        win.add(BorderLayout.CENTER, winResetButton);

        //constructing lose panel
        JButton loseBackButton = new JButton("back");
        loseBackButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                switchToContent();
                refresh();
            }
        });

        JButton loseResetButton = new JButton("reset");
        loseResetButton.addActionListener(resetActionListener);

        lose = new JPanel(new GridLayout(3,1));
        //lose.setName("lose");
        lose.setBackground(Color.RED);
        lose.add(new JLabel("Oops, Try Again"));
        lose.add(BorderLayout.CENTER, loseBackButton);
        lose.add(BorderLayout.CENTER, loseResetButton);

        content = new MineFrame(x, y, this);
        
        JMenuItem reset = new JMenuItem("reset");
        reset.addActionListener(resetActionListener);
        
        // reveals all tiles on content page
        JMenuItem reveal = new JMenuItem("reveal");
        reveal.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent event) {
                content.revealTiles();
            }
        });

        // hides tiles on content page
        JMenuItem hide = new JMenuItem("hide");
        hide.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent event) {
                System.out.println("hide");
                content.hideTiles();
            }
        });
        
        JMenuItem update = new JMenuItem("Update Screen");
        update.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                refresh();
            }
        });
        
        //testing purposes
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
        
        JMenu debug = new JMenu("Debug");
        debug.add(update);
        debug.addSeparator();
        debug.add(rev);
        
        bar = new JMenuBar();
        bar.add(actions);
        bar.add(debug);
        this.getContentPane().add(BorderLayout.NORTH, bar);
        
        // initalize cards
        pages = new JPanel(new CardLayout());
        
        pages.add(content, CONTENT_SCREEN);
        pages.add(win, WIN_SCREEN);
        pages.add(lose, LOSE_SCREEN);

        this.add(pages);
        // this.add(content);

        refresh();
        this.setTitle("Grid");
        this.setVisible(true);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(300,200);
    }

    // reset the game
    private final ActionListener resetActionListener = new ActionListener(){
        @Override
        public void actionPerformed(ActionEvent event){
            content.reset();
            content.populate();
        }
    };

    // switch cards
    public void switchToContent() {
        System.out.println("Switching to content panel");
        // cheeky growing map code
        // this.pages.remove(content);
        // this.x ++;
        // this.y ++;
        // this.content = new MineFrame(x, y, this);
        // this.pages.add(content, CONTENT_SCREEN);

        ((CardLayout)(this.pages.getLayout())).show(this.pages, CONTENT_SCREEN);
        return;
    }

    public void switchToWin() {
        System.out.println("Switching to win page");
        ((CardLayout)(this.pages.getLayout())).show(this.pages, WIN_SCREEN);
    }

    public void switchToLose() {
        System.out.println("Switching to lose page");
        ((CardLayout)(this.pages.getLayout())).show(this.pages, LOSE_SCREEN);
    }

    // convienience
    public void refresh(){
        /*
         * Refresh app view
         */
        revalidate();
        repaint();
    }




    // utils
    private static boolean isInt(String input){
        try{
            Integer.parseInt(input);
            return true;
        }catch(NumberFormatException _){
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello world");
        CLI(args);
    }

    @SuppressWarnings("unused")
    public static void CLI(String[] args){
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
        App theGUI = new App(column, row);
        // System.exit(0);
        //theGUI.setTitle("Grid");
        //theGUI.setVisible(true);
        //theGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //theGUI.setSize(330,400);

    }
}
