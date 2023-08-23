package terrain;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import VASSAL.build.module.documentation.HelpFile;
import VASSAL.command.ChangeTracker;
import VASSAL.command.Command;
import VASSAL.configure.ChooseComponentDialog;
import VASSAL.configure.HotKeyConfigurer;
import VASSAL.configure.StringConfigurer;
import VASSAL.counters.Decorator;
import VASSAL.counters.GamePiece;
import VASSAL.counters.KeyCommand;
import VASSAL.counters.PieceEditor;
import VASSAL.i18n.TranslatablePiece;
import VASSAL.tools.SequenceEncoder;

/**
 * Displays a transparency surrounding the GamePiece which represents the Area of Effect of the GamePiece
 * 
 * @author morvael
 * @author Scott Giese sgiese@sprintmail.com
 */
public class TerrainMapAreaOfEffect extends Decorator implements TranslatablePiece {

  public static final String ID = "TerrainMapAreaOfEffect;";
  protected boolean active;
  protected String showCommandText;
  protected KeyStroke showCommandKey;
  protected KeyCommand showCommand;
  protected KeyCommand[] commands;
  protected String terrainMapShaderName;
  protected TerrainMapShader shader;
  protected String description = "";

  public TerrainMapAreaOfEffect() {
    this(ID, null);
  }

  public TerrainMapAreaOfEffect(String type, GamePiece inner) {
    mySetType(type);
    setInner(inner);
  }

  public String getDescription() {
    String d = "Terrain Map Area Of Effect";
    if (description.length() > 0) {
      d += " - " + description;
    }
    return d;
  }

  public String myGetType() {
    SequenceEncoder se = new SequenceEncoder(';');
    se.append(showCommandText);
    se.append(showCommandKey);
    se.append(terrainMapShaderName == null ? "" : terrainMapShaderName);
    se.append(description);
    return ID + se.getValue();
  }

  public void mySetType(String type) {
    SequenceEncoder.Decoder st = new SequenceEncoder.Decoder(type, ';');
    st.nextToken();		// Discard ID
    showCommandText = st.nextToken("Toggle visibility");
    showCommandKey = st.nextKeyStroke(null);
    showCommand = new KeyCommand(showCommandText, showCommandKey, Decorator.getOutermost(this), this);
    terrainMapShaderName = st.nextToken("");
    if (terrainMapShaderName.length() == 0) {
      terrainMapShaderName = null;
    }
    description = st.nextToken("");
    shader = null;
    commands = null;
  }

  // State does not change during the game
  public String myGetState() {
    return String.valueOf(active);
  }

  // State does not change during the game
  public void mySetState(String newState) {
    active = "true".equals(newState);
  }

  public Rectangle boundingBox() {
    // TODO: Need the context of the parent Component, because the transparency is only drawn
    // on a Map.View object.  Because this context is not known, the bounding box returned by
    // this method does not encompass the bounds of the transparency.  The result of this is
    // that portions of the transparency will not be drawn after scrolling the Map window.
    return piece.boundingBox();
  }

  public Shape getShape() {
    return piece.getShape();
  }

  public String getName() {
    return piece.getName();
  }

  public void draw(Graphics g, int x, int y, Component obs, double zoom) {
// Draw the GamePiece
    piece.draw(g, x, y, obs, zoom);
  }

  protected TerrainMapShader getShader() {
    if (shader == null) {
      for (TerrainMapShader sh : piece.getMap().getAllDescendantComponentsOf(TerrainMapShader.class)) {
        if (sh.getConfigureName().equals(terrainMapShaderName)) {
          shader = sh;
          return shader;
        }
      }
      return null;
    } else {
      return shader;
    }
  }

  // No hot-keys
  protected KeyCommand[] myGetKeyCommands() {
    if (commands == null) {
      if (showCommandText.length() == 0) {
        commands = new KeyCommand[0];
      } else {
        commands = new KeyCommand[]{showCommand};
      }
    }
    return commands;
  }

  // No hot-keys
  public Command myKeyEvent(KeyStroke stroke) {
    Command c = null;
    myGetKeyCommands();
    if (showCommand.matches(stroke)) {
      ChangeTracker t = new ChangeTracker(this);
      active = !active;
      getShader();
      if (shader != null) {
        TerrainMapAreaOfEffect old = shader.getDecorator();
        if ((old != null) && (old.active) && (old != this)) {
          old.active = false;
        }
        shader.setDecorator(active ? this : null);
      }
      c = t.getChangeCommand();
    }
    return c;
  }

  public HelpFile getHelpFile() {
    return HelpFile.getReferenceManualPage("TerrainMapAreaOfEffect.htm");
  }

  public PieceEditor getEditor() {
    return new TraitEditor(this);
  }

  protected static class TraitEditor implements PieceEditor {

    protected JPanel panel;
    protected StringConfigurer descriptionConfigurer;
    protected Box selectShader;
    protected String mapShaderId;
    protected StringConfigurer showCommandTextConfigurer;
    protected HotKeyConfigurer showCommandKeyConfigurer;

    protected TraitEditor(TerrainMapAreaOfEffect trait) {
      panel = new JPanel();
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

      panel.add(new JLabel("Contributed by Scott Giese (sgiese@sprintmail.com)", JLabel.CENTER));
      panel.add(new JLabel("Modifications by morvael", JLabel.CENTER));
      panel.add(new JSeparator());
      panel.add(new JLabel(" "));

      descriptionConfigurer = new StringConfigurer(null, "Description:  ", trait.description);
      panel.add(descriptionConfigurer.getControls());

      mapShaderId = trait.terrainMapShaderName;
      selectShader = Box.createHorizontalBox();

      panel.add(selectShader);
      JLabel l = new JLabel("Map Shading:  ");
      selectShader.add(l);
      final JTextField tf = new JTextField(12);
      tf.setEditable(false);
      selectShader.add(tf);
      tf.setText(trait.terrainMapShaderName);
      JButton b = new JButton("Select");
      selectShader.add(b);
      b.addActionListener(new ActionListener() {

        public void actionPerformed(ActionEvent e) {
          ChooseComponentDialog d = new ChooseComponentDialog((Frame) SwingUtilities.getAncestorOfClass(Frame.class, panel), TerrainMapShader.class);
          d.setVisible(true);
          if (d.getTarget() != null) {
            mapShaderId = d.getTarget().getConfigureName();
            tf.setText(mapShaderId);
          } else {
            mapShaderId = null;
            tf.setText("");
          }
        }
      });

      showCommandTextConfigurer = new StringConfigurer(null, "Toggle visible command:  ", trait.showCommandText);
      showCommandKeyConfigurer = new HotKeyConfigurer(null, "Toggle visible keyboard shortcut:  ", trait.showCommandKey);

      panel.add(showCommandTextConfigurer.getControls());
      panel.add(showCommandKeyConfigurer.getControls());
    }

    public Component getControls() {
      return panel;
    }

    public String getState() {
      return "false";
    }

    public String getType() {
      SequenceEncoder se = new SequenceEncoder(';');
      se.append(showCommandTextConfigurer.getValueString());
      se.append((KeyStroke) showCommandKeyConfigurer.getValue());
      se.append(mapShaderId != null ? mapShaderId : "");
      se.append(descriptionConfigurer.getValueString());
      return TerrainMapAreaOfEffect.ID + se.getValue();
    }
  }
}
