package de.uni.freiburg.iig.telematik.secsy.gui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JFrame;

public class CustomButton extends JButton implements MouseListener{
	
	private final int horizontalMargin = 5;
	private final int verticalMargin = 5;
	
	private final Color COLOR_BG_PRESSED = Color.darkGray;
	private final Color COLOR_BG_HOVER = Color.gray;
	private final Color COLOR_BG_DISABLED = Color.lightGray;
	private final Color COLOR_BG_DEFAULT = Color.lightGray;
	
	private boolean drawLine = false;
	
	private boolean mouseOverComponent = false;
	private boolean mousePressed = false;

    public CustomButton(String label) {
        super(label);
        setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
        addMouseListener(this);
        
    }

    @Override
    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);

    	Dimension originalSize = getSize();
        Dimension canvasSize = new Dimension(originalSize.width-2*horizontalMargin, originalSize.height-2*verticalMargin);
        int r = canvasSize.height / 2;
        
        
        g.setColor(getBackgroundColor());
        g.fillOval(horizontalMargin, verticalMargin, canvasSize.height, canvasSize.height);
        g.fillOval(horizontalMargin + canvasSize.width - r - r, verticalMargin, canvasSize.height, canvasSize.height);
        
        if(drawLine){
        	g.setColor(Color.BLACK);
            g.drawOval(horizontalMargin, verticalMargin, canvasSize.height, canvasSize.height);
            g.drawOval(horizontalMargin + canvasSize.width - r - r, verticalMargin, canvasSize.height, canvasSize.height);
            g.setColor(getBackgroundColor());
        }
        
        g.fillRect(horizontalMargin + r, verticalMargin, canvasSize.width - r - r, canvasSize.height+1);
        
        if(drawLine){
        	g.setColor(Color.BLACK);
            g.drawLine(horizontalMargin + r, verticalMargin, horizontalMargin - r + canvasSize.width, verticalMargin);
            g.drawLine(horizontalMargin + r, verticalMargin + canvasSize.height, horizontalMargin - r + canvasSize.width, verticalMargin + canvasSize.height);	
        }
        
        g.setColor(Color.BLACK);
        FontMetrics fm = g.getFontMetrics();
        
        int x = verticalMargin + r;
        int space = (int) (canvasSize.getWidth()-(2*r) - fm.stringWidth(getText()))/2;
        x = space>0 ? x + space: x;
        
        int y = canvasSize.height - fm.getHeight() + verticalMargin + verticalMargin;
        y = y<0 ? 0: y;
        y = y==0 ? 0: y/2;
        y += fm.getAscent();
        
        g.drawString(getText(), x, y);
    }
    
    private Color getBackgroundColor(){
    	if(!isEnabled())
    		return COLOR_BG_DISABLED;
    	
    	if(mousePressed)
    		return COLOR_BG_PRESSED;

    	if(mouseOverComponent)
    		return COLOR_BG_HOVER;
    	
    	return COLOR_BG_DEFAULT;
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize();
        size.width += size.height;
        return size;
    }

    /*Test the button*/
    public static void main(String[] args) {
        CustomButton button = new CustomButton("Hello, World!");
        
        button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("juhu");
			}
		});

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);

        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new FlowLayout());
        contentPane.add(button);

        frame.pack();
        frame.setVisible(true);
    }


	@Override
	public void mouseClicked(MouseEvent e) {
		setSelected(!isSelected());
	}

	@Override
	public void mousePressed(MouseEvent e) {
		mousePressed = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		mousePressed = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		mouseOverComponent = true;
		repaint();
	}

	@Override
	public void mouseExited(MouseEvent e) {
		mouseOverComponent = false;
		mousePressed = false;
		repaint();
	}

}