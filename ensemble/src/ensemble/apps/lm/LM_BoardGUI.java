package ensemble.apps.lm;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import javax.swing.*;

import ensemble.apps.lm.LM_World.Position;
import ensemble.apps.lm.LM_World.Site;
import ensemble.world.WorldGUI;


public class LM_BoardGUI extends JFrame implements WorldGUI {
	
    final double AGENT_PAD = 0.3;
    final double SOUND_PAD = 0.0;
	final int PAD = 0;
    private int rows;
    private int cols;
    
    private Site[][] squareLattice;
    private int turn = 0;
    
    public LM_BoardGUI(Site[][] squareLattice) {

    	this.squareLattice = squareLattice;
    	rows = squareLattice.length;
    	cols = squareLattice[0].length;
    	
        initComponents();

        this.setLocation(200, 200);
        this.setVisible(true);
    }
    
    @Override
    public void update() {
    	turn++;
    	lblTurn.setText(String.valueOf(turn));
    	repaint();
    }

 	private void initComponents() {

		jPanel1 = new MyPanel();
        jLabel1 = new javax.swing.JLabel();
        lblTurn = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Living Melodies Simulation");
        setResizable(false);

        jPanel1.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 255)));
        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );

        jLabel1.setText("Turn:");

        lblTurn.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        lblTurn.setText("1");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblTurn, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(lblTurn))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }
 	
    // Variables declaration - do not modify
    private javax.swing.JLabel jLabel1;
    private MyPanel jPanel1;
    private javax.swing.JLabel lblTurn;
    // End of variables declaration

	class MyPanel extends JPanel {
		
		protected void paintComponent(Graphics g) {
	    	super.paintComponent(g);
	        Graphics2D g2 = (Graphics2D)g;
	        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
	                            RenderingHints.VALUE_ANTIALIAS_ON);
	        int w = getWidth();
	        int h = getHeight();
	        double xInc = (double)(w - 2*PAD)/rows;
	        double yInc = (double)(h - 2*PAD)/cols;
	        // Draw vertical grid lines.
	        g2.setPaint(Color.BLUE);
	        for(int i = 0; i <= cols + 1; i++) {
	            double x = PAD + i*xInc;
	            g2.draw(new Line2D.Double(x, PAD, x, h-PAD));
	        }
	        // Draw horizontal grid lines.
	        for(int i = 0; i <= rows + 1; i++) {
	            double y = PAD + i*yInc;
	            g2.draw(new Line2D.Double(PAD, y, w-PAD, y));
	        }
	        
			// Percorre o tabuleiro e pinta a posi��o dos agentes
	        for (int i = 0; i < squareLattice.length; i++) {
				for (int j = 0; j < squareLattice[i].length; j++) {
	
					// Pinta o som presente no ambiente
					if (squareLattice[i][j].sound.direction != LM_World.DIR_NONE) {
						GeneralPath p1 = new GeneralPath(GeneralPath.WIND_NON_ZERO);
						p1.moveTo((j + SOUND_PAD) * xInc, (i + SOUND_PAD) * yInc);
				  		p1.lineTo((j + SOUND_PAD) * xInc, (i + 1 - SOUND_PAD) * yInc);
				  		p1.lineTo((j + 1 - SOUND_PAD) * xInc, (i + 1 - SOUND_PAD) * yInc);
				  		p1.lineTo((j + 1 - SOUND_PAD) * xInc, (i + SOUND_PAD) * yInc);
				  		p1.lineTo((j + SOUND_PAD) * xInc, (i + SOUND_PAD) * yInc);
				  		p1.closePath();
	
				  		float[] rgs = Color.BLACK.getRGBColorComponents(null);;
				  		//float ratio = squareLattice[i][j].sound.sound_amplitude / 10.0f;
				  		float ratio = 1.0f;
				  		// Seleciona a cor de acordo com a nota
				  		switch (squareLattice[i][j].sound.note) {
						case 1:
							rgs = Color.GREEN.getRGBColorComponents(null);
							break;
						case 2:
							rgs = Color.BLUE.getRGBColorComponents(null);
							break;
						case 3:
							rgs = Color.ORANGE.getRGBColorComponents(null);
							break;
						case 4:
							rgs = Color.PINK.getRGBColorComponents(null);
							break;
						case 5:
							rgs = Color.MAGENTA.getRGBColorComponents(null);
							break;
						case 6:
							rgs = Color.YELLOW.getRGBColorComponents(null);
							break;
						case 7:
							rgs = Color.BLACK.getRGBColorComponents(null);
							break;
						case 8:
							rgs = Color.CYAN.getRGBColorComponents(null);
							break;
						case 9:
							rgs = Color.WHITE.getRGBColorComponents(null);
							break;
						case 10:
							rgs = Color.DARK_GRAY.getRGBColorComponents(null);
							break;
						case 11:
							rgs = Color.RED.getRGBColorComponents(null);
							break;
						case 12:
							rgs = Color.LIGHT_GRAY.getRGBColorComponents(null);
							break;
						default:
							break;
						}
				  		g2.setPaint(new Color(rgs[0] * ratio, rgs[1] * ratio, rgs[2] * ratio));
				  		g2.fill(p1);
				  		g2.draw(p1);
					}
					
					Position agent = squareLattice[i][j].agent;
					if (agent != null) {
	
						// Pinta o agente
						int direction = agent.direction;
				        
				  		GeneralPath p2 = new GeneralPath(GeneralPath.WIND_NON_ZERO);
	
				  		if (direction == LM_World.DIR_N) {
					  		p2.moveTo((j + AGENT_PAD) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + 0.5) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + AGENT_PAD) * xInc, (i + (1 - AGENT_PAD)) * yInc);
				  		} else if (direction == LM_World.DIR_NW) {
					  		p2.moveTo((j + AGENT_PAD) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j +  AGENT_PAD) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + AGENT_PAD) * xInc, (i + AGENT_PAD) * yInc);
				  		} else if (direction == LM_World.DIR_W) {
					  		p2.moveTo((j + (1 - AGENT_PAD)) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j + AGENT_PAD) * xInc, (i + 0.5) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + AGENT_PAD) * yInc);
				  		} else if (direction == LM_World.DIR_SW) {
					  		p2.moveTo((j + AGENT_PAD) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + AGENT_PAD) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + AGENT_PAD) * xInc, (i + AGENT_PAD) * yInc);
				  		} else if (direction == LM_World.DIR_S) {
					  		p2.moveTo((j + AGENT_PAD) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j + 0.5) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j + AGENT_PAD) * xInc, (i + AGENT_PAD) * yInc);
				  		} else if (direction == LM_World.DIR_SE) {
					  		p2.moveTo((j + (1 - AGENT_PAD)) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + AGENT_PAD) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + AGENT_PAD) * yInc);
				  		} else if (direction == LM_World.DIR_E) {
					  		p2.moveTo((j + AGENT_PAD) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + 0.5) * yInc);
					  		p2.lineTo((j + AGENT_PAD) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + AGENT_PAD) * xInc, (i + AGENT_PAD) * yInc);
				  		} else if (direction == LM_World.DIR_NE) {
					  		p2.moveTo((j + AGENT_PAD) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + AGENT_PAD) * yInc);
					  		p2.lineTo((j + (1 - AGENT_PAD)) * xInc, (i + (1 - AGENT_PAD)) * yInc);
					  		p2.lineTo((j + AGENT_PAD) * xInc, (i + AGENT_PAD) * yInc);
				  		}
					  		
				  		p2.closePath();
						g2.setPaint(Color.red);
				  		g2.fill(p2);
				  		g2.draw(p2);
	
					}
				}
			}
		}        
	}
}
