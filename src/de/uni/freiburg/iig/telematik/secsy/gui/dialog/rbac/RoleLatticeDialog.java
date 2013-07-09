package de.uni.freiburg.iig.telematik.secsy.gui.dialog.rbac;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;

import de.invation.code.toval.validate.CompatibilityException;
import de.invation.code.toval.validate.ParameterException;
import de.invation.code.toval.validate.Validate;
import de.uni.freiburg.iig.telematik.secsy.gui.dialog.DefineGenerateDialog;
import de.uni.freiburg.iig.telematik.seram.accesscontrol.RoleLattice;

import logic.generator.Context;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;
import gui.misc.CustomListRenderer;


public class RoleLatticeDialog extends JDialog implements EdgeAddedListener{
	
	private static final long serialVersionUID = -5216821409053567193L;

	private final JPanel graphPanel = new JPanel();
	
	private DirectedSparseGraph<String,String> graph = new DirectedSparseGraph<String,String>();
	private RoleGraphLayout layout = null;
	private VisualizationViewer<String,String> vv = null;
	
	private JList roleList = null;
	private DefaultListModel roleListModel = new DefaultListModel();
	
	private RoleLattice lattice = null;
	
	private boolean editMode = false;
	
	/**
	 * @wbp.parser.constructor
	 */
	public RoleLatticeDialog(Window owner, Context context, RoleLattice lattice) throws ParameterException {
		super(owner);
		setResizable(false);
		this.lattice = lattice;
		editMode = true;
		initialize(owner, context);
	}

	public RoleLatticeDialog(Window owner, Context context) throws ParameterException {
		super(owner);
		initialize(owner, context);
	}
	
	private void initialize(Window owner, Context context) throws ParameterException{
		if(!editMode){
			setTitle("Create role lattice");
		} else {
			setTitle("Edit role lattice");
		}
		Validate.notNull(context);
		setBounds(100, 100, 480, 400);
		setModal(true);
		setLocationRelativeTo(owner);
		getContentPane().setLayout(new BorderLayout());
		
		setupGraph();
		
		layout = new RoleGraphLayout(graph);
		layout.setSize(new Dimension(300,300));
	
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						if(lattice == null){
							JOptionPane.showMessageDialog(RoleLatticeDialog.this, "Incomplete role lattice definition.", "Invalid Parameter", JOptionPane.ERROR_MESSAGE);
							return;
						}
						dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						lattice = null;
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				cancelButton.setEnabled(!editMode);
				buttonPane.add(cancelButton);
			}
		}
		
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(null);
		panel.setPreferredSize(new Dimension(420, 340));
		graphPanel.setBounds(160, 20, 300, 300);
		panel.add(graphPanel);
		graphPanel.setBorder(null);
		graphPanel.setPreferredSize(new Dimension(300, 337));
		graphPanel.setLayout(null);
		
		vv = new VisualizationViewer<String,String>(layout);
		vv.setBorder(new LineBorder(new Color(0, 0, 0)));
		vv.setBounds(0, 0, 300, 300);
		graphPanel.add(vv);
		vv.getRenderContext().setVertexFillPaintTransformer(new RoleVertexColorTransformer());
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>()); 
		vv.getRenderContext().setVertexShapeTransformer(new RoleVertexSizeTransformer());
		vv.getRenderContext().setEdgeLabelTransformer(new RoleEdgeLabeller()); 
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		vv.setPreferredSize(new Dimension(350,350));
		final RoleGraphMouse gm = new RoleGraphMouse(vv.getRenderContext());
		vv.setGraphMouse(gm);
		gm.setPickingMode();
		gm.addEdgeAddedListener(this);
		
		vv.setFocusable(true);
		
		vv.addKeyListener(new KeyListener(){
			
			@Override
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_DELETE){
					boolean graphChanges = false;
					Set<String> pickedEdges = vv.getPickedEdgeState().getPicked();
					if(!pickedEdges.isEmpty()){
						for(String pickedEdge: pickedEdges){
							try {
								lattice.removeRelation(getTargetFromEdgeName(pickedEdge), getSourceFromEdgeName(pickedEdge));
							} catch (ParameterException e1) {
								e1.printStackTrace();
							}
							graph.removeEdge(pickedEdge);
						}
						graphChanges = true;
						vv.getPickedEdgeState().clear();
					} else {
					Set<String> pickedVertices = vv.getPickedVertexState().getPicked();
					if(!pickedVertices.isEmpty()){
						for(String pickedVertex: pickedVertices){
							try {	
//								System.out.println("Remove role " + pickedVertex + " from roles " + lattice.getRoles());
								lattice.removeRole(pickedVertex);
							} catch (ParameterException e1) {
								e1.printStackTrace();
							}
							graph.removeVertex(pickedVertex);
						}
						graphChanges = true;
						updateRoleList();
						vv.getPickedVertexState().clear();
					}
					}
					if(graphChanges){
						layout.reset();
						vv.repaint();
					}
				}
			}
		});
		
		JButton btnAddRoles = new JButton("Add roles");
		btnAddRoles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<String> newRoles = DefineGenerateDialog.showDialog(RoleLatticeDialog.this, "Roles");
				if(newRoles != null){
					try {
						if(lattice == null){
							lattice = new RoleLattice(newRoles);
						} else {
							lattice.addRoles(newRoles);
						}
						updateRoleList();
						updateGraph();
					} catch(ParameterException ex){
						ex.printStackTrace();
					}
				}
			}
		});
		btnAddRoles.setBounds(20, 200, 120, 29);
		panel.add(btnAddRoles);
		
		JLabel lblRoles = new JLabel("Roles:");
		lblRoles.setBounds(20, 20, 61, 16);
		panel.add(lblRoles);
		
		JButton btnClear = new JButton("Clear roles");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				lattice = null;
				updateRoleList();
				clearGraph();
			}
		});
		btnClear.setBounds(20, 230, 120, 29);
		panel.add(btnClear);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(20, 40, 120, 154);
		panel.add(scrollPane);
		scrollPane.setViewportView(getRoleList());
		
		JButton btnEdit = new JButton("Add edges");
		btnEdit.setBounds(20, 260, 120, 29);
		panel.add(btnEdit);
		
		JButton btnPicking = new JButton("Move nodes");
		btnPicking.setBounds(20, 290, 120, 29);
		panel.add(btnPicking);
		
		btnPicking.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gm.setPickingMode();
			}
		});
		btnEdit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				gm.setEditingMode();
			}
		});
//		pack();
		setVisible(true);
	}
	
	private void setupGraph() {
		if(lattice == null)
			return;
		for(String role: lattice.getRoles()){
			graph.addVertex(role);
		}
		try{
			for(String role: lattice.getRoles()){
				for(String dominatedRole: lattice.getDominatedRolesFor(role, false)){
					graph.addEdge(role+"-"+dominatedRole, dominatedRole, role);
				}
			}
		}catch(Exception e){}
	}
	
	private String getSourceFromEdgeName(String edgeName){
		return edgeName.substring(0, edgeName.indexOf('-'));
	}
	
	private String getTargetFromEdgeName(String edgeName){
		return edgeName.substring(edgeName.indexOf('-')+1, edgeName.length());
	}
	
	private JList getRoleList(){
		if(roleList == null){
			roleList = new JList(roleListModel);
			roleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			roleList.setCellRenderer(new CustomListRenderer());
			roleList.setFixedCellHeight(20);
			roleList.setVisibleRowCount(10);
			roleList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			roleList.setBorder(null);
			
			if(lattice != null){
				for(String role: lattice.getRoles()){
					roleListModel.addElement(role);
				}
			}
			
			roleList.setFocusable(true);
			
			roleList.addKeyListener(new KeyListener(){

				@Override
				public void keyTyped(KeyEvent e) {}

				@Override
				public void keyPressed(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_DELETE){
						if(roleList.getSelectedValues().length > 0){
						for(Object selectedRole: roleList.getSelectedValues()){
							roleListModel.removeElement(selectedRole);
							graph.removeVertex((String) selectedRole);
							try {
								lattice.removeRole((String) selectedRole);
							} catch (CompatibilityException e1) {
								e1.printStackTrace();
							} catch (ParameterException e1) {
								e1.printStackTrace();
							}
						}
						layout.reset();
						vv.repaint();
						}
					}
				}

				@Override
				public void keyReleased(KeyEvent e) {}
				
			});
		}
		return roleList;
	}
	
	private void updateRoleList(){
		roleListModel.removeAllElements();
		if(lattice != null){
			for(String role: lattice.getRoles()){
				roleListModel.addElement(role);
			}
		}
	}
	
	private void clearGraph(){
		Set<String> vertices = new HashSet<String>(graph.getVertices());
		for(String vertex: vertices){
			graph.removeVertex(vertex);
		}
		layout.reset();
		vv.repaint();
	}
	
	private void updateGraph(){
		if(lattice != null){
			for(String role: lattice.getRoles()){
				graph.addVertex(role);
			}
		}
		layout.reset();
		vv.repaint();
	}
	
	public RoleLattice getRoleLattice(){
		return lattice;
	}
	
	
	public static RoleLattice showDialog(Window owner, Context context) throws ParameterException{
		RoleLatticeDialog roleLatticeDialog = new RoleLatticeDialog(owner, context);
		return roleLatticeDialog.getRoleLattice();
	}
	
	public static RoleLattice showDialog(Window owner, Context context, RoleLattice roleLattice) throws ParameterException{
		RoleLatticeDialog roleLatticeDialog = new RoleLatticeDialog(owner, context, roleLattice);
		return roleLatticeDialog.getRoleLattice();
	}
	
	public static void main(String[] args) {
		final DirectedSparseGraph<String,String> graph = new DirectedSparseGraph<String,String>();
		graph.addVertex("Test");
		
		graph.addVertex("Test 2");
		
		graph.addVertex("Test 3");
		
		
		final RoleGraphLayout layout = new RoleGraphLayout(graph);
		layout.setSize(new Dimension(350,350));
		
		final VisualizationViewer<String,String> vv = new VisualizationViewer<String,String>(layout);
		
		vv.getRenderContext().setVertexFillPaintTransformer(new RoleVertexColorTransformer());
		vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>()); 
		vv.getRenderContext().setEdgeLabelTransformer(new RoleEdgeLabeller()); 
		vv.getRenderer().getVertexLabelRenderer().setPosition(Position.CNTR);
		vv.setPreferredSize(new Dimension(350,350));
		
		JButton button = new JButton("add");
		button.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				graph.addVertex("Test 4");
				layout.reset();
				vv.repaint();
			};
			
		});
		
		RoleGraphMouse gm = new RoleGraphMouse(vv.getRenderContext());
		
		vv.setGraphMouse(gm);
		gm.setEditingMode();
		JFrame frame = new JFrame("Simple Graph View 2"); 
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); 
		frame.getContentPane().add(vv); 
		frame.getContentPane().add(button, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
		
		
	}

	@Override
	public void edgeAdded(String sourceVertex, String targetVertex) {
		try {
			lattice.addRelation(targetVertex, sourceVertex);
		} catch (ParameterException e) {
			e.printStackTrace();
		}
	}
}
